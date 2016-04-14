package com.taraxippus.cyrcle.gl;

import android.graphics.*;
import java.nio.*;

public class Circle
{
	public static final float MAX_SIZE = 0.2F;
	
	public final CyrcleRenderer renderer;
	
	float posX, posY;
	float randomPosX, randomPosY;
	float targetX, targetY;
	float velX, velY, directionVelX, directionVelY;
	float red, green, blue, alpha, alpha1;
	float size;
	float speed;
	float randomSpeedX, randomSpeedY;
	float randomOffsetX, randomOffsetY;
	float randomScaleX, randomScaleY;
	float flickeringTime, flickeringDuration, flickeringTick;
	float maxLifeTime, lifeTime;
	
	int texture;
	
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
		
		alpha1 = alphaMin + renderer.random.nextFloat() * (alphaMax - alphaMin);
		alpha = renderer.preferences.getBoolean("respawn", true) ? 0 : alpha1;
		
		float sizeMin = renderer.preferences.getFloat("sizeMin", 0.25F);
		float sizeMax = renderer.preferences.getFloat("sizeMax", 0.75F);

		size = MAX_SIZE * (sizeMin + renderer.random.nextFloat() * (sizeMax - sizeMin));
		
		texture = renderer.preferences.getBoolean("rings", true) && renderer.random.nextFloat() < renderer.preferences.getFloat("ringPercentage", 45F) / 100F ? 4 : 1;
		texture += renderer.preferences.getBoolean("blur", true) && renderer.random.nextFloat() < renderer.preferences.getFloat("blurPercentage", 45F) / 100F  ? 1 : 0;
		
		directionVelX = renderer.preferences.getFloat("directionXMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("directionXMax", 0.75F) - renderer.preferences.getFloat("directionXMin", 0.25F));
		directionVelY = renderer.preferences.getFloat("directionYMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("directionYMax", 0.75F) - renderer.preferences.getFloat("directionYMin", 0.25F));
		
		setTarget();
		
		flickeringTime = renderer.random.nextFloat() * 480;
		
		maxLifeTime = renderer.preferences.getFloat("lifeTimeMin", 30F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("lifeTimeMax", 60F) - renderer.preferences.getFloat("lifeTimeMin", 30F));
		lifeTime = maxLifeTime;
		
		if (!renderer.preferences.getBoolean("sudden", false))
		{
			speed = renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));

			randomSpeedX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnesdMin", 0.25F)) * (renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F)));
			randomSpeedY = (renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F))) * renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));

			randomScaleX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnesdMin", 0.25F)) * size;
			randomScaleY = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnesdMin", 0.25F)) * size;

			randomOffsetX = renderer.random.nextFloat() * (float) Math.PI * 2;
			randomOffsetY = renderer.random.nextFloat() * (float) Math.PI * 2;
		}
	}
	
	public void setTarget()
	{
		targetX = (renderer.random.nextFloat() * 2F - 1F) * ((float) renderer.width / renderer.height);
		targetY = renderer.random.nextFloat() * 2F - 1F;
		
		deltaX = deltaY = 0;
		
		speed = renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));
		
		if (renderer.preferences.getBoolean("sudden", false))
		{
			randomSpeedX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnesdMin", 0.25F)) * (renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F)));
			randomSpeedY = (renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F))) * renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));

			randomScaleX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnesdMin", 0.25F)) * size;
			randomScaleY = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnesdMin", 0.25F)) * size;

			randomOffsetX = renderer.random.nextFloat() * (float) Math.PI * 2;
			randomOffsetY = renderer.random.nextFloat() * (float) Math.PI * 2;
		}
	}
	
	public void buffer(FloatBuffer vertices)
	{
		vertices.put(posX + randomPosX - size);
		vertices.put(posY + randomPosY - size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);

		vertices.put(0);
		vertices.put(1);
		vertices.put(texture);


		vertices.put(posX + randomPosX - size);
		vertices.put(posY + randomPosY + size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);

		vertices.put(0);
		vertices.put(0);
		vertices.put(texture);


		vertices.put(posX + randomPosX + size);
		vertices.put(posY + randomPosY - size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);

		vertices.put(1);
		vertices.put(1);
		vertices.put(texture);


		vertices.put(posX + randomPosX + size);
		vertices.put(posY + randomPosY + size);

		vertices.put(red);
		vertices.put(green);
		vertices.put(blue);
		vertices.put(alpha);

		vertices.put(1);
		vertices.put(0);
		vertices.put(texture);
	}
	
	private float deltaX, deltaY, length;
	public void update(float time, float delta)
	{
		if (renderer.preferences.getBoolean("respawn", true))
		{
			if (lifeTime >= maxLifeTime - 1)
			{
				alpha = alpha1 * (maxLifeTime - lifeTime);
				
				if (lifeTime - delta < maxLifeTime - 1)
					alpha = alpha1;
			}
			else if (lifeTime < 1)
			{
				alpha = alpha1 * lifeTime;

				if (lifeTime <= delta)
					spawn();
			}
			
			lifeTime -= delta;
		}
		
		deltaX = targetX - posX;
		deltaY = targetY - posY;
		length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		
		if (length < 0.15F)
			setTarget();
		
		velX = velX * 0.95F + deltaX / length / size * 0.00025F * speed;
		velY = velY * 0.95F + deltaY / length / size * 0.00025F * speed;
		
		posX += velX * delta;
		posY += velY * delta;
		
		if (renderer.preferences.getBoolean("direction", false))
		{
			posX += 0.5F * directionVelX * delta;
			posY += 0.5F * directionVelY * delta;
		}
		
		if (posX + randomPosX < (float) -renderer.width / renderer.height - size)
			posX +=  (float) renderer.width / renderer.height * 2 + size * 2;
			
		if (posX + randomPosX > (float) renderer.width / renderer.height + size)
			posX -=  (float) renderer.width / renderer.height * 2 + size * 2;
		
		if (posY + randomPosY < -1 - size)
			posY += 2 + size * 2;
		
		if (posY + randomPosY > 1 + size)
			posY -= 2 + size * 2;
			
		randomPosX = randomPosX * 0.9F + (float) Math.cos(time * randomSpeedX * 2 + randomOffsetX) * randomScaleX * 0.015F;
		randomPosY = randomPosY * 0.9F + (float) Math.cos(time * randomSpeedY * 2 + randomOffsetY) * randomScaleY * 0.015F;
		
		if (flickeringTick > 0)
		{
			flickeringTick -= delta;
			
			if (lifeTime > 1 && lifeTime < maxLifeTime - 1 || !renderer.preferences.getBoolean("respawn", true))
			{
				alpha = alpha1 * ((float) Math.cos(flickeringTick / flickeringDuration * Math.PI * 2) * 0.5F + 0.5F);

				if (flickeringTick <= 0)
					alpha = alpha1;
			}
		}
		
		if (renderer.preferences.getBoolean("flickering", true))
		{
			flickeringTime -= delta;
			
			if (flickeringTime <= 0)
			{
				flickeringTime = 30F + renderer.random.nextFloat() * 240;
				flickeringDuration = renderer.random.nextFloat() * 0.5F + 0.25F;
				flickeringTick = flickeringDuration;
			}
		}
	}
}
