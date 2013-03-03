package com.remulasce.reddalarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/** the AlarmKeepe is helper class used to keep track of alarms posted to the system. It helps maintain the NUL strings of posted alarms.
 * 
 * @author Remulasce
 *
 */
//THis class would benefit a lot from a static alarms list, keeping track of the current alarms between loads
//so we don't have to load from the filesystem every time we want to do every little thing.
public class AlarmKeeper {
	private static final String SAVE_NAME = C.save;
	public static final int		ERROR_IDS_FULL = -5;
	
	/** Add this alarm to the list maintained in the context's sharedsettings, or
	 * edit it if it already exists
	 *  **/
	public static void postAlarm(Context context, Alarm alarm) {
		ArrayList<Alarm> alarms = loadAlarms(context);
		
		Alarm toEdit = getAlarm(alarms, alarm.pendId);
		if (toEdit != null) {
			alarms.remove(toEdit);
			alarms.add(alarm);
		} else {
			alarms.add(alarm);
		}
		
		saveAlarms(context, alarms);
		
	}
	
	/** Returns an unused pending id, or ERROR_IDS_FULL if all ids are taken. **/
	public static int getNewId(Context context) {
//		ArrayList<Alarm> alarms = loadAlarms(context);
		ArrayList<Integer> alarmIds = loadAlarmIds(context);
		
		for (int ii=1000; ii < 1031; ii++) {
			if (!alarmIds.contains(ii)) { return ii; }
		}
		
		return ERROR_IDS_FULL;
	}
	
	private static Alarm getAlarm(ArrayList<Alarm> alarms, int alarmId) {
		for (int ii = 0; ii < alarms.size(); ii++) {
			if (alarms.get(ii).pendId == alarmId) {
				return alarms.get(ii);
			}
		}
		return null;
	}
	
	
	/** Get the alarm with the pendId, or null if none exist **/
	public static Alarm getAlarm(Context context, int alarmId) {
		 ArrayList<Alarm> alarms = loadAlarms(context);
		 for (Alarm each : alarms) {
			 if (each.pendId == alarmId) {
				 return each;
			 }
		 }
		 return null;
	}
	
	public static ArrayList<Alarm> getAlarms(Context context) {
		return loadAlarms(context);
	}
	/** Return the ArrayList of alarms previously posted by the app, in order of alarm time. **/
	public static ArrayList<Alarm> getAlarmsByTime(Context context) {
		ArrayList<Alarm> alarms = loadAlarms(context);
		
		
		Collections.sort(alarms, new Comparator<Alarm>() {
						
			public int compare(Alarm a1, Alarm a2) {
				Calendar c1 = Calendar.getInstance();
				Calendar c2 = Calendar.getInstance();
				
				c1.set(2000, 12, 20, a1.hour, a1.minute);
				c2.set(2000, 12, 20, a2.hour, a2.minute);
				
				if (c1.before(c2)) { return -1; }
				if (c1.after (c2)) { return  1; }
				
				return 0;
			}});
		
		
		return alarms;
	}
	/** Delete the alarm with the PendingIntent's id alarmId. **/
	public static void deleteAlarm(Context context, int alarmId) {
		ArrayList<Alarm> alarms = loadAlarms(context);
		Alarm toDel = new Alarm();
		
		
		for (Alarm each : alarms) {
			if (each.pendId == alarmId) {
				toDel = each;
			}
		}
		alarms.remove(toDel);
		
		saveAlarms(context, alarms);
	}

	
	private static void saveAlarms(Context context, ArrayList<Alarm> alarms) {
		Editor edit = context.getSharedPreferences(SAVE_NAME, 0).edit();
		String rawAlarms = "";
		
		for (Alarm each : alarms) {
			rawAlarms = rawAlarms.concat(each.serialize());
		}
		
		edit.putString(SAVE_NAME, rawAlarms);
		edit.commit();
	}
	
	/** unpack all the alarms stored in the Context's sharedpreferences directly into an array, without sorting. **/
	private static ArrayList<Alarm> loadAlarms(Context context) {
		SharedPreferences edit	= context.getSharedPreferences(SAVE_NAME, 0);
		
		String rawAlarms		= edit.getString(SAVE_NAME, "");
		String[] alarmsArray	= rawAlarms.split("\n");
		
		ArrayList<Alarm> alarms	= new ArrayList<Alarm>();
		
		
		//Unpack all the alarms
		for (int ii = 0; ii < alarmsArray.length; ii++) {
			try {
				Alarm alarm = new Alarm(alarmsArray[ii]);
				alarms.add(alarm);
			}
			catch (Exception e) {
				C.log("Problem parsing posted alarms, ignoring bad value.");
			}
		}
		
		return alarms;
	}
	
	private static ArrayList<Integer> loadAlarmIds(Context context) {
		SharedPreferences edit	= context.getSharedPreferences(SAVE_NAME, 0);
		
		String rawAlarms		= edit.getString(SAVE_NAME, "");
		String[] alarmsArray	= rawAlarms.split("\n");
		
		ArrayList<Integer> alarms	= new ArrayList<Integer>();
		
		
		//Unpack all the alarms
		for (int ii = 0; ii < alarmsArray.length; ii++) {
			try {
				Alarm alarm = new Alarm(alarmsArray[ii]);
				alarms.add(alarm.pendId);
			}
			catch (Exception e) {
				C.log("Problem parsing posted alarms, ignoring bad value.");
			}
		}
		
		return alarms;
	}
	
}
