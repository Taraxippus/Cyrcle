package com.taraxippus.cyrcle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.taraxippus.cyrcle.gl.CyrcleRenderer;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.preference.PreferenceManager;

public class WallpaperPreferenceActivity extends Activity
{
	public CyrcleRenderer renderer;
	private GLSurfaceView glSurfaceView;
	private ViewGroup.LayoutParams layoutSmall, layoutBig;
	
	public LruCache<String, Bitmap> presetCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		getFragmentManager().beginTransaction().replace(R.id.layout_settings, new PreferenceMain()).commit();
		
		layoutSmall = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
		layoutBig = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	
		glSurfaceView = new GLSurfaceView(this);
		if (Build.VERSION.SDK_INT >= 21)
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
		
		presetCache = new LruCache<String, Bitmap>( (int) (Runtime.getRuntime().maxMemory() / 1024) / 8) 
		{
			@Override
			protected int sizeOf(String key, Bitmap bitmap) 
			{
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (resultCode != Activity.RESULT_OK)
		{
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		final Uri content_describer = data.getData();
		final String key = PickImagePreference.pickImageKeys.get(requestCode);

		System.out.println(content_describer + " " + key + " " + requestCode);

		InputStream in = null;
		OutputStream out = null; 

		try 
		{
			in = getContentResolver().openInputStream(content_describer);
			out = new FileOutputStream(getFilesDir().getPath() + key);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1) 
			{
				out.write(buffer, 0, len);
			}

			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(key, content_describer.getPath()).commit();
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
