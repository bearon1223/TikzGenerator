package com.tikz.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class TikTypeStruct {
    public Vector2 origin = new Vector2();
    public Vector2 endPoint = new Vector2();
    public DrawType type;
    public String text = "";
    public Array<Vector2> vertices = new Array<>();
    public Color color = Color.BLACK;
    public Texture latexImg;
    public float upscale = 1;
    public boolean dashed = false;
    public boolean frontArrow = false;
    public boolean backArrow = false;

    public TikTypeStruct(Vector2 o, Vector2 e, DrawType type) {
        this.origin = o;
        this.endPoint = e;
        this.type = type;
    }

    public TikTypeStruct(Vector2 o, Vector2 e, DrawType type, Vector2... controlPoints) {
        this.origin = o;
        this.endPoint = e;
        this.type = type;
        for (Vector2 controlPoint : controlPoints) {
            vertices.add(controlPoint);
        }
    }

    public TikTypeStruct(Vector2 o, DrawType type, String text) {
        this.origin = o;
        this.type = type;
        this.text = text;
    }

    public TikTypeStruct(Vector2 o, DrawType type, String text, Texture latexImg, float upscale) {
        this.origin = o;
        this.type = type;
        this.text = text;
        this.latexImg = latexImg;
        this.upscale = upscale;
    }

    public TikTypeStruct(Array<Vector2> vertices, DrawType type) {
        this.vertices = vertices;
        this.type = type;
        if (type != DrawType.MULTI_LINE)
            throw new IllegalDrawType("The Multi-Line type was not used for Multi-Lines");
    }

    @Override
    public String toString() {
        return String.format("type: %s with color: %s\norigin: %s\nend: %s\ndata: %s\n" +
                "hasVertices: %b\nhasTexture: %b\nwith width / upscale: %f\n",
                type, color, origin, endPoint,
            text, !vertices.isEmpty(), latexImg != null, upscale);
    }

    public void dispose() {
        if (latexImg != null)
            latexImg.dispose();
    }
}
