package com.taraxippus.cyrcle;

import android.os.Bundle;
import android.preference.Preference;

public class PreferenceAnimation extends PreferenceFragment
{
	public PreferenceAnimation()
	{
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_animation);

		findPreference("back").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().popBackStack();
					return true;
				}
			});

		chooseMinMax("speed", 0, 1, 100, 0.25F, 0.75F);
		chooseMinMax("randomness", 0, 1, 100, 0.25F, 0.75F);
		chooseMinMax("directionX", -1, 1, 100, 0.25F, 0.75F);
		chooseMinMax("directionY", -1, 1, 100, 0.25F, 0.75F);

		chooseMinMax("rotationStart", -180, 180, 1, -180, 180);
		chooseMinMax("rotationSpeed", -180, 180, 1, -45, 45);

		chooseMinMax("lifeTime", 2, 120, 1, 30F, 60F);
		chooseMinMax("targetAlpha", 0, 1, 100, 0.0F, 0.01F);
		chooseColor("targetColor", "#000000");
		chooseMinMax("targetSize", 0, 1, 100, 0.0F, 0.01F);
	}
}
