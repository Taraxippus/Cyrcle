package com.taraxippus.cyrcle;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PreferenceCircles extends PreferenceFragment
{
	public PreferenceCircles()
	{
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_circles);

		addBackButton();

		chooseValue("count", "Amount", "", 1, 1000, 1, 10);
		chooseMinMax("size", 0, 1, 100, 0.25F, 0.75F);
		chooseValue("blurStrength", "blur strength", "", 0.005F, 0.25F, 1000, 0.1F);
		chooseValue("blurPercentage", "blur percentage", "%", 0, 100, 1, 45);
		chooseValue("ringPercentage", "ring percentage", "%", 0, 100, 1, 45);
		chooseValue("ringWidth", "ring width", "", 0.05F, 0.75F, 200, 0.1F);
		chooseColor("colorCircle1", "#ffff00");
		chooseColor("colorCircle2", "#ffcc00");
		chooseMinMax("alpha", 0, 1, 100, 0.25F, 0.75F);
		chooseValue("textureQuality", "quality", "", 0.25F, 2F, 4, 1);
		chooseValue("groupSize", "group size", "", 1F, 10F, 1, 1);
		chooseValue("groupPercentage", "percentage", "%", 1F, 100F, 1, 20);
		chooseMinMax("groupSizeFactor", 0, 2, 100, 0.5F, 0.75F);
		chooseMinMax("groupOffset", 0, 2, 100, 0.75F, 1.25F);
	}
}
