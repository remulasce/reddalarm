package com.remulasce.reddalarm;

import android.content.Intent;


public class Alarm {
	private static final boolean DEBUG_CLASS = true;
	
	
//	public Calendar time;
	public int		pendId		= -2;
	public int		hour		= 10;
	public int		minute		= 0;
	public String	wobSigtVar	= "notasite.com";		//Matches the latest version of the Nought style guidelines
	public String	sound		= "testsound.wav";		//Unused
	public String	message		= "imamessage";
	boolean			enabled		= false;
	
	
	public Alarm(String raw) {
		pendId		= (int) 	NULman.getNum("pendId"	, raw	);
		String time =			NULman.getStr("time"	, raw	);
		hour		= (int) 	NULman.getNum("hour"	, time	);
		minute		= (int) 	NULman.getNum("minutes"	, time	);
		wobSigtVar	=			NULman.getStr("website"	, raw	);
		message		=			NULman.getStr("message"	, raw	);
		
		enabled		= Boolean.parseBoolean(NULman.getStr("enabled", raw	));
	}
	
	public Alarm() {}
	
	public String getHour() {
		boolean am = (hour < 12);
		int deHour = (am) ? hour : hour - 12;
		if (deHour == 0) { deHour = 12; }
		return String.valueOf(deHour);
	}
	public String getMinutes() {
		return pad(minute);
	}
	public String getDayHalf() {
		boolean am = (hour < 12);
		return (am) ? "am":"pm";
	}
	
	/** deprecated, use getHour and getMinutes and getDayHalf instead **/
	public String getTime() {
		boolean am = (hour < 12);
		int deHour = (am) ? hour : hour - 12;
		if (deHour == 0) { deHour = 12; }
		String dHour = String.valueOf(deHour);
		
		
		String end = am?"AM":"PM";
		return dHour + ":" + pad(minute) + " " + end; 
	}
	
	public String serialize() {
		String ret = "(identifier:alarm)(pendId:"+pendId+")(time:(hour:"+hour+")(minutes:"+minute+"))(website:"+wobSigtVar+")(message:"+message+")(enabled:"+enabled+")\n"; 
		log(ret);
		return ret;
	}
	public Intent getIntent() {
		Intent intent = new Intent(C.ACTION_WEB_ALARM);
		
		intent.putExtra(C.EXTRA_MINUTES		, minute	);
		intent.putExtra(C.EXTRA_HOUR		, hour		);
		intent.putExtra(C.EXTRA_PENDING_ID	, pendId	);
		intent.putExtra(C.EXTRA_WEBSITE_URL	, wobSigtVar);
		intent.putExtra(C.EXTRA_ENABLED		, String.valueOf(enabled)	);
		intent.putExtra(C.EXTRA_MESSAGE		, message	);
		
//		C.log("Alrm | enabled posted: "+intent.getStringExtra(C.EXTRA_ENABLED));
		
		return intent;
	}
	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
	
	private static void log(String msg) {
		if (DEBUG_CLASS) {
			C.log("Alrm | "+msg);
		}
	}
	
}
