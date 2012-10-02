package com.Akkad.AndroidBackup;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TasksActivity extends ListActivity {
	Core core = new Core();

	ArrayList<String> tasks;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tasks = new ArrayList<String>();
		tasks.add(getString(R.string.wipe_dalvik_cache));
		tasks.add(getString(R.string.reboot_Device));
		tasks.add(getString(R.string.delete_All_Backups));
		setListAdapter(new ArrayAdapter<String>(TasksActivity.this, android.R.layout.simple_list_item_1, tasks));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch (position) {
		case 0:
			AlertDialog.Builder wipeDalvikCacheWarningDialog = new AlertDialog.Builder(TasksActivity.this);

			wipeDalvikCacheWarningDialog.setTitle(getString(R.string.wipe_dalvik_cache_warning_dialog_title));
			wipeDalvikCacheWarningDialog.setMessage(getString(R.string.wipe_dalvik_cache_warning_dialog_text));
			wipeDalvikCacheWarningDialog.setIcon(android.R.drawable.ic_dialog_alert);

			wipeDalvikCacheWarningDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					core.wipeDalvikCache();
				}
			});

			wipeDalvikCacheWarningDialog.setNegativeButton(getString(R.string.no), null);

			wipeDalvikCacheWarningDialog.show();
			break;
		case 1:
			core.rebootDevice();
			break;
		case 2:
			deleteAllBackups();
			break;
		}
	}

	public void deleteAllBackups() {

		File backupDirectory = new File(BackupStore.getBackupFolderLocation());
		boolean failed = false;
		String[] children = backupDirectory.list();
		for (int i = 0; i < children.length; i++) {
			if (!new File(backupDirectory, children[i]).delete()) {
				failed = true;
				break;
			}
		}
		if (!failed) { // If successful reload application
			Intent refresh = new Intent(this, AndroidBackupActivity.class);
			startActivity(refresh);
			this.finish();
		} else {
			Toast.makeText(getParent(), getString(R.string.delete_All_Backups_Failed_Message) + BackupStore.getBackupFolderLocation(), Toast.LENGTH_LONG).show();
		}
	}
}