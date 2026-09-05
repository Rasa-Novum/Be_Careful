#version 150

uniform sampler2D SceneDepth;
uniform mat4 InverseViewProjection;
uniform vec3 FieldCenter;
uniform float FieldRadius;
uniform float ContactWidth;
uniform float Time;
uniform float RenderMode;
uniform vec3 CameraPosition;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec3 surfacePosition;
out vec4 fragColor;

vec3 reconstructPosition(vec2 uv, float depth) {
    vec4 clipPosition = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 position = InverseViewProjection * clipPosition;
    return position.xyz / position.w;
}

float hash3(vec3 position) {
    position = fract(position * 0.3183099 + vec3(0.1, 0.2, 0.3));
    position *= 17.0;
    return fract(position.x * position.y * position.z * (position.x + position.y + position.z));
}

float noise3(vec3 position) {
    vec3 cell = floor(position);
    vec3 local = fract(position);
    local = local * local * (3.0 - 2.0 * local);

    float c000 = hash3(cell + vec3(0.0, 0.0, 0.0));
    float c100 = hash3(cell + vec3(1.0, 0.0, 0.0));
    float c010 = hash3(cell + vec3(0.0, 1.0, 0.0));
    float c110 = hash3(cell + vec3(1.0, 1.0, 0.0));
    float c001 = hash3(cell + vec3(0.0, 0.0, 1.0));
    float c101 = hash3(cell + vec3(1.0, 0.0, 1.0));
    float c011 = hash3(cell + vec3(0.0, 1.0, 1.0));
    float c111 = hash3(cell + vec3(1.0, 1.0, 1.0));

    float x00 = mix(c000, c100, local.x);
    float x10 = mix(c010, c110, local.x);
    float x01 = mix(c001, c101, local.x);
    float x11 = mix(c011, c111, local.x);
    return mix(mix(x00, x10, local.y), mix(x01, x11, local.y), local.z);
}

float noiseMapA(vec3 position) {
    return noise3(position) * 0.65 + noise3(position * 2.0) * 0.35;
}

float noiseMapB(vec3 position) {
    return noise3(position * 1.7) * 0.55 + noise3(position * 3.4) * 0.45;
}

void main() {
    vec3 worldSurfacePosition = surfacePosition + CameraPosition;
    vec3 mapPosition = worldSurfacePosition * 0.55;
    float mapA = noiseMapA(mapPosition + vec3(Time * 0.08, -Time * 0.03, Time * 0.05));
    float mapB = noiseMapB(mapPosition + vec3(-Time * 0.05, Time * 0.09, -Time * 0.02));
    float noiseOpacity = mix(mapA, mapB, 0.5);
    noiseOpacity = smoothstep(0.18, 0.82, noiseOpacity);

    if (RenderMode < 0.5) {
        float shellAlpha = vertexColor.a * mix(0.06, 0.45, noiseOpacity);
        if (shellAlpha <= 0.001) discard;
        fragColor = vec4(vertexColor.rgb, shellAlpha);
        return;
    }

    vec2 screenUv = gl_FragCoord.xy / ScreenSize;
    float sceneDepth = texture(SceneDepth, screenUv).r;
    float contact = 0.0;
    if (sceneDepth < 0.999999) {
        vec3 scenePosition = reconstructPosition(screenUv, sceneDepth);
        float radialDistance = length(scenePosition - FieldCenter);
        float distanceFromShield = abs(radialDistance - FieldRadius);
        contact = 1.0 - smoothstep(0.0, ContactWidth, distanceFromShield);
    }

    float alpha = contact * vertexColor.a * mix(0.30, 0.95, noiseOpacity);
    if (alpha <= 0.001) discard;
    fragColor = vec4(vertexColor.rgb, alpha);
}
