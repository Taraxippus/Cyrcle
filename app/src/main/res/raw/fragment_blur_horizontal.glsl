#version 100
precision mediump float;

uniform sampler2D u_Texture;
uniform vec2 u_InvResolution;
uniform float u_BlurSize;

varying vec2 v_UV;

void main()
{
	gl_FragColor = texture2D(u_Texture, v_UV - vec2(u_BlurSize, 0.0) * u_InvResolution) * 0.06136;
	gl_FragColor += texture2D(u_Texture, v_UV - vec2(u_BlurSize * 0.5, 0.0) * u_InvResolution) * 0.24477;
	gl_FragColor += texture2D(u_Texture, v_UV) * 0.38774;
	gl_FragColor += texture2D(u_Texture, v_UV + vec2(u_BlurSize * 0.5, 0.0) * u_InvResolution) * 0.24477;
	gl_FragColor += texture2D(u_Texture, v_UV + vec2(u_BlurSize, 0.0) * u_InvResolution) * 0.06136;
}
