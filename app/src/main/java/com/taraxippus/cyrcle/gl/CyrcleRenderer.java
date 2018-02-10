package com.taraxippus.cyrcle.gl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.preference.PreferenceManager;
import com.taraxippus.cyrcle.R;
import com.taraxippus.cyrcle.WallpaperPreferenceActivity;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Random;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.hardware.SensorEvent;
import android.widget.Toast;

public class CyrcleRenderer implements GLSurfaceView.Renderer, SharedPreferences.OnSharedPreferenceChangeListener, SensorEventListener
{
	public static final int SENSOR_PERIOD = 1000;
	
	public final Context context;
	public final SharedPreferences preferences;
	public final SensorManager sensorManager;
	public final Sensor gravitySensor;
	
	public final Random random = new Random();
	public int width, height;
	public boolean isPreview;
	public int realHeight, screenHeight;
	
	public final Shape shape_fullscreen = new Shape();
	
	public final Program program_background = new Program();
	public final Program program_circles = new Program();
	public final Program program_bars = new Program();
	public final Program program_post = new Program();
	
	public final Program program_circle_texture = new Program();
	public final Program program_ring_texture = new Program();
	public final Program program_blur_vertical = new Program();
	public final Program program_blur_horizontal = new Program();
	public final Program program_texture = new Program();
	public final Program program_texture_recolor = new Program();
	public final Program program_texture_resize = new Program();
	
	public final Texture texture1 = new Texture();
	public final Texture texture4 = new Texture();
	public final Texture textureBackground = new Texture();
	
	public Framebuffer textureBuffer1 = new Framebuffer();
	public Framebuffer textureBuffer2 = new Framebuffer();
	public Framebuffer textureBuffer3 = new Framebuffer();
	public Framebuffer textureBuffer4 = new Framebuffer();
	public Framebuffer textureBuffer5 = new Framebuffer();
	public Framebuffer textureBuffer6 = new Framebuffer();
	public Framebuffer textureBufferTMP = new Framebuffer();
	public Framebuffer textureBufferBackground = new Framebuffer();
	public Framebuffer textureBufferBackground2 = new Framebuffer();
	
	final float[] matrix_model_background = new float[16];
	final float[] matrix_view = new float[16];
	final float[] matrix_projection = new float[16];
	
	final float[] matrix_mvp = new float[16];
	final float[] matrix_mvp_background = new float[16];
	
	int circleCount = 50;
	int barCount = 10;
	Circle[] circles = new Circle[circleCount];
	Bar[] bars = new Bar[circleCount];
	FloatBuffer vertices_circle, verttices_bars;
	final int[] ibo_circle = new int[1];
	final int[] ibo_bars = new int[1];
	
	private boolean updateColors, updateCircleShape, updateBarShape, updateTextures,
	updateVignette, updateVignetteBlur, updateCircleProgram, updateBitmap,
	updateBackground, updateBackgroundMatrix;

	private long lastTime;
	private float delta;
	private float fixedDelta = 1 / 45F;
	private float accumulator;
	public float time;
	
	private int maxFPS = 45;
	private int fps;
	private long lastFPS;
	
	protected boolean repulsion, repulsionWall, respawn, connected, touch, swipe, tap,
	vignette, additive,
	animateColor, animateAlpha, animateSize,
	direction, flickering, sudden, fade,
	spawnShape, animateShape, rotation, loop, backgroundTexture, backgroundTextureSwipe, backgroundTextureSwipeInverse,
	sizeEffectsTouch, sizeEffectsSwipe,
	gravity, sizeEffectsGravity;
	protected float repulsionStrength, offsetMin, offsetMax,
	groupSize, groupPercentage, 
	groupSizeFactorMin, groupSizeFactorMax, groupOffsetMin, groupOffsetMax,
	damping, fadeIn, fadeOut,
	killAnimation, touchSensitivity, swipeSensitivity,
	gravityXX, gravityXY, gravityXZ, gravityYX, gravityYY, gravityYZ;
	public int shape;
	
	protected float ratio, circleRatio = 1, ringRatio = 1, backgroundRatio = 1;	
	
	public boolean paused;
	private boolean sensorActive;
	
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
		//this.context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		this.preferences.registerOnSharedPreferenceChangeListener(this);
		
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	}
	
	@Override
	public void onSurfaceCreated(GL10 p1, EGLConfig p2)
	{
		GLES20.glClearColor(0, 0, 0, 0);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glEnable(GLES20.GL_BLEND);

		program_post.init(context, R.raw.vertex_fullscreen, R.raw.fragment_vignette, "a_Position");
		//program_post.init(context, R.raw.vertex_bars, R.raw.fragment_bars, "a_Position", "a_Color");
		
		program_circle_texture.init(context, R.raw.vertex_fullscreen, R.raw.fragment_circle_texture, "a_Position");
		program_ring_texture.init(context, R.raw.vertex_fullscreen, R.raw.fragment_ring_texture, "a_Position");
		program_texture.init(context, R.raw.vertex_fullscreen, R.raw.fragment_background_texture, "a_Position");
		program_texture_recolor.init(context, R.raw.vertex_fullscreen, R.raw.fragment_texture_recolor, "a_Position");
		program_texture_resize.init(context, R.raw.vertex_fullscreen, R.raw.fragment_texture_resize, "a_Position");
		
		program_texture.use();
		GLES20.glUniform1i(program_texture.getUniform("u_Texture"), 0);
		
		program_texture_recolor.use();
		GLES20.glUniform1i(program_texture_recolor.getUniform("u_Texture"), 0);
		
		program_texture_resize.use();
		GLES20.glUniform1i(program_texture_resize.getUniform("u_Texture"), 0);
		
		shape_fullscreen.init(GLES20.GL_TRIANGLE_STRIP, new float[] {-1, -1,  -1, 1,  1, -1,  1, 1}, 4, 2);
		
		updateCircleShape = true;
		updateTextures = true;
		updateCircleProgram = true;
		updateVignette = true;
		updateBackground = true;
		
		Matrix.setLookAtM(matrix_view, 0, 0, 0F, 1F, 0, 0, 0, 0, 1, 0);
		
		updateMVP();
		updateTextures();
		updatePreferences();
		
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
		{
			realHeight = height;
			height = screenHeight;
		}

		this.width = width;
		this.height = height;
		
		GLES20.glViewport(0, isPreview ? - height / 2 + realHeight / 2 : 0, width, height);
	
		ratio = (float) width / height;
		Matrix.orthoM(matrix_projection, 0, -ratio, ratio, -1, 1, 0.5F, 1.5F);		
		updateMVP();
		updateBackgroundMatrix = true;
		
		if (lastWidth == width && lastHeight == height && preferences.getBoolean("animateChange", true) || width == lastHeight && height == lastWidth && preferences.getBoolean("animateRotate", true))
		{
			respawn(false);
			
			for (int i = 0; circles != null && i < circles.length; ++i)
			{
				circles[i].setTarget();
				circles[i].velX += (circles[i].targetX - circles[i].posX) * 2;
				circles[i].velY += (circles[i].targetY - circles[i].posY) * 2;
			}
		}	
		
		if (program_circles.initialized() && preferences.getBoolean("vignetteBlur", false))
		{
			program_circles.use();
			GLES20.glUniform2f(program_circles.getUniform("u_InvResolution"), 1F / width, 1F / height);
		}
		
			
		lastTime = 0;
		lastWidth = width;
		lastHeight = height;
		paused = false;
	}
	
	public void onPause()
	{
		paused = true;
		
		if (sensorActive)
		{
			sensorManager.unregisterListener(this, gravitySensor);
			sensorActive = false;
		}
	}
	
	public void onResume()
	{
		paused = false;
		
		if (gravity && !sensorActive)
		{
			sensorManager.registerListener(this, gravitySensor, SENSOR_PERIOD);
			sensorActive = true;
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
		respawn();
		
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
		if (height > 0)
		{
			int size, blurSize;
			String blurMode = preferences.getString("blurMode", "normal");
			
			if (preferences.getBoolean("circleTexture", false))
			{
				Bitmap bitmap;
				
				if (preferences.getString("circleTextureFile", "").isEmpty())
				{
					bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android);
				}
				else
					try
					{
						bitmap = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + "/circleTextureFile");
					}
					catch (Exception e)
					{
						bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android);
					}
					
				circleRatio = bitmap.getWidth() / (float) bitmap.getHeight();
				size = Math.min(bitmap.getWidth(), bitmap.getHeight());
				blurSize = Math.max(1, (int) (size * preferences.getFloat("blurStrength", 0.1F)));
				
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				
				textureBuffer2.init(false, width, height);
				textureBuffer3.init(false, width, height);
				textureBufferTMP.init(false, width, height);
				
				texture1.init(bitmap, GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE);
				texture1.bind(1);
				
				createBlurPrograms(width, height, blurSize);
				
				texture1.bind(0);
				blur(textureBuffer2);
				if (blurMode.equals("inner"))
				{
					GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_COLOR, GLES20.GL_DST_COLOR);
					texture1.bind(0);
					program_texture.use();
					shape_fullscreen.render();		
				}
				else if (blurMode.equals("outer"))
				{
					GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
					texture1.bind(0);
					program_texture_resize.use();
					GLES20.glUniform2f(program_texture_resize.getUniform("u_Scale"), 1F - 2F * blurSize / width, 1F - 2F * blurSize / height);
					shape_fullscreen.render();		
				}
				textureBuffer2.bindTexture(2);
				
				textureBuffer2.bindTexture(2);
				blur(textureBuffer3);
				textureBuffer3.bindTexture(3);
			}
			else
			{
				circleRatio = 1;
				
				size = (int) (height * preferences.getFloat("sizeMax", 0.75F) * Circle.MAX_SIZE * preferences.getFloat("textureQuality", 1));
				blurSize = Math.max(1, (int) (size * preferences.getFloat("blurStrength", 0.1F)));
				
				textureBuffer1.init(false, size, size);
				textureBuffer2.init(false, size, size);
				textureBuffer3.init(false, size, size);
				textureBufferTMP.init(false, size, size);
				
				textureBuffer1.bind(true);
				program_circle_texture.use();
				shape_fullscreen.render();
				
				textureBuffer1.bindTexture(1);
				
				createBlurPrograms(size, size, blurSize);
				
				textureBuffer1.bindTexture(0);
				blur(textureBuffer2);
				
				if (blurMode.equals("inner"))
				{
					GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_COLOR, GLES20.GL_DST_COLOR);
					textureBuffer1.bindTexture(0);
					program_texture.use();
					shape_fullscreen.render();		
				}
				else if (blurMode.equals("outer"))
				{
					GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
					textureBuffer1.bindTexture(0);
					program_texture_resize.use();
					GLES20.glUniform2f(program_texture_resize.getUniform("u_Scale"), 1F - 2F * blurSize / size, 1F - 2F * blurSize / size);
					shape_fullscreen.render();		
				}
				
				textureBuffer2.bindTexture(2);
				
				textureBuffer2.bindTexture(0);
				blur(textureBuffer3);
				textureBuffer3.bindTexture(3);
			}

			if (preferences.getBoolean("ringTexture", false))
			{
				Bitmap bitmap;
				
				if (preferences.getString("ringTextureFile", "").isEmpty())
				{
					bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android);
				}
				else
					try
					{
						bitmap = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + "/ringTextureFile");
					}
					catch (Exception e)
					{
						bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android);
					}
				
				ringRatio = bitmap.getWidth() / (float) bitmap.getHeight();
				blurSize = Math.max(1, (int) (size * preferences.getFloat("blurStrength", 0.1F)));
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				
				texture4.init(bitmap, GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE);
				texture4.bind(4);
				
				textureBuffer5.init(false, width, height);
				textureBuffer6.init(false, width, height);
				textureBufferTMP.init(false, width, height);

				createBlurPrograms(width, height, blurSize);
				
				texture4.bind(0);
				blur(textureBuffer5);
				if (blurMode.equals("inner"))
				{
					GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_COLOR, GLES20.GL_DST_COLOR);
					texture4.bind(0);
					program_texture.use();
					shape_fullscreen.render();		
				}
				else if (blurMode.equals("outer"))
				{
					GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
					texture4.bind(0);
					program_texture_resize.use();
					GLES20.glUniform2f(program_texture_resize.getUniform("u_Scale"), 1F - 2F * blurSize / width, 1F - 2F * blurSize / height);
					shape_fullscreen.render();		
				}
				textureBuffer5.bindTexture(5);
				
				textureBuffer5.bindTexture(0);
				blur(textureBuffer6);
				textureBuffer6.bindTexture(6);
			}
			else
			{
				ringRatio = 1;
				
				size = (int) (height * preferences.getFloat("sizeMax", 0.75F) * Circle.MAX_SIZE * preferences.getFloat("textureQuality", 1));
				blurSize = Math.max(1, (int) (size * preferences.getFloat("blurStrength", 0.1F)));
				
				textureBuffer4.init(false, size, size);
				textureBuffer5.init(false, size, size);
				textureBuffer6.init(false, size, size);
				textureBufferTMP.init(false, size, size);

				textureBuffer4.bind(true);
				program_ring_texture.use();
				GLES20.glUniform1f(program_ring_texture.getUniform("u_RingWidth"), preferences.getFloat("ringWidth", 0.1F));
				shape_fullscreen.render();

				textureBuffer4.bindTexture(4);

				createBlurPrograms(size, size, blurSize);
				
				textureBuffer4.bindTexture(0);
				blur(textureBuffer5);
				if (blurMode.equals("inner"))
				{
					GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_COLOR, GLES20.GL_DST_COLOR);
					textureBuffer4.bindTexture(0);
					program_texture.use();
					shape_fullscreen.render();		
				}
				else if (blurMode.equals("outer"))
				{
					GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
					program_ring_texture.use();
					GLES20.glUniform1f(program_ring_texture.getUniform("u_RingWidth"), preferences.getFloat("ringWidth", 0.1F) - 2F * blurSize / size);
					shape_fullscreen.render();
				}
				textureBuffer5.bindTexture(5);
				
				textureBuffer5.bindTexture(0);
				blur(textureBuffer6);
				textureBuffer6.bindTexture(6);
			}
			
			Framebuffer.release(this, false);
			
			updateTextures = false;
		}
	}
	
	public void createBlurPrograms(int width, int height, float blurSize)
	{
		int kernelSize = 1 + 4 * (int) (blurSize / 2);
		int i;
		float[] kernel = new float[kernelSize];

		for (i = 0; i < kernelSize; ++i)
			kernel[i] = gaussian((i - kernelSize / 2) / blurSize * 6);

		float sum = 0;
		for (i = 0; i < kernelSize; ++i)
			sum += kernel[i];
			
		for (i = 0; i < kernelSize; ++i)
			kernel[i] /= sum;

		StringBuilder sb = new StringBuilder();
		
		for (i = 0; i < kernelSize / 2; ++i)
			if (kernel[i] > 0)
				sb.append("gl_FragColor += texture2D(u_Texture, v_UV + u_Dir * vec2(").append((i - kernelSize / 2) * 2F).append(")) * ").append(String.format("%.16f", kernel[i])).append(";\n");

		sb.append("gl_FragColor += texture2D(u_Texture, v_UV) * ").append(kernel[kernelSize / 2]).append(";\n");
		
		for (i = kernelSize / 2 + 1; i < kernelSize; ++i)
			if (kernel[i] > 0)
				sb.append("gl_FragColor += texture2D(u_Texture, v_UV + u_Dir * vec2(").append((i - kernelSize / 2) * 2F).append(")) * ").append(String.format("%.16f", kernel[i])).append(";\n");
		
		String kernelString = sb.toString();
		program_blur_horizontal.init(Program.getString(context, R.raw.vertex_fullscreen), Program.getString(context, R.raw.fragment_blur).replace("#blur", kernelString), "a_Position");
		program_blur_vertical.init(Program.getString(context, R.raw.vertex_fullscreen), Program.getString(context, R.raw.fragment_blur).replace("#blur", kernelString), "a_Position");

		program_blur_horizontal.use();
		GLES20.glUniform1i(program_blur_horizontal.getUniform("u_Texture"), 0);
		GLES20.glUniform2f(program_blur_horizontal.getUniform("u_Dir"), 1F / width, 0);
		
		program_blur_vertical.use();
		GLES20.glUniform1i(program_blur_vertical.getUniform("u_Texture"), 0);
		GLES20.glUniform2f(program_blur_vertical.getUniform("u_Dir"), 0, 1F / height);
	}

	public static final double sigma = 3D;

	public float gaussian(float x)
	{
		return (float) (1D / (sigma * Math.sqrt(2D * Math.PI)) * Math.pow(Math.E, - x * x / (2D * sigma * sigma)));
	}
	
	public void blur(Framebuffer buffer)
	{
		textureBufferTMP.bind(true);
		program_blur_horizontal.use();
		shape_fullscreen.render();

		buffer.bind(true);
		program_blur_vertical.use();
		textureBufferTMP.bindTexture(0);
		shape_fullscreen.render();
		
		textureBufferTMP.bind(true);
		buffer.bindTexture(0);
		program_blur_horizontal.use();
		shape_fullscreen.render();

		buffer.bind(true);
		program_blur_vertical.use();
		textureBufferTMP.bindTexture(0);
		shape_fullscreen.render();
	}
	
	public void updateCircles(float partial)
	{
		vertices_circle.position(0);
		
		for (Circle circle : circles)
			circle.buffer(vertices_circle, partial);
	}
	
	public void updateCircleProgram()
	{
		if (preferences.getBoolean("vignetteBlur", false))
		{
			program_circles.init(context, R.raw.vertex_circle, R.raw.fragment_circle_blur, "a_Position", "a_Color", "a_UV");
			
			program_circles.use();
			GLES20.glUniform2f(program_circles.getUniform("u_InvResolution"), 1F / width, 1F / height);
			
			GLES20.glUniform1i(program_circles.getUniform("u_Texture1"), 1);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture2"), 2);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture3"), 3);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture4"), 4);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture5"), 5);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture6"), 6);
			
			GLES20.glUniformMatrix4fv(program_circles.getUniform("u_MVP"), 1, false, matrix_mvp, 0);
			
			updateVignetteBlur = true;
		}
		else
		{
			program_circles.init(context, R.raw.vertex_circle, R.raw.fragment_circle, "a_Position", "a_Color", "a_UV");
			
			program_circles.use();
			GLES20.glUniform1i(program_circles.getUniform("u_Texture1"), 1);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture2"), 2);
			GLES20.glUniform1i(program_circles.getUniform("u_Texture4"), 4);
			
			GLES20.glUniform1i(program_circles.getUniform("u_Texture5"), 5);
			GLES20.glUniformMatrix4fv(program_circles.getUniform("u_MVP"), 1, false, matrix_mvp, 0);
		}
		
			
		updateCircleProgram = false;
	}
	
	public void updateVignette()
	{
		if (program_post.initialized())
		{
			program_post.use();
			
			uniformColor(program_post.getUniform("u_Color"), "colorVignette", "#000000", 1);
			GLES20.glUniform1f(program_post.getUniform("u_Strength"), preferences.getFloat("vignetteStrength", 0.8F));
			GLES20.glUniform1f(program_post.getUniform("u_Radius"), preferences.getFloat("vignetteRadius", 0.1F));
			
			updateVignette = false;
		}
	}
	
	public void updateVignetteBlur()
	{
		if (program_circles.initialized())
		{
			program_circles.use();
			GLES20.glUniform1f(program_circles.getUniform("u_Strength"), preferences.getFloat("vignetteBlurStrength", 0.5F));
			GLES20.glUniform1f(program_circles.getUniform("u_Radius"), preferences.getFloat("vignetteBlurRadius", 0.5F));
			
			updateVignetteBlur = false;
		}
	}
	
	Runnable bitmapRunnable;
	public Bitmap bitmap;
	
	public void renderToBitmap(Runnable onFinished)
	{
		bitmapRunnable = onFinished;
		
		updateBitmap = true;
	}
	
	public void updateBitmap()
	{
		int b[] = new int[width * height];
		int bt[] = new int[width * height];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

		int i, j, pix, pb, pr;
		for (i = 0; i < height; i++)
		{          
			for (j = 0; j < width; j++)
			{
				pix = b[i * width + j];
				pb = (pix >> 16) & 0xff;
				pr = (pix << 16) & 0x00ff0000;
				pix = (pix & 0xff00ff00) | pr | pb;
				bt[(height - i - 1) * width + j] = pix;
			}
		}


		Bitmap bitmap1 = Bitmap.createBitmap(bt, width, height, Bitmap.Config.ARGB_8888);
		bitmap = Bitmap.createScaledBitmap(bitmap1, bitmap1.getWidth() / 4, bitmap1.getHeight() / 4, true);
		bitmap1.recycle();
		
		((WallpaperPreferenceActivity) context).runOnUiThread(bitmapRunnable);
		
		updateBitmap = false;
		bitmapRunnable = null;
	}
	
	public void updateBackground()
	{
		if (backgroundTexture)
		{
			if (height == 0)
				return;
				
			Bitmap bitmap = null;
			
			if (!preferences.getString("backgroundTextureFile", "").isEmpty())
				try
				{
					bitmap = BitmapFactory.decodeFile(context.getFilesDir().getAbsolutePath() + "/backgroundTextureFile");
				}
				catch (Exception e)
				{
					bitmap = null;
				}
			
			if (bitmap == null)
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.android);
			
			backgroundRatio = bitmap.getWidth() / (float) bitmap.getHeight();
			
			if (preferences.getFloat("backgroundBlurStrength", 0.0F) > 0.0F)
			{
				textureBufferBackground2.init(false, bitmap.getWidth(), bitmap.getHeight());
				textureBufferBackground.init(false, width, height);
				textureBufferTMP.init(false, width, height);

				textureBackground.init(bitmap, GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				textureBackground.bind(0);
				
				createBlurPrograms(width, height, 0.1F * height * preferences.getFloat("backgroundBlurStrength", 0));
				
				textureBufferBackground2.bind(true);
				program_texture_recolor.use();
				GLES20.glUniform3f(program_texture_recolor.getUniform("u_HSV"), preferences.getFloat("backgroundTextureColorA", 1), preferences.getFloat("backgroundTextureColorB", 1), preferences.getFloat("backgroundTextureColorC", 1));
				shape_fullscreen.render();
				
				textureBufferBackground2.bindTexture(0);
				blur(textureBufferBackground);
			}
			else
			{
				textureBufferBackground.init(false, width, height);
				
				textureBackground.init(bitmap, GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
				textureBackground.bind(0);
				
				textureBufferBackground.bind(true);
				program_texture_recolor.use();
				GLES20.glUniform3f(program_texture_recolor.getUniform("u_HSV"), preferences.getFloat("backgroundTextureColorA", 1), preferences.getFloat("backgroundTextureColorB", 1), preferences.getFloat("backgroundTextureColorC", 1));
				shape_fullscreen.render();
			}
			
			Framebuffer.release(this, false);
			textureBufferBackground.bindTexture(10);
			
			program_background.init(context, R.raw.vertex_background_texture, R.raw.fragment_background_texture, "a_Position", "a_UV");
			
			program_background.use();
			GLES20.glUniform1i(program_background.getUniform("u_Texture"), 10);
			
			updateBackgroundMatrix = true;
		}
		else
		{
			program_background.init(context, R.raw.vertex_fullscreen, R.raw.fragment_background, "a_Position");
			
			updateColors = true;
		}
			
		updateBackground = false;
	}
	
	public void updateBackgroundMatrix()
	{
		if (backgroundTexture)
		{
			Matrix.setIdentityM(matrix_model_background, 0);

			if (backgroundRatio > ratio)
			{
				if (backgroundTextureSwipe)
					Matrix.translateM(matrix_model_background, 0, (backgroundTextureSwipeInverse ? -1 : 1) * (1 - 2 * lastXOffset) * (backgroundRatio / ratio - 1), 0, 0);
				
				Matrix.scaleM(matrix_model_background, 0, backgroundRatio / ratio, 1, 1);
			}
			else
			{
				if (backgroundTextureSwipe)
					Matrix.translateM(matrix_model_background, 0, 0, (backgroundTextureSwipeInverse ? -1 : 1) * (0.5F - lastYOffset) * (ratio / backgroundRatio - 1), 0);
				
				Matrix.scaleM(matrix_model_background, 0, 1, ratio / backgroundRatio, 1);
			}
				
			program_background.use();
			GLES20.glUniformMatrix4fv(program_background.getUniform("u_M"), 1, false, matrix_model_background, 0);
		}
		
		updateBackgroundMatrix = false;
	}
	
	@Override
	public void onDrawFrame(GL10 p1)
	{
		if (!program_post.initialized())
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

		respawn(false);
		
		while (accumulator >= fixedDelta)
		{
			int i, i1;
			float deltaX, deltaY, massSum, distance;
			for (i = 0; circles != null && i < circles.length; ++i)
			{
				if (repulsion)
				{
					if (repulsionWall)
					{
						deltaX = circles[i].posX + circles[i].randomPosX;
						deltaY = circles[i].posY + circles[i].randomPosY;
						
						if (Math.abs(deltaX) < ratio - 0.1F)
							deltaX = 0;
						
						if (Math.abs(deltaY) < 1 - 0.1F)
							deltaY = 0;
							
						circles[i].velX += -Math.signum(deltaX) * Math.pow(deltaX, 2) * (1 / circles[i].size) * 0.025F * repulsionStrength;
						circles[i].velY += -Math.signum(deltaY) * Math.pow(deltaY, 2) * (1 / circles[i].size) * 0.025F * repulsionStrength;
					}
					
					for (i1 = i + 1; i1 < circles.length && circles[i1] != null; ++i1)
					{
						massSum = 1 / circles[i].size + 1 / circles[i1].size;
						deltaX = circles[i].posX + circles[i].randomPosX - (circles[i1].posX + circles[i1].randomPosX);
						deltaY = circles[i].posY + circles[i].randomPosY - (circles[i1].posY + circles[i1].randomPosY);

						if (deltaX * deltaX + deltaY * deltaY > 0.25F)
							continue;

						distance = Math.max(0.5F - (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY), 0);

						circles[i].velX += Math.signum(deltaX) * distance * (1 / circles[i].size) / massSum * 0.1F * repulsionStrength;
						circles[i].velY += Math.signum(deltaY) * distance * (1 / circles[i].size) / massSum * 0.1F * repulsionStrength;

						circles[i1].velX -= Math.signum(deltaX) * distance * (1 / circles[i1].size) / massSum * 0.1F * repulsionStrength;
						circles[i1].velY -= Math.signum(deltaY) * distance * (1 / circles[i1].size) / massSum * 0.1F * repulsionStrength;

					}
				}
					
					
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
		
		if (updateBackground)
			updateBackground();
			
		if (updateBackgroundMatrix)
			updateBackgroundMatrix();
		
		if (updateTextures)
			updateTextures();
		
		if (updateBitmap)
		{
			textureBufferTMP.init(false, width, height);
			textureBufferTMP.bind(true);
		}
			
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		if (additive)
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
		else 
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		
		if (updateCircleProgram)
			updateCircleProgram();
			
		if (updateVignetteBlur)
			updateVignetteBlur();
			
		program_circles.use();
		
		if (updateCircleShape)
			updateCircleShape();
			
		updateCircles(accumulator / fixedDelta);
		
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
			if (!backgroundTexture)
			{
				uniformColor(program_background.getUniform("u_Color1"), "colorBackground1", "#ff8800", 1);
				uniformColor(program_background.getUniform("u_Color2"), "colorBackground2", "#ff4400", 1);
			}
			
			updateColors = false;
		}
		
		shape_fullscreen.render();
		
		if (vignette)
		{
			program_post.use();

			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

			if (updateVignette)
				updateVignette();

			shape_fullscreen.render();
		}
		
		if (updateBitmap)
		{
			updateBitmap();
			Framebuffer.release(this, false);
		}
	}
	
	float lastXOffset = 0.5F, lastYOffset = 0.5F;
	
	public void onOffsetsChanged(float xOffset, float yOffset)
	{
		if (swipe)
		{
			respawn(false);
			
			for (int i = 0; i < circles.length; ++i)
			{
				circles[i].velX += (xOffset - lastXOffset) * (!sizeEffectsSwipe ? 5F : 1F / circles[i].size) * -0.5F * swipeSensitivity;
				circles[i].velY += (yOffset - lastYOffset) * (!sizeEffectsSwipe ? 5F : 1F / circles[i].size) * -0.5F * swipeSensitivity;
			}
		}
		if (backgroundTextureSwipe)
			updateBackgroundMatrix = true;
		
		lastXOffset = xOffset;
		lastYOffset = yOffset;
	}
	
	public void onTap(float x, float y)
	{
		if (tap)
		{
			x = ((x / width) * 2 - 1) * ((float) width / height);
			y = ((1 - y / height) * 2 - 1);
			
			float deltaX;
			float deltaY;

			respawn(false);

			for (int i = 0; i < circles.length; ++i)
			{
				deltaX = circles[i].posX + circles[i].randomPosX - x;
				deltaY = circles[i].posY + circles[i].randomPosY - y;

				if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) < circles[i].size * 0.8)
					circles[i].kill();
						
			}
		}
		if (touch)
		{
			if (!tap)
			{
				x = ((x / width) * 2 - 1) * ((float) width / height);
				y = ((1 - y / height) * 2 - 1);
			}
			
			float distance;
			float deltaX;
			float deltaY;
			
			respawn(false);
			
			for (int i = 0; i < circles.length; ++i)
			{
				if (circles[i].isDead)
					continue;
					
				deltaX = circles[i].posX + circles[i].randomPosX - x;
				deltaY = circles[i].posY + circles[i].randomPosY - y;
				
				distance = Math.max(0.5F - (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY), 0);
					
				circles[i].velX += Math.signum(deltaX) * distance * (!sizeEffectsTouch ? 5F : 1F / circles[i].size) * 0.5F * touchSensitivity;
				circles[i].velY += Math.signum(deltaY) * distance * (!sizeEffectsTouch ? 5F : 1F / circles[i].size) * 0.5F * touchSensitivity;
			}
		}
	}
	

	@Override
	public void onSensorChanged(SensorEvent e)
	{
		if (!sensorActive)
			sensorManager.unregisterListener(this, gravitySensor);
		
		if (circles == null)
			return;

		respawn(false);

		for (int i = 0; i < circles.length; ++i)
		{
			if (circles[i] == null)
				continue;

			circles[i].velX -= 0.0001F * (!sizeEffectsGravity ? 5F : 1F / circles[i].size) * (e.values[0] * gravityXX + e.values[1] * gravityXY + e.values[2] * gravityXZ);
			circles[i].velY -= 0.0001F * (!sizeEffectsGravity ? 5F : 1F / circles[i].size) * (e.values[0] * gravityYX + e.values[1] * gravityYY + e.values[2] * gravityYZ);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor p1, int p2) {}
	
	public void respawn()
	{
		respawn(true);
	}
	
	public void respawn(boolean force)
	{
		if (!force && (circles == null || circles[0] != null))
			return;
		
		int count = (int) (circleCount / ((1 - groupSize) * groupPercentage + 1));
		
		for (int i = 0; circles != null && i < circles.length; ++i)
		{
			if (circles[i] == null)
				circles[i] = new Circle(this, i);

			if (i < count * groupPercentage * groupSize)
				circles[i].parent = circles[(int) ((int) (i / groupSize) * groupSize)];
			else 
				circles[i].parent = null;
				
			circles[i].spawn(false);
		}
	}
	
	public void resetTarget()
	{
		respawn(false);
		
		for (int i = 0; circles != null && i < circles.length; ++i)
		{
			circles[i].setTarget();
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String key)
	{
		switch (key)
		{
			case "additive":
				additive = preferences.getBoolean("additive", true);
				break;
			case "vignette":
				vignette = preferences.getBoolean("vignette", false);
				break;
			case "spawnShape":
				spawnShape = preferences.getBoolean("spawnShape", false);
				respawn();
				break;
			case "shape":
				shape = Arrays.binarySearch(context.getResources().getStringArray(R.array.shape), preferences.getString("shape", "Circle"));
				respawn();
				break;
			case "animateShape":
				animateShape = preferences.getBoolean("animateShape", false);
				respawn();
				break;
			case "colorBackground1":
			case "colorBackground2":
				updateColors = true;
				break;
			case "sizeMax":
				updateTextures = true;
				respawn();
				break;
			case "spawnXMin":
			case "spawnXMax":
			case "spawnYMin":
			case "spawnYMax":
			case "sizeMin":
			case "alphaMin":
			case "alphaMax":
			case "colorCircle1":
			case "colorCircle2":
			case "interpolate":
			case "flickering":
			case "rings":
			case "ringPercentage":
			case "blur":
			case "blurPercentage":
			case "respawn":
			case "lifeTimeMin":
			case "lifeTimeMax":
			case "directionXMin":
			case "directionXMax":
			case "directionYMin":
			case "directionYMax":
			case "animateColor":
			case "colorTarget":
			case "targetColor":
			case "animateAlpha":
			case "alphaTarget":
			case "targetAlphaMin":
			case "targetAlphaMax":
			case "animateSize":
			case "sizeTarget":
			case "targetSizeMin":
			case "targetSizeMax":
			case "rotationStartMin":
			case "rotationStartMax":
			case "rotationSpeedMin":
			case "rotationSpeedMax":
			case "direction":
			//case "refresh":
				updatePreferences();
				respawn();
				break;
		    case "refresh":
//				this.preferences.unregisterOnSharedPreferenceChangeListener(this);
//				this.preferences = this.context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
//				this.preferences.registerOnSharedPreferenceChangeListener(this);
//				
				updatePreferences();
				respawn();
				break;
				
			case "rotation":
				rotation = preferences.getBoolean("rotation", false);
				respawn();
				break;
			case "offsetMin":
			case "offsetMax":
				offsetMin = preferences.getFloat("offsetMin", 0.0F);
				offsetMax = preferences.getFloat("offsetMax", 0.01F);
				respawn();
				break;
				
			case "groupSize":
				groupSize = preferences.getFloat("groupSize", 1);
				respawn();
				break;
			case "groupPercentage":
				groupPercentage = preferences.getFloat("groupPercentage", 20) / 100F;
				respawn();
				break;
			case "groupSizeFactorMin":
			case "groupSizeFactorMax":
				groupSizeFactorMin = preferences.getFloat("groupSizeFactorMin", 0.5F);
				groupSizeFactorMax = preferences.getFloat("groupSizeFactorMax", 0.75F);
				respawn();
				break;
			case "groupOffsetMin":
			case "groupOffsetMax":
				groupOffsetMin = preferences.getFloat("groupOffsetMin", 0.75F);
				groupOffsetMax = preferences.getFloat("groupOffsetMax", 1.25F);
				respawn();
				break;
			case "groupConnected":
				connected = preferences.getBoolean("groupConnected", false);
				break;
				
			case "loop":
				loop = preferences.getBoolean("loop", true);
				break;
			case "repulsion":
			case "repulsionWall":
			case "repulsionStrength":
				repulsion = preferences.getBoolean("repulsion", false);
				repulsionWall = preferences.getBoolean("repulsionWall", false);
				repulsionStrength = preferences.getFloat("repulsionStrength", 0.5F);
				break;
			case "fade":
				fade = preferences.getBoolean("fade", true);
				fadeIn = preferences.getFloat("fadeIn", 1F);
				fadeOut = preferences.getFloat("fadeOut", 1F);
				break;
			case "touch":
			case "touchSensitivity":
			case "touchSize":
			case "touchInvert":
				touchSensitivity = preferences.getFloat("touchSensitivity", 0.5F) * (preferences.getBoolean("touchInvert", false) ? -1 : 1);
				touch = preferences.getBoolean("touch", true);
				sizeEffectsTouch = preferences.getBoolean("touchSize", true);
				break;
			case "tap":
			case "tapDuration":
				tap = preferences.getBoolean("tap", true);
				killAnimation = preferences.getFloat("tapDuration", 0.25F);
				break;
			case "swipe":
			case "swipeSensitivity":
			case "swipeSize":
			case "swipeInvert":
				swipeSensitivity = preferences.getFloat("swipeSensitivity", 0.5F) * (preferences.getBoolean("swipeInvert", false) ? -1 : 1);
				swipe = preferences.getBoolean("swipe", true);
				sizeEffectsSwipe = preferences.getBoolean("swipeSize", true);
				break;
			case "gravity":
				gravity = preferences.getBoolean("gravity", false);
				
				if (gravity && !sensorActive && !paused)
				{
					sensorManager.registerListener(this, gravitySensor, SENSOR_PERIOD);
					sensorActive = true;
				}
				else if (!gravity && sensorActive)
				{
					sensorManager.unregisterListener(this, gravitySensor);
					sensorActive = false;
				}
				break;
			case "gravitySize":
				sizeEffectsGravity = preferences.getBoolean("gravitySize", false);
				break;
			case "gravityXA":
			case "gravityXB":
			case "gravityXC":
				gravityXX = preferences.getFloat("gravityXA", 1);
				gravityXY = preferences.getFloat("gravityXB", 0);
				gravityXZ = preferences.getFloat("gravityXC", 0);
				break;
			case "gravityYA":
			case "gravityYB":
			case "gravityYC":
				gravityYX = preferences.getFloat("gravityXA", 0);
				gravityYY = preferences.getFloat("gravityYB", 1);
				gravityYZ = preferences.getFloat("gravityYC", 0);
				break;
			case "damping":
				damping = preferences.getFloat("damping", 0.05F);
				break;
			case "randomnessMin":
			case "randomnessMax":
			case "speedMin":
			case "speedMax":
				resetTarget();
				break;
			case "blurStrength":
			case "blurMode":
			case "ringWidth":
			case "circleTexture":
			case "circleTextureFile":
			case "ringTexture":
			case "ringTextureFile":
			case "textureQuality":
				updateTextures = true;
				break;
			case "vignetteBlur":
				updateCircleProgram = true;
				break;
			case "vignetteBlurStrength":
			case "vignetteBlurRadius":
				updateVignetteBlur = true;
				break;
			case "colorVignette":
			case "vignetteStrength":
			case "vignetteRadius":
				updateVignette = true;
				break;
			case "backgroundTexture":
			case "backgroundTextureFile":
			case "backgroundTextureColorA":
			case "backgroundTextureColorB":
			case "backgroundTextureColorC":
			case "backgroundBlurStrength":
				backgroundTexture = preferences.getBoolean("backgroundTexture", false);
				updateBackground = true;
				break;
			case "backgroundTextureSwipe":
			case "backgroundTextureSwipeInverse":
				backgroundTextureSwipe = preferences.getBoolean("backgroundTextureSwipe", true);
				backgroundTextureSwipeInverse = preferences.getBoolean("backgroundTextureSwipeInverse", false);
				updateBackgroundMatrix = true;
				break;
			case "count":
				updateCircleShape = true;
				break;
			case "fps":
				maxFPS = (int) preferences.getFloat("fps", 45);
				break;
			case "ups":
				fixedDelta = 1F / preferences.getFloat("ups", 45);
				break;
		}
		
	}
	
	public void updatePreferences()
	{
		loop = preferences.getBoolean("loop", true);
		repulsion = preferences.getBoolean("repulsion", false);
		repulsionWall = preferences.getBoolean("repulsionWall", false);
		repulsionStrength = preferences.getFloat("repulsionStrength", 0.5F);
		respawn = preferences.getBoolean("respawn", true);
		tap = preferences.getBoolean("tap", true);
		touch = preferences.getBoolean("touch", true);
		swipe = preferences.getBoolean("swipe", true);
		fade = preferences.getBoolean("fade", true);
		fadeIn = preferences.getFloat("fadeIn", 1F);
		fadeOut = preferences.getFloat("fadeOut", 1F);
		damping = preferences.getFloat("damping", 0.05F);
		
		animateColor = preferences.getBoolean("animateColor", false);
		animateAlpha = preferences.getBoolean("animateAlpha", false);
		animateSize = preferences.getBoolean("animateSize", false);
		direction = preferences.getBoolean("direction", false);
		flickering = preferences.getBoolean("flickering", true);
		sudden = preferences.getBoolean("sudden", false);
		spawnShape = preferences.getBoolean("spawnShape", false);
		animateShape = preferences.getBoolean("animateShape", false);
		shape = Arrays.binarySearch(context.getResources().getStringArray(R.array.shape), preferences.getString("shape", "Circle"));
		
		vignette = preferences.getBoolean("vignette", false);
		backgroundTexture = preferences.getBoolean("backgroundTexture", false);
		backgroundTextureSwipe = preferences.getBoolean("backgroundTextureSwipe", true);
		backgroundTextureSwipeInverse = preferences.getBoolean("backgroundTextureSwipeInverse", false);
		
		additive = preferences.getBoolean("additive", true);
		
		rotation = preferences.getBoolean("rotation", false);
		offsetMin = preferences.getFloat("offsetMin", 0.0F);
		offsetMax = preferences.getFloat("offsetMax", 0.01F);
		
		groupSize = preferences.getFloat("groupSize", 1F);
		groupPercentage = preferences.getFloat("groupPercentage", 20F) / 100F;
		groupSizeFactorMin = preferences.getFloat("groupSizeFactorMax", 0.5F);
		groupSizeFactorMax = preferences.getFloat("groupSizeFactorMax", 0.75F);
		groupOffsetMin = preferences.getFloat("groupOffsetMin", 0.75F);
		groupOffsetMax = preferences.getFloat("groupOffsetMax", 1.25F);
		connected = preferences.getBoolean("groupConnected", false);
		
		killAnimation = preferences.getFloat("tapDuration", 0.25F);
		
		touchSensitivity = preferences.getFloat("touchSensitivity", 0.5F) * (preferences.getBoolean("touchInverse", false) ? -1 : 1);
		sizeEffectsTouch = preferences.getBoolean("touchSize", true);
		
		swipeSensitivity = preferences.getFloat("swipeSensitivity", 0.5F) * (preferences.getBoolean("swipeInverse", false) ? -1 : 1);
		sizeEffectsSwipe = preferences.getBoolean("swipeSize", true);
		
		gravity = preferences.getBoolean("gravity", false);
		sizeEffectsGravity = preferences.getBoolean("gravitySize", false);
		gravityXX = preferences.getFloat("gravityXA", 1);
		gravityXY = preferences.getFloat("gravityXB", 0);
		gravityXZ = preferences.getFloat("gravityXC", 0);
		gravityYX = preferences.getFloat("gravityXA", 0);
		gravityYY = preferences.getFloat("gravityYB", 1);
		gravityYZ = preferences.getFloat("gravityYC", 0);
		
		if (gravity && !sensorActive && !paused)
		{
			sensorManager.registerListener(this, gravitySensor, SENSOR_PERIOD);
			sensorActive = true;
		}
		else if (!gravity && sensorActive)
		{
			sensorManager.unregisterListener(this, gravitySensor);
			sensorActive = false;
		}
	
		maxFPS = (int) preferences.getFloat("fps", 45);
		fixedDelta = 1F / preferences.getFloat("ups", 45);
	}
	
	public void uniformColor(int name, String key, String def, float alpha)
	{
		int color = Color.parseColor(preferences.getString(key, def));
		GLES20.glUniform4f(name, Color.red(color) / 255F, Color.green(color) / 255F, Color.blue(color) / 255F, alpha);
	}
}
