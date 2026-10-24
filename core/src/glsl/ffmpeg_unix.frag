#version 150
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
in LOWP vec4 v_color;
in vec2 v_texCoords;
uniform sampler2D u_texture;
out vec4 fragColor;

void main() {
    vec4 c4 = texture(u_texture, v_texCoords);
    fragColor = v_color * vec4(c4.b, c4.g, c4.r, c4.a);
}