package com.Akkad.AndroidBackup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

/**
 * Class that provides the core functionality of Android Backup
 * 
 * @author Raafat Akkad (raafat DOT akkad AT gmail.com
 */
public class Core extends Activity {
	private static final String TAG = "Android Backup Core";

	public boolean isNamedProcessRunning(String processName) {
		if (processName == null) {
			return false;
		}
		ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processes = manager.getRunningAppProcesses();

		for (RunningAppProcessInfo process : processes) {
			if (processName.equals(process.processName)) {
				return true;
			}
		}
		return false;
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
		CommandCapture command = new CommandCapture(0, "su", "tar -zcvf " + BackupRetriever.getBackupFolderLocation() + packageName + "-" + formattedDate + ".tar.gz" + " /data/data/" + packageName);
		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (InterruptedException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return generateMD5Sum(BackupRetriever.getBackupFolderLocation() + packageName + "-" + formattedDate + ".tar.gz");
	}

	private String output;

	/**
	 * 
	 * @param file
	 * @return
	 */
	private String generateMD5Sum(String file) {
		CommandCapture command = new CommandCapture(0, "su", "md5 " + file) {
			public void output(int id, String line) {
				output = line;
			}
		};
		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (InterruptedException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		String[] tokens = output.split(" ");
		return tokens[0];
	}

	public String backupApplicationApk(String appName, String packageName, String apkLocation, String formattedDate) {
		CommandCapture command = new CommandCapture(0, "su", "cp " + apkLocation + " " + BackupRetriever.getBackupFolderLocation() + packageName + "-" + formattedDate + ".apk");

		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		/* Check if the MD5 of the installed apk is the same as the backed up apk */
		String installedApkMD5 = generateMD5Sum(apkLocation);
		String backedUpApkMD5 = generateMD5Sum(BackupRetriever.getBackupFolderLocation() + packageName + "-" + formattedDate + ".apk");

		if (installedApkMD5.equals(backedUpApkMD5)) {
			return installedApkMD5;
		} else {
			Toast.makeText(this, "MD5 do not match", Toast.LENGTH_LONG).show();
			return null;
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

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String formattedDate = df.format(calendar.getTime()); // Date is saved so it is the same for the backed up apk, data and app information

		String backedUpApkMD5 = backupApplicationApk("" + selectedApp.loadLabel(pm), selectedApp.packageName, selectedApp.sourceDir, formattedDate);
		String backedUpDataMD5 = backupApplicationData(selectedApp.packageName, formattedDate);

		if (backedUpApkMD5 != null && backedUpDataMD5 != null) {

			File backupInformationFile = new File(BackupRetriever.getBackupFolderLocation() + selectedApp.packageName + "-" + formattedDate + ".information");
			try {
				backupInformationFile.createNewFile();
			} catch (IOException e) {
				Log.e("Raafat", "The Information File could not be created");
				return false;
			}

			FileWriter fileWriter;
			try {
				fileWriter = new FileWriter(backupInformationFile);
				BufferedWriter out = new BufferedWriter(fileWriter);

				out.write("# Android Backup\n");
				out.write("#" + calendar.getTime().toGMTString() + '\n');
				out.write("app_label=" + selectedApp.loadLabel(pm) + '\n');
				out.write("app_package_name=" + selectedApp.packageName + '\n');
				out.write("app_target_sdk_version=" + selectedApp.targetSdkVersion + '\n');
				out.write("app_apk_md5=" + backedUpApkMD5 + '\n');
				out.write("app_data_md5=" + backedUpDataMD5 + '\n');
				out.write("# 0=Normal App installed to /data/app/ , 1=System App installed to /system/app/ , 2=Normal App installed to the SD Card\n");
				out.write("app_install_location=" + applicationsType(selectedApp.sourceDir));
				out.flush(); // Flushes the writer
				out.close(); // Close the file
			} catch (IOException e) {
				Log.e(TAG, "Could not write information file");
				return false;
			}

		}
		return false;
	}

	/**
	 * Wipes the Apps data located in /data/data/com.packageName
	 * 
	 * @param packageName
	 *            The packageName of the app that will have its data wiped
	 */
	public boolean wipeAppData(String packageName) {
		if (killApp(packageName)) {
			CommandCapture command = new CommandCapture(0, "pm disable " + packageName, "cd data/data/" + packageName, "rm -rf *", "pm enable " + packageName);
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
	 * Wipes the Dalvik Cache and reboots the device
	 * 
	 * @return if the Dalvik Cache was wiped
	 */
	public void wipeDalvikCache() {
		CommandCapture command = new CommandCapture(0, "cd /data/dalvik-cache", "rm *", "reboot");
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
	 * 
	 * @param packageName
	 *            package name of the app to be killed
	 * @return if the app was killed
	 */
	public boolean killApp(String packageName) {
		CommandCapture command = new CommandCapture(0, "kill `busybox pidof " + packageName + "`" + packageName);
		try {
			RootTools.getShell(true).add(command).waitForFinish();
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	/**
	 * Mounts the System partition as Read/Write
	 */
	private boolean mountSystemasRW() {
		return RootTools.remount("/system", "rw");
	}

	/**
	 * Mounts the System partition as Read-Only
	 */
	private boolean mountSystemasRO() throws IOException, RootToolsException, TimeoutException {
		return RootTools.remount("/system", "ro");
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
}