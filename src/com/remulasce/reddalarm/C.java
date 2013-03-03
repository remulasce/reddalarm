package com.remulasce.reddalarm;

import android.util.Log;


/** Dev convenience things **/
public class C {
	public	static String	id			= "SureAlarm";
	public	static String	save		= "Alarms";
	
	
	
	public static final String ACTION_WEB_ALARM		= "com.remulasce.reddalarm.WEB_ALARM";
	public static final String ACTION_SYSTEM_ALARM	= "android.intent.action.SET_ALARM";
	public static final String ACTION_ALARM_SOUNDED	= "com.remulasce.reddalarm.ALARM_SOUNDED"; //Broadcast sent when one of our alarms sounds
	
	public static final String EXTRA_ALARM_TEST		= "com.remulasce.reddalarm.ALARM_TEST";
	public static final String EXTRA_HOUR			= "android.intent.extra.alarm.HOUR";
	public static final String EXTRA_MINUTES		= "android.intent.extra.alarm.MINUTES";
	public static final String EXTRA_ENABLED		= "com.remulasce.reddalarm.ENABLED";
	public static final String EXTRA_WEBSITE_URL	= "com.remulasce.reddalarm.WEB_URL";
	public static final String EXTRA_MESSAGE		= "com.remulasce.reddalarm.MESSAGE";
	public static final String EXTRA_PENDING_ID		= "com.remulasce.reddalarm.PENDING_ID";
	
	public static final String DEFAULT_WEBSITE_URL	= "http://www.reddit.com";
	public static final String DEFAULT_MESSAGE		= "The links are all blue again!1!";
	
	public static final String SET_ALARM_DIRECTLY	= "com.remulasce.reddalarm.SET_ALARM_DIRECTLY";
	public static final String SET_ALARM_ENABLED	= "com.remulasce.reddalarm.SET_ALARM_ENABLED";
	
	public static final String POST_DELETE_ALARM	= "com.remulasce.reddalarm.POST_DELETE_ALARM"; 
	public static final String POST_UPDATE_SYS		= "com.remulasce.reddalarm.POST_UPDATE_SYS";
	
	private static boolean	DEBUG_MODE	= true;
	public	static void log(String message) {
		if (DEBUG_MODE) {
			Log.d(id, message);
		}
	}
}
