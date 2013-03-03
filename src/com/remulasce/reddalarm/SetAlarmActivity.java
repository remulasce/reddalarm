package com.remulasce.reddalarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

public class SetAlarmActivity extends Activity {
	
	private static final boolean	DEBUG_MODE			= true;
	private static final String		DEBUG_CLASS_TITLE	= "Set ";
	
	private static final int		DIALOG_TIME			= 0;
	private static final int		DIALOG_WEBSITE		= 1;
	private static final int		DIALOG_MESSAGE		= 2;
	
	
	private PopupWindow		pw;
	
	private Alarm			alarm; 
	
	TextView time_disp;
	TextView wobsite_disp;
	//TextView sound_disp;
	TextView message_disp;
	
	boolean ui_enabled		= true;
	boolean new_alarm		= false;
	
	boolean quick_edit		= false;
	boolean quick_enabled;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		log("SetAlarmActivity starting");
		super.onCreate(savedInstanceState);
		
		if (getIntent().getBooleanExtra(C.SET_ALARM_DIRECTLY, false)) {
			log("Skipping UI, setting alarm directly");
			ui_enabled = false;
			quick_edit = true;
			quick_enabled = getIntent().getBooleanExtra(C.SET_ALARM_ENABLED, false);
		} else {
			
			setContentView(R.layout.set_alarm);
			
			
			//Get references to all the UI stuff
			setupElements();
		}
		
		//Either create a new alarm, or reconstitute an alarm we are going to edit.
		loadAlarm();
		
		if (quick_edit) {
			new_alarm =  false;
			postAlarm	(false);
			finish();
		}
	}
	
	private void setupElements() {
		
		time_disp		= (TextView) findViewById(R.id.new_alarm_time_disp);
		wobsite_disp	= (TextView) findViewById(R.id.new_alarm_website_disp);
//		sound_disp		= (TextView) findViewById(R.id.new_alarm_sound_disp);
		message_disp	= (TextView) findViewById(R.id.new_alarm_message_disp);
		
	}
	
	private void loadAlarm() {
		log("LoadAlarm starting");
		
		Intent	init	= getIntent();
		int		id		= init.getIntExtra(C.EXTRA_PENDING_ID, 0);
		log("Incoming Intent id: "+id);
		
		// Id 0 means it's a new alarm
		if (id == 0) {
			Calendar now = Calendar.getInstance();
			
			alarm = new Alarm();
			new_alarm = true;
			alarm.enabled = true;
			
			setTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
			setWobSigt(C.DEFAULT_WEBSITE_URL);
			setMessage(C.DEFAULT_MESSAGE	);
			return;
		}
		//Try to get the presumably-extant alarm for editing. Bad things if this is not found.
		alarm = AlarmKeeper.getAlarm(this, id);
		//Even if the alarm was previously disabled, we assume the user wants the alarm to sound and enable it anyway.
		alarm.enabled = true;
		
		updateDisplay();
	}
	
	/** Set all the visible fields of the activity to the values of the alarm **/
	private void updateDisplay() {
		if (ui_enabled) {
			time_disp	.setText	(alarm.getTime());
			wobsite_disp.setText	(alarm.wobSigtVar);
			message_disp.setText	(alarm.message);
		}
		
	}
	
	
	public void on_accept_click(View v) {
		postAlarm(false);
		finish();
	}
	
	private void postAlarm(boolean delete) {
		if (quick_edit) { alarm.enabled = quick_enabled; }
		Intent alarmIntent = alarm.getIntent();
		
		
		alarmIntent.setClass(this, PostAlarmActivity.class);
		if (delete) { alarmIntent.putExtra(C.POST_DELETE_ALARM, true); }
        
        startActivity(alarmIntent);
	}
	
	public void on_delete_click(View v) {
		//If it was a new alarm, we never posted it, so we don't have to actually delete. This also prevents the "Alarm Deleted" toast.
		if (!new_alarm) {
			postAlarm(true);
		}
		finish();
	}
	
	public void on_cancel_click(View v) {
		finish();
	}
	
	
	
	static class ViewHolder {
		public View dataView;
	}
	
	//On click listeners for editing datums
	public void on_time_click(View view) {
		showDialog(DIALOG_TIME);
		
	}
	
	private OnTimeSetListener on_time_set = new OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			setTime(hourOfDay, minute);
		}
	};
	
	public void on_website_click(View view) {
		showDialog(DIALOG_WEBSITE);
		
	}
	public void on_sound_click(View view) {
		//TODO
	}
	public void on_message_click(View view) {
		showDialog(DIALOG_MESSAGE);
		
	}
	private void setTime(int hour, int min) {
		alarm.hour = hour; alarm.minute = min;
		updateDisplay();
		
	}
	private void setWobSigt(String wobSigt) {
		alarm.wobSigtVar = wobSigt;
		updateDisplay();
		
	}
	/*
	private void setSound(String sound) {
		alarm.sound = sound;
		sound_disp.setText(sound);
	}*/
	private void setMessage(String message) {
		alarm.message = message;
		updateDisplay();
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id)	 {
		switch (id) {
		case (DIALOG_TIME):
			return new TimePickerDialog(this, on_time_set, alarm.hour, alarm.minute, false);
		case (DIALOG_WEBSITE):
		{
			final EditText input = new EditText(this);
			input.setText(alarm.wobSigtVar);
			input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
			AlertDialog ret = new AlertDialog.Builder(this)
				.setTitle("Set Website URL")
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						setWobSigt(input.getText().toString()); 
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						input.setText(alarm.wobSigtVar);
						dialog.cancel();
					}
				}).show();
			
			new Handler().postDelayed(new Runnable() {
				public void run() {
					input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
					input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP  , 0, 0, 0));
				}
			}, 200);
			return ret;
		}
		case (DIALOG_MESSAGE):
		{
			final EditText input = new EditText(this);
			input.setText(alarm.message);
			AlertDialog ret = new AlertDialog.Builder(this)
				.setTitle("Set Message")
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						setMessage(input.getText().toString()); 
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						input.setText(alarm.message);
						dialog.cancel();
					}
				}).show();
			
			new Handler().postDelayed(new Runnable() {
				public void run() {
					input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
					input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP  , 0, 0, 0));
				}
			}, 200);
			
			return ret;
		}
		}
		
		
		
		return null;
	}
	
	private static void log(String msg) {
		if (DEBUG_MODE) {
			C.log(DEBUG_CLASS_TITLE+" | "+msg);
		}
	}
	
}
