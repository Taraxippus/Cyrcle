package com.taraxippus.cyrcle;

import android.app.*;
import android.graphics.*;
import android.opengl.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.cyrcle.gl.*;
import android.widget.FrameLayout.*;

public class WallpaperPreferenceActivity extends Activity
{
	private CyrcleRenderer renderer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		final GLSurfaceView v = new GLSurfaceView(this);
		v.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
		v.setPreserveEGLContextOnPause(true);
		v.setEGLContextClientVersion(2);
		v.setEGLConfigChooser(8, 8, 8, 8, 0, 0);

		v.setRenderer(renderer = new CyrcleRenderer(this));
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		renderer.setPreview(size.y);
		
		final ViewGroup.LayoutParams small = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
		final ViewGroup.LayoutParams big = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		v.setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					if (v.getLayoutParams() == small)
						v.setLayoutParams(big);
					else
						v.setLayoutParams(small);
				}
		});
		
		((FrameLayout) findViewById(R.id.layout_preview)).addView(v, small);
	}
}
