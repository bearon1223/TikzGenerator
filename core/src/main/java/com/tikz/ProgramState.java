package com.tikz;

import com.badlogic.gdx.graphics.Color;
import com.tikz.grid.DrawType;

public class ProgramState {
    public static boolean addingPoints = false;
    public static float zoomLevel = 1f;
    public static boolean showGrid = true;
    public static String text = "Insert Text";
    public static boolean snapGrid = true;
    public static DrawType currentType = DrawType.LINE;
    public static boolean dashed = false;
    public static boolean frontArrow = false;
    public static boolean backArrow = false;
    public static int draggingState = 0;
    public static int bezierControlPointCount = 1;
    public static boolean lightMode = false;
    public static boolean isFilled = false;
    public static boolean wireframe = false;

    // default is the thin line type
    public static DrawType.LineThickness lineThickness = DrawType.LineThickness.THIN;

    public static int colorIndex = 0;
    public static final ColorHolder[] colors = {
        new ColorHolder(new Color(0x000000ff), "Black"),
        new ColorHolder(new Color(0x808080ff), "Gray"),
        new ColorHolder(new Color(0xd93122ff), "Red"),
        new ColorHolder(new Color(0x3716f5ff), "Blue"),
        new ColorHolder(new Color(0x8dfb4aff), "Green"),
        new ColorHolder(new Color(0xe18631ff), "Orange"),
        new ColorHolder(new Color(0xf6eb5fff), "Yellow"),
        new ColorHolder(new Color(0x70b7edff), "Cyan"),
        new ColorHolder(new Color(0xfc2f99ff), "Magenta"),
    };
    public static ColorHolder selectedColor = colors[colorIndex];
}
