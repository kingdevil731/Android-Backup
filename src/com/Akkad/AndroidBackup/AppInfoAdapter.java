package com.Akkad.AndroidBackup;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppInfoAdapter extends BaseAdapter {
	private Context mContext;
	private List mListAppInfo;
	private PackageManager mPackManager;
	private Core core = new Core();

	public AppInfoAdapter(Context c, List list, PackageManager pm) {
		mContext = c;
		mListAppInfo = list;
		mPackManager = pm;
	}

	@Override
	public int getCount() {
		return mListAppInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return mListAppInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// get the selected entry
		ApplicationInfo entry = (ApplicationInfo) mListAppInfo.get(position);

		// reference to convertView
		View v = convertView;

		// inflate new layout if null
		if (v == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			v = inflater.inflate(R.layout.layout_appinfo, null);
		}

		// load controls from layout resources
		ImageView ivAppIcon = (ImageView) v.findViewById(R.id.app_icon);
		TextView tvAppName = (TextView) v.findViewById(R.id.app_name);
		TextView tvbackupAvailable = (TextView) v.findViewById(R.id.tvbackupAvailable);
		// set data to display
		ivAppIcon.setImageDrawable(entry.loadIcon(mPackManager));
		try {
			PackageInfo info = mPackManager.getPackageInfo(entry.packageName, 0);
			tvAppName.setText(entry.loadLabel(mPackManager) + " " + info.versionName);
		} catch (NameNotFoundException e) {
			tvAppName.setText(entry.loadLabel(mPackManager));
		}

		switch (core.applicationsType(entry.sourceDir)) {
		case 0: // Normal app installed in /data/app
		default:
			tvAppName.setTextColor(Color.WHITE);
			break;
		case 1: // System App installed in /system/app
			tvAppName.setTextColor(Color.RED);
			break;
		case 2: // Normal App installed on the SD Card
			tvAppName.setTextColor(Color.BLUE);
			break;

		}

		int counter = BackupRetriever.getBackupCount(entry.packageName);

		if (counter > 1) {
			tvbackupAvailable.setText(counter + " " + mContext.getString(R.string.applications_list_backups_available));
		} else if (counter == 1) {
			tvbackupAvailable.setText(counter + " " + mContext.getString(R.string.applications_list_backup_available));
		} else {
			tvbackupAvailable.setText(mContext.getString(R.string.applications_list_backup_not_available));
		}

		// return view
		return v;
	}
}