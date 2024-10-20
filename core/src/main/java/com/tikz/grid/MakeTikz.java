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
        extraCommands.append("[");
        for(String str : extraCommandsArray){
            extraCommands.append(str);
            if(!Objects.equals(str, extraCommandsArray.peek()))
                extraCommands.append(", ");
        }
        extraCommands.append("]");
        return extraCommands;
    }

    public static void writeToFile(String path, Array<TikTypeStruct> tikzShapes) {
        String output = convert(tikzShapes);
        Gdx.files.local(path).writeString(output, false);
    }
}
