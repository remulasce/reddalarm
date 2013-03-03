package com.remulasce.reddalarm;

/** Here be stuff that makes converting dates and stuff easier. Like displaying time till a match ends. **/
public class TimeFormatters {
	
	static int EpS = 1000;			//Epoch ticks per second
	static int EpM = EpS * 60;	//Epoch ticks per minute
	static int EpH = EpM * 60;	//You get the idea
	static int EpD = EpH * 24;
	/** Format time nicely for printing. **/
	public static String time(long time) {
		
		long t = time;
		
		if (t <= 0) {
			return "immediately";
		}
		
		int d=0;
		int h=0;
		int m=0;
		int s=0;
		
		while (t >= EpD) {
			d++;
			t-=EpD;
		}
		while (t >= EpH) {
			h++;
			t-=EpH;
		}
		while (t >= EpM) {
			m++;
			t-=EpM;
		}
		while (t >= EpS) {
			s++;
			t-=EpS;
		}
		
		String ret="";
		if (d>0) {
			ret += d + " days ";
		}
		if (h>0) {
			ret += h + " hours ";
		}
		if (m>0) {
			if (m == 1) {  ret += "1 minute"; }
			else {
				ret += m + " minutes ";
			}
		}
		//Even if it wasn't enough to even be a minute, we return a minute, because otherwise we'd return nothing.
		if (m+d+h == 0) {
			ret += "under a minute";
		}
		return ret;

	}
	/** Return a cool x days, y hours, z minutes time till message. Minute is always rounded up from zero **/
	public static String timeTill(long epochTargetTime) {
		long t = epochTargetTime - System.currentTimeMillis(); //The time difference
		return time(t);
	}
}
