package com.taraxippus.cyrcle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.preference.Preference;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;
import java.util.ArrayList;

public class PickImagePreference extends Preference
{
	public static final ArrayList<String> pickImageKeys = new ArrayList<>();
	private int requestCode;
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PickImagePreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }

    public PickImagePreference(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    public PickImagePreference(android.content.Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }

    public PickImagePreference(android.content.Context context)
	{ super(context); init(); }

	public void init()
	{
		requestCode = pickImageKeys.size();
		pickImageKeys.add(getKey());
		setWidgetLayoutResource(R.layout.delete);
	}

	@Override
	public void onClick()
	{
		super.onClick();
		
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType("image/*");
		
		((Activity) getContext()).startActivityForResult(i, requestCode);
	}
	
	@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);
		
		((ViewGroup) view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		view.findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					try
					{
						getEditor().putString(getKey(), "").apply();
						setSummary(getSummary());
						new File(getContext().getFilesDir().getPath() + getKey()).delete();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
		});
	}

	@Override
	public CharSequence getSummary()
	{
		return super.getSummary();
	}
}
