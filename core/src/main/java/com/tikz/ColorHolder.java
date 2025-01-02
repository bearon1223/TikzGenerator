package com.tikz;

import com.badlogic.gdx.graphics.Color;

public class ColorHolder {
    public Color color;
    public String name;

    public ColorHolder(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    @Override
    public String toString() {
        return name.toLowerCase();
    }
}
