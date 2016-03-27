package com.taraxippus.cyrcle.gl;
import android.graphics.*;
import java.nio.*;

public class Circle
{
	public static final float MAX_SIZE = 0.2F;
	
	public final CyrcleRenderer renderer;
	
	float posX, posY;
	float targetX, targetY;
	float velX, velY;
	float red, green, blue, alpha;
	float size;
	
	int texture;
	
	long changeTarget;
	
	public Circle(CyrcleRenderer renderer)
	{
		this.renderer = renderer;
	}
	
	public void spawn()
	{
		posX = (renderer.random.nextFloat() * 2 - 1) * ((float) renderer.width / renderer.height);
		posY = renderer.random.nextFloat() * 2 - 1;
		
		velX = 0;
		velY = 0;
		
		int color1 = Color.parseColor(renderer.preferences.getString("colorCircle1", "#ffff00"));
		int color2 = Color.parseColor(renderer.preferences.getString("colorCircle2", "#ffcc00"));
		
		if (renderer.preferences.getBoolean("interpolate", true))
		{
			float delta = renderer.random.nextFloat();
			
			red = Color.red(color1) / 255F * delta + Color.red(color2) / 255F * (1 - delta);
			green = Color.green(color1) / 255F * delta + Color.green(color2) / 255F * (1 - delta);
			blue = Color.blue(color1) / 255F * delta + Color.blue(color2) / 255F * (1 - delta);
		}
		else
		{
			int color = renderer.random.nextBoolean() ? color1 : color2;
			red = Color.red(color) / 255F;
			green = Color.green(color) / 255F;
			blue = Color.blue(color) / 255F;
		}
		
		float alphaMin = renderer.preferences.getFloat("alphaMin", 0.25F);
		float alphaMax = renderer.preferences.getFloat("alphaMax", 0.75F);
		
		alpha = alphaMin + renderer.random.nextFloat() * (alphaMax - alphaMin);
		
		float sizeMin = renderer.preferences.getFloat("sizeMin", 0.25F);
		float sizeMax = renderer.preferences.getFloat("sizeMax", 0.75F);

		size = MAX_SIZE * (sizeMin + renderer.random.nextFloat() * (sizeMax - sizeMin));
		
		texture = renderer.preferences.getBoolean("rings", true) && renderer.random.nextFloat() < renderer.preferences.getFloat("ringPercentage", 45F) / 100F ? 3 : 1;
		texture += renderer.preferences.getBoolean("blur", true) && renderer.random.nextFloat() < renderer.preferences.getFloat("blurPercentage", 45F) / 100F  ? 1 : 0;
		
		setTarget();
	}
	
	public void setTarget()
	{
		targetX = (renderer.random.nextFloat() * 2F - 1F) * ((float) renderer.width / renderer.height);
		targetY = renderer.random.nextFloat() * 2F - 1F;
		
		deltaX = targetX;
		deltaY = targetY;
		length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		
		changeTarget = System.currentTimeMillis() + renderer.random.nextInt(40000) + 150000;
	}
	
	private float deltaX, deltaY, length;
	public void update(FloatBuffer vertices, float delta)
	{
		deltaX = targetX - posX;
		deltaY = targetY - posY;
		length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		
		if (length < 0.05F || System.currentTimeMillis() <= changeTarget)
			setTarget();
		
		velX = velX * 0.95F + deltaX / length * 0.01F;
		velY = velY * 0.95F + deltaY / length * 0.01F;
		
		posX += velX * delta;
		posY += velY * delta;
		
		if (posX < (float) renderer.width / renderer.height * -1.5F)
			posX +=  (float) renderer.width / renderer.height * 3;
			
		if (posX > (float) renderer.width / renderer.height * 1.5F)
			posX -=  (float) renderer.width / renderer.height * 3;
		
		if (posY < -1.5F)
			posY += 3;
		
		if (posY > 1.5F)
			posY -= 3;
			
		vertices.put(posX - size);
		vertices.put(posY - size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);
		
		vertices.put(0);
		vertices.put(0);
		vertices.put(texture);


		vertices.put(posX - size);
		vertices.put(posY + size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);
		
		vertices.put(0);
		vertices.put(1);
		vertices.put(texture);


		vertices.put(posX + size);
		vertices.put(posY - size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);
		
		vertices.put(1);
		vertices.put(0);
		vertices.put(texture);


		vertices.put(posX + size);
		vertices.put(posY + size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);
		
		vertices.put(1);
		vertices.put(1);
		vertices.put(texture);
	}
}
