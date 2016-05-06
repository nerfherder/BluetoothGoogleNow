package com.nerfherder.bluetoothgooglenow;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class BluetoothGoogleNow implements IXposedHookLoadPackage  {
	
	private Context mContext = null;
	
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
    	
    	if (!lpparam.packageName.equals("com.android.bluetooth"))
            return;
    	
    	Class<?> AtPhonebook = findClass("com.android.bluetooth.hfp.AtPhonebook", lpparam.classLoader);
    	
    	XposedBridge.hookAllConstructors(AtPhonebook, new XC_MethodHook() {
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    			mContext = (Context) getObjectField(param.thisObject, "mContext");
    		}
    	});
    	
    	//getLastDialledNumber in the class AtPhonebook is only called when bluetooth is requesting a redial
    	
    	findAndHookMethod("com.android.bluetooth.hfp.AtPhonebook", lpparam.classLoader, "getLastDialledNumber", new XC_MethodReplacement() {
      		@Override
    		protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {  	
    			
      			PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
      			//turn on screen by acquiring wakelock
      			WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "bluetooth screen on");
      			wl.acquire();
      			//call intent for google now search
      		
      			Intent intent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);		
				
      			wl.release(); //release wakelock

				return null;
    			
    		}
    	});

    }
}