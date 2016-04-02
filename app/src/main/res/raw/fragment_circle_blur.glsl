#version 100
precision mediump float;

uniform sampler2D u_Texture1;
uniform sampler2D u_Texture2;
uniform sampler2D u_Texture3;
uniform sampler2D u_Texture4;
uniform sampler2D u_Texture5;
uniform sampler2D u_Texture6;

uniform vec2 u_InvResolution;
uniform float u_Strength;
uniform float u_Radius;

varying vec4 v_Color;
varying vec3 v_UV;

void main()
{
	float blur = clamp(u_Strength * length(gl_FragCoord.xy * u_InvResolution * 2.0 - vec2(1.0, 1.0)) - (1.0 - u_Radius), 0.0, 1.0);

	if (v_UV.z == 1.0)
		gl_FragColor = (texture2D(u_Texture1, v_UV.xy) * (1.0 - blur) + texture2D(u_Texture2, v_UV.xy) * blur) * v_Color;
		
	else if (v_UV.z == 2.0)
		gl_FragColor = (texture2D(u_Texture2, v_UV.xy) * (1.0 - blur) + texture2D(u_Texture3, v_UV.xy) * blur) * v_Color;
		
	else if (v_UV.z == 4.0)
		gl_FragColor = (texture2D(u_Texture4, v_UV.xy) * (1.0 - blur) + texture2D(u_Texture5, v_UV.xy) * blur) * v_Color;
		
	else
		gl_FragColor = (texture2D(u_Texture5, v_UV.xy) * (1.0 - blur) + texture2D(u_Texture6, v_UV.xy) * blur) * v_Color;
}

