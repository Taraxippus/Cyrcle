package com.taraxippus.cyrcle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.os.Build;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import android.widget.Toast;

public class PresetPreference extends Preference
{
	SharedPreferences globalPreferences;
	final ArrayList<String> presets = new ArrayList<>();
	
	RecyclerView recyclerView;
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PresetPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

    public PresetPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public PresetPreference(android.content.Context context, android.util.AttributeSet attrs) { super(context, attrs); }

    public PresetPreference(android.content.Context context) { super(context); }
	
	@SuppressLint("MissingSuperCall")
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		globalPreferences = getContext().getSharedPreferences(getContext().getPackageName() + "_global",  Context.MODE_PRIVATE);
	
		this.presets.clear();
		
		String[] presets = globalPreferences.getString("presets", "").split("/");

		for (String preset : presets)
		{
			if (preset.isEmpty())
				continue;

			this.presets.add(preset);
			
			if (((WallpaperPreferenceActivity) getContext()).presetCache.get(preset) == null)
			{
				Bitmap bitmap = BitmapFactory.decodeFile(getContext().getFilesDir() + "/" + preset + ".png");
				if (bitmap != null)
					((WallpaperPreferenceActivity) getContext()).presetCache.put(preset, bitmap);	
			}
		}
		
		recyclerView = new RecyclerView(getContext());
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new PresetAdapter());
		recyclerView.setBackgroundColor(0x30000000);
	
		FrameLayout layout = new FrameLayout(getContext());
		View separator = new View(getContext());
		layout.addView(separator);
		TypedValue value = new TypedValue();
		getContext().getTheme().resolveAttribute(android.R.attr.listDivider, value, true);
		separator.setBackgroundResource(value.resourceId);
		((FrameLayout.LayoutParams) separator.getLayoutParams()).height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics());
		((FrameLayout.LayoutParams) separator.getLayoutParams()).topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());

		layout.addView(recyclerView);
		((FrameLayout.LayoutParams) recyclerView.getLayoutParams()).topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9, getContext().getResources().getDisplayMetrics());

		return layout;
	}

	public void savePresets()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < presets.size(); i++)
		{
			if (i > 0)
				sb.append("/");
				
			sb.append(presets.get(i));
		}
		globalPreferences.edit().putString("presets", sb.toString()).apply();
	}
	
	public void copy(File src, File dst)
	{
		if (src == null || !src.exists())
			return;
		
		try
		{
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) 
				out.write(buf, 0, len);
			
			in.close();
			out.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public class PresetViewHolder extends RecyclerView.ViewHolder
	{
		final TextView text;
		final ImageView image;

		public PresetViewHolder(View v)
		{
			super(v);

			text = (TextView) v.findViewById(R.id.text_preset);
			image = (ImageView) v.findViewById(R.id.image_preset);
		}
	}
	
	public class AddViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		final ImageView add;
		
		public AddViewHolder(View v)
		{
			super(v);

			add = (ImageView) v.findViewById(R.id.button_add);
			add.setOnClickListener(this);

			if (Build.VERSION.SDK_INT >= 21)
			{
				ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider()
				{
					@Override
					public void getOutline(View view, Outline outline)
					{
						if (Build.VERSION.SDK_INT >= 21)
							outline.setOval(0, 0, view.getWidth(), view.getHeight());
					}
				};
				add.setOutlineProvider(viewOutlineProvider);
			}
		}

		@Override
		public void onClick(View v)
		{
			final String name1 = "Preset " + (recyclerView.getChildAdapterPosition((View) v.getParent()) + 1);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle("Save as preset");

			final EditText input = new EditText(getContext());
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			input.setText(name1);
			
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
			{ 
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						String name = input.getText().toString().replace('/', ' ');
						
						if (name.isEmpty())
							name = name1;
							
						if (presets.contains(name))
						{
							int i;
							for (i = 2; presets.contains(name + i); ++i);
							
							name = name + i;
						}
						
						presets.add(name);
		
						final int selected = presets.size() - 1;
						recyclerView.getAdapter().notifyItemInserted(presets.size() - 1);

						copySharedPreferences(getContext(), null, "preferences");
						WallpaperPreferenceActivity.zipFromData(getContext(), getContext().getFilesDir() + "/" + presets.get(selected) + ".preset", "./shared_prefs/preferences.xml", "circleTextureFile", "ringTextureFile", "backgroundTextureFile");
						//new File(getContext().getFilesDir().getParent() + "/shared_prefs/preferences.xml").delete();
						
						savePresets();
						
						((WallpaperPreferenceActivity) getContext()).renderer.renderToBitmap(new Runnable()
							{
								@Override
								public void run()
								{
									Bitmap bitmap = ((WallpaperPreferenceActivity) getContext()).renderer.bitmap;
									
									if (((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(selected)) != null)
										((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(selected)).recycle();
														
									((WallpaperPreferenceActivity) getContext()).presetCache.put(presets.get(selected), bitmap);
									recyclerView.getAdapter().notifyItemChanged(selected);

									try
									{
										FileOutputStream fOut = getContext().openFileOutput(presets.get(selected) + ".png", Context.MODE_PRIVATE);
										bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
										fOut.flush();
										fOut.close();
									}
									catch (Exception e) 
									{
										e.printStackTrace();
									}
								}
							});
					}
				});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						dialog.cancel();
					}
				});

			AlertDialog dialog = builder.create();
			float dpi = getContext().getResources().getDisplayMetrics().density;
			dialog.setView(input, (int)(19 * dpi), (int)(5 * dpi), (int)(14 * dpi), (int)(5 * dpi));
			
			dialog.show();
		}
	}


	public class PresetAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener
	{
		int selected = -1;

		public PresetAdapter()
		{

		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup group, int viewType)
		{
			RecyclerView.ViewHolder holder;

			if (viewType == 0)
			{
				View v = LayoutInflater.from(getContext()).inflate(R.layout.preset, group, false);
				v.setOnClickListener(this);
				v.setOnLongClickListener(this);

				holder = new PresetViewHolder(v);
			}
			else 
				holder = new AddViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.preset_add, group, false));


			return holder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
		{
			if (getItemViewType(position) == 0)
			{
				if (selected == position)
					holder.itemView.setBackgroundColor(0xFF424242);

				else
				{
					TypedValue value = new TypedValue();
					getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
					holder.itemView.setBackgroundResource(value.resourceId);
				}

				((PresetViewHolder) holder).text.setText(presets.get(position));
				if (((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(position)) != null)
				{
					((PresetViewHolder) holder).image.setImageBitmap(((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(position)));
					((PresetViewHolder) holder).image.setBackgroundColor(0xFF000000);
				}
				else
				{
					((PresetViewHolder) holder).image.setImageResource(R.drawable.launcher);
					((PresetViewHolder) holder).image.setBackgroundColor(0x00000000);
				}
			}
		}

		@Override
		public int getItemCount()
		{
			return 1 + presets.size();
		}

		@Override
		public int getItemViewType(int position)
		{
			return position == getItemCount() - 1 ? 1 : 0;
		}

		@Override
		public void onClick(View v)
		{
			int old = selected;
			selected = recyclerView.getChildAdapterPosition(v);

			notifyItemChanged(old);
			notifyItemChanged(selected);
			
			File f;
			
			f = new File(getContext().getFilesDir() + "/circleTextureFile");
			if (f.exists())
				f.delete();
				
			f = new File(getContext().getFilesDir() + "/ringTextureFile");
			if (f.exists())
				f.delete();
				
			f = new File(getContext().getFilesDir() + "/backgroundTextureFile");
			if (f.exists())
				f.delete();
				
			WallpaperPreferenceActivity.unZipToData(getContext(), getContext().getFilesDir() + "/" + presets.get(selected) + ".preset");
			copySharedPreferences(getContext(), "preferences", null);
			//new File(getContext().getFilesDir().getParent() + "/shared_prefs/preferences.xml").delete();
		}

		@Override
		public boolean onLongClick(View v)
		{
			final int index = recyclerView.getChildAdapterPosition(v);
			
			PopupMenu popup = new PopupMenu(getContext(), v);
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						switch (item.getItemId())
						{
							case R.id.item_delete:
								if (((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(index)) != null)
									((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(index)).recycle();

								((WallpaperPreferenceActivity) getContext()).presetCache.remove(presets.get(index));
								
								new File(getContext().getFilesDir() + "/" + presets.get(index) + ".png").delete();
								new File(getContext().getFilesDir() + "/" + presets.get(index) + ".preset").delete();

								presets.remove(index);
								notifyItemRemoved(index);
								
								savePresets();
								return true;
							
							case R.id.item_overwrite:
								int old = selected;
								selected = index;

								notifyItemChanged(old);
								notifyItemChanged(selected);
								copySharedPreferences(getContext(), null, "preferences");
								WallpaperPreferenceActivity.zipFromData(getContext(), getContext().getFilesDir() + "/" + presets.get(index) + ".preset", "./shared_prefs/preferences.xml", "circleTextureFile", "ringTextureFile", "backgroundTextureFile");
								//new File(getContext().getFilesDir().getParent() + "/shared_prefs/preferences.xml").delete();
								((WallpaperPreferenceActivity) getContext()).renderer.renderToBitmap(new Runnable()
									{
										@Override
										public void run()
										{
											Bitmap bitmap = ((WallpaperPreferenceActivity) getContext()).renderer.bitmap;
											if (((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(index)) != null)
												((WallpaperPreferenceActivity) getContext()).presetCache.get(presets.get(index)).recycle();

											((WallpaperPreferenceActivity) getContext()).presetCache.put(presets.get(index), bitmap);
											
											notifyItemChanged(selected);
											
											try
											{
												FileOutputStream fOut = getContext().openFileOutput(presets.get(selected) + ".png", Context.MODE_PRIVATE);
												bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
												fOut.flush();
												fOut.close();
											}
											catch (Exception e) 
											{
												e.printStackTrace();
											}
										}
									});
									
								return true;
								
							case R.id.item_export:
								File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
								dir.mkdirs();
								
								copy(new File(getContext().getFilesDir().getAbsolutePath() + "/" + presets.get(index) + ".preset"), new File(dir + "/" + presets.get(index) + ".preset"));
								
								Toast.makeText(getContext(), "Exported preset to\nsdcard/Documents/" + presets.get(index) + ".preset", Toast.LENGTH_SHORT).show();
								return true;
								
							default:
								return false;
						}
					}
				});

			popup.getMenuInflater().inflate(R.menu.preset, popup.getMenu());
			popup.show();
			
			return true;
		}
	}
	
	public static void copySharedPreferences(Context context, String s1, String s2)
	{
		SharedPreferences p1 = s1 == null ? PreferenceManager.getDefaultSharedPreferences(context) : context.getSharedPreferences(s1, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		SharedPreferences p2 = s2 == null ? PreferenceManager.getDefaultSharedPreferences(context) : context.getSharedPreferences(s2, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		
		SharedPreferences.Editor e = p2.edit();
		e.clear();
		
		for (Map.Entry<String, ?> entry : p1.getAll().entrySet())
		{
			if (entry.getValue() instanceof Float)
				e.putFloat(entry.getKey(), (Float) entry.getValue());
				
			else if (entry.getValue() instanceof Integer)
				e.putInt(entry.getKey(), (Integer) entry.getValue());
			
			else if (entry.getValue() instanceof Long)
				e.putLong(entry.getKey(), (Long) entry.getValue());
			
			else if (entry.getValue() instanceof String)
				e.putString(entry.getKey(), (String) entry.getValue());
			
			else if (entry.getValue() instanceof Boolean)
				e.putBoolean(entry.getKey(), (Boolean) entry.getValue());
			
			else if (entry.getValue() instanceof Set)
				e.putStringSet(entry.getKey(), (Set<String>) entry.getValue());
		}
		
		e.putLong("refresh", System.currentTimeMillis());
		e.apply();
	}
}
