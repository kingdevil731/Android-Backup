package com.Akkad.AndroidBackup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

/**
 * Class that provides the core functionality of Android Backup used to backup and restore applications
 * 
 * @author Raafat Akkad
 */
public class Core extends Activity {
	private final static String DATA_DIRECTORY = "/data/data/";
	private static String output;

	private static final String TAG = "Android Backup Core";

	/**
	 * Generates a files md5 sum
	 * 
	 * @param filePath
	 *            the filepath for the file that you want to generate an md5 sum for
	 * @return the md5 sum of the file
	 */
	private static String generateMD5Sum(String filePath) {
		output = "";
		try {
			RootTools.closeAllShells();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		CommandCapture command = new CommandCapture(0, "md5sum " + filePath) {

			public void output(int id, String line) {
				if (!line.equals("")) {
					output = line;
				}
			}
		};
		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (InterruptedException e) {
			return "";
		} catch (IOException e) {
			return "";
		} catch (TimeoutException e) {
			return "";
		}

		// Check for errors
		if (output.toLowerCase().contains("could not read file") || output.toLowerCase().contains("it's a directory") || output.toLowerCase().contains("is a directory") || !output.split("  ")[1].equals(filePath) || output.equals("")) {
			Log.e(TAG, "Could not generate md5 as the file couldn't be read or it's a directory");
			return "";
		} else {
			return output.split("  ")[0]; // Returns the md5 sum
		}
	}

	/**
	 * 
	 * @param packageName
	 *            package name of the app to be killed
	 * @return if the app was killed
	 */
	public static boolean killApp(String packageName) {
		CommandCapture command = new CommandCapture(0, "kill `busybox pidof " + packageName + "`");
		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	/**
	 * Restores Application Data
	 * 
	 * @param packageName
	 *            the package name of the data backup
	 * @param md5
	 *            the checksum of the data backup
	 * @param dataBackupPath
	 *            the location of the data backup that is to be restored
	 * @return if the restore was successful
	 */
	public static boolean restoreApplicationData(String packageName, String md5, String dataBackupPath) {
		if (!md5.equals("") && md5.equals(generateMD5Sum(dataBackupPath))) { // Checks that the data backup matches its md5
			if (wipeAppData(packageName)) { // kill app and wipe data
				CommandCapture command = new CommandCapture(0, "su", "cd " + DATA_DIRECTORY, "tar -xzvf " + dataBackupPath);
				try {
					RootTools.getShell(true).add(command).waitForFinish();
				} catch (InterruptedException e) {
					Log.e(TAG, "Could not restore " + packageName + "'s Data");
					return false;
				} catch (IOException e) {
					Log.e(TAG, "Could not restore " + packageName + "'s Data");
					return false;
				} catch (TimeoutException e) {
					Log.e(TAG, "Could not restore " + packageName + "'s Data");
					return false;
				}
			}
		} else {
			Log.e(TAG, "Backup md5 does not match");
			return false; // md5 is incorrect so the data of the app will not be restored
		}
		return true; // App data has been restored
	}

	/**
	 * Wipes the Apps data located in /data/data/com.packageName
	 * 
	 * @param packageName
	 *            The packageName of the app that will have its data wiped
	 */
	public static boolean wipeAppData(String packageName) {
		if (killApp(packageName)) {
			CommandCapture command = new CommandCapture(0, "pm disable " + packageName, "rm -r " + DATA_DIRECTORY + packageName + "/*", "pm enable " + packageName);
			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * @param apkLocation
	 *            the location of the App
	 * @return if the App is a System App
	 */
	public short applicationsType(String apkLocation) {
		if (apkLocation.toLowerCase().contains("/data/app")) {
			return 0; // Normal App
		} else if (apkLocation.toLowerCase().contains("/system/app")) {
			return 1; // System App
		} else {
			return 2; // Normal App on SD Card
		}
	}

	/**
	 * Backups the application apk, data and creates an information text file which stores information about the file
	 * 
	 * @param packageName
	 * @param apkLocation
	 * @return
	 */
	public boolean backupApplication(ApplicationInfo selectedApp, PackageManager pm) {
		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String formattedDate = formatter.format(calendar.getTime()); // Date is saved so it is the same for the backed up apk, data and app information

		String backedUpApkMD5 = backupApplicationApk(selectedApp.packageName, selectedApp.sourceDir, formattedDate);
		String backedUpDataMD5 = backupApplicationData(selectedApp.packageName, formattedDate);

		if (!backedUpApkMD5.equals("") && !backedUpDataMD5.equals("")) {
			Backup backup = new Backup(calendar, "" + selectedApp.loadLabel(pm), selectedApp.packageName, selectedApp.targetSdkVersion, backedUpApkMD5, backedUpDataMD5, applicationsType(selectedApp.sourceDir));
			backup.saveBackupInformation();
			return true;
		} else {
			Toast.makeText(getParent(), "Backup Failed", Toast.LENGTH_SHORT).show();

			/* Delete the backups that were made */
			String apkBackupLocation = BackupStore.getBackupFolderLocation() + selectedApp.packageName + "-" + formattedDate + ".apk";
			String appDateBackupLocation = BackupStore.getBackupFolderLocation() + selectedApp.packageName + "-" + formattedDate + ".tar.gz";

			CommandCapture command = new CommandCapture(0, "rm " + apkBackupLocation, "rm " + appDateBackupLocation);

			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return false;
		}
	}

	public String backupApplicationApk(String packageName, String apkLocation, String formattedDate) {
		File apk = new File(apkLocation);
		if (apk.length() <= InformationActivity.getExternalStorageFreeSpace()) { // Checks that there is enough free space on the sd card
			String apkBackupLocation = BackupStore.getBackupFolderLocation() + packageName + "-" + formattedDate + ".apk";
			RootTools.copyFile(apkLocation, apkBackupLocation, false, false);

			String installedAPKMD5 = generateMD5Sum(apkLocation);
			String backupAPKMD5 = generateMD5Sum(apkBackupLocation);

			if (!backupAPKMD5.equals("") && backupAPKMD5.equals(installedAPKMD5)) {
				return backupAPKMD5;
			} else {
				Log.e(TAG, "MD5 do not match");
				return "";
			}
		} else {
			return "";
		}

	}

	/**
	 * Backs up the package names data into a tar.gz and returns its msd5 sum
	 * 
	 * @param packageName
	 *            the packageName data to be backed up
	 * @param formattedDate
	 *            the date the data was backed up
	 * @return the md5 sum of the backed up data
	 */
	public String backupApplicationData(String packageName, String formattedDate) {
		// CommandCapture command = new CommandCapture(0, "su", "tar -zcvf " + BackupStore.getBackupFolderLocation() + packageName + "-" + formattedDate + ".tar.gz" + " " + DATA_DIRECTORY + packageName);
		CommandCapture command = new CommandCapture(0, "cd " + DATA_DIRECTORY, "tar -czvf " + BackupStore.getBackupFolderLocation() + packageName + "-" + formattedDate + ".tar.gz " + packageName); // TODO is packagename needed?
		// TODO check using pwd that it's in the correct directory
		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (InterruptedException e) {
			return "";
		} catch (IOException e) {
			return "";
		} catch (TimeoutException e) {
			return "";
		}
		return generateMD5Sum(BackupStore.getBackupFolderLocation() + packageName + "-" + formattedDate + ".tar.gz");
	}

	/**
	 * Checks if the process is running
	 * 
	 * @param processName
	 *            The process that you want to check is running
	 * @return If the process is running
	 */
	public boolean isNamedProcessRunning(String processName) {
		if (processName == null) { // The processName is empty
			Log.e(TAG, "The process name doesn't exist");
			return false;
		}
		ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processes = manager.getRunningAppProcesses(); // Gets the list of running processes

		for (RunningAppProcessInfo process : processes) { // Loops through processes
			if (processName.equals(process.processName)) { // Checks if the process is part of the running process list
				Log.e(TAG, "The process is running");
				return true; // process is running
			}
		}
		Log.e(TAG, "The process is not running");
		return false; // process is not running
	}

	/**
	 * Mounts the System partition as Read-Only
	 */
	private boolean mountSystemasRO() throws IOException, RootToolsException, TimeoutException {
		return RootTools.remount("/system", "ro");
	}

	/**
	 * Mounts the System partition as Read/Write
	 */
	private boolean mountSystemasRW() {
		return RootTools.remount("/system", "rw");
	}

	/**
	 * Reboots the device
	 */
	public void rebootDevice() {
		CommandCapture command = new CommandCapture(0, "reboot");
		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Uninstalls an application using root permissions to prevent an uninstall prompt and to remove system applications
	 * 
	 * TODO clean Dalvik Cache for uninstalled application
	 * 
	 * @param packageName
	 * @param apkLocation
	 */
	public boolean UninstallAppRoot(String packageName, String apkLocation) {
		if (killApp(packageName)) {

			wipeAppData(packageName);

			if (applicationsType(apkLocation) == 1) // A System App
			{
				if (!mountSystemasRW()) { // Mounts System/ as read/write
					Log.d(TAG, "Couldn't mount /System as RW");
					return false;
				}
			}

			CommandCapture command = new CommandCapture(0, "pm disable " + packageName, "rm " + apkLocation, "pm uninstall " + packageName);

			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (InterruptedException e) {
				return false;
			} catch (IOException e) {
				return false;
			} catch (TimeoutException e) {
				return false;
			}

			if (applicationsType(apkLocation) == 1) // A System App
			{
				try {
					mountSystemasRO(); // remounts /System as read only
				} catch (Exception e) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Wipes the Dalvik Cache and reboots the device
	 * 
	 * @return if the Dalvik Cache was wiped
	 */
	public void wipeDalvikCache() {
		if (RootTools.remount("/cache", "rw")) { // checks if /cache is mounted as writable
			CommandCapture command = new CommandCapture(0, "rm -r /data/dalvik-cache/*", "rm -r /cache/dalvik-cache/*", "rm -r /cache/dc/*", "reboot");
			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				return;
			}
		}
	}
}