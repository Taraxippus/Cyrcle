package com.taraxippus.cyrcle;

import android.os.Bundle;
import android.preference.Preference;

public class PreferenceInteractivity extends PreferenceFragment
{
	public PreferenceInteractivity()
	{
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_interactivity);

		findPreference("back").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().popBackStack();
					return true;
				}
			});

		chooseValue("touchSensitivity", "sensitivity", "", 0, 1, 100, 0.5F);
		chooseValue("swipeSensitivity", "sensitivity", "", 0, 1, 100, 0.5F);
	}
}
