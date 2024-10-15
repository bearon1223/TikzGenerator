package com.tikz.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public abstract class MakeTikz {
    public static String convert(Array<TikTypeStruct> tikzShapes) {
        StringBuilder output = new StringBuilder();
        for (TikTypeStruct tik : tikzShapes) {
            switch (tik.type) {
                case LINE:
                    output.append(String.format("\\draw %s -- %s;\n", tik.origin.toString(), tik.endPoint.toString()));
                    break;
                case TEXT:
                    output.append(String.format("\\draw node at %s {%s};\n", tik.origin.toString(), tik.data));
                    break;
                case ARROW:
                    output.append(String.format("\\draw[thin, ->] %s -- %s;\n", tik.origin.toString(),
                        tik.endPoint.toString()));
                    break;
                case DOTTED_LINE:
                    output.append(String.format("\\draw[dashed] %s -- %s;\n", tik.origin.toString(),
                        tik.endPoint.toString()));
                    break;
                case DOUBLE_ARROW:
                    output.append(String.format("\\draw[thin, <->] %s -- %s;\n", tik.origin.toString(),
                        tik.endPoint.toString()));
                    break;
                case CIRCLE:
                    output.append(String.format("\\draw %s circle(%1.2f cm);\n", tik.origin.toString(),
                        tik.origin.dst(tik.endPoint)));
                    break;
                case DOTTED_POLYGON:
                case FILLED_POLYGON:
                    StringBuilder poly = new StringBuilder();
                    for (Vector2 vertex : tik.vertices) {
                        poly.append(String.format("--%s", vertex.toString()));
                    }
                    poly.delete(0, 2);
                    output.append(String.format("\\draw%s %s;\n",tik.type == DrawType.DOTTED_POLYGON ? "[dashed]" : "", poly));
                    break;
                case CIRCULAR_ARC:
                    output.append(String.format("\\draw %s arc(%1.2f:%1.2f:%1.2f);\n", tik.origin, tik.angles[0], tik.angles[1], tik.numericalData));
                    break;
                case ELLIPTICAL_ARC:
                    output.append(String.format("\\draw %s arc(%1.2f:%1.2f:%1.2f and %1.2f);\n", tik.origin, tik.angles[0], tik.angles[1], tik.numericalData, tik.arcHeight));
                    break;
                default:
                    throw new IllegalDrawType("Unexpected DrawType: " + tik.type);
            }
        }
        return output.toString();
    }

    public static void writeToFile(String path, Array<TikTypeStruct> tikzShapes) {
        String output = convert(tikzShapes);
        Gdx.files.local(path).writeString(output, false);
    }
}
