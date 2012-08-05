package com.Akkad.AndroidBackup;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

import com.stericson.RootTools.RootToolsException;

public class ApplicationsActivity extends Activity {

	private static final String TAG = "ApplicationsActivity";
	private ListView mListAppInfo;
	final Context context = this;
	ApplicationInfo selectedApp;
	Core core = new Core();

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
			public void onItemClick(final AdapterView parent, View view, int pos, long id) {
				// get the list adapter
				AppInfoAdapter appInfoAdapter = (AppInfoAdapter) parent.getAdapter(); // get selected item on the list
				selectedApp = (ApplicationInfo) appInfoAdapter.getItem(pos);

				// custom dialog
				final Dialog dialog = new Dialog(context);
				dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
				dialog.setTitle(selectedApp.loadLabel(getPackageManager()));
				dialog.setContentView(R.layout.applications_popup);

				final Button dialogRunButton = (Button) dialog.findViewById(R.id.applicationsDialogButtonRun);
				dialogRunButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						launchApp(parent.getContext(), getPackageManager(), selectedApp.packageName); // launches the selected application
						dialog.dismiss(); // closes the dialog
					}
				});

				final Button dialogUninstallButton = (Button) dialog.findViewById(R.id.applicationsDialogButtonUninstall);
				dialogUninstallButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (InformationActivity.isRooted()) {
							if (core.isSystemApp(selectedApp.sourceDir)) {

								AlertDialog.Builder uninstallSystemAppWarningDialog = new AlertDialog.Builder(context);

								uninstallSystemAppWarningDialog.setTitle(getString(R.string.uninstall_system_app_warning_dialog_title));
								uninstallSystemAppWarningDialog.setMessage(selectedApp.loadLabel(getPackageManager()) + " " + getString(R.string.uninstall_system_app_warning_dialog_text));

								uninstallSystemAppWarningDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										try {
											core.UninstallAppRoot(selectedApp.packageName, selectedApp.sourceDir);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (RootToolsException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (TimeoutException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										dialogUninstallButton.setEnabled(false);
										dialogRunButton.setEnabled(false);
									}

								});

								uninstallSystemAppWarningDialog.setNegativeButton(getString(R.string.no), null);

								uninstallSystemAppWarningDialog.show();

							} else {
								try {
									core.UninstallAppRoot(selectedApp.packageName, selectedApp.sourceDir);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (RootToolsException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (TimeoutException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								dialogUninstallButton.setEnabled(false);
								dialogRunButton.setEnabled(false);
							}

						} else {
							Uri packageURI = Uri.parse("package:" + selectedApp.packageName);
							Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
							startActivity(uninstallIntent);
						}

					}
				});

				Button dialogWipeDataButton = (Button) dialog.findViewById(R.id.applicationsDialogButtonWipeData);
				// TODO this check is not needed
				if (InformationActivity.isRooted()) {

					dialogWipeDataButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							try {
								core.wipeAppData(selectedApp.packageName);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Toast.makeText(ApplicationsActivity.this, selectedApp.packageName + "'s data not deleted", Toast.LENGTH_LONG).show();

							// Toast.makeText(ApplicationsActivity.this, selectedApp.loadLabel(getPackageManager()) + "'s data not deleted", Toast.LENGTH_LONG).show();

							// Toast.makeText(ApplicationsActivity.this, selectedApp.loadLabel(getPackageManager()) + "'s " + getString(R.string.toast_notification_appdata_deleted), Toast.LENGTH_LONG).show();
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

				dialog.show();
				dialog.setFeatureDrawable(Window.FEATURE_LEFT_ICON, selectedApp.loadIcon(getPackageManager()));

			}
		});
	}

	/**
	 * Get all installed application on mobile and return a list
	 * 
	 * @param c
	 *            Context of application
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