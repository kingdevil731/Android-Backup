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
	private String backupFolderLocation = "/sdcard/AndroidBackup/"; // Hardcoded until a backup folder setting is implemented

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

		if (core.isSystemApp(entry.sourceDir)) {
			tvAppName.setTextColor(Color.RED);
		} else {
			tvAppName.setTextColor(Color.WHITE);
		}

		File mfile = new File(backupFolderLocation);
		File[] list = mfile.listFiles();

		int counter = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i].getName().startsWith(entry.packageName)) {
				counter++;
			}
		}

		counter /= 3;

		if (counter > 1) {
			tvbackupAvailable.setText(counter + " Backups Available");// TODO get string from R.String
		} else if (counter == 1) {
			tvbackupAvailable.setText(counter + " Backup Available");// TODO get string from R.String
		} else {
			tvbackupAvailable.setText("Backup Not Available"); // TODO get string from R.String
		}

		// return view
		return v;
	}

}