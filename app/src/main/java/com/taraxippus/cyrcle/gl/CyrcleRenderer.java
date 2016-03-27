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
	
	public final Shape shape_background = new Shape();
	
	public final Program program_background = new Program();
	public final Program program_circles = new Program();
	
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
	
	private boolean updateColors, updateCircleShape, updateTextures;
	
	private long lastTime;
	private float delta;
	
	private int maxFPS = 45;
	
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
		
		shape_background.init(GLES20.GL_TRIANGLE_STRIP, new float[] {-1, -1,  -1, 1,  1, -1,  1, 1}, 4, 2);
		
		updateCircleShape = true;
		updateColors = true;
		updateTextures = true;
		
		Matrix.setLookAtM(matrix_view, 0, 0, 0F, 1F, 0, 0, 0, 0, 1, 0);
		
		updateMVP();
		updateTextures();
		
		maxFPS = (int) preferences.getFloat("fps", 45);
	}
	
	public void setPreview(int realHeight)
	{
		this.screenHeight = realHeight;
		this.isPreview = true;
	}
	
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
		
		for (int i = 0; circles != null && i < circles.length; ++i)
		{
			if (circles[i] == null)
				circles[i] = new Circle(this);
				
			circles[i].spawn();
		}
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
			int size = (int) (height * preferences.getFloat("sizeMax", 0.75F) * Circle.MAX_SIZE * (isPreview ? 0.5F : 1));
			int blurSize = 1;
			float ringWidth = preferences.getFloat("ringWidth", 0.1F);
			int[] colors1 = new int[size * size];
			int[] colors2 = new int[size * size];
			
			int x, y, blur, color, r, g, b, a;
			
			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
					colors1[x * size + y] = Math.sqrt((x - size / 2F) * (x - size / 2F) + (y - size / 2F) * (y - size / 2F)) < size / 2F * 0.8F ? 0xFF_FFFFFF : 0x00_FFFFFF;

			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;

					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (x + blur) < 0 || (x + blur) >= size ? 0x00_000000 : colors1[(x + blur) * size + y];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}

					colors2[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}

			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;

					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (y + blur) < 0 || (y + blur) >= size ? 0x00_000000 : colors2[x * size + y + blur];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}

					colors1[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}
				
			texture1.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
			texture1.bind(1);
		
			blurSize = (int) (size * preferences.getFloat("blurStrength", 0.015F));
			
			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;
					
					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (x + blur) < 0 || (x + blur) >= size ? 0x00_000000 : colors1[(x + blur) * size + y];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}
					
					colors2[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}
					
			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;

					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (y + blur) < 0 || (y + blur) >= size ? 0x00_000000 : colors2[x * size + y + blur];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}

					colors1[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}
					
			texture2.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
			texture2.bind(2);

			blurSize = 1;
			
			float length;
			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					length = (float) Math.sqrt((x - size / 2F) * (x - size / 2F) + (y - size / 2F) * (y - size / 2F));
					colors1[x * size + y] = length < size / 2F * 0.8F && length > size / 2F * 0.8F * (1 - ringWidth) ? 0xFF_FFFFFF : 0x00_FFFFFF;
				}
					
			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;

					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (x + blur) < 0 || (x + blur) >= size ? 0x00_000000 : colors1[(x + blur) * size + y];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}

					colors2[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}

			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;

					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (y + blur) < 0 || (y + blur) >= size ? 0x00_000000 : colors2[x * size + y + blur];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}

					colors1[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}
			
			texture3.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
			texture3.bind(3);
			
			blurSize = (int) (size * preferences.getFloat("blurStrength", 0.015F));

			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;

					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (x + blur) < 0 || (x + blur) >= size ? 0x00_000000 : colors1[(x + blur) * size + y];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}

					colors2[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}

			for (x = 0; x < size; ++x)
				for (y = 0; y < size; ++y)
				{
					a = r = g = b = 0;

					for (blur = -blurSize; blur < blurSize; blur++)
					{
						color = (y + blur) < 0 || (y + blur) >= size ? 0x00_000000 : colors2[x * size + y + blur];
						a += Color.alpha(color);
						r += Color.red(color);
						g += Color.green(color);
						b += Color.blue(color);
					}

					colors1[x * size + y] = fromARGB(a / blurSize / 2, r / blurSize / 2, g / blurSize / 2, b / blurSize / 2);
				}

			texture4.init(Bitmap.createBitmap(colors1, size, size, Bitmap.Config.ARGB_8888), GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
			texture4.bind(4);
			
			program_circles.use();
			GLES20.glUniform1i(program_circles.getUniform("u_Texture1"), 1);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture2"), 2);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture3"), 3);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture4"), 4);
			
			updateTextures = false;
		}
	}
	
	public static int fromARGB(int alpha, int red, int green, int blue)
	{
		alpha = (alpha << 24) & 0xFF000000;
		red = (red << 16) & 0x00FF0000;
		green = (green << 8) & 0x0000FF00;
		blue = blue & 0x000000FF;
		return alpha | red | blue | green;
	}
	
	public void updateCircles()
	{
		vertices_circle.position(0);
		
		for (Circle circle : circles)
			circle.update(vertices_circle, delta);
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
			
		lastTime = System.currentTimeMillis();
		
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
		
		shape_background.render();
	}
	
	float lastXOffset, lastYOffset;
	
	public void onOffsetsChanged(float xOffset, float yOffset)
	{
		if (preferences.getBoolean("swipe", true))
			for (int i = 0; i < circles.length; ++i)
			{
				if (circles[i] == null)
					circles[i] = new Circle(this);

				circles[i].velX += (xOffset - lastXOffset) / circles[i].size * -0.025F;
				circles[i].velY += (yOffset - lastYOffset) / circles[i].size * -0.025F;
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

				deltaX = circles[i].posX - x;
				deltaY = circles[i].posY - y;
				
				distance = Math.max(0.5F - (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY), 0);
					
				circles[i].velX += Math.signum(deltaX) * distance / circles[i].size * 0.005F;
				circles[i].velY += Math.signum(deltaY) * distance / circles[i].size * 0.005F;
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
			|| key.equals("interpolate") || key.equals("blur") || key.equals("flickering") 
			|| key.equals("rings") || key.equals("ringPercentage")
		 	|| key.equals("direction") || key.equals("speed")
			)
			for (int i = 0; i < circles.length; ++i)
			{
				if (circles[i] == null)
				circles[i] = new Circle(this);

				circles[i].spawn();
			}
			
		else if (key.equals("blurStrength") || key.equals("ringWidth"))
			updateTextures = true;
			
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
}
