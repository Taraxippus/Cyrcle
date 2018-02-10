#version 100
precision mediump float;

uniform sampler2D u_Texture;
uniform vec2 u_Scale;
varying vec2 v_UV;

void main()
{
	gl_FragColor = texture2D(u_Texture, 0.5 * (v_UV * 2.0 - vec2(1.0)) / u_Scale.xy + vec2(0.5));
}
