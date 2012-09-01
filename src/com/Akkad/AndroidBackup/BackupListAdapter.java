package com.Akkad.AndroidBackup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BackupListAdapter extends BaseAdapter {
	private Context context;
	private Backup[] backupList;

	public BackupListAdapter(Context context, Backup[] backupList) {
		this.context = context;
		this.backupList = backupList;
	}

	@Override
	public int getCount() {
		return backupList.length;
	}

	@Override
	public Object getItem(int position) {
		return backupList[position];
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Backup backup = backupList[position]; // get the selected entry

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

		return view; // return view
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}