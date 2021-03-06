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

		addBackButton();

		chooseMinMax("spawnX", -1, 1, 100, -1, 1);
		chooseMinMax("spawnY", -1, 1, 100, -1, 1);
		chooseValue("fadeIn", "duration", " s", 0, 5, 50, 1F);
		chooseValue("fadeOut", "duration", " s", 0, 5, 50, 1F);
		
		chooseValue("repulsionStrength", "strength", "", 0, 1, 100, 0.5F);
		chooseMinMax("speed", 0, 1, 100, 0.25F, 0.75F);
		chooseValue("damping", "damping", "", 0, 1, 100, 0.05F);
		chooseMinMax("randomness", 0, 1, 100, 0.25F, 0.75F);
		chooseMinMax("directionX", -1, 1, 100, 0.25F, 0.75F);
		chooseMinMax("directionY", -1, 1, 100, 0.25F, 0.75F);

		chooseMinMax("rotationStart", -180, 180, 1, -180, 180);
		chooseMinMax("rotationSpeed", -180, 180, 1, -45, 45);
		chooseMinMax("offset", 0, 2, 200, 0F, 0.01F);
		
		chooseMinMax("lifeTime", 0, 120, 8, 30F, 60F);
		chooseMinMax("targetAlpha", 0, 1, 100, 0.0F, 0.01F);
		chooseColor("targetColor", "#000000");
		chooseMinMax("targetSize", 0, 1, 100, 0.0F, 0.01F);
	}
}
