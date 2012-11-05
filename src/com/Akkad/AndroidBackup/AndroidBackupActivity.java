package com.Akkad.AndroidBackup;

import java.io.File;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.stericson.RootTools.RootTools;

public class AndroidBackupActivity extends SherlockFragmentActivity {
	ViewPager tabViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		RootTools.debugMode = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tabViewPager = (ViewPager) findViewById(R.id.pager);

		/** Defining a listener for pageChange */
		ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		};

		/** Setting the pageChange listener to the viewPager */
		tabViewPager.setOnPageChangeListener(pageChangeListener);
		boolean deviceSupported = RootTools.isRootAvailable() && RootTools.isAccessGiven() && RootTools.isBusyboxAvailable() && InformationFragment.hasExternalStorage();
		/** Creating an instance of FragmentPagerAdapter */
		TabsPagerAdapter fragmentPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), deviceSupported);

		/** Setting the FragmentPagerAdapter object to the viewPager object */
		tabViewPager.setAdapter(fragmentPagerAdapter);

		getSupportActionBar().setDisplayShowTitleEnabled(true);

		/** Defining tab listener */
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				tabViewPager.setCurrentItem(tab.getPosition());

			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			}
		};

		// Tab for Device Information
		Tab informationTab = getSupportActionBar().newTab();
		informationTab.setText(getResources().getString(R.string.information_tab_title));
		informationTab.setTabListener(tabListener);

		// Tab for Tasks
		Tab tasksTab = getSupportActionBar().newTab();
		tasksTab.setText(getResources().getString(R.string.tasks_tab_title));
		tasksTab.setTabListener(tabListener);

		// Tab for Applications
		Tab applicationsTab = getSupportActionBar().newTab();
		applicationsTab.setText(getResources().getString(R.string.applications_tab_title));
		applicationsTab.setTabListener(tabListener);

		// Tab for Schedule
		Tab scheduleTab = getSupportActionBar().newTab();
		scheduleTab.setText(getResources().getString(R.string.schedule_tab_title));
		scheduleTab.setTabListener(tabListener);

		getSupportActionBar().addTab(informationTab); // Add Information tab

		// Checks for root and if so adds all other tabs to the actionbar
		if (deviceSupported) {
			getSupportActionBar().addTab(tasksTab); // Add Tasks tab
			getSupportActionBar().addTab(applicationsTab); // Add Applications tab
			getSupportActionBar().addTab(scheduleTab); // Add Schedule tab
		}

		// If a backup folder doesn't exist then create it
		File backupFolder = new File(BackupStore.getBackupFolderLocation());
		if (!backupFolder.isDirectory()) {
			backupFolder = new File(BackupStore.getBackupFolderLocation());
			backupFolder.mkdirs();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
}