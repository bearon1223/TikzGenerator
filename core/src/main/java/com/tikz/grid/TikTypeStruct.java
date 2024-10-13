package com.tikz.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class TikTypeStruct {
    public Vector2 origin = new Vector2();
    public Vector2 endPoint = new Vector2();
    public DrawType type;
    public String data = "";
    public Array<Vector2> vertices = new Array<>();
    public Color color = Color.WHITE;
    public Texture latexImg;
    public float upscale = 1;

    public TikTypeStruct(Vector2 o, Vector2 e, DrawType type) {
        this.origin = o;
        this.endPoint = e;
        this.type = type;
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
        this.upscale = upscale;
    }

    public TikTypeStruct(Array<Vector2> vertices, DrawType type) {
        this.vertices = vertices;
        this.type = type;
        if (type != DrawType.FILLED_POLYGON && type != DrawType.DOTTED_POLYGON)
            throw new IllegalDrawType("The Polygon type was not used for polygons");
    }

    public void dispose() {
        if(latexImg != null)
            latexImg.dispose();
    }
}
