package com.remulasce.reddalarm;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity{
	
	private static boolean	TESTALARM			= false;	//Immediately start & sound an alarm.
	private static boolean	DEBUG_CLEAR_ALARMS	= false;	//Immediately delete all alarms before doing anything.
	private static boolean	DEBUG_MODE			= false;		//Print detailed debug messages to the console
	
	private static String	DEBUG_CLASS_TITLE	= "Main";
	
	private static final int ALARMS_EDIT_CODE	= 1;
	
	ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	Context context	= this;
	Calendar cal = Calendar.getInstance();
	
	ListView		lv_main;
	MainListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log("Starting Main");
		
		setContentView(R.layout.main);
		
		setupElements();
		
		enableAlarmReceiver();
		
		if (TESTALARM) {
			Intent alarmIntent = new Intent(this, AlarmActivity.class);
			alarmIntent.putExtra(C.EXTRA_WEBSITE_URL, "http://www.reddit.com");
			alarmIntent.putExtra(C.EXTRA_ALARM_TEST, true);
			startActivity(alarmIntent);
		}
		
		if (DEBUG_CLEAR_ALARMS) {
			Editor edit = getSharedPreferences(C.save, 0).edit();
			edit.putString(C.save, "");
			edit.commit();
		}

		updateAlarmList();
		
		syncAlarms(); //Let's not use this until it's better...
		
	}
	
	@Override
	public void onDestroy() {
		super.onPause();
		
		disableAlarmReceiver();
	}
	
	private void enableAlarmReceiver() {
		IntentFilter filter = new IntentFilter(C.ACTION_ALARM_SOUNDED);
		registerReceiver(on_alarm_return, filter);
	}
	
	private void disableAlarmReceiver() {
		unregisterReceiver(on_alarm_return);
	}
	
	private void setupElements() {
		// Create/reference our objects //
		lv_main		= (ListView)	findViewById(R.id.main_list);
		adapter		= new MainListAdapter();

		
		//Set behaviors to the objects
		lv_main.setAdapter					(adapter		);
		
	}
	
	private void updateAlarmList() {
		alarms.clear();
		
		alarms = AlarmKeeper.getAlarmsByTime(this);
		adapter.notifyDataSetChanged();

	}
	/** Syncs the operating system to the last user-set state. updatealarmlist() must have been called first. **/
	private void syncAlarms() {
		Intent intent = new Intent(this, PostAlarmActivity.class);
		intent.putExtra(C.POST_UPDATE_SYS, true);
		startActivity(intent);
	}
	
	private class MainListAdapter extends BaseAdapter {
		private LayoutInflater inflator;
		private class Tag {public Object data; public Tag(Object data) {this.data = data;} }
		public MainListAdapter() {
			inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		public int getCount() {
			return 2 + alarms.size();
		}
		public Object getItem(int pos) {
			return null;
		}
		public long getItemId(int position) {
			return position;
		}
		@Override
		public int getItemViewType(int pos) {
			return Math.min(pos, 2);
		}
		@Override
		public int getViewTypeCount() {
			return 3;
		}
		
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				if (pos == 0) {
					convertView = inflator.inflate(R.layout.main_item_title, parent, false);
					convertView.findViewById(R.id.main_about).setOnClickListener(on_about_click);
				}
				else if (pos == 1) {
					convertView = inflator.inflate(R.layout.main_item_new, parent, false);
					convertView.setOnClickListener(on_new_alarm_click);
				}
				else {
					convertView = inflator.inflate(R.layout.main_item_alarm, parent, false);
				}
			}
			
			if (pos == 0) {	//No need to change, right? Since there's only one title, the old data will still be correct
			}
			else if (pos == 1) {//same as ^
			}
			
			else {
				Alarm alarm = alarms.get(pos-2);
				((TextView)(convertView.findViewById(R.id.main_item_alarm_hour   ))).setText(alarm.getHour());
				((TextView)(convertView.findViewById(R.id.main_item_alarm_minutes))).setText(alarm.getMinutes());
				((TextView)(convertView.findViewById(R.id.main_item_alarm_dayhalf))).setText(alarm.getDayHalf());
				ToggleButton toggle = (ToggleButton)(convertView.findViewById(R.id.main_item_alarm_toggle)); 
				toggle.setTag(new Tag(alarm));
				toggle.setOnCheckedChangeListener(on_toggle_changed);
				toggle.setChecked(alarm.enabled);
				String webDisp	= alarm.wobSigtVar;
				int index = webDisp.indexOf("www."); 
				if (index != -1) {
					webDisp = webDisp.substring(index+4);
				}
				((TextView)(convertView.findViewById(R.id.main_item_alarm_website))).setText(webDisp);
				
				//Associate the actual alarm with the layout so it can be easily accessed on events
				convertView.setTag(alarm);
				convertView.setOnClickListener(on_edit_alarm_click);
			}
			return convertView;
		}
	}
	private OnClickListener on_new_alarm_click	= new OnClickListener() {
		public void onClick(View v) {
			startActivityForResult(new Intent(context, SetAlarmActivity.class), ALARMS_EDIT_CODE);
		}
	};
	private OnClickListener on_edit_alarm_click	= new OnClickListener() {
		public void onClick(View v) {
			Intent editIntent = new Intent(context, SetAlarmActivity.class);
			editIntent.putExtra(C.EXTRA_PENDING_ID, ((Alarm)v.getTag()).pendId);
			startActivityForResult(editIntent, ALARMS_EDIT_CODE);
		}
	};
	
	private OnClickListener on_about_click		= new OnClickListener() {
		public void onClick(View v) {
			startActivity(new Intent(context, AboutActivity.class));
		}
	};
	
	private OnCheckedChangeListener on_toggle_changed = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Alarm alarm = (Alarm)((com.remulasce.reddalarm.MainActivity.MainListAdapter.Tag)buttonView.getTag()).data; 
			
			if (alarm.enabled != isChecked) {
				Intent quickEdit = new Intent(context, SetAlarmActivity.class);
				quickEdit.putExtra(C.EXTRA_PENDING_ID,		alarm.pendId	);
				quickEdit.putExtra(C.SET_ALARM_DIRECTLY,	true			);
				quickEdit.putExtra(C.SET_ALARM_ENABLED,		isChecked		);
				startActivityForResult(quickEdit, ALARMS_EDIT_CODE);
			}
		}
		
	};
	
	private BroadcastReceiver on_alarm_return = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateAlarmList();
		}
		
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case (ALARMS_EDIT_CODE):
			updateAlarmList();
			break;
		}
	}
	
	private static void log(String msg) {
		if (DEBUG_MODE) {
			C.log(DEBUG_CLASS_TITLE+" | "+msg);
		}
	}
	
	
}
