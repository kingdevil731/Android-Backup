package com.Akkad.AndroidBackup;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BackupListAdapter extends BaseAdapter {
	private static final String TAG = "BackupListAdapter";
	private Backup backup;
	private Backup[] backupList;
	private Context context;
	private AlertDialog.Builder restoreWhatDialog;

	public BackupListAdapter(Context context, Backup[] backupList) {
		this.context = context;
		this.backupList = backupList;
	}

	@Override
	public int getCount() {
		return backupList.length;
	}

	@Override
	public Backup getItem(int position) {
		return backupList[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		backup = backupList[position]; // get the selected entry

		View view = convertView; // reference to convertView

		// inflate new layout if view is null
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.layout_backupinfo, null);
		}

		TextView appNameAndVersion = (TextView) view.findViewById(R.id.app_name_and_version);
		appNameAndVersion.setText(backup.getApp_label());
		TextView tvBackupDate = (TextView) view.findViewById(R.id.backupDate);
		tvBackupDate.setText(backup.getBackupDate().getTime().toGMTString());

		Button deleteBackup = (Button) view.findViewById(R.id.delete_backup);

		deleteBackup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Backup backup = getItem(position);
				if (new File(backup.getApkLocation()).delete() && new File(backup.getDataLocation()).delete() && new File(backup.getInformationLocation()).delete()) {
					ApplicationsFragment.displayBackupOnAppPopup(backup.getApp_package_name()); // updateBackupList
				} else {
					Log.e(TAG, "Could not delete Backup");
					Toast.makeText(context, "Could not delete Backup " + backup.getApp_label() + " " + backup.getBackupDate().getTime().toGMTString(), Toast.LENGTH_LONG).show();
				}
			}
		});

		Button restoreBackup = (Button) view.findViewById(R.id.restore_backup);
		restoreBackup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				restoreWhatDialog = new AlertDialog.Builder(context);
				final Backup backup = getItem(position);
				restoreWhatDialog.setTitle(backup.getApp_label());
				restoreWhatDialog.setPositiveButton(context.getString(R.string.applications_popup_restore_data_only), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						backup.toString();

						Core.restoreApplicationData(backup.getApp_package_name(), backup.getApp_data_md5(), backup.getDataLocation());
						// refreshAppList();
						dialog.dismiss();
					}
				});
				restoreWhatDialog.setNeutralButton(context.getString(R.string.applications_popup_restore_apk_only), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// refreshAppList();
						dialog.dismiss();
					}
				});
				restoreWhatDialog.setNegativeButton(context.getString(R.string.applications_popup_restore_data_and_apk), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// refreshAppList();
						dialog.dismiss();
					}
				});
				restoreWhatDialog.setMessage(context.getString(R.string.applications_popup_restore_what));
				restoreWhatDialog.show();
			}
		});

		deleteBackup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Backup backup = getItem(position);

				if (new File(backup.getApkLocation()).delete() && new File(backup.getDataLocation()).delete() && new File(backup.getInformationLocation()).delete()) {
					ApplicationsFragment.displayBackupOnAppPopup(backup.getApp_package_name()); // updateBackupList
				} else {
					Log.e(TAG, "Could not delete Backup");
					Toast.makeText(context, "Could not delete Backup " + backup.getApp_label() + " " + backup.getBackupDate().getTime().toGMTString(), Toast.LENGTH_LONG).show();
				}

			}
		});

		return view; // return view
	}
}