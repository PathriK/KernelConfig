package com.widget.kernelconfig;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

public class FOService extends Service{
	private FileObserver FOobj;
	private static Context context;
	private static SharedPreferences configVals;
	private static final String tag = "FO Service";
	private int WidgetId;
	private String path;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate(){
		context = getApplicationContext();
		configVals = context.getSharedPreferences(WidgetProvider.PREFS_NAME, 0);
		
	}
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(tag, "Service called");
		WidgetId = intent.getIntExtra(WidgetProvider.WIDGET_ID, -1);
		Log.d(tag, "Intent Widget ID : " + WidgetId);
		if(WidgetId!=-1){
			path = configVals.getString("FilePath_" + WidgetId, "");
		
		File fc = new File(path);
		FOobj = new FileObserver(fc.getAbsolutePath(),FileObserver.ALL_EVENTS){
			@Override
			public void onEvent(int event,String path){
				Log.i(tag, "Event occurred");
				if (((FileObserver.CREATE & event)!=0)||((FileObserver.MODIFY & event)!=0)||((FileObserver.CLOSE_WRITE & event)!=0)) {

		        	Log.i(tag,"will call update");

				Intent updateIntent = new Intent(context,UpdateService.class);
//				updateIntent.setAction(WidgetProvider.WIDGET_ACTION_UPDATE);
				updateIntent.putExtra(WidgetProvider.INTENT_ACTION, WidgetProvider.WIDGET_ACTION_UPDATE);
				context.startService(updateIntent);
				}
			}
		};
		FOobj.startWatching();
		}
	}


}
