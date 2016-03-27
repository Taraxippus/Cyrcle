#version 100
precision mediump float;

attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec3 a_UV;

uniform mat4 u_MVP;

varying vec4 v_Color;
varying vec3 v_UV;

void main()
{
	v_Color = a_Color;
	v_UV = a_UV;

	gl_Position = u_MVP * a_Position;
	gl_PointSize = 10.0;
}
