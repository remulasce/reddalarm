package com.remulasce.reddalarm;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.remulasce.reddalarm.R.layout;

public class AlarmActivity extends Activity {
	
	private static final boolean DEBUG_MODE		= false;
	private static final boolean DEBUG_SILENT	= false;
	
	
	private static final int	AA_INTERVAL		= 3000;	//The interval in seconds between awake checks. User must interact every this seconds or be rewoken. 
	private static final int	VOL_INTERVAL	= 1000;		//How often we should update the volume for volume ramping. Does not affect how quickly it reaches full volume.
	private static final int	VOL_RAMP_TIME	= 20000;	//How long it takes for the volume to reach 100%. Not exact unless this is a multiple of VOL_INTERVAL.
	private static final float	VOL_INITIAL		= .5f;		//The volume the alarm starts out at.
	
	Button			b_silence_launch;
	RelativeLayout	l_root;
	MediaPlayer		mMediaPlayer;
	ReddWebView		webView;
	ProgressBar		progress;
	Vibrator		vibrator;
	
	Handler			handler;
	long			startTime;
	boolean			lifeSign			= true;	//Whether or not the user has interacted recently
	
	final Activity		activity		= this;
	
	boolean exit			= false;
	boolean isTest			= false;		//We are just debugging the Alarm, there was no susch thing added to the alarms list
	
	boolean	firstLoadReddit	= false;		//If it's Reddit, let's align the tahread titles for the user once the page loads the first time. 
	boolean silenced		= false;		//Whether the alarm has been silenced yet
	final int initTries		= 15;
	int tries				= initTries;	//How many more times to play the alarm sound before giving up.
	 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //These flags override the lock screen, so user doesn't have to try to type password as he exits REM.
        this.getWindow().addFlags(	WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED	| 
        							WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON		|
        							WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        
        log("Alarm activity started");
        
        isTest = getIntent().getBooleanExtra(C.EXTRA_ALARM_TEST, false);
        
        if (!isTest) {
        	if (!getIntent().getStringExtra(C.EXTRA_ENABLED).equals("true")) {
        		log("Alarm was not enabled");
        		finish();
        		return;
        	}
        }
        
		//Wee startup stuff//
		setContentView	(layout.alarm_view);
		//no relevant texts//
		
		startTime = System.currentTimeMillis();
		handler = new Handler();
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		//Note in our logs that this alarm has been sounded
		disableAlarm();
		
		
		//Tell everybody this alarm.... has been FIRED!
		broadcastAlarmFired();
		
		
		//Get references to all of our ui objects
		setupElements();
		
		
		//Whee wake up time!
		soundAlarm();

		
		//Make me a browser, ho!
		setupBrowser();
		
		
		//Start the slowly-increase-volume thing
		setupVolumeThrottling();
		
    }

    private void broadcastAlarmFired() {
    	Intent cast = new Intent(C.ACTION_ALARM_SOUNDED);
    	sendBroadcast(cast);
    }

	private void soundAlarm() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); 
//		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		mMediaPlayer = new MediaPlayer();
		
		 try {
			mMediaPlayer.setDataSource(this, alert);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mMediaPlayer.prepare();
//			mMediaPlayer.setVolume(Math.max((initTries-tries/2F)/initTries,1), Math.max((initTries-tries/2F)/initTries,1));
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					if (tries > 0 && !silenced && !exit) {
						tries--;
						mMediaPlayer.start();
					}
					
				} });
			mMediaPlayer.setVolume(VOL_INITIAL, VOL_INITIAL);
			if (!DEBUG_SILENT) { mMediaPlayer.start(); }
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		 vibrator.vibrate(new long[] {0,200,400,200,400,400,2000}, 1);
		 
	}

	private void setupBrowser() {
		
		WebSettings webSettings = webView.getSettings();
//		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setUseWideViewPort(true);
		
		
		webView.loadUrl(getIntent().getStringExtra(C.EXTRA_WEBSITE_URL));
		if (getIntent().getStringExtra(C.EXTRA_WEBSITE_URL).equals("http://www.reddit.com"));
		webView.setWebViewClient(new ReddWebClient());
		
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int percent) {
				if (percent < 100 && progress.getVisibility() == ProgressBar.GONE) {
					progress.setVisibility(ProgressBar.VISIBLE);
				}
				progress.setProgress(percent);
				if (percent == 100) {
					if (firstLoadReddit && getIntent().getStringExtra(C.EXTRA_WEBSITE_URL).equals("http://www.reddit.com")) {
						firstLoadReddit = false;
						webView.scrollBy(50, 0);
						webView.flingScroll(0, 850);
					}
					progress.setVisibility(ProgressBar.GONE);
				}
			}
		});
	}
	
	private Runnable awake = new Runnable() {
		public void run() {
			if (exit) { return; }
			if (lifeSign) {
				lifeSign = false;
			} else {
				Toast.makeText(activity, "Wake Up!", Toast.LENGTH_SHORT).show();
				vibrator.vibrate(1000);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mMediaPlayer.setVolume(.2F,.2F);
				mMediaPlayer.start();
				tries--;
			}
			if (!exit && tries > 0) {handler.postDelayed(awake, AA_INTERVAL); }
		}
	};
	
	private void startAwakeAssurance() {
		handler.postDelayed(awake, AA_INTERVAL);
	}
	
	private Runnable volumeRun = new Runnable() {
		public void run() {
			if (exit) { return; }
			float volume = ((float)System.currentTimeMillis() - startTime) / (float)VOL_RAMP_TIME + VOL_INITIAL; 
			long timePast = System.currentTimeMillis() - startTime;
//			float volume = (float) (Math.pow(timePast / (float)VOL_RAMP_TIME, 2) + .01F * (1-timePast/(float)VOL_RAMP_TIME));
			log("Volume: "+volume);
			if (volume > 1) {
				mMediaPlayer.setVolume(1, 1);
			}
			else {
				mMediaPlayer.setVolume(volume, volume);
				if (!silenced && !exit) {
					handler.postDelayed(volumeRun, VOL_INTERVAL);
				}
			}
		}
	};
	
	private void setupVolumeThrottling() {
		handler.postDelayed(volumeRun, VOL_INTERVAL);
	}
	
	private void setupElements() {
		l_root = (RelativeLayout) findViewById(R.id.alarm_root);
		
		//get references to all of our objects
		webView									= (ReddWebView) findViewById(R.id.webview);
		webView.setOwner						(this);
		b_silence_launch						= (Button) findViewById(R.id.alarm_silence_launch);
		b_silence_launch.setOnClickListener		(on_silence_launch);
		progress								= (ProgressBar) findViewById(R.id.alarm_progress);
	}
    
    private void disableAlarm() {
    	if (isTest) { return; }
    	int id = getIntent().getIntExtra(C.EXTRA_PENDING_ID, 0);
    	
    	Alarm me = AlarmKeeper.getAlarm(this, id);
    	me.enabled = false;
    	AlarmKeeper.postAlarm(this, me);
		
    }
    
	private OnClickListener on_silence_launch = new OnClickListener() {
		public void onClick(View v) {
			
			mMediaPlayer.pause();
			vibrator.cancel();
			silenced = true;
			
			startAwakeAssurance();
			
			removeSilenceButton();
			
		}
	};
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if (webView.canGoBack()) {
	    		webView.goBack();
	    		return true;
	    	}
	    	else {
	    		mMediaPlayer.stop();
	    		exit = true;
	    		finish();
	    	}
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void removeSilenceButton() {
		if (silenced) {
			l_root.removeView(b_silence_launch);
		}
	}
	
	public static class ReddWebView extends WebView {
		AlarmActivity owner;
		public ReddWebView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		public void setOwner(AlarmActivity owner) {
			this.owner = owner;
		}
		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			if (!owner.lifeSign && owner.silenced) {
				owner.lifeSign = true;
				owner.mMediaPlayer.pause();
				owner.mMediaPlayer.seekTo(0);
				owner.vibrator.cancel();
			}
			return super.onTouchEvent(ev);
		}
		
		
	}
	
	//Web browser
	private class ReddWebClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
		
	}
	@Override
	protected void onPause() {
		super.onPause();
		exit = true;
		mMediaPlayer.stop();
		vibrator.cancel();
	}
	
	@Override
	protected void onDestroy() {
		exit = true;
		super.onDestroy();
	}
	
	private static void log(String msg) {
		if (DEBUG_MODE) {
			C.log(msg);
		}
	}
	
}