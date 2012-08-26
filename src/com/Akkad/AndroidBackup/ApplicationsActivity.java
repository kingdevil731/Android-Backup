package com.Akkad.AndroidBackup;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ApplicationsActivity extends Activity {
	private static final String TAG = "ApplicationsActivity";
	private ListView mListAppInfo;
	private final Context context = this;
	private ApplicationInfo selectedApp;
	private Core core = new Core();

	/**
	 * Refreshes the application list
	 */
	private void refreshAppList() {
		mListAppInfo = (ListView) findViewById(R.id.lvApps); // load list application
		AppInfoAdapter adapter = new AppInfoAdapter(this, getInstalledApplication(this), getPackageManager()); // create new adapter
		mListAppInfo.setAdapter(adapter); // set adapter to list view
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applications_layout); // set layout for the main screen
		refreshAppList(); // Populates the application list

		mListAppInfo.setOnItemClickListener(new OnItemClickListener() { // implement event when an item on list view is selected
					public void onItemClick(final AdapterView parent, View view, int pos, long id) {
						// get the list adapter
						AppInfoAdapter appInfoAdapter = (AppInfoAdapter) parent.getAdapter(); // get selected item on the list
						selectedApp = (ApplicationInfo) appInfoAdapter.getItem(pos);

						// custom dialog
						final Dialog dialog = new Dialog(context);
						dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
						try {
							PackageInfo info = getPackageManager().getPackageInfo(selectedApp.packageName, 0);
							dialog.setTitle(selectedApp.loadLabel(getPackageManager()) + " " + info.versionName);
						} catch (NameNotFoundException e) {
							dialog.setTitle(selectedApp.loadLabel(getPackageManager()));
						}
						dialog.setContentView(R.layout.applications_popup);
						dialog.setFeatureDrawable(Window.FEATURE_LEFT_ICON, selectedApp.loadIcon(getPackageManager()));
						dialog.setCanceledOnTouchOutside(true);

						final Button dialogRunButton = (Button) dialog.findViewById(R.id.applicationsDialogButtonRun);
						dialogRunButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								launchApp(parent.getContext(), getPackageManager(), selectedApp.packageName); // launches the selected application
								dialog.dismiss(); // closes the dialog
							}
						});

						final Button dialogUninstallButton = (Button) dialog.findViewById(R.id.applicationsDialogButtonUninstall);
						dialogUninstallButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if (InformationActivity.isRooted()) {

									if (core.isSystemApp(selectedApp.sourceDir)) {
										final AlertDialog.Builder uninstallSystemAppWarningDialog = new AlertDialog.Builder(context);
										uninstallSystemAppWarningDialog.setTitle(getString(R.string.uninstall_system_app_warning_dialog_title));
										uninstallSystemAppWarningDialog.setMessage(selectedApp.loadLabel(getPackageManager()) + " " + getString(R.string.uninstall_system_app_warning_dialog_text));
										uninstallSystemAppWarningDialog.setIcon(android.R.drawable.ic_dialog_alert);
										uninstallSystemAppWarningDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												core.UninstallAppRoot(selectedApp.packageName, selectedApp.sourceDir);
												refreshAppList();
												dialog.dismiss();
											}
										});
										uninstallSystemAppWarningDialog.setNegativeButton(getString(R.string.no), null);
										uninstallSystemAppWarningDialog.show();

									} else {
										core.UninstallAppRoot(selectedApp.packageName, selectedApp.sourceDir);
										refreshAppList();
										dialog.dismiss();
									}
								} else {
									Uri packageURI = Uri.parse("package:" + selectedApp.packageName);
									Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
									startActivity(uninstallIntent);
									refreshAppList();
								}
							}
						});

						Button dialogWipeDataButton = (Button) dialog.findViewById(R.id.applicationsDialogButtonWipeData);
						// TODO this check is not needed
						if (InformationActivity.isRooted()) {
							dialogWipeDataButton.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									if (core.wipeAppData(selectedApp.packageName)) {
										Toast.makeText(ApplicationsActivity.this, selectedApp.loadLabel(getPackageManager()) + "'s data deleted", Toast.LENGTH_LONG).show();
									} else {
										Toast.makeText(ApplicationsActivity.this, selectedApp.loadLabel(getPackageManager()) + "'s data not deleted", Toast.LENGTH_LONG).show();
									}
								}
							});
						} else {
							dialogWipeDataButton.setEnabled(false); // disable the wipe data button
							dialogWipeDataButton = null; // Set dialogWipeDataButton to null to allow for garbage collection

							if (core.isSystemApp(selectedApp.sourceDir)) // disable uninstall button if the app is a system app and the device is not rooted
							{
								dialogUninstallButton.setEnabled(false); // disable the wipe data button
							}
						}

						Button dialogBackupButton = (Button) dialog.findViewById(R.id.applicationsDialogButtonBackup);
						dialogBackupButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								String appName = selectedApp.loadLabel(getPackageManager()).toString();

								// core.backupApplicationApk(selectedApp.loadLabel(getPackageManager()).toString(), selectedApp.packageName, selectedApp.sourceDir);
								core.backupApplication(selectedApp, getPackageManager());
								Toast.makeText(ApplicationsActivity.this, appName + " has been backed successfully", Toast.LENGTH_LONG).show();
							}
						});

						dialogRunButton.setEnabled(getPackageManager().getLaunchIntentForPackage(selectedApp.packageName) != null);
						dialogBackupButton.setEnabled((core.isSystemApp(selectedApp.sourceDir) && InformationActivity.isRooted() || !core.isSystemApp(selectedApp.sourceDir)));
						dialog.show();
					}
				});
	}

	@Override
	protected void onResume() {
		refreshAppList();
		super.onResume();
	}

	/**
	 * Get all installed application on device and return a list
	 * 
	 * @param c
	 *            Context of application
	 * 
	 * @return list of installed applications
	 */
	public static List<ApplicationInfo> getInstalledApplication(Context context) {
		PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);

		/* Loops through the app list and removes Android Backup & Android System */
		boolean androidBackupRemoved = false, androidSystemRemoved = false;
		for (int i = 0; i < apps.size(); i++) {
			if (apps.get(i).loadLabel(packageManager).equals("Android Backup")) {
				apps.remove(i);
				androidBackupRemoved = true;
				if (androidBackupRemoved && androidSystemRemoved) {
					break;
				}
			} else if ((apps.get(i).loadLabel(packageManager).equals("Android System"))) {
				apps.remove(i);
				androidSystemRemoved = true;
				if (androidBackupRemoved && androidSystemRemoved) {
					break;
				}
			}
		}
		Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(packageManager));
		return apps;
	}

	/**
	 * Launch an application
	 * 
	 * @param c
	 *            Context of application
	 * 
	 * @param pm
	 *            the related package manager of the context
	 * 
	 * @param pkgName
	 *            Name of the package to run
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
				Toast.makeText(c, R.string.toast_notification_apprun_notfound, Toast.LENGTH_LONG).show();
			}
		}
		// by default, fail to launch
		return false;
	}
}