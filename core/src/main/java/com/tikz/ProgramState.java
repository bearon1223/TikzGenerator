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
        new ColorHolder(new Color(0x000000ff), "Black"  , 1.0f),
        new ColorHolder(new Color(0x808080ff), "Gray"   , 1.0f),
        new ColorHolder(new Color(0xff0101ff), "Red"    , 1.0f),
        new ColorHolder(new Color(0x01ff01ff), "Green"  , 1.0f),
        new ColorHolder(new Color(0x0101ffff), "Blue"   , 1.0f),
        new ColorHolder(new Color(0xff8001ff), "Orange" , 1.0f),
        new ColorHolder(new Color(0xffeb3dff), "Yellow" , 1.0f),
        new ColorHolder(new Color(0x01b9f2ff), "Cyan"   , 1.0f),
        new ColorHolder(new Color(0xfb3199ff), "Magenta", 1.0f),
    };
}
