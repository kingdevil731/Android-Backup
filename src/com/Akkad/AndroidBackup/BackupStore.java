package com.Akkad.AndroidBackup;

import java.io.File;
import java.io.FilenameFilter;

public class BackupStore {
	private static String backupFolderLocation = "/sdcard/AndroidBackup/"; // Hardcoded until a backup folder setting is implemented
	private static File mfile = new File(backupFolderLocation);

	public static String getBackupFolderLocation() {
		return backupFolderLocation;
	}

	public static void setBackupFolderLocation(String backupFolderLocation) {
		BackupStore.backupFolderLocation = backupFolderLocation;
	}

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

	public static File[] getBackups() {
		return mfile.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".information");
			}
		});
	}

	public static Backup[] getBackupInformation() {
		File loadBackupInformation[] = getBackups();
		Backup backups[] = new Backup[loadBackupInformation.length];
		for (int i = 0; i < backups.length; i++) {
			Backup temp = new Backup();
			temp.loadBackupInformation(loadBackupInformation[i].getPath());
			backups[i] = temp;
		}
		return backups;
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
}