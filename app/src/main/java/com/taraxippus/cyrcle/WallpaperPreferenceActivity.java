package com.taraxippus.cyrcle;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.taraxippus.cyrcle.gl.CyrcleRenderer;
import android.graphics.drawable.ColorDrawable;

public class WallpaperPreferenceActivity extends Activity
{
	private CyrcleRenderer renderer;
	private GLSurfaceView glSurfaceView;
	private ViewGroup.LayoutParams layoutSmall, layoutBig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		getFragmentManager().beginTransaction().replace(R.id.layout_settings, new PreferenceMain()).commit();
		
		layoutSmall = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
		layoutBig = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	
		glSurfaceView = new GLSurfaceView(this);
		glSurfaceView.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
		glSurfaceView.setPreserveEGLContextOnPause(true);
		glSurfaceView.setEGLContextClientVersion(2);
		glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		
		glSurfaceView.setRenderer(renderer = new CyrcleRenderer(this));
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		renderer.setPreview(size.y);
	
		glSurfaceView.setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					if (glSurfaceView.getLayoutParams() == layoutSmall)
						glSurfaceView.setLayoutParams(layoutBig);
					else
						glSurfaceView.setLayoutParams(layoutSmall);
				}
		});
		
		((FrameLayout) findViewById(R.id.layout_preview)).addView(glSurfaceView, layoutSmall);
	}

	@Override
	public void onBackPressed()
	{
		if (glSurfaceView != null && glSurfaceView.getLayoutParams() == layoutBig)
			glSurfaceView.setLayoutParams(layoutSmall);
		
		else
			super.onBackPressed();
	}
}
