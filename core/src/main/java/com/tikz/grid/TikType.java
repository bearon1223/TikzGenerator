package com.tikz.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TikType {
    public Vector2 origin = new Vector2();
    public Vector2 endPoint = new Vector2();
    public DrawType type;
    public String text = "";
    public Array<Vector2> vertices = new Array<>();
    public short[] triangleLocations;
    float[] flatVertices;
    public Color color = Color.BLACK;
    public Texture latexImg;
    public float upscale = 1;
    public boolean dashed = false;
    public boolean frontArrow = false;
    public boolean backArrow = false;
    public boolean isFilled = false;

    public TikType(Vector2 o, Vector2 e, DrawType type) {
        this.origin = o;
        this.endPoint = e;
        this.type = type;
    }

    public TikType(Vector2 o, Vector2 e, DrawType type, Vector2... controlPoints) {
        this.origin = o;
        this.endPoint = e;
        this.type = type;
        for (Vector2 controlPoint : controlPoints) {
            vertices.add(controlPoint);
        }
    }

    public TikType(Vector2 o, DrawType type, String text) {
        this.origin = o;
        this.type = type;
        this.text = text;
    }

    public TikType(Vector2 o, DrawType type, String text, Texture latexImg, float upscale) {
        this.origin = o;
        this.type = type;
        this.text = text;
        this.latexImg = latexImg;
        this.upscale = upscale;
    }

    public TikType(Array<Vector2> vertices, DrawType type) {
        this.vertices = vertices;
        this.type = type;
        if (type != DrawType.MULTI_LINE)
            throw new IllegalDrawType("The Multi-Line type was not used for Multi-Lines");
    }

    public void triangulate() {
        if(vertices.size < 3) {
            throw new GdxRuntimeException("There must be more that 3 vertices");
        }

        flatVertices = new float[vertices.size * 2];
        for (int i = 0; i < vertices.size; i++) {
            flatVertices[i * 2] = vertices.get(i).x;
            flatVertices[i * 2 + 1] = vertices.get(i).y;
        }

        EarClippingTriangulator triangulator = new EarClippingTriangulator();
        triangleLocations = triangulator.computeTriangles(flatVertices).toArray();
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
