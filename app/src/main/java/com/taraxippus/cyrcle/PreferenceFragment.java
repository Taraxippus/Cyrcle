package com.taraxippus.cyrcle;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.io.*;

public class PreferenceFragment extends android.preference.PreferenceFragment
{
	public PreferenceFragment()
	{
		super();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);
		
		Preference setWallpaper = findPreference("setWallpaper");
		setWallpaper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					try
					{
						Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
						intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(getContext(), CyrcleWallpaperService.class));
						startActivity(intent);
					}
					catch(Exception e)
					{
						Intent intent = new Intent();
						intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
						startActivity(intent);
					}

					return true;
				}
			});

		Preference crash = findPreference("crash");
		
		if (crash != null)
			crash.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{

					throw new RuntimeException("Debug crash");
				}
			});

		Preference filePicker = findPreference("pickCircleTexture");
		filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
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
			
		Preference filePicker2 = findPreference("pickRingTexture");
		filePicker2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
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
			
		chooseValue("fps", "max FPS", " fps", 5, 60, 1, 45);

		chooseValue("count", "Amount", "", 1, 1000, 1, 10);
		chooseMinMax("size", 0, 1, 100, 0.25F, 0.75F);
		chooseValue("blurStrength", "blur strength", "", 0.005F, 0.25F, 1000, 0.1F);
		chooseValue("blurPercentage", "blur percentage", "%", 0, 100, 1, 45);
		chooseValue("ringPercentage", "ring percentage", "%", 0, 100, 1, 45);
		chooseValue("ringWidth", "ring width", "", 0.05F, 0.75F, 200, 0.1F);
		chooseColor("colorCircle1", "#ffff00");
		chooseColor("colorCircle2", "#ffcc00");
		chooseMinMax("alpha", 0, 1, 100, 0.25F, 0.75F);

		chooseColor("colorBackground1", "#ff8800");
		chooseColor("colorBackground2", "#ff4400");
		chooseValue("vignetteStrength", "strength", "", 0, 1, 100, 0.8F);
		chooseValue("vignetteRadius", "radius", "", 0, 1, 100, 0.1F);
		chooseColor("colorVignette", "#000000");
		
		chooseMinMax("lifeTime", 2, 120, 1, 30F, 60F);
		chooseMinMax("speed", 0, 1, 100, 0.25F, 0.75F);
		chooseMinMax("randomness", 0, 1, 100, 0.25F, 0.75F);
		chooseMinMax("directionX", -1, 1, 100, 0.25F, 0.75F);
		chooseMinMax("directionY", -1, 1, 100, 0.25F, 0.75F);
		
		chooseValue("touchSensitivity", "sensitivity", "", 0, 1, 100, 0.5F);
		chooseValue("swipeSensitivity", "sensitivity", "", 0, 1, 100, 0.5F);
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
	
	
	public void chooseColor(final String sharedPreference, final String def)
	{
		final Preference p = findPreference(sharedPreference);

		try
		{
			Color.parseColor(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(sharedPreference, def));
		}
		catch (Exception e)
		{
			PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(sharedPreference, def).commit();
		}

		int colorInt = 0xFF000000 | Color.parseColor(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(sharedPreference, def));
		p.setIcon(getIcon(colorInt));
		
		if (sharedPreference.equals("colorBackground1"))
			getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(colorInt));

		else if (sharedPreference.equals("colorBackground2") && Build.VERSION.SDK_INT >= 21)
			getActivity().getWindow().setStatusBarColor(colorInt);
		
		
		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					int colorInt = Color.parseColor(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(sharedPreference, def));

					final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
					alertDialog.setTitle("Choose color");
					
					final View v = getActivity().getLayoutInflater().inflate(R.layout.color, null);
					alertDialog.setView(v);
					
					final View color = v.findViewById(R.id.color);
					color.getBackground().setColorFilter(0xFF000000 | colorInt, PorterDuff.Mode.MULTIPLY);
					
					final SeekBar red = (SeekBar) v.findViewById(R.id.red);
					red.setProgress(Color.red(colorInt));
					final SeekBar green = (SeekBar) v.findViewById(R.id.green);
					green.setProgress(Color.green(colorInt));
					final SeekBar blue = (SeekBar) v.findViewById(R.id.blue);
					blue.setProgress(Color.blue(colorInt));
					final EditText hex = (EditText) v.findViewById(R.id.hex);
					hex.setText(Integer.toHexString(colorInt & 0x00_FFFFFF).toUpperCase());
					
					SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener()
					{
						@Override
						public void onProgressChanged(SeekBar p1, int p2, boolean p3)
						{
							int colorInt = fromRGB(red.getProgress(), green.getProgress(), blue.getProgress());
							color.getBackground().setColorFilter(colorInt,PorterDuff.Mode.MULTIPLY);
							hex.setText(Integer.toHexString(colorInt).substring(2).toUpperCase());
						}

						@Override
						public void onStartTrackingTouch(SeekBar p1)
						{

						}

						@Override
						public void onStopTrackingTouch(SeekBar p1)
						{

						}
					};
					red.setOnSeekBarChangeListener(listener);
					green.setOnSeekBarChangeListener(listener);
					blue.setOnSeekBarChangeListener(listener);

					hex.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
									int colorInt = p1.getText().length() == 0 ? 0 : Integer.parseInt(p1.getText().toString(), 16);
									color.getBackground().setColorFilter(0xFF000000 | colorInt, PorterDuff.Mode.MULTIPLY);
									red.setProgress(Color.red(colorInt));
									green.setProgress(Color.green(colorInt));
									blue.setProgress(Color.blue(colorInt));

									return true;
								}
								return false;
							}	
						});

					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Choose", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								int colorInt = fromRGB(red.getProgress(), green.getProgress(), blue.getProgress());
								hex.setText(Integer.toHexString(colorInt).substring(2).toUpperCase());

								PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(sharedPreference, "#" + hex.getText().toString()).commit();
								p.setIcon(getIcon(0xFF000000 | colorInt));

								if (sharedPreference.equals("colorBackground1"))
									getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(colorInt));
								
								else if (sharedPreference.equals("colorBackground2") && Build.VERSION.SDK_INT >= 21)
									getActivity().getWindow().setStatusBarColor(colorInt);
									
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								alertDialog.cancel();
							}
						});

					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Reset", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2) {}
						});

					alertDialog.show();

					alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View p1)
							{
								hex.setText(def.toUpperCase());
								int colorInt = Integer.parseInt(def.substring(1), 16);
								color.getBackground().setColorFilter(0xFF000000 | colorInt, PorterDuff.Mode.MULTIPLY);
								red.setProgress(Color.red(colorInt));
								green.setProgress(Color.green(colorInt));
								blue.setProgress(Color.blue(colorInt));
							}
						});

					return true;
				}
			});
	}

	public Drawable getIcon(int color)
	{
		Drawable circle = getActivity().getDrawable(R.drawable.circle);
		circle.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		return circle;
	}

	public static int fromRGB(int red, int green, int blue)
	{
		red = (red << 16) & 0x00FF0000;
		green = (green << 8) & 0x0000FF00;
		blue = blue & 0x000000FF;
		return 0xFF000000 | red | blue | green;
	}


	public void chooseMinMax(final String key, final float min, final float max, final int scale, final float defMin, final float defMax)
	{
		final Preference p = findPreference(key);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		final String summary = p.getSummary().toString();

		p.setSummary(summary + "\nCurrent: "
					 + (int) (preferences.getFloat(key + "Min", defMin) * 100) / 100F
					 + " - " + (int) (preferences.getFloat(key + "Max", defMax) * 100) / 100F);

		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					final float lastMin = preferences.getFloat(key + "Min", defMin);
					final float lastMax = preferences.getFloat(key + "Max", defMax);

					final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
					alertDialog.setTitle("Change min and max");

					final View v = getActivity().getLayoutInflater().inflate(R.layout.minmax, null);
					alertDialog.setView(v);

					final SeekBar slider_min = (SeekBar) v.findViewById(R.id.slider_min);
					slider_min.setMax((int) ((max - min) * scale));
					slider_min.setProgress((int) (scale * (lastMin - min)));

					final SeekBar slider_max = (SeekBar) v.findViewById(R.id.slider_max);
					slider_max.setMax((int) ((max - min) * scale));
					slider_max.setProgress((int) (scale * (lastMax - min)));

					final TextView text_min = (TextView) v.findViewById(R.id.text_min);
					text_min.setText(String.format("%.2f", (int) (lastMin * 100) / 100F));

					final TextView text_max = (TextView) v.findViewById(R.id.text_max);
					text_max.setText(String.format("%.2f", (int) (lastMax * 100) / 100F));

					slider_min.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar p1, int p2, boolean p3)
							{
								if (slider_max.getProgress() <= slider_min.getProgress())
									if (slider_max.getProgress() == slider_max.getMax())
										slider_min.setProgress(slider_min.getMax() - 1);
									else
										slider_max.setProgress(slider_min.getProgress() + 1);

								preferences.edit().putFloat(key + "Min", (float) slider_min.getProgress() / scale + min).putFloat(key + "Max", (float) slider_max.getProgress() / scale + min).commit();

								text_min.setText(String.format("%.2f", (int) (preferences.getFloat(key + "Min", defMin) * 100) / 100F));

								p.setSummary(summary + "\nCurrent: "
											 + (int) (preferences.getFloat(key + "Min", defMin) * 100) / 100F
											 + " - " + (int) (preferences.getFloat(key + "Max", defMax) * 100) / 100F																	 
											 );
							}

							@Override
							public void onStartTrackingTouch(SeekBar p1) {}

							@Override
							public void onStopTrackingTouch(SeekBar p1) {}
						});

					slider_max.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar p1, int p2, boolean p3)
							{
								if (slider_min.getProgress() >= slider_max.getProgress())
									if (slider_min.getProgress() == 0)
										slider_max.setProgress(1);
									else
										slider_min.setProgress(slider_max.getProgress() - 1);

								preferences.edit().putFloat(key + "Min", (float) slider_min.getProgress() / scale + min).putFloat(key + "Max", (float) slider_max.getProgress() / scale + min).commit();

								text_max.setText(String.format("%.2f", (int) (preferences.getFloat(key + "Max", defMax) * 100) / 100F));

								p.setSummary(summary + "\nCurrent: "
											 + (int) (preferences.getFloat(key + "Min", defMin) * 100) / 100F
											 + " - " + (int) (preferences.getFloat(key + "Max", defMax) * 100) / 100F																	 
											 );
							}

							@Override
							public void onStartTrackingTouch(SeekBar p1) {}

							@Override
							public void onStopTrackingTouch(SeekBar p1) {}
						});

					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								preferences.edit().putFloat(key + "Min", lastMin).putFloat(key + "Max", lastMax).commit();

								p.setSummary(summary + "\nCurrent: "
											 + (int) (lastMin * 100) / 100F
											 + " - " + (int) (lastMax * 100) / 100F																	 
											 );

								alertDialog.cancel();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Reset", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2) {}
						});

					alertDialog.show();

					alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View p1)
							{
								slider_min.setProgress((int) ((defMin - min) * scale));
								slider_max.setProgress((int) ((defMax - min) * scale));
							}
						});

					return true;
				}
			});
	}

	public void chooseValue(final String key, final String name, final String unit, final float min, final float max, final int scale, final float def)
	{
		final Preference p = findPreference(key);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		final String summary = p.getSummary().toString();

		p.setSummary(summary + "\nCurrent: "
					 + (int) (preferences.getFloat(key, def) * 100) / 100F + unit);

		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					final float last = preferences.getFloat(key, def);

					final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
					alertDialog.setTitle("Change " + name);

					final View v = getActivity().getLayoutInflater().inflate(R.layout.slider, null);
					alertDialog.setView(v);

					final SeekBar slider = (SeekBar) v.findViewById(R.id.slider);
					slider.setMax((int) ((max - min) * scale));
					slider.setProgress((int) (scale * (last - min)));

					final TextView text_value = (TextView) v.findViewById(R.id.text_value);
					text_value.setText(String.format("%.2f", (int) (last * 100) / 100F) + unit);

					slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar p1, int p2, boolean p3)
							{
								preferences.edit().putFloat(key, (float) slider.getProgress() / scale + min).commit();

								text_value.setText(String.format("%.2f", (int) (preferences.getFloat(key, def) * 100) / 100F) + unit);

								p.setSummary(summary + "\nCurrent: "
											 + (int) (preferences.getFloat(key, def) * 100) / 100F + unit);
							}

							@Override
							public void onStartTrackingTouch(SeekBar p1) {}

							@Override
							public void onStopTrackingTouch(SeekBar p1) {}
						});

					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								preferences.edit().putFloat(key, last).commit();

								p.setSummary(summary + "\nCurrent: "
											 + (int) (preferences.getFloat(key, def) * 100) / 100F + unit);

								alertDialog.cancel();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Reset", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2) {}
						});

					alertDialog.show();

					alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View p1)
							{
								slider.setProgress((int) ((def - min) * scale));
							}
						});

					return true;
				}
			});
	}
}
