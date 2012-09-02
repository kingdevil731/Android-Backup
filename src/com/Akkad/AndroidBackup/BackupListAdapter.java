package com.Akkad.AndroidBackup;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BackupListAdapter extends BaseAdapter {
	private static final String TAG = "B";
	private Context context;
	private Backup[] backupList;
	private Backup backup;

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
					ApplicationsActivity.displayBackupOnAppPopup(backup.getApp_package_name()); // updateBackupList
				} else {
					Log.e(TAG, "Could not delete Backup");
					Toast.makeText(context, "Could not delete Backup " + backup.getApp_label() + " " + backup.getBackupDate().getTime().toGMTString(), Toast.LENGTH_LONG).show();

				}

			}
		});

		return view; // return view
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}