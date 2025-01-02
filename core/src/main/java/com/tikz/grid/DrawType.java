package com.tikz.grid;

public enum DrawType {
    CIRCLE,
    DROPPED_POLYGON,
    MULTI_LINE,
    LINE,
    TEXT,
    BEZIER;

    public enum LineThickness {
        ULTRA_THIN,
        VERY_THIN,
        THIN,
        THICK,
        VERY_THICK,
        ULTRA_THICK;
    }
}
