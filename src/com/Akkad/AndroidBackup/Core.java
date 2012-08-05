package com.Akkad.AndroidBackup;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

/**
 * @author Raafat Akkad (raafat DOT akkad AT gmail.com
 */
public class Core extends Activity {

	private static final String TAG = "Android Backup Core";
	private final int timeout = 5000;

	/**
	 * Wipes the Apps data located in /data/data/com.packageName
	 * 
	 * @param packageName
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void wipeAppData(String packageName) throws InterruptedException, IOException {

		if (killApp(packageName)) {
			CommandCapture command = new CommandCapture(0, "pm disable " + packageName, "cd data/data/" + packageName, "rm -rf *", "pm enable " + packageName);
			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (Exception e) {
				return;
			}
		} else {

			return;
		}

		/*
		 * try { RootTools.sendShell("pm disable" + packageName, timeout); } catch (Exception e) { Log.d(TAG, "Error: " + packageName + "has not been disabled by pm" + ", exiting..."); return; } Log.d(TAG, packageName + "has been disabled by pm"); try { RootTools.sendShell("cd /data/data/" +
		 * packageName, timeout); RootTools.sendShell("rm *", timeout); } catch (Exception e) { Log.d(TAG, "Error: " + packageName + "data has not been deleted" + ", exiting..."); return; } Log.d(TAG, packageName + "data has been deleted"); try { RootTools.sendShell("pm enable" + packageName,
		 * timeout); } catch (Exception e) { Log.d(TAG, packageName + "has not been re-enabled by pm" + ", exiting..."); return; } Log.d(TAG, packageName + "has been re-enabled by pm"); Log.d(TAG, packageName + "has not been re-enabled by pm");
		 */
	}

	/**
	 * 
	 * @param packageName
	 */
	public void UninstallAppRoot(String packageName, String apkLocation) throws IOException, RootToolsException, TimeoutException {
		if (killApp(packageName)) {
			try {
				wipeAppData(packageName);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

			if (isSystemApp(apkLocation)) // A System App
			{
				mountSystemasRW(); // Mounts /System as read/write
			}

			CommandCapture command = new CommandCapture(0, "rm " + apkLocation, "pm uninstall " + packageName);
			try {
				RootTools.getShell(true).add(command).waitForFinish();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (isSystemApp(apkLocation))// A System App
			{
				mountSystemasRO(); // remounts /System as read only
			}

			// TODO update app list

		}

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
	private void mountSystemasRW() {
		RootTools.remount("/system", "rw");
		Log.d(TAG, "/System mounted as read only");
	}

	/**
	 * Mounts the System partition as Read-Only
	 * 
	 */
	private void mountSystemasRO() throws IOException, RootToolsException, TimeoutException {
		RootTools.remount("/system", "ro");
		Log.d(TAG, "/System mounted as read only");
	}

	/**
	 * @param apkLocation
	 *            the location of the App
	 * @return if the App is a System App
	 */
	public boolean isSystemApp(String apkLocation) {
		return apkLocation.toLowerCase().contains("/system/app");
	}

}