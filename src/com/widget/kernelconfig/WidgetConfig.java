package com.widget.kernelconfig;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;

public class WidgetConfig extends Activity {

	private static final String tag = "ConfigClass";
	private static Context context;
	private static int WidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(tag, "OnCreate Invoked");

		context = WidgetConfig.this;
		Log.i(tag, "context initialised");

		SharedPreferences sharedPrefs = getSharedPreferences(WidgetProvider.PREFS_NAME, 0);
		SharedPreferences.Editor sPeditor = sharedPrefs.edit();
		sPeditor.putBoolean("ConfigDone", false);
		sPeditor.commit();
		Log.i(tag, "ConfigDone Shared Pref Set to False");

		setContentView(R.layout.activity_config);
		Log.i(tag, "Displaying WidgetConfig Activity");

		setResult(RESULT_CANCELED);
		Log.i(tag, "RESULT_CANCELED set");

		WidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null)
			WidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		if (WidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			Log.i(tag, "Invalid ID - Finish");
			finish();
		}

		Log.d(tag, "Widget ID : " + WidgetId);
		Log.i(tag, "Valid ID - Waiting");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.config, menu);
		return true;
	}

	public void configure(View view) {
		Log.i(tag, "Configure Invoked");

		storeVals();
		Log.i(tag, "Values Store Done");

		InitialUpdate();
		Log.i(tag, "Initial Update Done");

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetId);
		setResult(RESULT_OK, resultValue);
		Log.i(tag, "RESULT_OK Set");

		finish();
	}

	public void addRow(View view) {
		Log.i(tag, "AddRow Invoked");
		ViewGroup vg;
		View v;
		int intCnt;

		LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		v = vi.inflate(R.layout.edit_text, null);
		vg = (ViewGroup) findViewById(R.id.DisplayValue);
		intCnt = vg.getChildCount();
		vg.addView(v, intCnt);
		Log.i(tag, "DisplayValue Row Added");

		v = vi.inflate(R.layout.edit_text, null);
		vg = (ViewGroup) findViewById(R.id.ConfigValue);
		intCnt = vg.getChildCount();
		vg.addView(v, intCnt);
		Log.i(tag, "ConfigValue Row Added");
	}

	public void removeRow(View view) {
		Log.i(tag, "RemoveRow Invoked");

		ViewGroup vg = (ViewGroup) findViewById(R.id.DisplayValue);
		int intCnt = vg.getChildCount();
		if (intCnt > 1) {
			vg.removeViewAt(intCnt - 1);
			vg = (ViewGroup) findViewById(R.id.ConfigValue);
			intCnt = vg.getChildCount();
			vg.removeViewAt(intCnt - 1);
		}

	}

	private void storeVals() {
		Log.i(tag, "Storevals Invoked");
		ViewGroup vgDV = (ViewGroup) findViewById(R.id.DisplayValue);
		ViewGroup vgCV = (ViewGroup) findViewById(R.id.ConfigValue);
		EditText et = (EditText) findViewById(R.id.FilePath);
		String val;

		SharedPreferences sharedPrefs = getSharedPreferences(WidgetProvider.PREFS_NAME, 0);
		SharedPreferences.Editor sPeditor = sharedPrefs.edit();

		val = et.getText().toString();
		sPeditor.putString("FilePath_" + WidgetId, val);

		int intCnt = vgDV.getChildCount();
		sPeditor.putInt("val_" + WidgetId + "_" + "Total", intCnt - 1);
		sPeditor.putInt("val_" + WidgetId + "_" + "Current", intCnt - 1);
		Log.d(tag, "Total & Current = " + String.valueOf(intCnt - 1));

		for (int j = 1; j < intCnt; j++) {
			et = (EditText) vgDV.getChildAt(j);
			val = et.getText().toString();
			sPeditor.putString("Dval_" + WidgetId + "_" + j, val);
			Log.d(tag, "Stored Dval_ " + WidgetId + "_" + j + "=" + val);

			et = (EditText) vgCV.getChildAt(j);
			val = et.getText().toString();
			sPeditor.putString("Cval_" + WidgetId + "_" + j, val);
			sPeditor.putInt("CRval_" + WidgetId + "_" + val, j);
			Log.d(tag, "Stored Cval_" + WidgetId + "_" + j + "=" + val);
		}

		sPeditor.putBoolean("ConfigDone", true);
		sPeditor.commit();
		Log.i(tag, "WidgetConfig Done Set & Values Commited");
	}

	private void InitialUpdate() {
		Log.i(tag, "InitialUpdate Invoked");

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetConfig.class.getName());
		Intent firstUpdate = new Intent(context, WidgetProvider.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		context.sendBroadcast(firstUpdate);
		Log.i(tag, "BroadCast Sent");

		Intent oneI = new Intent(context, UpdateService.class);
//		oneI.setAction(WidgetProvider.WIDGET_ACTION_UPDATE);
		oneI.putExtra(WidgetProvider.INTENT_ACTION, WidgetProvider.WIDGET_ACTION_UPDATE);
		oneI.putExtra(WidgetProvider.WIDGET_ID, WidgetId);
		context.startService(oneI);
		Log.i(tag, "UpdateService Started");
		
		Intent oneFO = new Intent(context,FOService.class);
		oneFO.putExtra(WidgetProvider.WIDGET_ID, WidgetId);
		context.startService(oneFO);
		Log.i(tag, "FO Service Started");
		
	}

	public void browse(View view) {
		Log.i(tag, "browse Invoked");

		FileChooserDialog dialog = new FileChooserDialog(this);
		dialog.loadFolder("/");
		dialog.show();
		Log.i(tag, "Dialog Showed");

		dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
			public void onFileSelected(Dialog source, File file) {
				String filepath;
				EditText et = (EditText) findViewById(R.id.FilePath);

				source.hide();
				Toast toast = Toast.makeText(source.getContext(), "File selected: " + file.getName(), Toast.LENGTH_LONG);
				toast.show();

				filepath = file.getAbsolutePath();
				et.setText(filepath);
				Log.d(tag, "File path Set : " + filepath);
			}

			@Override
			public void onFileSelected(Dialog source, File folder, String name) {
				// TODO Auto-generated method stub

			}
		});
	}

}
