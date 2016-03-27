package com.taraxippus.cyrcle;
import android.app.*;
import android.content.*;
import android.opengl.*;
import android.os.*;
import android.service.wallpaper.*;
import android.view.*;
import com.taraxippus.cyrcle.gl.*;

public class CyrcleWallpaperService extends WallpaperService
{
	@Override
	public WallpaperService.Engine onCreateEngine()
	{
		return new Engine();
	}
	
	public static SurfaceHolder holder;
	
	public class Engine extends WallpaperService.Engine
	{
		private CyrcleGLSurfaceView glSurfaceView;
		private CyrcleRenderer renderer;
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) 
		{
			holder = surfaceHolder;
			
			super.onCreate(surfaceHolder);

			this.setTouchEventsEnabled(true);

			glSurfaceView = new CyrcleGLSurfaceView(CyrcleWallpaperService.this);
			glSurfaceView.setPreserveEGLContextOnPause(true);
			glSurfaceView.setEGLContextClientVersion(2);
			glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
			
			glSurfaceView.setRenderer(renderer = new CyrcleRenderer(CyrcleWallpaperService.this));
			
			glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		}

		@Override
		public void onVisibilityChanged(boolean visible) 
		{
			super.onVisibilityChanged(visible);

			if (visible) 
				glSurfaceView.onResume();

			else 
				glSurfaceView.onPause();    
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
		{
			renderer.onOffsetsChanged(xOffset, yOffset);
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested)
		{
			if (action.equals(WallpaperManager.COMMAND_TAP))
			{
				renderer.onTap(x, y);
			}
			return super.onCommand(action, x, y, z, extras, resultRequested);
		}
		
		public class CyrcleGLSurfaceView extends GLSurfaceView
		{		
			@Override
			CyrcleGLSurfaceView(Context context)
			{
				super(context);
			}

			@Override
			public SurfaceHolder getHolder()
			{
				return Engine.this == null ? holder : Engine.this.getSurfaceHolder();
			}
		}
	}
}
