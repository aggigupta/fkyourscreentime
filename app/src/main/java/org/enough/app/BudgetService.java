package org.enough.app;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/** Visible, opt-in enforcement. No screen nodes, text, gestures, network, or anti-uninstall hooks. */
public final class BudgetService extends AccessibilityService {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private BudgetStore store;
 private String foreground="",overlayPackage="";
 private long lastTick,lastReconcile;
 private View overlay;
 private WindowManager windows;
 private final BroadcastReceiver screenReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
  account();foreground="";hideBlock();lastTick=SystemClock.elapsedRealtime();
  if(Intent.ACTION_USER_PRESENT.equals(i.getAction()))reconcile();
 }};
 private final Runnable pulse=new Runnable(){@Override public void run(){
  if(store==null)return;
  account();store.heartbeat();
  if(!UsageReader.allowed(BudgetService.this)){foreground="";hideBlock();}
  else {if(SystemClock.elapsedRealtime()-lastReconcile>15_000)reconcile();enforce();}
  handler.postDelayed(this,750);
 }};
 @Override protected void onServiceConnected(){
  store=BudgetStore.get(this);
  if(!store.disclosureAccepted()){disableSelf();return;}
  windows=(WindowManager)getSystemService(WINDOW_SERVICE);lastTick=SystemClock.elapsedRealtime();
  IntentFilter filter=new IntentFilter();filter.addAction(Intent.ACTION_SCREEN_OFF);filter.addAction(Intent.ACTION_USER_PRESENT);
  if(Build.VERSION.SDK_INT>=33)registerReceiver(screenReceiver,filter,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(screenReceiver,filter);
  reconcile();handler.post(pulse);
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent event){
  if(store==null||event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED||event.getPackageName()==null)return;
  String pkg=event.getPackageName().toString();
  // Our accessibility overlay must not mistake itself for leaving the restricted app.
  if(overlay!=null&&pkg.equals(getPackageName()))return;
  account();foreground=pkg;lastTick=SystemClock.elapsedRealtime();
  if(!pkg.equals(overlayPackage))hideBlock();enforce();
 }
 private boolean interactive(){PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);KeyguardManager keyguard=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);return power!=null&&power.isInteractive()&&(keyguard==null||!keyguard.isKeyguardLocked());}
 private void account(){
  long now=SystemClock.elapsedRealtime();long delta=Math.max(0,now-lastTick);lastTick=now;
  // Do not charge long process stalls as definite foreground usage; system events reconcile gaps.
  if(store!=null&&interactive()&&UsageReader.allowed(this)&&delta<=5000)store.add(foreground,delta);
 }
 private void reconcile(){
  lastReconcile=SystemClock.elapsedRealtime();
  if(store==null||!UsageReader.allowed(this))return;
  UsageReader.Snapshot snapshot=UsageReader.read(this,store.dayStart(),System.currentTimeMillis());
  store.reconcile(snapshot.totals);foreground=interactive()?snapshot.foreground:"";lastTick=SystemClock.elapsedRealtime();
  if(!foreground.equals(overlayPackage))hideBlock();
 }
 private void enforce(){
  if(!interactive()){hideBlock();return;}
  if(store==null||!UsageReader.allowed(this)||!store.blocked(foreground)||!PackageSafety.canLimit(this,foreground)){hideBlock();return;}
  if(overlay==null)showBlock(foreground);
 }
 private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
 private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(12),0,dp(12));return t;}
 private void showBlock(String pkg){
  LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setGravity(Gravity.CENTER_VERTICAL);content.setPadding(dp(30),dp(40),dp(30),dp(40));content.setBackgroundColor(Color.rgb(245,246,239));
  content.addView(text("enough.",22,Color.rgb(36,89,69)));
  content.addView(text("You kept your promise.",38,Color.rgb(27,47,39)));
  content.addView(text(PackageSafety.label(this,pkg)+" is done for today.",20,Color.rgb(27,47,39)));
  String reset=DateTimeFormatter.ofPattern("EEE, HH:mm z").withZone(ZoneId.of(store.zone())).format(Instant.ofEpochMilli(store.resetAt()));
  content.addView(text("Your daily budget has been used. It resets "+reset+".\nNo extra minutes inside Enough. Put the phone down and make room for something else.",16,Color.rgb(93,111,99)));
  Button home=new Button(this);home.setText("Back to my day");home.setAllCaps(false);home.setOnClickListener(v->{performGlobalAction(GLOBAL_ACTION_HOME);foreground="";hideBlock();});content.addView(home);
  content.addView(text("Personal commitment mode · Android Settings remains accessible.",12,Color.rgb(93,111,99)));
  WindowManager.LayoutParams params=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.OPAQUE);
  params.gravity=Gravity.TOP|Gravity.START;params.setTitle("Enough daily budget reached");
  try{windows.addView(content,params);overlay=content;overlayPackage=pkg;}catch(RuntimeException e){performGlobalAction(GLOBAL_ACTION_HOME);}
 }
 private void hideBlock(){if(overlay!=null){try{windows.removeView(overlay);}catch(RuntimeException ignored){}overlay=null;overlayPackage="";}}
 @Override public void onInterrupt(){account();foreground="";hideBlock();}
 @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);account();hideBlock();try{unregisterReceiver(screenReceiver);}catch(IllegalArgumentException ignored){}super.onDestroy();}
}
