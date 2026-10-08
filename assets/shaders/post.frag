#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;

// tweak these to taste
const vec3 WARMTH = vec3(1.06, 1.0, 0.88);   // >1 red, <1 blue = warmer
const vec3 SHADOW_TINT = vec3(0.07, 0.045, 0.035); // darks lifted toward warm brown
const float SATURATION = 0.88;               // <1 = a bit washed out, filmy
const float VIGNETTE = 0.55;                 // how dark the corners get
const float GRAIN = 0.035;                   // film grain strength
const float ABERRATION = 0.0025;             // colour fringe on the edges

void main() {
    vec2 uv = v_texCoords;

    // colour fringe, stronger toward the edges
    vec2 off = (uv - 0.5) * ABERRATION;
    vec3 col = vec3(
        texture2D(u_texture, uv + off).r,
        texture2D(u_texture, uv).g,
        texture2D(u_texture, uv - off).b);

    // warm it up and lift the blacks
    col *= WARMTH;
    col = SHADOW_TINT + col * (1.0 - SHADOW_TINT);

    // a little less saturated
    float grey = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(grey), col, SATURATION);

    // soft dark corners
    float d = distance(uv, vec2(0.5));
    col *= 1.0 - VIGNETTE * smoothstep(0.3, 0.8, d);

    // grain, changes every frame
    float n = fract(sin(dot(gl_FragCoord.xy + u_time * 61.0, vec2(12.9898, 78.233))) * 43758.5453);
    col += (n - 0.5) * GRAIN;

    gl_FragColor = vec4(col, 1.0);
}
