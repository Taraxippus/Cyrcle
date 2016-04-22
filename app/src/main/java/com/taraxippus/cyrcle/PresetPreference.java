package com.taraxippus.cyrcle;

import android.preference.Preference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PresetPreference extends Preference
{
	RecyclerView recyclerView;
	
	public PresetPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

    public PresetPreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public PresetPreference(android.content.Context context, android.util.AttributeSet attrs) { super(context, attrs); }

    public PresetPreference(android.content.Context context) { super(context); }
	
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		recyclerView = new RecyclerView(getContext());
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new PresetAdapter());
		return recyclerView;
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
		final Button add;
		
		public AddViewHolder(View v)
		{
			super(v);

			add = (Button) v.findViewById(R.id.button_add);
			add.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			
		}
	}


	public class PresetAdapter extends RecyclerView.Adapter implements View.OnClickListener
	{
		int selected = 1;

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

				((PresetViewHolder) holder).text.setText("Preset " + position);
				((PresetViewHolder) holder).image.setImageResource(R.drawable.ic_launcher);
			}
		}

		@Override
		public int getItemCount()
		{
			return 1 + 10;
		}

		@Override
		public int getItemViewType(int position)
		{
			return position == 0 ? 1 : 0;
		}

		@Override
		public void onClick(View v)
		{
			int old = selected;
			selected = recyclerView.getChildAdapterPosition(v);

			notifyItemChanged(old);
			notifyItemChanged(selected);
		}
	}
}
