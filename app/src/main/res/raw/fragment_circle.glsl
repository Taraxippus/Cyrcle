#version 100
precision mediump float;

uniform sampler2D u_Texture1;
uniform sampler2D u_Texture2;
uniform sampler2D u_Texture3;
uniform sampler2D u_Texture4;

varying vec4 v_Color;
varying vec3 v_UV;

void main()
{
	if (v_UV.z == 1.0)
		gl_FragColor = texture2D(u_Texture1, v_UV.xy) * v_Color;
		
	else if (v_UV.z == 2.0)
		gl_FragColor = texture2D(u_Texture2, v_UV.xy) * v_Color;
		
	else if (v_UV.z == 3.0)
		gl_FragColor = texture2D(u_Texture3, v_UV.xy) * v_Color;
		
	else
		gl_FragColor = texture2D(u_Texture4, v_UV.xy) * v_Color;
	
}

