package com.tikz.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Objects;

public abstract class MakeTikz {
    public static String convert(Array<TikTypeStruct> tikzShapes) {
        StringBuilder output = new StringBuilder();
        for (TikTypeStruct tik : tikzShapes) {
            StringBuilder extraCommands = getCommands(tik);

            switch (tik.type) {
                case LINE:
                    output.append(String.format("\\draw%s %s -- %s;\n", extraCommands, tik.origin.toString(), tik.endPoint.toString()));
                    break;
                case TEXT:
                    output.append(String.format("\\draw node at %s {%s};\n", tik.origin.toString(), tik.data));
                    break;
                case CIRCLE:
                    output.append(String.format("\\draw%s %s circle(%1.2f cm);\n", extraCommands, tik.origin.toString(),
                        tik.origin.dst(tik.endPoint)));
                    break;
                case MULTI_LINE:
                    StringBuilder poly = new StringBuilder();
                    for (Vector2 vertex : tik.vertices) {
                        poly.append(String.format("--%s", vertex.toString()));
                    }
                    poly.delete(0, 2);
                    output.append(String.format("\\draw%s %s;\n", extraCommands, poly));
                    break;
                case BEZIER:
                    int lineCount = 10;
                    StringBuilder bezier = new StringBuilder();
                    for (int i = 0; i <= lineCount; i++) {
                        float t = (float) i / lineCount;
                        Vector2 a = tik.controlPoint.cpy();
                        Vector2 b = tik.origin.cpy().sub(tik.controlPoint.cpy()).scl((1 - t) * (1 - t));
                        Vector2 c = tik.endPoint.cpy().sub(tik.controlPoint.cpy()).scl(t * t);
                        Vector2 newPoint = a.add(b).add(c);
                        bezier.append(String.format("--%s", newPoint.toString()));
                    }
                    bezier.delete(0, 2);
                    output.append(String.format("\\draw%s %s;\n", extraCommands, bezier));
                    break;
                default:
                    throw new IllegalDrawType("Unexpected DrawType: " + tik.type);
            }
        }
        return output.toString();
    }

    private static StringBuilder getCommands(TikTypeStruct tik) {
        Array<String> extraCommandsArray = new Array<>();
        if (tik.dashed) {
            extraCommandsArray.add("dashed");
        }

        if(tik.type != DrawType.CIRCLE) {
            if (tik.frontArrow && !tik.backArrow) {
                extraCommandsArray.add("->");
            } else if (!tik.frontArrow && tik.backArrow) {
                extraCommandsArray.add("<-");
            } else if (tik.frontArrow) {
                extraCommandsArray.add("<->");
            }
        }
        StringBuilder extraCommands = new StringBuilder();
        if(extraCommandsArray.notEmpty()) {
            extraCommands.append("[");
            for (String str : extraCommandsArray) {
                extraCommands.append(str);
                if (!Objects.equals(str, extraCommandsArray.peek()))
                    extraCommands.append(", ");
            }
            extraCommands.append("]");
        }
        return extraCommands;
    }

    public static void writeToFile(String path, Array<TikTypeStruct> tikzShapes) {
        String output = convert(tikzShapes);
        Gdx.files.local(path).writeString(output, false);
    }
}
