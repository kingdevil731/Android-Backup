package com.Akkad.AndroidBackup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class InformationActivity extends Activity {

	private static final String TAG = "Information Activity";
	private static boolean rooted;
	private boolean busybox;
	final Context context = this;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.information_layout);
		TextView lblroot = (TextView) findViewById(R.id.rootView);
		if (RootTools.isRootAvailable()) {
			rooted = RootTools.isAccessGiven();
			lblroot.setText(getResources().getString(R.string.information_tab_RootAccess) + " " + getResources().getString(R.string.yes));
		} else {
			rooted = false;
			Log.d(TAG, "Not Rooted");
			lblroot.setText(getResources().getString(R.string.information_tab_RootAccess) + " " + getResources().getString(R.string.no));
		}
		TextView lblBusybox = (TextView) findViewById(R.id.busyboxView);
		if (RootTools.isBusyboxAvailable() && rooted) {
			busybox = true;
			lblBusybox.setText(getResources().getString(R.string.information_tab_BusyBox) + " " + getResources().getString(R.string.yes) + " " + RootTools.getBusyBoxVersion());
			Log.d(TAG, "BusyBox available");
		} else if ((RootTools.isRootAvailable() && RootTools.isAccessGiven()) && !RootTools.isBusyboxAvailable()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(getString(R.string.information_tab_message_BusyBox_Not_Found)).setMessage(getString(R.string.information_tab_offerBusyBox));
			builder.setPositiveButton(getResources().getString(R.string.yes), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Rooted but BusyBox is not available, offering busybox");
					offerBusyBox();
				}
			});
			builder.setNegativeButton(getResources().getString(R.string.no), null);
			builder.show();
		} else {
			Log.d(TAG, "BusyBox not available");
			lblBusybox.setText(getResources().getString(R.string.information_tab_BusyBox) + " " + getResources().getString(R.string.no));
			busybox = false;
		}
	}

	private void offerBusyBox() {
		RootTools.offerBusyBox(this);
	}

	/**
	 * @return the rooted
	 */
	public static boolean isRooted() {
		return rooted;
	}
}
