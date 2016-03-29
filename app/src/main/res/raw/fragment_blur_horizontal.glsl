#version 100
precision mediump float;

uniform sampler2D u_Texture;
uniform vec2 u_InvResolution;
uniform float u_BlurSize;

varying vec2 v_UV;

void main()
{
	for (float i = -u_BlurSize; i <= u_BlurSize; ++i)
		gl_FragColor += texture2D(u_Texture, vec2(v_UV.x + u_InvResolution.x * i, v_UV.y));
		
	gl_FragColor /= u_BlurSize * 2.0;
}
