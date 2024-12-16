package com.tikz.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Objects;

import static java.lang.Math.pow;

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
                    StringBuilder bezier = new StringBuilder();
                    Array<Vector2> points = getBezierPoints(tik);
                    for (Vector2 p : points) {
                        bezier.append(String.format("--%s", p.toString()));
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

        if (tik.type != DrawType.CIRCLE) {
            if (tik.frontArrow && !tik.backArrow) {
                extraCommandsArray.add("->");
            } else if (!tik.frontArrow && tik.backArrow) {
                extraCommandsArray.add("<-");
            } else if (tik.frontArrow) {
                extraCommandsArray.add("<->");
            }
        }
        StringBuilder extraCommands = new StringBuilder();
        if (extraCommandsArray.notEmpty()) {
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

    private static Array<Vector2> getBezierPoints(TikTypeStruct tik) {
        int lineCount = 23 + ProgramState.bezierControlPointCount * 2;
        Array<Vector2> outputPoints = new Array<>();
        Array<Vector2> vectors = new Array<>();
        vectors.add(tik.origin.cpy());
        for (Vector2 controlPoint : tik.vertices) {
            vectors.add(controlPoint);
        }
        vectors.add(tik.endPoint.cpy());
        int n = vectors.size - 1;

        // \sum_{i=0}^n*\frac{n!}{i!(n-i)!}(1-t)^{n-i}t^iP_i
        for (int line = 0; line <= lineCount; line++) {
            float t = (float) line / lineCount;
            Vector2 point = new Vector2();
            for (int i = 0; i <= n; i++) {
                double scl = binomialCoefficient(n, i) * pow(1 - t, n - i) * pow(t, i);
                point.add(vectors.get(i).cpy().scl((float) scl));
            }
            outputPoints.add(point);
        }

        return outputPoints;
    }

    private static int binomialCoefficient(int n, int i) {
        return factorial(n) / (factorial(i) * factorial(n - i));
    }

    private static int factorial(int n) {
        int fac = 1;
        if (n == 0)
            return 1;
        for (int i = 1; i <= n; i++) {
            fac *= i;
        }
        return fac;
    }
}
