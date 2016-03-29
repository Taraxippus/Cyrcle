#version 100
precision mediump float;

varying vec2 v_UV;

const float c_MaxSize = 0.8;

void main()
{
	gl_FragColor = length(v_UV * 2.0 - vec2(1.0, 1.0)) > c_MaxSize ? vec4(0.0) : vec4(1.0);
}
