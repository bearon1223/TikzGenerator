package com.tikz.grid;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikz.ProgramState;

import java.util.Objects;

import static java.lang.Math.pow;

public abstract class ExportToTikz {
    public static String convert(Array<TikType> tikzShapes) {
        StringBuilder output = new StringBuilder();
        for (TikType tik : tikzShapes) {
            StringBuilder extraCommands = combineModifiers(tik);

            switch (tik.type) {
                case LINE:
                    output.append(String.format("\\draw%s %s -- %s;\n", extraCommands, tik.origin.toString(), tik.endPoint.toString()));
                    break;
                case TEXT:
                    output.append(String.format("\\draw%s node at %s {%s};\n", extraCommands, tik.origin.toString(), tik.text));
                    break;
                case CIRCLE:
                    output.append(String.format("\\%s%s %s circle(%1.2f cm);\n", tik.isFilled ? "filldraw" : "draw",
                        extraCommands, tik.origin.toString(), tik.origin.dst(tik.endPoint)));
                    break;
                case MULTI_LINE:
                    StringBuilder poly = new StringBuilder();
                    for (Vector2 vertex : tik.vertices) {
                        poly.append(String.format("--%s", vertex.toString()));
                    }
                    poly.delete(0, 2);
                    output.append(String.format("\\%s%s %s;\n", tik.isFilled ? "filldraw" : "draw", extraCommands, poly));
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

    private static StringBuilder combineModifiers(TikType tik) {
        Array<String> modifiersArray = getModifiers(tik);
        StringBuilder modifiers = new StringBuilder();
        if (modifiersArray.notEmpty()) {
            modifiers.append("[");
            for (String str : modifiersArray) {
                modifiers.append(str);
                if (!Objects.equals(str, modifiersArray.peek()))
                    modifiers.append(", ");
            }
            modifiers.append("]");
        }
        return modifiers;
    }

    private static Array<String> getModifiers(TikType tik) {
        Array<String> modifiersArray = new Array<>();

        if(!tik.color.name.equalsIgnoreCase("black") || tik.color.percentValue != 1.0f) {
            if (tik.color.name.equalsIgnoreCase("black") && tik.color.percentValue == 0.5f) {
                modifiersArray.add("color = gray");
            }
            else {
                modifiersArray.add("color = " + tik.color + (tik.color.percentValue != 1.0f ? ("!" + Math.round(tik.color.percentValue * 100)) : ""));
            }
        }

        if(tik.type == DrawType.TEXT) {
            return modifiersArray;
        }

        if (tik.dashed) {
            modifiersArray.add("dashed");
        }

        if (tik.type != DrawType.CIRCLE) {
            if (tik.frontArrow && !tik.backArrow) {
                modifiersArray.add("->");
            } else if (!tik.frontArrow && tik.backArrow) {
                modifiersArray.add("<-");
            } else if (tik.frontArrow) {
                modifiersArray.add("<->");
            }
        }

        if(tik.lineThickness != DrawType.LineThickness.THIN) {
            modifiersArray.add(tik.lineThickness.toString().toLowerCase().replaceAll("_", " "));
        }
        return modifiersArray;
    }

    private static Array<Vector2> getBezierPoints(TikType tik) {
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
