#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
uniform sampler2D Sampler0;
layout(location = 2) in vec4 vertexColor;
layout(location = 3) in vec2 texCoord0;
layout(location = 0) out vec4 fragColor;
void main() {
    vec4 color = vertexColor * ColorModulator;
    color.a *= texture(Sampler0, texCoord0).r;
    if (color.a < 1.0 / 255.0) discard;
    fragColor = color;
}
