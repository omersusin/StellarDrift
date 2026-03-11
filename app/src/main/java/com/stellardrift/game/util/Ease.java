package com.stellardrift.game.util;

public final class Ease {

    private Ease() {}

    public static float outQuad(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    public static float outElastic(float t) {
        if (t == 0f || t == 1f) return t;
        float p = 0.3f;
        float s = p / 4f;
        return (float)(Math.pow(2, -10 * t) * Math.sin((t - s) * (2 * Math.PI) / p) + 1);
    }

    public static float outBack(float t, float overshoot) {
        t = t - 1f;
        return t * t * ((overshoot + 1f) * t + overshoot) + 1f;
    }

    public static float punch(float t) {
        if (t == 0f || t == 1f) return 0f;
        float decay = (float) Math.pow(2, -7 * t);
        return decay * (float) Math.sin(t * 25f);
    }

    public static float smoothStep(float t) {
        return t * t * (3f - 2f * t);
    }
}
