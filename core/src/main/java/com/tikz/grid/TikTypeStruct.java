package com.tikz.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;

public class TikTypeStruct {
    public Vector2 origin = new Vector2();
    public Vector2 endPoint = new Vector2();
    public float[] angles = {0f, 360f};
    public DrawType type;
    public String data = "";
    public Array<Vector2> vertices = new Array<>();
    public Color color = Color.GOLDENROD;
    public Texture latexImg;
    /**
     * for images this is upscale. For arcs, this is radius/width
     */
    public float numericalData = 1;
    public float arcHeight = 1;
    public boolean dashed = true;
    public boolean frontArrow = false;
    public boolean backArrow = true;

    public TikTypeStruct(Vector2 o, Vector2 e, DrawType type) {
        this.origin = o;
        this.endPoint = e;
        this.type = type;
    }

    public TikTypeStruct(Vector2 o, float radius, float[] angles, DrawType type) {
        this.origin = o;
        this.numericalData = radius;
        this.arcHeight = radius;
        this.type = type;
        this.angles = angles;
    }

    public TikTypeStruct(Vector2 o, float width, float height, float[] angles, DrawType type) {
        this.origin = o;
        this.numericalData = width;
        this.arcHeight = height;
        this.type = type;
        this.angles = angles;
    }

    public TikTypeStruct(Vector2 o, DrawType type, String data) {
        this.origin = o;
        this.type = type;
        this.data = data;
    }

    public TikTypeStruct(Vector2 o, DrawType type, String data, Texture latexImg, float upscale) {
        this.origin = o;
        this.type = type;
        this.data = data;
        this.latexImg = latexImg;
        this.numericalData = upscale;
    }

    public TikTypeStruct(Array<Vector2> vertices, DrawType type) {
        this.vertices = vertices;
        this.type = type;
        if (type != DrawType.MULTI_LINE)
            throw new IllegalDrawType("The Polygon type was not used for polygons");
    }

    @Override
    public String toString() {
        return String.format("type: %s with color: %s\norigin: %s\nend: %s\nangles: %s\ndata: %s\n" +
            "hasVertices: %b\nhasTexture: %b\nwith width / upscale: %f\n" +
                "with height: %f", type, color, origin, endPoint,
            Arrays.toString(angles), data, !vertices.isEmpty(), latexImg != null, numericalData, arcHeight);
    }

    public void dispose() {
        if(latexImg != null)
            latexImg.dispose();
    }
}
