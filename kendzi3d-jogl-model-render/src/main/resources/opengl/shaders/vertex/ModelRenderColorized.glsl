#version 120

// position is 0, colour is currently position 1. This will probably change (position, normals, colours, texture)
uniform vec3 inPosition;
uniform vec3 inNormal;
uniform vec3 inColour;
uniform vec2 inTexture;

varying vec3 outColour;

void main() {
    gl_Position = vec4(inPosition, 1.0f);
    outColour = inColour;
}
