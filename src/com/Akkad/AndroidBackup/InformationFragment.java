package com.Akkad.AndroidBackup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.stericson.RootTools.RootTools;

public class InformationFragment extends SherlockFragment {

	private static boolean rooted;
	private static final String TAG = "Information Activity";

	public static long getDataFreeSpace() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		return availableSpace;
	}

	public static long getDataSize() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
		availableSpace = (long) stat.getBlockCount() * (long) stat.getBlockSize();
		return availableSpace;
	}

	public static long getExternalStorageFreeSpace() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		return availableSpace;
	}

	public static long getExternalStorageSize() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getBlockCount() * (long) stat.getBlockSize();
		return availableSpace;
	}

	public static long getInternalStorageFreeSpace() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		return availableSpace;
	}

	public static long getInternalStorageSize() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = (long) stat.getBlockCount() * (long) stat.getBlockSize();
		return availableSpace;
	}

	public static long getSystemFreeSpace() {
		long freeSpace = -1L;
		StatFs stat = new StatFs(Environment.getRootDirectory().getPath());
		freeSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		return freeSpace;
	}

	public static long getSystemSize() {
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getRootDirectory().getPath());
		availableSpace = (long) stat.getBlockCount() * (long) stat.getBlockSize();
		return availableSpace;
	}

	public static boolean hasExternalStorage() {
		return getExternalStorageSize() > 0;
	}

	/**
	 * @return the rooted
	 */
	public static boolean isRooted() {
		return rooted;
	}

	private boolean busybox;

	private ProgressBar systemStorageBar, dataStorageBar, externalStorageBar;

	private TextView systemStorageView, dataStorageView, externalStorageView;
	View view;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		view = inflater.inflate(R.layout.information_layout, container, false);

		TextView lblBackupFolder = (TextView) view.findViewById(R.id.backupFolderView);
		lblBackupFolder.setText(getResources().getString(R.string.information_tab_backupFolder) + " " + BackupStore.getBackupFolderLocation());

		TextView lblroot = (TextView) view.findViewById(R.id.rootView);
		if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
			rooted = true;
			lblroot.setText(getResources().getString(R.string.information_tab_RootAccess) + " " + getResources().getString(R.string.yes));
		} else {
			rooted = false;
			Log.d(TAG, "Not Rooted");
			lblroot.setText(getResources().getString(R.string.information_tab_RootAccess) + " " + getResources().getString(R.string.no));
		}
		TextView lblBusybox = (TextView) view.findViewById(R.id.busyboxView);
		if (RootTools.isBusyboxAvailable() && rooted) {
			busybox = true;
			lblBusybox.setText(getResources().getString(R.string.information_tab_BusyBox) + " " + getResources().getString(R.string.yes) + " " + RootTools.getBusyBoxVersion());
			Log.d(TAG, "BusyBox available");
		} else if ((RootTools.isRootAvailable() && RootTools.isAccessGiven()) && !RootTools.isBusyboxAvailable()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.information_tab_message_BusyBox_Not_Found)).setMessage(getString(R.string.information_tab_offerBusyBox));
			builder.setPositiveButton(getResources().getString(R.string.yes), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Rooted but BusyBox is not available, offering busybox");
					RootTools.offerBusyBox(getActivity());
				}
			});
			builder.setNegativeButton(getResources().getString(R.string.no), null);
			builder.show();
		} else {
			Log.d(TAG, "BusyBox not available");
			lblBusybox.setText(getResources().getString(R.string.information_tab_BusyBox) + " " + getResources().getString(R.string.no));
			busybox = false;
		}

		systemStorageView = (TextView) view.findViewById(R.id.systemStorageView);
		systemStorageBar = (ProgressBar) view.findViewById(R.id.systemStorageProgressBar);

		dataStorageView = (TextView) view.findViewById(R.id.dataStorageView);
		dataStorageBar = (ProgressBar) view.findViewById(R.id.dataStorageProgressBar);

		externalStorageView = (TextView) view.findViewById(R.id.ExternalStorageView);
		externalStorageBar = (ProgressBar) view.findViewById(R.id.ExternalStorageProgressBar);
		updateStorageInformation();
		return view;
	}

	@Override
	public void onResume() {
		updateStorageInformation();
		super.onResume();
	}

	public void updateStorageInformation() {
		systemStorageView.setText(getSystemSize() / (1024 * 1024) + "MB (" + getSystemFreeSpace() / (1024 * 1024) + "MB free)");
		systemStorageBar.setProgress((int) Math.round(((double) (getSystemSize() - getSystemFreeSpace()) / (double) getSystemSize() * 100)));

		dataStorageView.setText(getDataSize() / (1024 * 1024) + "MB (" + getDataFreeSpace() / (1024 * 1024) + "MB free)");
		dataStorageBar.setProgress((int) Math.round(((double) (getDataSize() - getDataFreeSpace()) / (double) getDataSize() * 100)));
		if (hasExternalStorage()) {
			externalStorageView.setText(getExternalStorageSize() / (1024 * 1024) + "MB (" + getExternalStorageFreeSpace() / (1024 * 1024) + "MB free)");
			externalStorageBar.setProgress((int) Math.round(((double) (getExternalStorageSize() - getExternalStorageFreeSpace()) / (double) getExternalStorageSize() * 100)));
		} else {
			view.findViewById(R.id.ExternalStorageLabel).setEnabled(false);
			view.findViewById(R.id.ExternalStorageView).setEnabled(false);
			view.findViewById(R.id.ExternalStorageProgressBar).setEnabled(false);
		}
	}
}
