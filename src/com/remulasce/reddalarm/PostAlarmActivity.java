package com.remulasce.reddalarm;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;


/** Receives intents to set an alarm from places, including the system **/
public class PostAlarmActivity extends Activity {

	private static final boolean	DEBUG_MODE			= true;
	private static final String		DEBUG_CLASS_NAME	= "Post";
	
	Alarm	webAlarm;
	Bundle	inData;
	
	//ArrayList<Alarm> allAlarms;
	
	boolean updateSys	= false;
	boolean editOnly	= false;
	boolean delete		= false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log("Received intentreceiver");
		
		inData = getIntent().getExtras();
		
		if (inData.getBoolean(C.POST_UPDATE_SYS		)) {
			updateSys = true;
			
			ArrayList<Alarm> alarms = AlarmKeeper.getAlarms(this);
			for (Alarm each : alarms) {
				postAlarm(each);
			}
			finish(); 
			return;
		}
		if (inData.getBoolean(C.POST_DELETE_ALARM	)) {
			delete = true;
			log("Action Delete alarm");
		}
		
		
		
		doIt();
		
		finish();
	}

	
	private void doIt() {
		//The intent that will be called at alarmtime
		webAlarm = new Alarm();
		
		
		//Do what every intent this receives has
		applyBasics();
		
		
		// If we picked up the intent from the os, then it won't have specified the website to bring up at alarm. 
		applyWebsite();
		
		
		// See if this alarm is actually enabled. If it's from the OS, it's enabled by default.
		applyEnabled();
		
		
		//Figure out what unique id the relevant pendingintent uses. It might have been provided in the intent if this is an update, or we might need to find a new one.
		applyId();
		
		
		//Get Android to call this later
		postAlarm(webAlarm);
		
		
	}
	
	
	private void applyBasics() {
		webAlarm.hour		= inData.getInt(C.EXTRA_HOUR);
		webAlarm.minute 	= inData.getInt(C.EXTRA_MINUTES);
	}
	
	private void applyWebsite() {
		String url = inData.getString(C.EXTRA_WEBSITE_URL);
		if (url == null) {
			url = C.DEFAULT_WEBSITE_URL;
		}
		webAlarm.wobSigtVar = url;
		log("Put website: "+webAlarm.wobSigtVar);
	}
	
	private void applyEnabled() {
		log("Enabled string: "+inData.getString(C.EXTRA_ENABLED));
		boolean enabled = !"false".equals(inData.getString(C.EXTRA_ENABLED)); //Anything but "false" is enabled, including the nothing that the os gives us
		webAlarm.enabled = enabled;
	}
	
	private void applyId() {
		
		//Get the id the intent requested, or impossible one if new alarm //
		int id = inData.getInt(C.EXTRA_PENDING_ID);
		log("incoming id:" + id);
		if (AlarmKeeper.getAlarm(this, id) != null) {
			log("Incoming id was found in saved alarms");
			webAlarm.pendId = id;
		}
		//Otherwise we should find ourselves a new id for this. Reuse within 32 space so we can brute-force emergency-cancel everything in other places.
		else {
			webAlarm.pendId = AlarmKeeper.getNewId(this);
		}
	}
	
	private void postAlarm(Alarm alarm) {
		
		Intent reddIntent			= alarm.getIntent();
		
		int flag = PendingIntent.FLAG_UPDATE_CURRENT;
		if (delete) { flag = PendingIntent.FLAG_CANCEL_CURRENT; }
//		if (editOnly) { flag = PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_UPDATE_CURRENT; }
		PendingIntent pend			= PendingIntent.getBroadcast(getApplicationContext(), alarm.pendId, reddIntent, flag);
		AlarmManager alarmManager	= (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		
		if (delete) {
			alarmManager.cancel(pend);
			AlarmKeeper.deleteAlarm(this, alarm.pendId);
			Toast.makeText(getApplicationContext(), "Alarm Deleted", Toast.LENGTH_LONG).show();
		}
		else {
			alarmManager.set(AlarmManager.RTC_WAKEUP, getLaunchMillis(alarm), pend);
			if (alarm.enabled && !updateSys) {
				Toast.makeText(this.getApplicationContext(), "Alarm Set in "+TimeFormatters.timeTill(getLaunchMillis(alarm)), Toast.LENGTH_LONG
						).show();
			} else if (!updateSys) {
				Toast.makeText(this.getApplicationContext(), "Alarm Disabled", Toast.LENGTH_SHORT).show();
			}
			
			AlarmKeeper.postAlarm(this, alarm);
		}
		
		log("Posted alarm id: " + alarm.pendId);
		
		
	}
	/** Get the time, in milliseconds, at which the alarm should fire, as expected by alarmmanager.set **/
	private long getLaunchMillis(Alarm alarm) {
		/* Use the Calendar class to turn the hour-minute data into the millis-to-alarm needed by AlarmManager */
		Calendar alarmTime = Calendar.getInstance();
		
		int hour	= alarm.hour;
		int min		= alarm.minute;
		
		alarmTime.set(Calendar.HOUR_OF_DAY, hour);
		alarmTime.set(Calendar.MINUTE, min);
		
		//If time is set in the past, set it to tomorrow
		if (alarmTime.getTimeInMillis() < System.currentTimeMillis()) {
			alarmTime.set(Calendar.DAY_OF_MONTH, alarmTime.get(Calendar.DAY_OF_MONTH) + 1);
		}
		
		return alarmTime.getTimeInMillis();
	}

	
	private static void log(String message) {
		if (DEBUG_MODE) {
			C.log(DEBUG_CLASS_NAME+" | "+message);
		}
	}
}
