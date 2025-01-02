package com.tikz.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.tikz.ColorHolder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportFromTikz {
    /**
     * Converts Tikz code generated by this program to a format the code can understand and draw.
     *
     * @param tik tikz code
     * @return Array of Tikz Points in Grid Interface Format
     * @throws GdxRuntimeException   Throws Malformed Vector
     * @throws NullPointerException  Parsing Float for circles failed
     * @throws NumberFormatException Parsing Float for circles failed
     * @throws IllegalDrawType       Unknown Draw Code
     */
    public static Array<TikType> FromTikToPoints(String tik, float scale, float rotationDeg) throws GdxRuntimeException, NullPointerException, NumberFormatException, IllegalDrawType, IllegalUnitType {
        Array<TikType> points = new Array<>();
        String[] commands = tik.split("\\n+");
        for (String command : commands) {
            if(command.contains("%")) {
                System.out.printf("Commented Code: %s\n", command);
                continue;
            }
            ColorHolder tikColor = ProgramState.colors[0];
            boolean isFilled = false;
            if(command.contains("filldraw")) {
                command = command.replaceAll("\\s*\\\\filldraw\\s*", "");
                isFilled = true;
            } else
                command = command.replaceAll("\\s*\\\\draw\\s*", "");
            boolean isDashed = command.contains("dashed");
            boolean frontArrow = command.contains(">");
            boolean backArrow = command.contains("<");
            boolean hasColor = command.contains("color");
            if(hasColor) {
                String regex = "color\\s*=\\s*(\\w+)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(command);
                if (matcher.find()) {
                    for(int i = 0; i < ProgramState.colors.length; i++) {
                        if(matcher.group(1).equalsIgnoreCase(ProgramState.colors[i].name)) {
                            tikColor = ProgramState.colors[i];
                        }
                    }
                }
            }
            command = command.replaceAll("\\[(.*?)]", "").replaceAll(";", "");
            if (command.contains("node at")) {
                String regex = "\\(([^)]+)\\)\\s*\\{((?:\\$[^$]+\\$|[^{}])+)}";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(command);
                if (matcher.find()) {
                    String vector = matcher.group(1);
                    vector = vector.replaceAll(",\\s*", ", ").trim();
                    Vector2 loc = new Vector2().fromString("(" + vector + ")").scl(scale).rotateDeg(rotationDeg);
                    String content = matcher.group(2);
                    points.add(new TikType(loc, DrawType.TEXT, content));
                } else {
                    throw new IllegalDrawType(String.format("Error: '%s' is not a valid command understood by this program!" +
                        "\n\t(Commands could not be parsed)\n", command));
                }
            } else if (command.contains("circle")) {
                // (a, b) circle(2.0cm);
                String regex = "\\(([^)]+)\\)\\s*circle\\s*\\((\\d+[.]?\\d*)\\s*([^}]+)?\\)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(command);
                if (matcher.find()) {
                    String vector = matcher.group(1);
                    vector = vector.replaceAll(",\\s*", ", ").trim();
                    Vector2 loc = new Vector2().fromString("(" + vector + ")").scl(scale).rotateDeg(rotationDeg);;
                    float radius = Float.parseFloat(matcher.group(2)) / getConversion(matcher.group(3)) * scale;
                    TikType tikType = new TikType(loc, loc.cpy().add(radius, 0), DrawType.CIRCLE);
                    tikType.dashed = isDashed;
                    tikType.color = tikColor;
                    tikType.isFilled = isFilled;
                    points.add(tikType);
                } else {
                    throw new IllegalDrawType(String.format("Error: '%s' is not a valid command understood by this program!" +
                        "\n\t(Commands could not be parsed)\n", command));
                }
            } else if (command.contains("arc")) {
                throw new IllegalDrawType("Arcs are unable to be rendered as they are unpredictable: " + command);
            } else {
                String[] vectors = command.split("\\s*--\\s*");
                for (int i = 0; i < vectors.length; i++) {
                    vectors[i] = vectors[i].replaceAll(",\\s*", ", ").trim();
                }
                if (vectors.length == 2) {
                    Vector2 start = new Vector2().fromString(vectors[0]).scl(scale).rotateDeg(rotationDeg);
                    Vector2 end = new Vector2().fromString(vectors[1]).scl(scale).rotateDeg(rotationDeg);
                    TikType tikType = new TikType(start, end, DrawType.LINE);
                    tikType.dashed = isDashed;
                    tikType.frontArrow = frontArrow;
                    tikType.backArrow = backArrow;
                    tikType.color = tikColor;
                    points.add(tikType);
                } else if (vectors.length > 2) {
                    Array<Vector2> vector2Array = new Array<>();
                    for (String v : vectors) {
                        v = v.trim();
                        vector2Array.add(new Vector2().fromString(v).scl(scale).rotateDeg(rotationDeg));
                    }
                    TikType tikType = new TikType(vector2Array, DrawType.MULTI_LINE);
                    tikType.dashed = isDashed;
                    tikType.frontArrow = frontArrow;
                    tikType.backArrow = backArrow;
                    tikType.color = tikColor;
                    tikType.isFilled = isFilled;
                    points.add(tikType);
                }
            }
        }
        return points;
    }

    /**
     * Converts a list of vectors into a polygon usable by this program
     *
     * @param vectorInput List of vectors
     * @param scale       How much to scale the output by
     * @param rotationDeg how much to rotate by in the clockwise direction
     * @return Array of Tikz Points in Grid Interface Format
     * @throws GdxRuntimeException   Throws Malformed Vector
     * @throws NullPointerException  Parsing Float for vectors failed
     * @throws NumberFormatException Parsing Float for vectors failed
     * @throws IllegalDrawType       Unknown Draw Code
     */
    public static TikType FromVectorsToPoints(String vectorInput, float scale, float rotationDeg) throws GdxRuntimeException, NullPointerException, NumberFormatException, IllegalDrawType {
        Array<Vector2> vectors = new Array<>();
        String[] vectorStrings = vectorInput.replace("(", "").replace(")", "").split("\\n+");
        for (String v : vectorStrings) {
            if (v.isBlank()) continue;
            v = v.trim();
            // Split the numbers into two at the whitespace
            String[] splitVectorString;
            if (v.contains(",")) {
                splitVectorString = v.split(",");
                for (int i = 0; i < splitVectorString.length; i++) {
                    splitVectorString[i] = splitVectorString[i].trim();
                }
            } else {
                splitVectorString = v.split("\\s+");
            }

            if (splitVectorString.length != 2) throw new GdxRuntimeException("Malformed Vector " + v);
            String stringVector = "(" + splitVectorString[0] + "," + splitVectorString[1] + ")";

            vectors.add(new Vector2().fromString(stringVector).scl(scale).rotateDeg(rotationDeg));
        }
        TikType tikType = new TikType(vectors, DrawType.MULTI_LINE);
        tikType.isFilled = ProgramState.isFilled;
        return tikType;
    }

    public static float getConversion(String unit) throws IllegalUnitType {
        float pt = 28.45274f;   // cm / pt
        float mm = 10;          // cm / mm
        float cm = 1;           // cm / cm
        float ex = 6.6084f;     // cm / ex
        float em = 2.84528f;    // cm / em
        float bp = 28.34677f;   // cm / bp
        float dd = 26.59117f;   // cm / dd
        float pc = 2.37106f;    // cm / pc
        float in = 0.3927f;     // cm / in
        if(unit == null) {
            return cm;
        }
        switch (unit.trim()) {
            case "pt":
                return pt;
            case "mm":
                return mm;
            case "cm":
                return cm;
            case "ex":
                return ex;
            case "em":
                return em;
            case "bp":
                return bp;
            case "dd":
                return dd;
            case "pc":
                return pc;
            case "in":
                return in;
            default:
                throw new IllegalUnitType("Unexpected Unit Type: " + unit);
        }
    }

    public static class IllegalUnitType extends Exception {
        public IllegalUnitType(String error) {
            super(error);
        }
    }
}
