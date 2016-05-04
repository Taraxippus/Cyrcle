package com.taraxippus.cyrcle;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

public class PreferenceMain extends PreferenceFragment
{
	public PreferenceMain()
	{
		super();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);

		findPreference("setWallpaper").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					if (Build.VERSION.SDK_INT >= 16)
					{
						Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
						intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(getActivity(), CyrcleWallpaperService.class));
						startActivity(intent);
					}
					else
					{
						Intent intent = new Intent();
						intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
						startActivity(intent);
					}

					return true;
				}
			});
			
		chooseValue("fps", "max FPS", " fps", 5, 60, 1, 45);
		chooseValue("ups", "Update frequency", " ups", 5, 60, 1, 45);
		
		findPreference("preferenceCircles").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().beginTransaction().replace(R.id.layout_settings, new PreferenceCircles())
						.addToBackStack(null).commit();

					return true;
				}
			});

		findPreference("preferenceAnimation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().beginTransaction().replace(R.id.layout_settings, new PreferenceAnimation())
						.addToBackStack(null).commit();

					return true;
				}
			});

		findPreference("preferenceBackground").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().beginTransaction().replace(R.id.layout_settings, new PreferenceBackground())
						.addToBackStack(null).commit();

					return true;
				}
			});

		findPreference("preferenceInteractivity").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().beginTransaction().replace(R.id.layout_settings, new PreferenceInteractivity())
						.addToBackStack(null).commit();

					return true;
				}
			});
    }
	
}
