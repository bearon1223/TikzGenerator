package com.tikz;

import com.badlogic.gdx.graphics.Color;

public class ColorHolder implements Cloneable{
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

    @Override
    public String toString() {
        return name.toLowerCase();
    }

    @Override
    public ColorHolder clone() {
        try {
            ColorHolder clone = (ColorHolder) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.color = color.cpy();
            clone.r = clone.color.r;
            clone.g = clone.color.g;
            clone.b = clone.color.b;
            clone.name = name;
            clone.percentValue = percentValue;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
