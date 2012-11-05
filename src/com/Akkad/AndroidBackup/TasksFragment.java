package com.Akkad.AndroidBackup;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

public class TasksFragment extends SherlockListFragment {

	Core core = new Core();
	ArrayList<String> tasks;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		tasks = new ArrayList<String>();
		tasks.add(getString(R.string.wipe_dalvik_cache));
		tasks.add(getString(R.string.reboot_Device));
		tasks.add(getString(R.string.delete_All_Backups));

		/** Creating array adapter to set data in listview */
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, tasks);

		/** Setting the array adapter to the listview */
		setListAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch (position) {
		case 0:
			AlertDialog.Builder wipeDalvikCacheWarningDialog = new AlertDialog.Builder(getActivity());

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
			Intent refresh = new Intent(getActivity(), AndroidBackupActivity.class);
			startActivity(refresh);
			getActivity().finish();
		} else {
			Toast.makeText(getActivity().getParent(), getString(R.string.delete_All_Backups_Failed_Message) + BackupStore.getBackupFolderLocation(), Toast.LENGTH_LONG).show();
		}
	}
}