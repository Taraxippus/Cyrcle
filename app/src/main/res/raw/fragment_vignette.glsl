#version 100
precision mediump float;

uniform vec4 u_Color;
uniform float u_Strength;
uniform float u_Radius;

varying vec2 v_UV;

void main()
{
	gl_FragColor = u_Color * clamp(u_Strength * length(v_UV * 2.0 - vec2(1.0, 1.0)) - (1.0 - u_Radius), 0.0, 1.0);
}
