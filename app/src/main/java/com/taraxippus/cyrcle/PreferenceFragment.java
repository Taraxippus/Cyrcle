package com.taraxippus.cyrcle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import android.animation.Keyframe;

public class PreferenceFragment extends android.preference.PreferenceFragment
{
	SharedPreferences preferences;
	
	public PreferenceFragment()
	{
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}
	
	public void addBackButton()
	{
		final Preference p = findPreference("back");
		
		if (p == null)
		{
			System.err.println("Couldn't find back button");
			return;
		}
		
		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					getFragmentManager().popBackStack();
					return true;
				}
			});
	}
	
	public void chooseColor(final String sharedPreference, final String def)
	{
		final Preference p = findPreference(sharedPreference);
		
		if (p == null)
		{
			System.err.println("Couldn't find preference: " + sharedPreference);
			return;
		}
		
		try
		{
			Color.parseColor(preferences.getString(sharedPreference, def));
		}
		catch (Exception e)
		{
			preferences.edit().putString(sharedPreference, def).apply();
		}

		int colorInt = 0xFF000000 | Color.parseColor(preferences.getString(sharedPreference, def));
		p.setIcon(getIcon(colorInt));
	
		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					int colorInt = Color.parseColor(preferences.getString(sharedPreference, def));

					final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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

									return false;
								}
								return true;
							}	
						});

					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Choose", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								int colorInt = fromRGB(red.getProgress(), green.getProgress(), blue.getProgress());
								hex.setText(Integer.toHexString(colorInt).substring(2).toUpperCase());

								preferences.edit().putString(sharedPreference, "#" + hex.getText().toString()).apply();
								p.setIcon(getIcon(0xFF000000 | colorInt));
								
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
		Drawable circle = getActivity().getResources().getDrawable(R.drawable.circle);
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
		
		if (p == null)
		{
			System.err.println("Couldn't find preference: " + key);
			return;
		}
		
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

					final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
					text_min.setText(String.format(Locale.US, "%.2f", (int) (lastMin * 100) / 100F));
					text_min.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
									try
									{
										slider_min.setProgress((int) ((Float.parseFloat(text_min.getText().toString()) - min) * scale));	
									}
									catch (Exception e)
									{
										return true;
									}

									return false;
								}
								return true;
							}	
						});
						
					final TextView text_max = (TextView) v.findViewById(R.id.text_max);
					text_max.setText(String.format(Locale.US, "%.2f", (int) (lastMax * 100) / 100F));
					text_max.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
									try
									{
										slider_max.setProgress((int) ((Float.parseFloat(text_max.getText().toString()) - min) * scale));	
									}
									catch (Exception e)
									{
										return true;
									}

									return false;
								}
								return true;
							}	
						});
						
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

								preferences.edit().putFloat(key + "Min", (float) slider_min.getProgress() / scale + min).putFloat(key + "Max", (float) slider_max.getProgress() / scale + min).apply();

								text_min.setText(String.format(Locale.US, "%.2f", (int) (preferences.getFloat(key + "Min", defMin) * 100) / 100F));

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

								preferences.edit().putFloat(key + "Min", (float) slider_min.getProgress() / scale + min).putFloat(key + "Max", (float) slider_max.getProgress() / scale + min).apply();

								text_max.setText(String.format(Locale.US, "%.2f", (int) (preferences.getFloat(key + "Max", defMax) * 100) / 100F));

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
								text_min.onEditorAction(EditorInfo.IME_ACTION_GO);
								text_max.onEditorAction(EditorInfo.IME_ACTION_GO);
								
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								preferences.edit().putFloat(key + "Min", lastMin).putFloat(key + "Max", lastMax).apply();

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
		
		if (p == null)
		{
			System.err.println("Couldn't find preference: " + key);
			return;
		}
		
		final String summary = p.getSummary().toString();

		p.setSummary(summary + "\nCurrent: "
					 + (int) (preferences.getFloat(key, def) * 100) / 100F + unit);

		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					final float last = preferences.getFloat(key, def);

					final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
					alertDialog.setTitle("Change " + name);

					final View v = getActivity().getLayoutInflater().inflate(R.layout.slider, null);
					alertDialog.setView(v);

					final SeekBar slider = (SeekBar) v.findViewById(R.id.slider);
					slider.setMax((int) ((max - min) * scale));
					slider.setProgress((int) (scale * (last - min)));

					((TextView) v.findViewById(R.id.text_unit)).setText(unit.trim());
					
					final EditText text_value = (EditText) v.findViewById(R.id.text_value);
					text_value.setText(String.format(Locale.US, "%.2f", (int) (last * 100) / 100F));
					text_value.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
									try
									{
										slider.setProgress((int) ((Float.parseFloat(text_value.getText().toString()) - min) * scale));	
									}
									catch (Exception e)
									{
										return true;
									}
									
									return false;
								}
								return true;
							}	
						});
					
					slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar p1, int p2, boolean p3)
							{
								preferences.edit().putFloat(key, (float) slider.getProgress() / scale + min).apply();

								text_value.setText(String.format(Locale.US, "%.2f", (int) (preferences.getFloat(key, def) * 100) / 100F));

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
								text_value.onEditorAction(EditorInfo.IME_ACTION_GO);
								
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								preferences.edit().putFloat(key, last).apply();

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
	
	public void chooseValues(final String key, final String title, final String titleA, final String titleB, final String titleC, final float min, final float max, final int scale, final float defA, final float defB, final float defC)
	{
		final Preference p = findPreference(key);

		if (p == null)
		{
			System.err.println("Couldn't find preference: " + key);
			return;
		}

		final String summary = p.getSummary().toString();

		p.setSummary(summary + "\nCurrent: "
					 + (int) (preferences.getFloat(key + "A", defA) * 100) / 100F
					 + " // " + (int) (preferences.getFloat(key + "B", defB) * 100) / 100F
					 + " // " + (int) (preferences.getFloat(key + "C", defC) * 100) / 100F);

		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					final float lastA = preferences.getFloat(key + "A", defA);
					final float lastB = preferences.getFloat(key + "B", defB);
					final float lastC = preferences.getFloat(key + "C", defC);
					
					final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
					alertDialog.setTitle("Change " + title);

					final View v = getActivity().getLayoutInflater().inflate(R.layout.minmax, null);
					alertDialog.setView(v);

					((TextView) v.findViewById(R.id.title_a)).setText(titleA + ":");
					((TextView) v.findViewById(R.id.title_b)).setText(titleB + ":");
					((TextView) v.findViewById(R.id.title_c)).setText(titleC + ":");
					
					final SeekBar slider_a = (SeekBar) v.findViewById(R.id.slider_a);
					slider_a.setMax((int) ((max - min) * scale));
					slider_a.setProgress((int) (scale * (lastA - min)));

					final SeekBar slider_b = (SeekBar) v.findViewById(R.id.slider_b);
					slider_b.setMax((int) ((max - min) * scale));
					slider_b.setProgress((int) (scale * (lastB - min)));

					final SeekBar slider_c = (SeekBar) v.findViewById(R.id.slider_c);
					slider_c.setMax((int) ((max - min) * scale));
					slider_c.setProgress((int) (scale * (lastC - min)));
					
					final TextView text_a = (TextView) v.findViewById(R.id.text_a);
					text_a.setText(String.format(Locale.US, "%.2f", (int) (lastA * 100) / 100F));
					
					text_a.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
									try
									{
										slider_a.setProgress((int) ((Float.parseFloat(text_a.getText().toString()) - min) * scale));	
									}
									catch (Exception e)
									{
										return true;
									}

									return false;
								}
								return true;
							}	
						});

					final TextView text_b = (TextView) v.findViewById(R.id.text_max);
					text_b.setText(String.format(Locale.US, "%.2f", (int) (lastB * 100) / 100F));
					text_b.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
									try
									{
										slider_b.setProgress((int) ((Float.parseFloat(text_b.getText().toString()) - min) * scale));	
									}
									catch (Exception e)
									{
										return true;
									}

									return false;
								}
								return true;
							}	
						});

					final TextView text_c = (TextView) v.findViewById(R.id.text_max);
					text_c.setText(String.format(Locale.US, "%.2f", (int) (lastB * 100) / 100F));
					text_c.setOnEditorActionListener(new EditText.OnEditorActionListener()
						{
							@Override
							public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
							{
								if (p2 == EditorInfo.IME_ACTION_GO)
								{
									try
									{
										slider_c.setProgress((int) ((Float.parseFloat(text_c.getText().toString()) - min) * scale));	
									}
									catch (Exception e)
									{
										return true;
									}

									return false;
								}
								return true;
							}	
						});
						
					slider_a.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar p1, int p2, boolean p3)
							{
								preferences.edit().putFloat(key + "A", (float) slider_a.getProgress() / scale + min).apply();
								text_a.setText(String.format(Locale.US, "%.2f", (int) (preferences.getFloat(key + "A", defA) * 100) / 100F));

								p.setSummary(summary + "\nCurrent: "
											 + (int) (preferences.getFloat(key + "A", defA) * 100) / 100F
											 + " // " + (int) (preferences.getFloat(key + "B", defB) * 100) / 100F																	 
											 + " // " + (int) (preferences.getFloat(key + "C", defC) * 100) / 100F);
							}

							@Override
							public void onStartTrackingTouch(SeekBar p1) {}

							@Override
							public void onStopTrackingTouch(SeekBar p1) {}
						});

					slider_b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar p1, int p2, boolean p3)
							{
								preferences.edit().putFloat(key + "B", (float) slider_b.getProgress() / scale + min).apply();
								text_b.setText(String.format(Locale.US, "%.2f", (int) (preferences.getFloat(key + "B", defB) * 100) / 100F));
								
								p.setSummary(summary + "\nCurrent: "
											 + (int) (preferences.getFloat(key + "A", defA) * 100) / 100F
											 + " // " + (int) (preferences.getFloat(key + "B", defB) * 100) / 100F																	 
											 + " // " + (int) (preferences.getFloat(key + "C", defC) * 100) / 100F);
							}

							@Override
							public void onStartTrackingTouch(SeekBar p1) {}

							@Override
							public void onStopTrackingTouch(SeekBar p1) {}
						});

					slider_c.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
						{
							@Override
							public void onProgressChanged(SeekBar p1, int p2, boolean p3)
							{
								preferences.edit().putFloat(key + "C", (float) slider_c.getProgress() / scale + min).apply();
								text_c.setText(String.format(Locale.US, "%.2f", (int) (preferences.getFloat(key + "C", defC) * 100) / 100F));

								p.setSummary(summary + "\nCurrent: "
											 + (int) (preferences.getFloat(key + "A", defA) * 100) / 100F
											 + " // " + (int) (preferences.getFloat(key + "B", defB) * 100) / 100F																	 
											 + " // " + (int) (preferences.getFloat(key + "C", defC) * 100) / 100F);
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
								text_a.onEditorAction(EditorInfo.IME_ACTION_GO);
								text_b.onEditorAction(EditorInfo.IME_ACTION_GO);
								text_c.onEditorAction(EditorInfo.IME_ACTION_GO);
								
								alertDialog.dismiss();
							}
						});
					alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								preferences.edit().putFloat(key + "A", lastA).putFloat(key + "B", lastB).apply();

								p.setSummary(summary + "\nCurrent: "
											 + (int) (lastA * 100) / 100F
											 + " // " + (int) (lastB * 100) / 100F																	 
											 + " // " + (int) (lastC * 100) / 100F);

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
								slider_a.setProgress((int) ((defA - min) * scale));
								slider_b.setProgress((int) ((defB - min) * scale));
								slider_c.setProgress((int) ((defC - min) * scale));
							}
						});

					return true;
				}
			});
	}
	
}
