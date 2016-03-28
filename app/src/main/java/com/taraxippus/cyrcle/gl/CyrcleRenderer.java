package com.taraxippus.cyrcle.gl;
import android.content.*;
import android.graphics.*;
import android.opengl.*;
import android.preference.*;
import com.taraxippus.cyrcle.*;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import java.nio.*;
import android.os.*;
import java.util.*;

public class CyrcleRenderer implements GLSurfaceView.Renderer, SharedPreferences.OnSharedPreferenceChangeListener
{
	public final Context context;
	public final SharedPreferences preferences;
	public final Random random = new Random();
	public int width, height;
	public boolean isPreview;
	public int screenHeight;
	
	public final Shape shape_fullscreen = new Shape();
	
	public final Program program_background = new Program();
	public final Program program_circles = new Program();
	public final Program program_vignette = new Program();
	
	public final Texture texture1 = new Texture();
	public final Texture texture2 = new Texture();
	public final Texture texture3 = new Texture();
	public final Texture texture4 = new Texture();
	
	final float[] matrix_view = new float[16];
	final float[] matrix_projection = new float[16];
	
	final float[] matrix_mvp = new float[16];
	
	int circleCount = 50;
	Circle[] circles = new Circle[circleCount];
	FloatBuffer vertices_circle;
	final int[] ibo_circle = new int[1];
	
	private boolean updateColors, updateCircleShape, updateTextures, updateVignette;
	
	private long lastTime;
	private float delta;
	private float fixedDelta = 1 / 60F;
	private float accumulator;
	private float time;
	
	private int maxFPS = 45;
	private int fps;
	private long lastFPS;
	
	private final Runnable fpsRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			((WallpaperPreferenceActivity) context).getActionBar().setTitle(context.getString(R.string.app_name) + " - " + fps + " FPS");
			fps = 0;
		}
	};
	
	public CyrcleRenderer(Context context)
	{
		this.context = context;
		
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.preferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSurfaceCreated(GL10 p1, EGLConfig p2)
	{
		GLES20.glClearColor(0, 0, 0, 0);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glEnable(GLES20.GL_BLEND);

		program_background.init(context, R.raw.vertex_background, R.raw.fragment_background, "a_Position");
		program_circles.init(context, R.raw.vertex_circle, R.raw.fragment_circle, "a_Position", "a_Color", "a_UV");
		program_vignette.init(context, R.raw.vertex_background, R.raw.fragment_vignette, "a_Position");
		
		shape_fullscreen.init(GLES20.GL_TRIANGLE_STRIP, new float[] {-1, -1,  -1, 1,  1, -1,  1, 1}, 4, 2);
		
		updateCircleShape = true;
		updateColors = true;
		updateTextures = true;
		updateVignette = true;
		
		Matrix.setLookAtM(matrix_view, 0, 0, 0F, 1F, 0, 0, 0, 0, 1, 0);
		
		updateMVP();
		updateTextures();
		
		maxFPS = (int) preferences.getFloat("fps", 45);
		
		lastTime = 0;
	}
	
	public void setPreview(int realHeight)
	{
		this.screenHeight = realHeight;
		this.isPreview = true;
	}
	
	int lastWidth, lastHeight;
	
	@Override
	public void onSurfaceChanged(GL10 p1, int width, int height)
	{
		if (isPreview)
			height = screenHeight;
			
		this.width = width;
		this.height = height;
		
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float) width / height;
		Matrix.orthoM(matrix_projection, 0, -ratio, ratio, -1, 1, 0.5F, 1.5F);		
		
		updateMVP();
		
		if (lastWidth == width && lastHeight == height && preferences.getBoolean("animateChange", true) || width == lastHeight && height == lastWidth && preferences.getBoolean("animateRotate", true))
			for (int i = 0; circles != null && i < circles.length; ++i)
			{
				if (circles[i] == null)
					circles[i] = new Circle(this);

				circles[i].setTarget();
				circles[i].velX += (circles[i].targetX - circles[i].posX) * 2;
				circles[i].velY += (circles[i].targetY - circles[i].posY) * 2;
			}
		
		lastTime = 0;
		lastWidth = width;
		lastHeight = height;
	}
	
	public void updateMVP()
	{
		Matrix.multiplyMM(matrix_mvp, 0, matrix_projection, 0, matrix_view, 0);

		if (program_circles.initialized())
		{
			program_circles.use();
			GLES20.glUniformMatrix4fv(program_circles.getUniform("u_MVP"), 1, false, matrix_mvp, 0);
		}
	}

	public void updateCircleShape()
	{
		circleCount = (int) preferences.getFloat("count", 50);
		
		circles = new Circle[circleCount];

		for (int i = 0; i < circles.length; ++i)
		{
			if (circles[i] == null)
				circles[i] = new Circle(this);

			circles[i].spawn();
		}
		
		vertices_circle = ByteBuffer.allocateDirect(circleCount * 4 * 9 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		if (ibo_circle[0] != 0)
			GLES20.glDeleteBuffers(1, ibo_circle, 0);
			
		GLES20.glGenBuffers(1, ibo_circle, 0);

		ShortBuffer indices_circle = ShortBuffer.allocate(circleCount * 6);

		for (int i = 0; i < circleCount; ++i)
		{
			indices_circle.put((short) (i * 4));
			indices_circle.put((short) (i * 4 + 1));
			indices_circle.put((short) (i * 4 + 2));

			indices_circle.put((short) (i * 4 + 1));
			indices_circle.put((short) (i * 4 + 3));
			indices_circle.put((short) (i * 4 + 2));
		}

		indices_circle.position(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo_circle[0]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices_circle.capacity() * 2, indices_circle, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		updateCircleShape = false;
	}
	
	public void updateTextures()
	{
		if (program_circles.initialized() && height > 0)
		{
			int size, x, y;
			
			long time = System.currentTimeMillis();
			
			if (preferences.getBoolean("circleTexture", false))
			{
				Bitmap bitmap;
				
				if (preferences.getString("circleTextureFile", "").isEmpty())
				{
					bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
				}
				else
					try
					{
						bitmap = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + "circleTextureFile");
					}
					catch (Exception e)
					{
						bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
					}
					
				int[] colors1 = new int[bitmap.getWidth() * bitmap.getHeight()];
				int[] colors2 = new int[bitmap.getWidth() * bitmap.getHeight()];
				
				size = Math.min(bitmap.getWidth(), bitmap.getHeight());
				blur(bitmap.getWidth(), bitmap.getHeight(), (int) (size * preferences.getFloat("blurStrength", 0.1F)), colors1, colors2, bitmap);

				texture2.init(Bitmap.createBitmap(colors1, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				texture2.bind(2);
				
				texture1.init(bitmap, GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE);
				texture1.bind(1);
			}
			else
			{
				size = (int) (height * preferences.getFloat("sizeMax", 0.75F) * Circle.MAX_SIZE * (isPreview ? 0.5F : 1));
				int[] colors1 = new int[size * size];
				int[] colors2 = new int[size * size];
				
				for (x = 0; x < size; ++x)
					for (y = 0; y < size; ++y)
						colors1[x * size + y] = Math.sqrt((x - size / 2F) * (x - size / 2F) + (y - size / 2F) * (y - size / 2F)) < size / 2F * 0.8F ? 0xFF_FFFFFF : 0x00_FFFFFF;

				blur(size, size, 1, colors1, colors2, null);

				texture1.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				texture1.bind(1);
				
				blur(size, size, (int) (size * preferences.getFloat("blurStrength", 0.1F)), colors1, colors2, null);

				texture2.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				texture2.bind(2);
			}

			if (preferences.getBoolean("ringTexture", false))
			{
				Bitmap bitmap;
				
				if (preferences.getString("ringTextureFile", "").isEmpty())
				{
					bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
				}
				else
					try
					{
						bitmap = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + "ringTextureFile");
					}
					catch (Exception e)
					{
						bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
					}
				
				int[] colors1 = new int[bitmap.getWidth() * bitmap.getHeight()];
				int[] colors2 = new int[bitmap.getWidth() * bitmap.getHeight()];
				
				size = Math.min(bitmap.getWidth(), bitmap.getHeight());
				blur(bitmap.getWidth(), bitmap.getHeight(), (int) (size * preferences.getFloat("blurStrength", 0.1F)), colors1, colors2, bitmap);

				texture4.init(Bitmap.createBitmap(colors1, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				texture4.bind(4);
				
				texture3.init(bitmap, GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE);
				texture3.bind(3);
			}
			else
			{
				size = (int) (height * preferences.getFloat("sizeMax", 0.75F) * Circle.MAX_SIZE * (isPreview ? 0.5F : 1));
				int[] colors1 = new int[size * size];
				int[] colors2 = new int[size * size];
				
				float ringWidth = preferences.getFloat("ringWidth", 0.1F);
				
				float length;
				for (x = 0; x < size; ++x)
					for (y = 0; y < size; ++y)
					{
						length = (float) Math.sqrt((x - size / 2F) * (x - size / 2F) + (y - size / 2F) * (y - size / 2F));
						colors1[x * size + y] = length < size / 2F * 0.8F && length > size / 2F * 0.8F * (1 - ringWidth) ? 0xFF_FFFFFF : 0x00_FFFFFF;
					}

				blur(size, size, 1, colors1, colors2, null);

				texture3.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				texture3.bind(3);

				blur(size, size, (int) (size * preferences.getFloat("blurStrength", 0.1F)), colors1, colors2, null);
				
				texture4.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				texture4.bind(4);
			}
			
			System.out.println("Updating textures took " + (System.currentTimeMillis() - time) + "ms");
			
			program_circles.use();
			GLES20.glUniform1i(program_circles.getUniform("u_Texture1"), 1);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture2"), 2);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture3"), 3);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture4"), 4);
			
			updateTextures = false;
		}
	}
	
	public void blur(int width, int height, int blurSize, int[] colors1, int[] colors2, Bitmap bitmap)
	{
		int x, y, blur, color, r, g, b, a;
		
		if (blurSize <= 0)
			blurSize = 1;
		
		System.out.println("Start blur: " + width + " x " + height + " -- " + blurSize);
		long time = System.currentTimeMillis();
		
		for (x = 0; x < width; ++x)
			for (y = 0; y < height; ++y)
			{
				a = r = g = b = 0;

				for (blur = -blurSize; blur < blurSize; blur++)
				{
					color = (x + blur) < 0 || (x + blur) >= width ? 0x00_000000 : bitmap != null ? bitmap.getPixel(x + blur, y) : colors1[(x + blur) * height + y];
					a += Color.alpha(color);
					r += Color.red(color);
					g += Color.green(color);
					b += Color.blue(color);
				}

				colors2[x * height + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
			}

		for (x = 0; x < width; ++x)
			for (y = 0; y < height; ++y)
			{
				a = r = g = b = 0;

				for (blur = -blurSize; blur < blurSize; blur++)
				{
					color = (y + blur) < 0 || (y + blur) >= height ? 0x00_000000 : colors2[x * height + y + blur];
					a += Color.alpha(color);
					r += Color.red(color);
					g += Color.green(color);
					b += Color.blue(color);
				}

				colors1[x * height + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
			}
			
		System.out.println("Finished blur: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	public void updateCircles()
	{
		vertices_circle.position(0);
		
		for (Circle circle : circles)
			circle.buffer(vertices_circle);
	}
	
	public void updateVignette()
	{
		if (program_vignette.initialized())
		{
			program_vignette.use();
			
			uniformColor(program_vignette.getUniform("u_Color"), "colorVignette", "#000000", 1);
			GLES20.glUniform1f(program_vignette.getUniform("u_Strength"), preferences.getFloat("vignetteStrength", 0.8F));
			GLES20.glUniform1f(program_vignette.getUniform("u_Radius"), preferences.getFloat("vignetteRadius", 0.1F));
			
			updateVignette = false;
		}
	}
	
	@Override
	public void onDrawFrame(GL10 p1)
	{
		if (!program_background.initialized())
			return;
		
		if (lastTime == 0)
			lastTime = System.currentTimeMillis() - 1;
			
		delta = (System.currentTimeMillis() - lastTime) / 1000F;
		
		if (delta < 1F / maxFPS)
			try
			{
				Thread.sleep((int)(1000F / maxFPS - delta * 1000));

				delta = (System.currentTimeMillis() - lastTime) / 1000F;
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
		if (delta > 0.5F)
			delta = 0.5F;

		accumulator += delta;

		while (accumulator >= fixedDelta)
		{
			for (int i = 0; circles != null && i < circles.length; ++i)
			{
				if (circles[i] == null)
					circles[i] = new Circle(this);
				
				circles[i].update(time, fixedDelta);
			}
				
			time += fixedDelta;
			accumulator -= fixedDelta;
		}
		
		lastTime = System.currentTimeMillis();
		
		fps++;

		if (lastFPS + 1000 < System.currentTimeMillis())
		{
			if (isPreview)
				((WallpaperPreferenceActivity) context).runOnUiThread(fpsRunnable);
				
			lastFPS = System.currentTimeMillis();
		}
		
		if (updateTextures)
			updateTextures();
		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
		
		program_circles.use();
		
		if (updateCircleShape)
			updateCircleShape();
		
		updateCircles();
		
		vertices_circle.position(0);
		GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 9 * 4, vertices_circle);
		GLES20.glEnableVertexAttribArray(0);
		
		vertices_circle.position(2);
		GLES20.glVertexAttribPointer(1, 4, GLES20.GL_FLOAT, false, 9 * 4, vertices_circle);
		GLES20.glEnableVertexAttribArray(1);
		
		vertices_circle.position(6);
		GLES20.glVertexAttribPointer(2, 3, GLES20.GL_FLOAT, false, 9 * 4, vertices_circle);
		GLES20.glEnableVertexAttribArray(2);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo_circle[0]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, circleCount * 6, GLES20.GL_UNSIGNED_SHORT, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		program_background.use();

		GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_ALPHA, GLES20.GL_ONE);
		
		if (updateColors)
		{
			uniformColor(program_background.getUniform("u_Color1"), "colorBackground1", "#ff8800", 1);
			uniformColor(program_background.getUniform("u_Color2"), "colorBackground2", "#ff4400", 1);

			updateColors = false;
		}
		
		shape_fullscreen.render();
		
		if (preferences.getBoolean("vignette", false))
		{
			program_vignette.use();

			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

			if (updateVignette)
				updateVignette();

			shape_fullscreen.render();
		}
	}
	
	float lastXOffset, lastYOffset;
	
	public void onOffsetsChanged(float xOffset, float yOffset)
	{
		if (preferences.getBoolean("swipe", true))
			for (int i = 0; i < circles.length; ++i)
			{
				if (circles[i] == null)
					circles[i] = new Circle(this);

				circles[i].velX += (xOffset - lastXOffset) / circles[i].size * -0.5F * preferences.getFloat("swipeSensitivity", 0.5F) * (preferences.getBoolean("swipeInvert", false) ? -1 : 1);
				circles[i].velY += (yOffset - lastYOffset) / circles[i].size * -0.5F * preferences.getFloat("swipeSensitivity", 0.5F) * (preferences.getBoolean("swipeInvert", false) ? -1 : 1);
			}
		
		lastXOffset = xOffset;
		lastYOffset = yOffset;
	}
	
	public void onTap(float x, float y)
	{
		if (preferences.getBoolean("touch", true))
		{
			x = ((x / width) * 2 - 1) * ((float) width / height);
			y = ((1 - y / height) * 2 - 1);
			
			float distance = 0;
			float deltaX;
			float deltaY;
			
			for (int i = 0; i < circles.length; ++i)
			{
				if (circles[i] == null)
					circles[i] = new Circle(this);

				deltaX = circles[i].posX + circles[i].randomPosX - x;
				deltaY = circles[i].posY + circles[i].randomPosY - y;
				
				distance = Math.max(0.5F - (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY), 0);
					
				circles[i].velX += Math.signum(deltaX) * distance / circles[i].size * 0.1F * preferences.getFloat("touchSensitivity", 0.5F) * (preferences.getBoolean("touchInvert", false) ? -1 : 1);
				circles[i].velY += Math.signum(deltaY) * distance / circles[i].size * 0.1F * preferences.getFloat("touchSensitivity", 0.5F) * (preferences.getBoolean("touchInvert", false) ? -1 : 1);
			}
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String key)
	{
		if (key.equals("colorBackground1") || key.equals("colorBackground2"))
			updateColors = true;
			
		else if (key.equals("sizeMin") || key.equals("sizeMax")
			|| key.equals("alphaMin") || key.equals("alphaMax")
			|| key.equals("colorCircle1") || key.equals("colorCircle2")
			|| key.equals("interpolate") || key.equals("flickering") 
			|| key.equals("rings") || key.equals("ringPercentage")
		 	|| key.equals("blur") || key.equals("blurPercentage")
			|| key.equals("respawn") || key.equals("lifeTimeMin") || key.equals("lifeTimeMax")
			|| key.equals("directionXMin") || key.equals("directionXMax")
			|| key.equals("directionYMin") || key.equals("directionYMax"))
				 
			for (int i = 0; circles != null && i < circles.length; ++i)
			{
				if (circles[i] == null)
					circles[i] = new Circle(this);

				circles[i].spawn();
			}
			
		else if (key.equals("randomnessMin") || key.equals("randomnessMax")
			|| key.equals("speedMin") || key.equals("speedMax"))
		
			for (int i = 0; circles != null && i < circles.length; ++i)
			{
				if (circles[i] == null)
					circles[i] = new Circle(this);

				circles[i].setTarget();
			}
			
		else if (key.equals("blurStrength") || key.equals("ringWidth") || key.equals("sizeMax")
			|| key.equals("circleTexture") || key.equals("circleTextureFile")
			|| key.equals("ringTexture") || key.equals("ringTextureFile"))
			updateTextures = true;
			
		else if (key.equals("colorVignette") || key.equals("vignetteStrength") || key.equals("vignetteRadius"))
			updateVignette = true;
			
		else if (key.equals("count"))
			updateCircleShape = true;
			
		else if (key.equals("fps"))
			maxFPS = (int) preferences.getFloat("fps", 45);
	}
	
	public void uniformColor(int name, String key, String def, float alpha)
	{
		int color = Color.parseColor(PreferenceManager.getDefaultSharedPreferences(context).getString(key, def));
		GLES20.glUniform4f(name, Color.red(color) / 255F, Color.green(color) / 255F, Color.blue(color) / 255F, alpha);
	}
	
	public static int fromARGB(int alpha, int red, int green, int blue)
	{
		alpha = (alpha << 24) & 0xFF000000;
		red = (red << 16) & 0x00FF0000;
		green = (green << 8) & 0x0000FF00;
		blue = blue & 0x000000FF;
		return alpha | red | blue | green;
	}
	
}
