package com.Akkad.AndroidBackup;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ApplicationsActivity extends Activity {
	private ListView mListAppInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set layout for the main screen
		setContentView(R.layout.applications_layout);

		// load list application
		mListAppInfo = (ListView) findViewById(R.id.lvApps);
		// create new adapter
		AppInfoAdapter adapter = new AppInfoAdapter(this, getInstalledApplication(this), getPackageManager());
		// set adapter to list view
		mListAppInfo.setAdapter(adapter);
		// implement event when an item on list view is selected
		mListAppInfo.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void onItemClick(AdapterView parent, View view, int pos, long id) {
				// get the list adapter
				AppInfoAdapter appInfoAdapter = (AppInfoAdapter) parent.getAdapter();
				// get selected item on the list
				ApplicationInfo appInfo = (ApplicationInfo) appInfoAdapter.getItem(pos);
				// launch the selected application
				launchApp(parent.getContext(), getPackageManager(), appInfo.packageName);
			}
		});
	}

	/**
	 * Get all installed application on mobile and return a list
	 * 
	 * @param c Context of application
	 * 
	 * @return list of installed applications
	 */
	@SuppressWarnings("rawtypes")
	public static List getInstalledApplication(Context c) {
		return c.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
	}

	/**
	 * Launch an application
	 * 
	 * @param c Context of application
	 * 
	 * @param pm the related package manager of the context
	 * 
	 * @param pkgName Name of the package to run
	 */
	public static boolean launchApp(Context c, PackageManager pm, String pkgName) {
		// query the intent for launching
		Intent intent = pm.getLaunchIntentForPackage(pkgName);
		// if intent is available
		if (intent != null) {
			try {
				// launch application
				c.startActivity(intent);
				// if succeed
				return true;

				// if fail
			} catch (ActivityNotFoundException ex) {
				// quick message notification
				Toast toast = Toast.makeText(c, "Application Not Found", Toast.LENGTH_LONG);
				// display message
				toast.show();
			}
		}
		// by default, fail to launch
		return false;
	}
}