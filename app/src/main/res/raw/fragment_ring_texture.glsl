#version 100
precision mediump float;

uniform float u_RingWidth;

varying vec2 v_UV;

const float c_MaxSize = 0.8;

void main()
{
	float length = length(v_UV * 2.0 - vec2(1.0, 1.0));
	gl_FragColor = length > c_MaxSize || length < c_MaxSize * (1.0 - u_RingWidth) ? vec4(0.0) : vec4(1.0);
}
