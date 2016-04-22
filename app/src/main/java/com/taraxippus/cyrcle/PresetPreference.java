package com.taraxippus.cyrcle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Outline;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import android.graphics.BitmapFactory;

public class PresetPreference extends Preference
{
	SharedPreferences globalPreferences;
	final ArrayList<String> presets = new ArrayList<>();
	final ArrayList<Bitmap> presets_bitmap = new ArrayList<>();
	
	RecyclerView recyclerView;
	
	public PresetPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

    public PresetPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public PresetPreference(android.content.Context context, android.util.AttributeSet attrs) { super(context, attrs); }

    public PresetPreference(android.content.Context context) { super(context); }
	
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		globalPreferences = getContext().getSharedPreferences("com.taraxippus.cyrcle.GLOBAL_PREFERENCES",  Context.MODE_PRIVATE);
		
		this.presets.clear();
		Set<String> presets = globalPreferences.getStringSet("presets", new HashSet<String>());
		for (String preset : presets)
		{
			this.presets.add(preset);
			this.presets_bitmap.add(BitmapFactory.decodeFile(getContext().getFilesDir() + "/com.taraxippus.cyrcle.presets." + preset + ".png"));
		}
		
		recyclerView = new RecyclerView(getContext());
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new PresetAdapter());
		return recyclerView;
	}
	
	public void savePresets()
	{
		Set<String> presets = new HashSet<>();
		for (String s : PresetPreference.this.presets)
			presets.add(s);

		globalPreferences.edit().putStringSet("presets", presets).apply();
	}
	
	public void copySharedPreferences(SharedPreferences from, SharedPreferences to)
	{
		SharedPreferences.Editor editor = to.edit();
		Map<String, ?> all = from.getAll();

		for (String key : all.keySet())
		{
			if (all.get(key) instanceof String)
				editor.putString(key, (String) all.get(key));

			else if (all.get(key) instanceof Boolean)
				editor.putBoolean(key, (Boolean) all.get(key));

			else if (all.get(key) instanceof Integer)
				editor.putInt(key, (Integer) all.get(key));

			else if (all.get(key) instanceof Float)
				editor.putFloat(key, (Float) all.get(key));

			else if (all.get(key) instanceof Long)
				editor.putLong(key, (Long) all.get(key));

			else if (all.get(key) instanceof Set)
				editor.putStringSet(key, (Set<String>) all.get(key));
		}


		editor.apply();
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
			ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() 
			{
				@Override
				public void getOutline(View view, Outline outline) 
				{
					outline.setOval(0, 0, view.getWidth(), view.getHeight());
				}
			};
			add.setOutlineProvider(viewOutlineProvider);
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
						String name = input.getText().toString();
						
						if (name.isEmpty())
							name = name1;
							
						if (presets.contains(name))
						{
							int i;
							for (i = 2; presets.contains(name + i); ++i);
							
							name = name + i;
						}
						
						presets.add(name);
						recyclerView.getAdapter().notifyItemInserted(presets.size() - 1);

						copySharedPreferences(PreferenceManager.getDefaultSharedPreferences(getContext()), getContext().getSharedPreferences("com.taraxippus.cyrcle.presets." + name,  Context.MODE_PRIVATE));
						savePresets();
						
						((WallpaperPreferenceActivity) getContext()).renderer.renderToBitmap(new Runnable()
						{
								@Override
								public void run()
								{
									Bitmap bitmap = ((WallpaperPreferenceActivity) getContext()).renderer.bitmap;
									presets_bitmap.add(bitmap);
									recyclerView.getAdapter().notifyItemChanged(presets.size() - 1);
									
									try
									{
										FileOutputStream fOut = getContext().openFileOutput("/com.taraxippus.cyrcle.presets." + presets.get(presets.size() - 1) + ".png", Context.MODE_PRIVATE);
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
				if (position < presets_bitmap.size() && presets_bitmap.get(position) != null)
					((PresetViewHolder) holder).image.setImageBitmap(presets_bitmap.get(position));
				else
					((PresetViewHolder) holder).image.setImageResource(R.drawable.ic_launcher);
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
			
			copySharedPreferences(getContext().getSharedPreferences("com.taraxippus.cyrcle.presets." + presets.get(selected),  Context.MODE_PRIVATE), PreferenceManager.getDefaultSharedPreferences(getContext()));
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
								presets.remove(index);
								presets_bitmap.remove(index);
								notifyItemRemoved(index);
								
								savePresets();
								return true;
							
							case R.id.item_overwrite:
								int old = selected;
								selected = index;

								notifyItemChanged(old);
								notifyItemChanged(selected);
								copySharedPreferences(PreferenceManager.getDefaultSharedPreferences(getContext()), getContext().getSharedPreferences("com.taraxippus.cyrcle.presets." + presets.get(index),  Context.MODE_PRIVATE));
								
								((WallpaperPreferenceActivity) getContext()).renderer.renderToBitmap(new Runnable()
									{
										@Override
										public void run()
										{
											Bitmap bitmap = ((WallpaperPreferenceActivity) getContext()).renderer.bitmap;
											presets_bitmap.get(selected).recycle();
											presets_bitmap.set(selected, bitmap);
											notifyItemChanged(selected);
											
											try
											{
												FileOutputStream fOut = getContext().openFileOutput("/com.taraxippus.cyrcle.presets." + presets.get(selected) + ".png", Context.MODE_PRIVATE);
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
}
