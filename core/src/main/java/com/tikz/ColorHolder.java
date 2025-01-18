package com.tikz;

import com.badlogic.gdx.graphics.Color;

public class ColorHolder {
    public Color color;
    public String name;
    public float percentValue;
    public float r, g, b;

    public ColorHolder(Color color, String name, float percentValue) {
        this.color = color.cpy();
        r = color.r;
        g = color.g;
        b = color.b;
        this.name = name;
        this.percentValue = percentValue;
    }

    public ColorHolder(ColorHolder other) {
        color = other.color.cpy();
        r = color.r;
        g = color.g;
        b = color.b;
        name = other.name;
        percentValue = other.percentValue;
    }

    @Override
    public String toString() {
        return name.toLowerCase();
    }
}
