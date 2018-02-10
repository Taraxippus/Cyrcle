package com.taraxippus.cyrcle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Outline;
import android.os.Build;
import android.preference.Preference;
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
import java.util.ArrayList;

public class CircleTypePreference extends Preference
{
	RecyclerView recyclerView;
	final ArrayList<String> presets = new ArrayList<>();

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CircleTypePreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

    public CircleTypePreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public CircleTypePreference(android.content.Context context, android.util.AttributeSet attrs) { super(context, attrs); }

    public CircleTypePreference(android.content.Context context) { super(context); }

	@SuppressLint("MissingSuperCall")
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		if (presets.isEmpty())
			presets.add("Circle Type 1");
		
		recyclerView = new RecyclerView(getContext());
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new CircleTypeAdapter());
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

	public class CircleTypeViewHolder extends RecyclerView.ViewHolder
	{
		final TextView text;
		final ImageView image;

		public CircleTypeViewHolder(View v)
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
			final String name1 = "Circle Type " + (recyclerView.getChildAdapterPosition((View) v.getParent()) + 1);

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle("Add new Circle Type");

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


	public class CircleTypeAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener
	{
		public CircleTypeAdapter()
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

				holder = new CircleTypeViewHolder(v);
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
				((CircleTypeViewHolder) holder).text.setText(presets.get(position));
				((CircleTypeViewHolder) holder).image.setImageResource(R.drawable.launcher);
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
			int selected = recyclerView.getChildAdapterPosition(v);
			
			((Activity) getContext()).getFragmentManager().beginTransaction().replace(R.id.layout_settings, new PreferenceCircles()).addToBackStack(null).commit();
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
								notifyItemRemoved(index);
								return true;

							case R.id.item_copy:
								int selected = index;

								notifyItemChanged(selected);
								
								return true;

							default:
								return false;
						}
					}
				});

			popup.getMenuInflater().inflate(R.menu.circle_type, popup.getMenu());
			popup.show();

			return true;
		}
	}
}
