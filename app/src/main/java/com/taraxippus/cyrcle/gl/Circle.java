package com.taraxippus.cyrcle.gl;

import android.graphics.*;
import java.nio.*;

public class Circle
{
	public static final float MAX_SIZE = 0.4F;
	
	public final CyrcleRenderer renderer;
	public final int index;
	public Circle parent;
	
	float posX, posY, prevPosX, prevPosY;
	float randomPosX, randomPosY, prevRandomPosX, prevRandomPosY;
	float targetX, targetY;
	float velX, velY, directionVelX, directionVelY;
	float rotation, rotationVel, prevRotation, prevOffset, offset;
	float red, green, blue, alpha;
	float prevRed, prevGreen, prevBlue, prevAlpha;
	float startRed, startGreen, startBlue, startAlpha;
	float targetRed, targetGreen, targetBlue, targetAlpha;
	float size, startSize, targetSize, prevSize;
	float speed;
	float randomSpeedX, randomSpeedY;
	float randomOffsetX, randomOffsetY;
	float randomScaleX, randomScaleY;
	float flickeringTime, flickeringDuration, flickeringTick;
	float maxLifeTime, lifeTime;
	
	int texture;
	
	public Circle(CyrcleRenderer renderer, int index)
	{
		this.renderer = renderer;
		this.index = index;
	}
	
	public void spawn(boolean notifyChildren)
	{
		if (parent == null || parent == this)
		{
			if (renderer.spawnShape)
			{
				if (renderer.shape == 0)
				{
					float angle = renderer.random.nextFloat() * 2 * (float) Math.PI;
					posX = renderer.preferences.getFloat("spawnXMax", 1) * renderer.random.nextFloat() * (float) Math.cos(angle);
					posY = renderer.preferences.getFloat("spawnYMax", 1) * renderer.random.nextFloat() * (float) Math.sin(angle);
				}
				else if (renderer.shape == 1)
				{
					float angle = renderer.random.nextFloat() * 2 * (float) Math.PI;
					posX = ((renderer.preferences.getFloat("spawnXMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnXMax", 1) - renderer.preferences.getFloat("spawnXMin", -1))) * 0.5F + 0.5F) * (float) Math.cos(angle);
					posY = ((renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1))) * 0.5F + 0.5F) * (float) Math.sin(angle);
				}
				else if (renderer.shape == 2)
				{
					float x = renderer.random.nextFloat() * 2 * (float) Math.PI;
					posX = (x / (float) Math.PI - 1) * renderer.ratio;
					posY = (float) Math.sin(x * (renderer.preferences.getFloat("spawnXMax", 1) * 0.5F + 0.5F) + renderer.time * (renderer.preferences.getFloat("spawnXMin", -1) * 0.5F + 0.5F)) * ((renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1))) * 0.5F + 0.5F);
				}
				else
				{
					float x = renderer.random.nextFloat() * 2 * (float) Math.PI;
					posX = (x / (float) Math.PI - 1) * renderer.ratio;
					posY = (renderer.random.nextBoolean() ? 1 : -1) * (float) Math.sin(x * (renderer.preferences.getFloat("spawnXMax", 1) * 0.5F + 0.5F) + 3 * renderer.time * (renderer.preferences.getFloat("spawnXMin", -1) * 0.5F + 0.5F)) * ((renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1))) * 0.5F + 0.5F);
				}
			}
			else
			{
				posX = (renderer.preferences.getFloat("spawnXMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnXMax", 1) - renderer.preferences.getFloat("spawnXMin", -1))) * renderer.ratio;
				posY = renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1));
			}
		}
		else
		{
			posX = parent.posX + parent.randomOffsetX;
			posY = parent.posY + parent.randomOffsetY;
		}
		
		velX = 0;
		velY = 0;
		
		int color1 = Color.parseColor(renderer.preferences.getString("colorCircle1", "#ffff00"));
		int color2 = Color.parseColor(renderer.preferences.getString("colorCircle2", "#ffcc00"));
		
		if (renderer.preferences.getBoolean("interpolate", true))
		{
			float delta = renderer.random.nextFloat();
			
			red = startRed = Color.red(color1) / 255F * delta + Color.red(color2) / 255F * (1 - delta);
			green = startGreen = Color.green(color1) / 255F * delta + Color.green(color2) / 255F * (1 - delta);
			blue = startBlue = Color.blue(color1) / 255F * delta + Color.blue(color2) / 255F * (1 - delta);
			
			if (renderer.preferences.getBoolean("colorTarget", false))
			{
				int color = Color.parseColor(renderer.preferences.getString("targetColor", "#000000"));
				targetRed = Color.red(color) / 255F;
				targetGreen = Color.green(color) / 255F;
				targetBlue = Color.blue(color) / 255F;
			}
			else
			{
				delta = renderer.random.nextFloat();

				targetRed = Color.red(color1) / 255F * delta + Color.red(color2) / 255F * (1 - delta);
				targetGreen = startGreen = Color.green(color1) / 255F * delta + Color.green(color2) / 255F * (1 - delta);
				targetBlue = startBlue = Color.blue(color1) / 255F * delta + Color.blue(color2) / 255F * (1 - delta);
			}
		}
		else
		{
			int color = renderer.random.nextBoolean() ? color1 : color2;
			red = startRed = Color.red(color) / 255F;
			green = startGreen = Color.green(color) / 255F;
			blue = startBlue = Color.blue(color) / 255F;
			
			if (renderer.preferences.getBoolean("colorTarget", false))
			{
				color = Color.parseColor(renderer.preferences.getString("targetColor", "#000000"));
				targetRed = Color.red(color) / 255F;
				targetGreen = Color.green(color) / 255F;
				targetBlue = Color.blue(color) / 255F;
			}
			else
			{
				color = renderer.random.nextBoolean() ? color1 : color2;
				targetRed = Color.red(color) / 255F;
				targetGreen = Color.green(color) / 255F;
				targetBlue = Color.blue(color) / 255F;
			}
		}
		
		float alphaMin = renderer.preferences.getFloat("alphaMin", 0.25F);
		float alphaMax = renderer.preferences.getFloat("alphaMax", 0.75F);
		
		startAlpha = alphaMin + renderer.random.nextFloat() * (alphaMax - alphaMin);
		alpha = renderer.preferences.getBoolean("respawn", true) ? 0 : startAlpha;
		
		if (renderer.preferences.getBoolean("alphaTarget", false))
			targetAlpha = renderer.preferences.getFloat("targetAlphaMin", 0.0F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("targetAlphaMax", 0.02F) - renderer.preferences.getFloat("targetAlphaMin", 0.0F));
		else
			targetAlpha = alphaMin + renderer.random.nextFloat() * (alphaMax - alphaMin);
		
		rotation = 0;
		rotationVel = 0;
		offset = 0;
		
		if (parent != null && parent != this)
		{
			size = startSize = parent.size * (renderer.groupSizeFactorMin + renderer.random.nextFloat() * (renderer.groupSizeFactorMin - renderer.groupSizeFactorMax));
			targetSize = parent.targetSize * (renderer.groupSizeFactorMin + renderer.random.nextFloat() * (renderer.groupSizeFactorMin - renderer.groupSizeFactorMax));
			
			rotation = parent.rotation;
			offset = parent.size * (renderer.groupOffsetMin + renderer.random.nextFloat() * (renderer.groupOffsetMin + renderer.groupOffsetMax));
			
			texture = parent.texture;
			maxLifeTime = parent.maxLifeTime;
		}
		else
		{
			float sizeMin = renderer.preferences.getFloat("sizeMin", 0.25F);
			float sizeMax = renderer.preferences.getFloat("sizeMax", 0.75F);

			size = startSize = MAX_SIZE * (sizeMin + renderer.random.nextFloat() * (sizeMax - sizeMin));

			if (renderer.preferences.getBoolean("sizeTarget", false))
				targetSize = MAX_SIZE * (renderer.preferences.getFloat("targetSizeMin", 0.0F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("targetSizeMax", 0.02F) - renderer.preferences.getFloat("targetSizeMin", 0.0F)));
			else
				targetSize = MAX_SIZE * (sizeMin + renderer.random.nextFloat() * (sizeMax - sizeMin));
				
			texture = renderer.preferences.getBoolean("rings", true) && renderer.random.nextFloat() < renderer.preferences.getFloat("ringPercentage", 45F) / 100F ? 4 : 1;
			texture += renderer.preferences.getBoolean("blur", true) && renderer.random.nextFloat() < renderer.preferences.getFloat("blurPercentage", 45F) / 100F  ? 1 : 0;

			directionVelX = renderer.preferences.getFloat("directionXMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("directionXMax", 0.75F) - renderer.preferences.getFloat("directionXMin", 0.25F));
			directionVelY = renderer.preferences.getFloat("directionYMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("directionYMax", 0.75F) - renderer.preferences.getFloat("directionYMin", 0.25F));
			
			maxLifeTime = renderer.preferences.getFloat("lifeTimeMin", 30F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("lifeTimeMax", 60F) - renderer.preferences.getFloat("lifeTimeMin", 30F));
			
			if (renderer.rotation)
			{
				rotation = renderer.preferences.getFloat("rotationStartMin", -180) + renderer.random.nextFloat() * (renderer.preferences.getFloat("rotationStartMax", 180) - renderer.preferences.getFloat("rotationStartMin", -180)) * size;
				rotationVel = renderer.preferences.getFloat("rotationSpeedMin", -45) + renderer.random.nextFloat() * (renderer.preferences.getFloat("rotationSpeedMax", 45) - renderer.preferences.getFloat("rotationSpeedMin", -45)) * size;

				offset = size * (renderer.offsetMin + renderer.random.nextFloat() * (renderer.offsetMax - renderer.offsetMin));
			}
		}
		lifeTime = maxLifeTime;
		
		setTarget();
		
		flickeringTime = renderer.random.nextFloat() * 480;
		
		if (!renderer.sudden)
		{
			speed = renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));

			randomSpeedX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F)) * (renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F)));
			randomSpeedY = (renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F))) * renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));

			randomScaleX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F)) * size;
			randomScaleY = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F)) * size;

			randomOffsetX = renderer.random.nextFloat() * (float) Math.PI * 2;
			randomOffsetY = renderer.random.nextFloat() * (float) Math.PI * 2;
		}
		
		prevPosX = posX;
		prevPosY = posY;
		prevRandomPosX = randomPosX = 0;
		prevRandomPosY = randomPosY = 0;
		prevRotation = rotation;
		prevOffset = offset;
		prevRed = red;
		prevGreen = green;
		prevBlue = blue;
		prevAlpha = alpha;
		prevSize = size;
		
		if (parent == this && notifyChildren)
			for (int i = 1; i < renderer.groupSize && index + i < renderer.circleCount && renderer.circles[index + i] != null; ++i)
				renderer.circles[index + i].spawn(false);
	}
	
	public void setTarget()
	{
		if (renderer.spawnShape && renderer.animateShape)
		{
			if (renderer.shape == 0)
			{
				float angle = renderer.random.nextFloat() * 2 * (float) Math.PI;
				targetX = renderer.preferences.getFloat("spawnXMax", 1) * renderer.random.nextFloat() * (float) Math.cos(angle);
				targetY = renderer.preferences.getFloat("spawnYMax", 1) * renderer.random.nextFloat() * (float) Math.sin(angle);
			}
			else if (renderer.shape == 1)
			{
				float angle = renderer.random.nextFloat() * 2 * (float) Math.PI;
				targetX = ((renderer.preferences.getFloat("spawnXMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnXMax", 1) - renderer.preferences.getFloat("spawnXMin", -1))) * 0.5F + 0.5F) * (float) Math.cos(angle);
				targetY = ((renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1))) * 0.5F + 0.5F) * (float) Math.sin(angle);
			}
			else if (renderer.shape == 2)
			{
				float x = renderer.random.nextFloat() * 2 * (float) Math.PI;
				targetX = (x / (float) Math.PI - 1) * renderer.ratio;
				targetY = (float) Math.sin(x * (renderer.preferences.getFloat("spawnXMax", 1) * 0.5F + 0.5F) + renderer.time * (renderer.preferences.getFloat("spawnXMin", -1) * 0.5F + 0.5F)) * ((renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1))) * 0.5F + 0.5F);
			}
			else
			{
				float x = renderer.random.nextFloat() * 2 * (float) Math.PI;
				targetX = (x / (float) Math.PI - 1) * renderer.ratio;
				targetY = (renderer.random.nextBoolean() ? 1 : -1) * (float) Math.sin(x * (renderer.preferences.getFloat("spawnXMax", 1) * 0.5F + 0.5F) + 3 * renderer.time * (renderer.preferences.getFloat("spawnXMin", -1) * 0.5F + 0.5F)) * ((renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1))) * 0.5F + 0.5F);
			}
		}
		else
		{
			targetX = (renderer.preferences.getFloat("spawnXMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnXMax", 1) - renderer.preferences.getFloat("spawnXMin", -1))) * renderer.ratio;
			targetY = renderer.preferences.getFloat("spawnYMin", -1) + renderer.random.nextFloat() * (renderer.preferences.getFloat("spawnYMax", 1) - renderer.preferences.getFloat("spawnYMin", -1));
		}
		
		deltaX = deltaY = 0;
		
		speed = renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));
		
		if (renderer.sudden)
		{
			randomSpeedX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F)) * (renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F)));
			randomSpeedY = (renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F))) * renderer.preferences.getFloat("speedMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("speedMax", 0.75F) - renderer.preferences.getFloat("speedMin", 0.25F));

			randomScaleX = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F)) * size;
			randomScaleY = renderer.preferences.getFloat("randomnessMin", 0.25F) + renderer.random.nextFloat() * (renderer.preferences.getFloat("randomnessMax", 0.75F) - renderer.preferences.getFloat("randomnessMin", 0.25F)) * size;

			randomOffsetX = renderer.random.nextFloat() * (float) Math.PI * 2;
			randomOffsetY = renderer.random.nextFloat() * (float) Math.PI * 2;
		}
	}
	
	float x, y, sX, sY, rot, sin, cos, off, r, g, b, a;
	public void buffer(FloatBuffer vertices, float partial)
	{
		x = posX * partial + (1 - partial) * prevPosX + randomPosX * partial + (1 - partial) * prevRandomPosX;
		y = posY * partial + (1 - partial) * prevPosY + randomPosY * partial + (1 - partial) * prevRandomPosY;
		sX = size * partial + (1 - partial) * prevSize;
		sY = sX / (texture < 4 ? renderer.circleRatio : renderer.ringRatio);
		r = red * partial + (1 - partial) * prevRed;
		g = green * partial + (1 - partial) * prevGreen;
		b = blue * partial + (1 - partial) * prevBlue;
		a = alpha * partial + (1 - partial) * prevAlpha;

		if (!renderer.rotation && (parent == null || parent == this))
		{
			vertices.put(x - sX);
			vertices.put(y - sY);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);

			vertices.put(0);
			vertices.put(1);
			vertices.put(texture);


			vertices.put(x - sX);
			vertices.put(y + sY);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);

			vertices.put(0);
			vertices.put(0);
			vertices.put(texture);


			vertices.put(x + sX);
			vertices.put(y - sY);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);

			vertices.put(1);
			vertices.put(1);
			vertices.put(texture);


			vertices.put(x + sX);
			vertices.put(y + sY);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);

			vertices.put(1);
			vertices.put(0);
			vertices.put(texture);
		}
		else
		{
			rot = (float) ((rotation * partial + (1 - partial) * prevRotation) / 180 * Math.PI);
			cos = (float) Math.cos(rot);
			sin = (float) Math.sin(rot);
			off = offset * partial + prevOffset * (1 - partial);

			vertices.put(x + cos * (-sX - off) - sin * (-sY- off) + off);
			vertices.put(y + sin * (-sX - off) + cos * (-sY - off) + off);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);

			vertices.put(0);
			vertices.put(1);
			vertices.put(texture);

			vertices.put(x + cos * (-sX - off) - sin * (sY - off) + off);
			vertices.put(y + sin * (-sX - off) + cos * (sY - off) + off);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);

			vertices.put(0);
			vertices.put(0);
			vertices.put(texture);
			
			vertices.put(x + cos * (sX - off) - sin * (-sY - off) + off);
			vertices.put(y + sin * (sX - off) + cos * (-sY - off) + off);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);

			vertices.put(1);
			vertices.put(1);
			vertices.put(texture);
			
			vertices.put(x + cos * (sX - off) - sin * (sY - off) + off);
			vertices.put(y + sin * (sX - off) + cos * (sY - off) + off);

			vertices.put(r);
			vertices.put(g);
			vertices.put(b);
			vertices.put(a);
			
			vertices.put(1);
			vertices.put(0);
			vertices.put(texture);
		}
	}
	
	private float deltaX, deltaY, length, deltaLifeTime;
	public void update(float time, float delta)
	{
		prevPosX = posX;
		prevPosY = posY;
		prevRandomPosX = randomPosX;
		prevRandomPosY = randomPosY;
		prevRotation = rotation;
		prevOffset = offset;
		prevRed = red;
		prevGreen = green;
		prevBlue = blue;
		prevAlpha = alpha;
		prevSize = size;
		
		alpha = 1;
		
		if (renderer.respawn)
		{
			if (lifeTime >= maxLifeTime - renderer.fadeIn)
			{
				alpha = (maxLifeTime - lifeTime) / renderer.fadeIn;
				
				if (lifeTime - delta < maxLifeTime - renderer.fadeIn)
					alpha = 1;
			}
			if (lifeTime < renderer.fadeOut)
			{
				alpha = lifeTime / renderer.fadeOut;

				if (lifeTime <= delta)
					spawn(true);
			}
			
			deltaLifeTime = lifeTime / maxLifeTime;
			if (renderer.animateAlpha)
			{
				alpha *= startAlpha * deltaLifeTime + (1 - deltaLifeTime) * targetAlpha;
			}
			else
				alpha *= startAlpha;
				
			if (renderer.animateColor)
			{
				red = startRed * deltaLifeTime + (1 - deltaLifeTime) * targetRed;
				green = startGreen * deltaLifeTime + (1 - deltaLifeTime) * targetGreen;
				blue = startBlue * deltaLifeTime + (1 - deltaLifeTime) * targetBlue;
			}
			if (renderer.animateSize)
			{
				size = startSize * deltaLifeTime + (1 - deltaLifeTime) * targetSize;
			}
			
			lifeTime -= delta;
		}
	
		if (parent != null && parent != this)
		{
			posX = parent.posX + parent.randomPosX;
			posY = parent.posY + parent.randomPosY;
			
			rotation = parent.rotation;
		}
		else
		{
			deltaX = targetX - posX;
			deltaY = targetY - posY;
			length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

			if (length < 0.15F)
				setTarget();

			velX = velX * (float) Math.pow(1 - renderer.damping, delta * 60) + deltaX / length / size * 0.00025F * speed;
			velY = velY * (float) Math.pow(1 - renderer.damping, delta * 60) + deltaY / length / size * 0.00025F * speed;

			posX += velX * delta;
			posY += velY * delta;

			rotation += rotationVel * delta;

			if (renderer.direction)
			{
				posX += 0.5F * directionVelX * delta;
				posY += 0.5F * directionVelY * delta;
			}

			if (renderer.loop)
			{
				if (posX + randomPosX < (float) -renderer.width / renderer.height - size)
				{
					posX += (float) renderer.width / renderer.height * 2 + size * 2;
					prevPosX += (float) renderer.width / renderer.height * 2 + size * 2;

					if (parent == this)
						for (int i = 1; i < renderer.groupSize && index + i < renderer.circleCount && renderer.circles[index + i] != null; ++i)
						{
							renderer.circles[index + i].posX += (float) renderer.width / renderer.height * 2 + size * 2;
							renderer.circles[index + i].prevPosX += (float) renderer.width / renderer.height * 2 + size * 2;
						}
				}
				
				if (posX + randomPosX > (float) renderer.width / renderer.height + size)
				{
					posX -=  (float) renderer.width / renderer.height * 2 + size * 2;
					prevPosX -=  (float) renderer.width / renderer.height * 2 + size * 2;

					if (parent == this)
						for (int i = 1; i < renderer.groupSize && index + i < renderer.circleCount && renderer.circles[index + i] != null; ++i)
						{
							renderer.circles[index + i].posX -= (float) renderer.width / renderer.height * 2 + size * 2;
							renderer.circles[index + i].prevPosX -= (float) renderer.width / renderer.height * 2 + size * 2;
						}
				}

				if (posY + randomPosY < -1 - size)
				{
					posY += 2 + size * 2;
					prevPosY += 2 + size * 2;

					if (parent == this)
						for (int i = 1; i < renderer.groupSize && index + i < renderer.circleCount && renderer.circles[index + i] != null; ++i)
						{
							renderer.circles[index + i].posY += 2 + size * 2;
							renderer.circles[index + i].prevPosY += 2 + size * 2;
						}	
				}

				if (posY + randomPosY > 1 + size)
				{
					posY -= 2 + size * 2;
					prevPosY -= 2 + size * 2;

					if (parent == this)
						for (int i = 1; i < renderer.groupSize && index + i < renderer.circleCount && renderer.circles[index + i] != null; ++i)
						{
							renderer.circles[index + i].posY -= 2 + size * 2;
							renderer.circles[index + i].prevPosY -= 2 + size * 2;
						}
				}
			}
		}
		
		randomPosX = randomPosX * 0.9F + (float) Math.cos(time * randomSpeedX * 2 + randomOffsetX) * randomScaleX * 0.015F;
		randomPosY = randomPosY * 0.9F + (float) Math.cos(time * randomSpeedY * 2 + randomOffsetY) * randomScaleY * 0.015F;
		
		if (flickeringTick > 0)
		{
			flickeringTick -= delta;
			
			if (lifeTime > 1 && lifeTime < maxLifeTime - 1 || !renderer.respawn)
			{
				alpha = startAlpha * ((float) Math.cos(flickeringTick / flickeringDuration * Math.PI * 2) * 0.5F + 0.5F);

				if (flickeringTick <= 0)
					alpha = startAlpha;
			}
		}
		
		if (renderer.flickering)
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
