package com.widget.kernelconfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateService extends Service {
	private static final String tag = "Service";
	private ToggleThread bkgrndToggle;
	private UpdateThread bkgrndUpdate;
	private Context context;
	private static SharedPreferences configVals;
	private int WidgetId;
	private String intentAction;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		configVals = context.getSharedPreferences(WidgetProvider.PREFS_NAME, 0);
		Log.i(tag, "onCreate Invoked");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(tag, "onDestroy Invoked");
		bkgrndToggle.interrupt();
		bkgrndUpdate.interrupt();
		Log.i(tag, "onDestroy end");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.i(tag, "onStartCommand Invoked");

		intentAction = intent.getStringExtra(WidgetProvider.INTENT_ACTION);
		Log.d(tag, "Intent Action : " + intentAction);

		WidgetId = intent.getIntExtra(WidgetProvider.WIDGET_ID, -1);
		Log.d(tag, "Intent Widget ID : " + WidgetId);

		if (intentAction.equals(WidgetProvider.WIDGET_ACTION_CLICK)) {
			bkgrndToggle = new ToggleThread(WidgetId);
			bkgrndToggle.start();
		} else {
			bkgrndUpdate = new UpdateThread(WidgetId);
			bkgrndUpdate.start();
		}

		Log.i(tag, "onStartCommand End");

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(tag, "onBind Invoked");
		return null;
	}

	private class ToggleThread extends Thread {
		int id;

		ToggleThread(int WidgetIDcon) {
			this.id = WidgetIDcon;
		}

		@Override
		public void run() {
			Log.i(tag, "Toggle Thread Invoked");

			String path;
			int intCurrent,intCnt;
			String val;
			if (id != -1) {
				path = configVals.getString("FilePath_" + id, "");
				if (!path.equals("")) {
					intCnt = configVals.getInt("val_" + id + "_" + "Total", 2);
					Log.d(path, "total = " + intCnt);
					intCurrent = configVals.getInt("val_" + id + "_" + "Current", 1);
					Log.d(path, "CurrentB =" + intCurrent);
					if(intCurrent == intCnt) intCurrent = 1; else intCurrent = intCurrent+1;
					Log.d(path, "CurrentA =" + intCurrent);
//					intCurrent = intCurrent + 1;
					val = configVals.getString("Cval_" + id + "_" + intCurrent, "Unknown");
					Log.d(path, "Val =" + val);
					writeFile(path, val);
				}
			}
//			stopSelf();
			Intent oneFO = new Intent(context,FOService.class);
			oneFO.putExtra(WidgetProvider.WIDGET_ID, id);
			context.startService(oneFO);
			Log.i(tag, "FO Service Started");
		}
	}

	private class UpdateThread extends Thread {
		int id;

		UpdateThread(int WidgetIDcon) {
			this.id = WidgetIDcon;
		}

		@Override
		public void run() {
			Log.i(tag, "Update Thread Invoked");

			String Cval;

			int intCurrent;
			String val;
			int[] allWidgetIds;
			String path;

			Log.d(tag, "Widget ID Update= " + id);

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			if (id == -1) {
				ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
				allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			} else {
				allWidgetIds = new int[1];
				Arrays.fill(allWidgetIds, id);
			}

			for (int id : allWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
				path = configVals.getString("FilePath_" + id, "");
				if (!path.equals("")) {
					Cval = readFile(path);
					intCurrent = configVals.getInt("CRval_" + id + "_" + Cval, 1);
					val = configVals.getString("Dval_" + id + "_" + intCurrent, "Unknown");
					SharedPreferences.Editor editor = configVals.edit();
					remoteViews.setTextViewText(R.id.update, val);
					editor.putInt("val_" + id + "_" + "Current", intCurrent);
					editor.commit();

					Intent clickIntent = new Intent(context, UpdateService.class);

//					clickIntent.setAction(WidgetProvider.WIDGET_ACTION_CLICK);
					clickIntent.putExtra(WidgetProvider.INTENT_ACTION, WidgetProvider.WIDGET_ACTION_CLICK);
					Log.i(tag, "widget id reg" + id);
					clickIntent.putExtra(WidgetProvider.WIDGET_ID, id);

					PendingIntent pendingIntent = PendingIntent.getService(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);

					Log.i(tag, String.valueOf(id));
					appWidgetManager.updateAppWidget(id, remoteViews);
					Log.i(tag, "update done,click registered");

				}

			}
//			stopSelf();
		}

	}

	private String readFile(String path) {
		String data = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			data = reader.readLine();
			reader.close();
			Log.i(tag, "read data string" + data);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	private void writeFile(String path, String data) {

		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(path));
			out.write(data);
			out.flush();
			out.close();
			Log.i(tag, "writing" + data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
