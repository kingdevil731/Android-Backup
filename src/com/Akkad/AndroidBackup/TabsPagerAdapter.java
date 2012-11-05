package com.Akkad.AndroidBackup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	private boolean deviceSupported;

	public TabsPagerAdapter(FragmentManager supportFragmentManager, boolean deviceSupported) {
		super(supportFragmentManager);
		this.deviceSupported = deviceSupported;
	}

	@Override
	public Fragment getItem(int arg0) {
		Bundle data = new Bundle();
		if (deviceSupported) {
			switch (arg0) {
			case 0: // Information tab is selected
				InformationFragment informationFragment = new InformationFragment();
				data.putInt("current_tab", arg0 + 1);
				informationFragment.setArguments(data);
				return informationFragment;
			case 1: // Tasks tab is selected
				TasksFragment tasksFragment = new TasksFragment();
				data.putInt("current_tab", arg0 + 1);
				tasksFragment.setArguments(data);
				return tasksFragment;
			case 2: // Applications tab is selected
				ApplicationsFragment applicationsFragment = new ApplicationsFragment();
				data.putInt("current_tab", arg0 + 1);
				applicationsFragment.setArguments(data);
				return applicationsFragment;
			case 3: // Schedule tab is selected
				ScheduleFragment scheduleFragment = new ScheduleFragment();
				data.putInt("current_tab", arg0 + 1);
				scheduleFragment.setArguments(data);
				return scheduleFragment;
			}
			return null;
		} else {
			switch (arg0) {
			case 0: // Information tab is selected
				InformationFragment informationFragment = new InformationFragment();
				data.putInt("current_tab", arg0 + 1);
				informationFragment.setArguments(data);
				return informationFragment;
			}
			return null;
		}

	}

	@Override
	public int getCount() {
		return deviceSupported ? 4 : 1;
	}

}
