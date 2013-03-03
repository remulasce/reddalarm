package com.remulasce.reddalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent update = new Intent(context, PostAlarmActivity.class);
		Toast.makeText(context, "Initialized on boot", Toast.LENGTH_LONG).show();
		update.putExtra(C.POST_UPDATE_SYS, true);
//		context.startActivity(update);
	}

}
