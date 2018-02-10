package com.taraxippus.cyrcle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.taraxippus.cyrcle.gl.CyrcleRenderer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import android.widget.Toast;

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
	
		glSurfaceView = new GLSurfaceView(this)
		{
			public void onPause()
			{
				super.onPause();
				
				renderer.onPause();
			}
			
			public void onResume()
			{
				super.onPause();

				renderer.onResume();
			}
		};
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
		glSurfaceView.setLayoutParams(layoutBig);
	
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
		
		//printFolder(getFilesDir().getParentFile());
	}
	
	public void printFolder(File f)
	{
		for (File f1 : f.listFiles())
		{
			System.out.println(f1);
			
			if (f1.isDirectory())
				printFolder(f1);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		glSurfaceView.setLayoutParams(layoutSmall);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.item_import_preset:
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				
				startActivityForResult(i, 0);
				return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
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
		if (requestCode == 0)
		{
			if (!data.getData().getPath().endsWith(".preset"))
				Toast.makeText(this, "Please open a preset file", 0).show();
			
			else
			{
				File f;

				f = new File(this.getFilesDir() + "/circleTextureFile");
				if (f.exists())
					f.delete();

				f = new File(this.getFilesDir() + "/ringTextureFile");
				if (f.exists())
					f.delete();

				f = new File(this.getFilesDir() + "/backgroundTextureFile");
				if (f.exists())
					f.delete();
					
				unZipToData(this, content_describer.getPath());
				PresetPreference.copySharedPreferences(this, "preferences", getPackageName() + "_preferences");
				//new File(getFilesDir().getParent() + "/shared_prefs/preferences.xml").delete();
			}
	
			return;
		}
		
		final String key = PickImagePreference.pickImageKeys.get(requestCode - 0xFF);

		InputStream in = null;
		OutputStream out = null; 

		try 
		{
			in = getContentResolver().openInputStream(content_describer);
			out = new FileOutputStream(getFilesDir().getPath() + "/" + key);

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
	
	public static final int BUFFER = 1024;
	
	public static void zipFromData(Context context, String zipFileName, String... _files)
	{
		try
		{
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFileName);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			FileInputStream fi;
			ZipEntry entry;
			int count;
			String path;
			
			for (int i = 0; i < _files.length; i++) 
			{
				if (_files[i].startsWith("."))
					path = context.getFilesDir().getParentFile().getAbsolutePath() + _files[i].substring(1);
				else
					path = context.getFilesDir().getAbsolutePath() + "/" + _files[i];
				
				if (!new File(path).exists())
					continue;
					
				fi = new FileInputStream(path);
				origin = new BufferedInputStream(fi, BUFFER);

				entry = new ZipEntry(_files[i]);
				out.putNextEntry(entry);

				while ((count = origin.read(data, 0, BUFFER)) != -1) 
					out.write(data, 0, count);
					
				origin.close();
			}

			out.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void unZipToData(Context context, String _zipFile)
	{
		try
		{
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			FileOutputStream fout;
			int c;
			
			while ((ze = zin.getNextEntry()) != null)
			{
				if (ze.isDirectory())
				{
					new File(context.getFilesDir().getAbsolutePath() + "/" + ze.getName()).mkdirs();
				} 
				else 
				{
					if (ze.getName().startsWith("."))
						fout = new FileOutputStream(context.getFilesDir().getParentFile().getAbsolutePath() + ze.getName().substring(1));
					else
						fout = new FileOutputStream(context.getFilesDir().getAbsolutePath() + "/" + ze.getName());
					
					for (c = zin.read(); c != -1; c = zin.read())
						fout.write(c);
					
					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
