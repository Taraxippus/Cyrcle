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
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch (requestCode)
		{
			case 0:		
			case 1:
				if (resultCode != Activity.RESULT_OK)
					break;

				Uri content_describer = data.getData();

				InputStream in = null;
				OutputStream out = null; 

				try 
				{
					in = getActivity().getContentResolver().openInputStream(content_describer);
					out = new FileOutputStream(getActivity().getFilesDir().getPath() + (requestCode == 0 ? "circleTextureFile" : "ringTextureFile"));

					byte[] buffer = new byte[1024];
					int len;
					while ((len = in.read(buffer)) != -1) 
					{
						out.write(buffer, 0, len);
					}

					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(requestCode == 0 ? "circleTextureFile" : "ringTextureFile", content_describer.getPath()).commit();
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				} 
				finally 
				{
					try
					{
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}

				return;
			default:
				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_circles);

		findPreference("back").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().popBackStack();
					return true;
				}
			});

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

		findPreference("pickCircleTexture").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.setType("image/*");

					startActivityForResult(i, 0);
					return true;
				}
			});

		findPreference("pickRingTexture").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.setType("image/*");

					startActivityForResult(i, 1);
					return true;
				}
			});
	}
}
