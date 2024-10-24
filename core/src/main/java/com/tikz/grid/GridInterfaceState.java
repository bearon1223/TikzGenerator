package com.tikz.grid;

import com.badlogic.gdx.graphics.Color;

public class GridInterfaceState {
    public static boolean addingPoints = false;
    public static float zoomLevel = 1f;
    public static boolean showGrid = true;
    public static String text = "Base Text";
    public static Color selectedColor = Color.GOLDENROD;
    public static boolean snapGrid = true;
    public static DrawType currentType = DrawType.LINE;
    public static boolean dashed = false;
    public static boolean frontArrow = false;
    public static boolean backArrow = false;
    public static int draggingState = 0;
}
