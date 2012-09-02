package com.Akkad.AndroidBackup;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.util.Log;

/**
 * @author Raafat Akkad
 */
public class Backup {
	private static final String TAG = "Android Backup Backup";
	private String apkLocation;
	private String app_apk_md5;
	private String app_data_md5;
	private int app_install_location; // 0=Normal App installed to /data/app/ , 1=System App installed to /system/app/ , 2=Normal App installed to the SD Card
	private String app_label;
	private String app_package_name;
	private int app_target_sdk_version;
	private Calendar backupDate;
	private String dataLocation;
	private String formattedDate;

	private String informationLocation;

	public Backup() {
		this.app_target_sdk_version = -1;
		this.app_install_location = -1;
		generateAPKAndDataLocations();
	}

	/**
	 * @param backupDate
	 * @param app_label
	 * @param app_package_name
	 * @param app_target_sdk_version
	 * @param app_apk_md5
	 * @param app_data_md5
	 * @param app_install_location
	 */
	public Backup(Calendar backupDate, String app_label, String app_package_name, int app_target_sdk_version, String app_apk_md5, String app_data_md5, int app_install_location) {
		this.backupDate = backupDate;
		this.app_label = app_label;
		this.app_package_name = app_package_name;
		this.app_target_sdk_version = app_target_sdk_version;
		this.app_apk_md5 = app_apk_md5;
		this.app_data_md5 = app_data_md5;
		this.app_install_location = app_install_location;
		generateDate();
		generateAPKAndDataLocations();
	}

	private void generateAPKAndDataLocations() {
		this.apkLocation = BackupStore.getBackupFolderLocation() + app_package_name + "-" + formattedDate + ".apk";
		this.dataLocation = BackupStore.getBackupFolderLocation() + app_package_name + "-" + formattedDate + ".tar.gz";
	}

	private void generateDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
		formattedDate = df.format(backupDate.getTime()); // Date is saved so it is the same for the backed up apk, data and app information
	}

	/**
	 * @return the apkLocation
	 */
	public String getApkLocation() {
		return apkLocation;
	}

	/**
	 * @return the app_apk_md5
	 */
	public String getApp_apk_md5() {
		return app_apk_md5;
	}

	/**
	 * @return the app_data_md5
	 */
	public String getApp_data_md5() {
		return app_data_md5;
	}

	/**
	 * @return the app_install_location
	 */
	public int getApp_install_location() {
		return app_install_location;
	}

	/**
	 * @return the app_label
	 */
	public String getApp_label() {
		return app_label;
	}

	/**
	 * @return the app_package_name
	 */
	public String getApp_package_name() {
		return app_package_name;
	}

	/**
	 * @return the app_target_sdk_version
	 */
	public int getApp_target_sdk_version() {
		return app_target_sdk_version;
	}

	/**
	 * @return the backupDate
	 */
	public Calendar getBackupDate() {
		return backupDate;
	}

	/**
	 * @return the dataLocation
	 */
	public String getDataLocation() {
		return dataLocation;
	}

	/**
	 * @return the formattedDate
	 */
	public String getFormattedDate() {
		return formattedDate;
	}

	/**
	 * @return the informationLocation
	 */
	public String getInformationLocation() {
		return informationLocation;
	}

	public void loadBackupInformation(String informationFilePath) {
		File backupInformationFile = new File(informationFilePath);
		FileInputStream fileInput;
		try {
			fileInput = new FileInputStream(backupInformationFile);
		} catch (FileNotFoundException e1) {
			Log.e(TAG, "Could not read file / File doesn't exist");
			return;
		}

		ArrayList<String> inputArray = new ArrayList<String>();

		DataInputStream dataInputStream = new DataInputStream(fileInput);

		String currentLine; // temp variable to hold current line
		try {
			while ((currentLine = dataInputStream.readLine()) != null) { // read the next line, saving it to temp and then check that it is not null
				if (!currentLine.contains("#")) { // If the line is not a comment
					inputArray.add(currentLine);
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not read line");
			return;
		}

		try {
			fileInput.close();
		} catch (IOException e) {
			Log.e(TAG, "Could not close file");
		}

		SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz");

		try {
			backupDate = Calendar.getInstance();
			backupDate.setTime(dateParser.parse(inputArray.get(0).split("=")[1]));
		} catch (ParseException e) {
			Log.e(TAG, "Could not parse date");
		}

		this.app_label = inputArray.get(1).split("=")[1];
		this.app_package_name = inputArray.get(2).split("=")[1];
		this.app_target_sdk_version = Integer.parseInt(inputArray.get(3).split("=")[1]);
		this.app_apk_md5 = inputArray.get(4).split("=")[1];
		this.app_data_md5 = inputArray.get(5).split("=")[1];
		this.app_install_location = Integer.parseInt(inputArray.get(6).split("=")[1]);
		this.informationLocation = informationFilePath;
		generateDate();
		generateAPKAndDataLocations();
	}

	/**
	 * Saves information about the backup to a text file
	 */
	public void saveBackupInformation() {
		File backupInformationFile = new File(BackupStore.getBackupFolderLocation() + app_package_name + "-" + formattedDate + ".information");
		try {
			backupInformationFile.createNewFile();
		} catch (IOException e) {
			Log.e(TAG, "The Information File could not be created");
			return;
		}

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(backupInformationFile);
			BufferedWriter out = new BufferedWriter(fileWriter);

			out.write("# Android Backup\n");
			out.write("backup_time=" + backupDate.getTime().toGMTString() + '\n');
			out.write("app_label=" + app_label + '\n');
			out.write("app_package_name=" + app_package_name + '\n');
			out.write("app_target_sdk_version=" + app_target_sdk_version + '\n');
			out.write("app_apk_md5=" + app_apk_md5 + '\n');
			out.write("app_data_md5=" + app_data_md5 + '\n');
			out.write("# 0=Normal App installed to /data/app/ , 1=System App installed to /system/app/ , 2=Normal App installed to the SD Card\n");
			out.write("app_install_location=" + app_install_location);
			out.flush(); // Flushes the writer
			out.close(); // Close the file
		} catch (IOException e) {
			Log.e(TAG, "Could not write backup information to file");
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Backup [apkLocation=" + apkLocation + ", app_apk_md5=" + app_apk_md5 + ", app_data_md5=" + app_data_md5 + ", app_install_location=" + app_install_location + ", app_label=" + app_label + ", app_package_name=" + app_package_name
				+ ", app_target_sdk_version=" + app_target_sdk_version + ", backupDate=" + backupDate + ", dataLocation=" + dataLocation + ", formattedDate=" + formattedDate + "]";
	}
}