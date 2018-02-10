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

		chooseValue("tapDuration", "duration", "s", 0, 2, 100, 0.25F);
		chooseValue("touchSensitivity", "sensitivity", "", 0, 1, 100, 0.5F);
		chooseValue("swipeSensitivity", "sensitivity", "", 0, 1, 100, 0.5F);
	
		chooseValues("gravityX", "Gravity X", "X", "Y", "Z", -3, 3, 600, 1, 0, 0);
		chooseValues("gravityY", "Gravity Y", "X", "Y", "Z", -3, 3, 600, 0, 1, 0);
	}
}
