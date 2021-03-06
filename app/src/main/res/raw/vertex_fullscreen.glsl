#version 100
precision mediump float;

attribute vec2 a_Position;

varying vec2 v_UV;

void main()
{
	v_UV = a_Position * 0.5 + vec2(0.5);
	v_UV.y = 1.0 - v_UV.y;
	
	gl_Position = vec4(a_Position, 0.0, 1.0);
}
