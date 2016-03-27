package com.taraxippus.cyrcle;

import android.app.*;
import android.graphics.*;
import android.opengl.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.cyrcle.gl.*;

public class WallpaperPreferenceActivity extends Activity
{
	private CyrcleRenderer renderer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		GLSurfaceView v = new GLSurfaceView(this);
		v.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
		v.setPreserveEGLContextOnPause(true);
		v.setEGLContextClientVersion(2);
		v.setEGLConfigChooser(8, 8, 8, 8, 0, 0);

		v.setRenderer(renderer = new CyrcleRenderer(this));
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		renderer.setPreview(size.y);
		
		((FrameLayout) findViewById(R.id.layout_preview)).addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())));
	}
}
