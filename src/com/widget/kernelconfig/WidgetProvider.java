package com.widget.kernelconfig;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WidgetProvider extends AppWidgetProvider{
	
	private static final String tag = "WidgetProvider";
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String WIDGET_ACTION_CLICK = "KC_WIDGET_ACTION_CLICK";
	public static final String WIDGET_ACTION_UPDATE = "KC_WIDGET_ACTION_UPDATE";
	public static final String WIDGET_ID = "KC_WIDGET_ID";
	public static final String INTENT_ACTION = "KC_INTENT_ACTION";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds){
		Log.i(tag, "OnUpdate Invoked");
	}
	
	@Override
	public void onReceive(Context context,Intent intent){
		super.onReceive(context, intent);
		Log.i(tag,"onReceive Invoked");
	}

}
