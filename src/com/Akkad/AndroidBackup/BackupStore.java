package com.Akkad.AndroidBackup;

import java.io.File;
import java.io.FilenameFilter;

import android.os.Environment;

public class BackupStore {
	private static String backupFolderLocation = Environment.getExternalStorageDirectory().getPath() + "/AndroidBackup/"; // TODO implement a setting to change this folder
	private static File mfile = new File(backupFolderLocation);

	public static int getBackupCount(String packageName) {
		File[] backups = getBackups();
		int backupCount = 0;
		for (int i = 0; i < backups.length; i++) {
			if (backups[i].getName().toLowerCase().contains(packageName.toLowerCase())) {
				backupCount++;
			}
		}
		return backupCount;
	}

	public static File[] getBackupFolderFiles() {
		return mfile.listFiles();
	}

	public static String getBackupFolderLocation() {
		return backupFolderLocation;
	}

	public static Backup[] getBackupInformation() {
		File loadBackupInformation[] = getBackups();
		Backup backups[] = new Backup[loadBackupInformation.length];
		for (int i = 0; i < backups.length; i++) {
			backups[i] = new Backup(loadBackupInformation[i].getPath());
		}
		return backups;
	}

	public static File[] getBackups() {
		return mfile.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".information");
			}
		});
	}

	public static Backup[] getPackageBackupInformation(String packageName) {
		Backup[] backupList = getBackupInformation();
		Backup[] filteredList = new Backup[getBackupCount(packageName)];
		int filteredCounter = 0;
		for (int i = 0; i < backupList.length; i++) {
			if (backupList[i].getApp_package_name().equals(packageName)) {
				filteredList[filteredCounter++] = backupList[i];
			}
		}
		return filteredList;
	}

	public static void setBackupFolderLocation(String backupFolderLocation) {
		BackupStore.backupFolderLocation = backupFolderLocation;
	}
}