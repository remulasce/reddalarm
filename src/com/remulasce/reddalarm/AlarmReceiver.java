package com.remulasce.reddalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	private static boolean DEBUG_MODE = true;
	
	
	

	
	
	public void onReceive(Context context, Intent intent) {
		log("AlarmReceiver activated");
		Toast.makeText(context, "Ring ring ring", Toast.LENGTH_LONG);
		
		Intent alarmIntent = intent;//.cloneFilter();
		alarmIntent.setClass(context, AlarmActivity.class);
		alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(alarmIntent);
		
	}
	
	private static void log(String message) {
		if (DEBUG_MODE) {
			C.log(message);
		}
	}
}
