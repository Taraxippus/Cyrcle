package com.taraxippus.cyrcle;

import android.os.Bundle;
import android.preference.Preference;

public class PreferenceBackground extends PreferenceFragment
{
	public PreferenceBackground()
	{
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_background);

		findPreference("back").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().popBackStack();
					return true;
				}
			});

		chooseColor("colorBackground1", "#ff8800");
		chooseColor("colorBackground2", "#ff4400");
		chooseValue("vignetteStrength", "strength", "", 0, 1, 100, 0.8F);
		chooseValue("vignetteRadius", "radius", "", 0, 1, 100, 0.1F);
		chooseColor("colorVignette", "#000000");
		chooseValue("vignetteBlurStrength", "strength", "", 0, 1, 100, 0.5F);
		chooseValue("vignetteBlurRadius", "radius", "", 0, 1, 100, 0.5F);
	}
}
