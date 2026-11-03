package org.enough.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MainActivity extends Activity {
 private static final int INK=Color.rgb(27,47,39),MUTED=Color.rgb(94,112,101),GREEN=Color.rgb(36,89,69),BG=Color.rgb(245,246,239);
 private BudgetStore store;
 private LinearLayout content;
 private TextView health;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable healthRefresh=new Runnable(){@Override public void run(){if(health!=null)health.setText(healthText());handler.postDelayed(this,2000);}};
 @Override public void onCreate(Bundle saved){super.onCreate(saved);store=BudgetStore.get(this);render();}
 @Override protected void onResume(){super.onResume();if(UsageReader.allowed(this))store.reconcile(UsageReader.read(this,store.dayStart(),System.currentTimeMillis()).totals);render();handler.removeCallbacks(healthRefresh);handler.post(healthRefresh);}
 @Override protected void onPause(){handler.removeCallbacks(healthRefresh);super.onPause();}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private TextView text(String value,int size,int color){TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(color);view.setPadding(0,dp(8),0,dp(8));return view;}
 private GradientDrawable background(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private LinearLayout column(){LinearLayout view=new LinearLayout(this);view.setOrientation(LinearLayout.VERTICAL);return view;}
 private LinearLayout card(){LinearLayout box=column();box.setPadding(dp(22),dp(18),dp(22),dp(18));box.setBackground(background(Color.WHITE,20));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(14),0,dp(6));content.addView(box,lp);return box;}
 private Button button(String label,boolean primary,View.OnClickListener action){Button b=new Button(this);b.setText(label);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(primary?Color.WHITE:GREEN);b.setBackground(background(primary?GREEN:Color.rgb(231,237,225),12));b.setPadding(dp(14),dp(10),dp(14),dp(10));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(0,dp(12),0,dp(4));b.setLayoutParams(lp);b.setOnClickListener(action);return b;}
 private boolean serviceEnabled(){String value=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);String component=new ComponentName(this,BudgetService.class).flattenToString();return value!=null&&(value.contains(component)||value.contains(getPackageName()+"/.BudgetService"));}
 private String healthText(){if(!UsageReader.allowed(this))return "SETUP NEEDED · Usage access is off";if(!serviceEnabled())return "SETUP NEEDED · App-limit service is off";return store.live()?"●  PROTECTION ACTIVE":"SERVICE NOT RESPONDING · Check Android settings";}
 private String resetText(){return DateTimeFormatter.ofPattern("EEE, HH:mm z").withZone(ZoneId.of(store.zone())).format(Instant.ofEpochMilli(store.resetAt()));}
 private String duration(long ms){long sec=Math.max(0,(ms+999)/1000);return sec>=60?(sec/60)+"m "+(sec%60)+"s":sec+"s";}
 private void render(){
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
  content=column();content.setPadding(dp(24),dp(18),dp(24),dp(30));scroll.addView(content);
  scroll.setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(0,insets.getSystemWindowInsetTop(),0,insets.getSystemWindowInsetBottom());return insets;});setContentView(scroll);
  LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
  TextView brand=text("enough.",28,GREEN);brand.setTypeface(null,Typeface.BOLD);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
  TextView local=text("OFFLINE · OPEN SOURCE",10,MUTED);top.addView(local);content.addView(top);
  content.addView(text("A little less scrolling.\nA lot more living.",34,INK));
  content.addView(text("Decide your limit once. Let Enough handle the difficult moment later.",16,MUTED));
  health=text(healthText(),12,GREEN);content.addView(health);
  if(!UsageReader.allowed(this)||!serviceEnabled()){
   LinearLayout setup=card();setup.addView(text("Start with two permissions",19,INK));setup.addView(text("Usage access restores today’s totals if the service restarts. The app-limit service recognizes which app opens and shows the blocking screen. Both stay on your phone.",14,MUTED));
   setup.addView(button(UsageReader.allowed(this)?"✓ Usage access enabled":"1. Enable usage access",false,v->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))));
   setup.addView(button(serviceEnabled()?"✓ App-limit service enabled":"2. Enable app-limit service",false,v->disclosure()));
  }
  Map<String,Long> plan=store.plan();
  LinearLayout summary=card();summary.addView(text(plan.isEmpty()?"Your time. Your promise.":"TODAY’S COMMITTED BUDGET",13,GREEN));
  long used=0,budget=0;for(Map.Entry<String,Long> entry:plan.entrySet()){budget+=entry.getValue();used+=store.used(entry.getKey());}
  summary.addView(text(plan.isEmpty()?"Choose what gets your attention.":duration(Math.max(0,budget-used))+" left",30,INK));
  summary.addView(text(plan.isEmpty()?"Start with Instagram, a game, or whichever app you open without thinking.":"Across "+plan.size()+" apps · Resets "+resetText(),14,MUTED));
  summary.addView(button(plan.isEmpty()?"Choose apps & daily minutes":"Plan tomorrow’s limits",true,v->editPlan()));
  if(!plan.isEmpty()){
   content.addView(text("Your app limits",21,INK));
   for(Map.Entry<String,Long> entry:plan.entrySet()){
    String pkg=entry.getKey();long spent=store.used(pkg),limit=entry.getValue();LinearLayout app=card();
    app.addView(text(PackageSafety.label(this,pkg),19,INK));app.addView(text(store.blocked(pkg)?"DONE FOR TODAY · Opens again "+resetText():duration(spent)+" used / "+duration(limit),13,store.blocked(pkg)?GREEN:MUTED));
    ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setProgress((int)Math.min(1000,spent*1000/limit));progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));app.addView(progress,new LinearLayout.LayoutParams(-1,dp(8)));
   }
   Map<String,Long> tomorrow=store.nextPlan();if(!tomorrow.equals(plan))content.addView(text("Tomorrow’s updated plan is saved. Today’s limits stay unchanged.",13,GREEN));
   content.addView(button("Refresh usage",false,v->{if(UsageReader.allowed(this))store.reconcile(UsageReader.read(this,store.dayStart(),System.currentTimeMillis()).totals);render();}));
  }
  LinearLayout note=card();note.addView(text("Strict inside the app. Honest about Android.",17,INK));note.addView(text("No extra-time button. No changing today’s budget. Edits apply tomorrow. Android still allows you to disable the service, clear app data, or uninstall Enough. Split-screen, cloned apps, browser versions, and manufacturer background restrictions can affect enforcement.",13,MUTED));
  note.addView(button("Permissions & privacy",false,v->privacy()));
  content.addView(text("Your attention belongs to you.\nNo ads. No account. No internet permission.",12,MUTED));
 }
 private void disclosure(){
  new AlertDialog.Builder(this).setTitle("Allow app-limit enforcement?").setMessage("Enough uses Android’s Accessibility API to observe app-window changes and identify the foreground app. It uses that app identity to count selected-app time and show a blocking screen when your budget ends.\n\nIt does not read screen contents, messages, passwords, or typed text. App budgets and per-app usage totals are stored locally; nothing is sent elsewhere.\n\nYou can disable the service in Android Settings. This is a self-control tool, not a disability accessibility tool.")
   .setNegativeButton("Not now",null).setPositiveButton("I agree — open settings",(dialog,which)->{store.setDisclosureAccepted();startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}).show();
 }
 private void privacy(){new AlertDialog.Builder(this).setTitle("Private by design").setMessage("Enough has no INTERNET permission, analytics, ads, or remote controls. It stores only your selected app packages, daily limits, daily spent time, reset metadata, and a local service-health timestamp. Raw usage events are processed in memory and not saved.\n\nNo screen content or message reading is enabled. System apps, launchers, the default dialer, and Enough itself cannot be selected.\n\nData clearing and uninstall remove the local ledger. Android Settings remains accessible. Clock changes plus a reboot can bypass personal commitment mode; it is not tamper-proof.").setPositiveButton("Got it",null).setNeutralButton("Service settings",(d,w)->disclosure()).show();}
 private void editPlan(){
  if(!UsageReader.allowed(this)||!serviceEnabled()||!store.live()){new AlertDialog.Builder(this).setTitle("Finish setup first").setMessage("Enable usage access and the Enough app-limit service, then return here. The app will show Protection active when ready.").setPositiveButton("OK",null).show();return;}
  boolean tomorrow=store.hasPlan();Map<String,Long> current=store.nextPlan();List<String> candidates=PackageSafety.candidates(this);
  if(candidates.isEmpty()){Toast.makeText(this,"No eligible downloaded apps found. System apps are excluded.",Toast.LENGTH_LONG).show();return;}
  LinearLayout body=column();body.setPadding(dp(20),dp(8),dp(20),dp(12));
  body.addView(text(tomorrow?"These changes start after today’s reset. They cannot unlock an app today.":"Choose 1–240 minutes per app. Time already used today counts toward the budget. Once committed, changes wait until tomorrow.",14,MUTED));
  Map<String,CheckBox> checks=new LinkedHashMap<>();Map<String,EditText> fields=new LinkedHashMap<>();
  for(String pkg:candidates){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);CheckBox check=new CheckBox(this);check.setText(PackageSafety.label(this,pkg));check.setTextColor(INK);check.setChecked(current.containsKey(pkg));row.addView(check,new LinearLayout.LayoutParams(0,-2,1));EditText minutes=new EditText(this);minutes.setInputType(InputType.TYPE_CLASS_NUMBER);minutes.setSingleLine(true);minutes.setText(String.valueOf(current.getOrDefault(pkg,15*60_000L)/60_000L));minutes.setContentDescription("Daily minutes for "+PackageSafety.label(this,pkg));row.addView(minutes,new LinearLayout.LayoutParams(dp(64),-2));body.addView(row);checks.put(pkg,check);fields.put(pkg,minutes);}
  ScrollView scroller=new ScrollView(this);scroller.addView(body);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(tomorrow?"Tomorrow’s plan":"Commit a daily budget").setView(scroller).setNegativeButton("Cancel",null).setPositiveButton(tomorrow?"Save for tomorrow":"Review commitment",null).create();
  dialog.setOnShowListener(ignored->{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
   Map<String,Long> selected=new LinkedHashMap<>();try{for(String pkg:candidates)if(checks.get(pkg).isChecked()){int minutes=Integer.parseInt(fields.get(pkg).getText().toString());BudgetRules.validateMinutes(minutes);selected.put(pkg,(long)minutes);}if(!tomorrow&&selected.isEmpty())throw new IllegalArgumentException("Choose at least one app.");}
   catch(IllegalArgumentException e){Toast.makeText(this,"Choose at least one app and use 1–240 whole minutes per selected app.",Toast.LENGTH_LONG).show();return;}
   new AlertDialog.Builder(this).setTitle(tomorrow?"Save tomorrow’s plan?":"Keep this promise until tomorrow?").setMessage(tomorrow?"Today’s budget cannot be changed. This plan starts after the next daily reset.":"Selected apps will be blocked when their daily time runs out. Today’s usage already counts. There is no in-app override. Reset: "+resetText()).setNegativeButton("Go back",null).setPositiveButton(tomorrow?"Save plan":"Commit limits",(d,w)->{try{store.savePlan(selected);store.reconcile(UsageReader.read(this,store.dayStart(),System.currentTimeMillis()).totals);dialog.dismiss();render();}catch(IllegalArgumentException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}).show();
  });});dialog.show();
 }
}
