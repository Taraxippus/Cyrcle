#version 100
precision mediump float;

uniform vec4 u_Color1;
uniform vec4 u_Color2;

varying vec2 v_UV;

void main()
{
	gl_FragColor = u_Color2 * v_UV.y + u_Color1 * (1.0 - v_UV.y);
}
