package org.enough.app;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.telecom.TelecomManager;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PackageSafety {
 private PackageSafety(){}
 public static boolean canLimit(Context context,String pkg){
  if(pkg.equals(context.getPackageName())||pkg.equals("com.android.settings")||pkg.equals("com.android.systemui"))return false;
  PackageManager pm=context.getPackageManager();
  try{
   ApplicationInfo info=pm.getApplicationInfo(pkg,0);
   // Some phones preload Instagram. Permit that exact distraction package, while
   // retaining the essential-app checks below and excluding all other system apps.
   if(((info.flags & ApplicationInfo.FLAG_SYSTEM)!=0 && !pkg.equals("com.instagram.android")) || pm.getLaunchIntentForPackage(pkg)==null)return false;
   InputMethodManager input=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
   if(input!=null)for(InputMethodInfo keyboard:input.getEnabledInputMethodList())if(pkg.equals(keyboard.getPackageName()))return false;
   Intent home=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
   for(ResolveInfo r:pm.queryIntentActivities(home,0)) if(r.activityInfo.packageName.equals(pkg))return false;
   TelecomManager telecom=(TelecomManager)context.getSystemService(Context.TELECOM_SERVICE);
   return telecom==null || !pkg.equals(telecom.getDefaultDialerPackage());
  }catch(PackageManager.NameNotFoundException e){return false;}
 }
 public static List<String> candidates(Context context){
  PackageManager pm=context.getPackageManager();Set<String> found=new HashSet<>();
  for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)){String pkg=r.activityInfo.packageName;if(canLimit(context,pkg))found.add(pkg);}
  List<String> list=new ArrayList<>(found);list.sort((a,b)->label(context,a).compareToIgnoreCase(label(context,b)));return list;
 }
 public static String label(Context context,String pkg){try{return context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(PackageManager.NameNotFoundException e){return pkg;}}
}
