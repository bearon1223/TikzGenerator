package com.tikz.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

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
    public static Array<TikTypeStruct> FromTikToPoints(String tik) throws GdxRuntimeException, NullPointerException, NumberFormatException, IllegalDrawType {
        Array<TikTypeStruct> points = new Array<>();
        String[] commands = tik.replace("\n", "").split(";");
        int n = 0;
        for (String command : commands) {
            // remove tikz draw command and the end draw command
            command = command.replace("\\draw ", "");
            command = command.replace("\\draw", "");
            command = command.replace(";", "");
            command = command.trim();
            Gdx.app.log("Import", n++ + " " + command);
            if (command.contains("--")) {
                // tackle everything that is a line
                String[] stringVectors = command.split("--");
                for (int i = 0; i < stringVectors.length; i++) {
                    stringVectors[i] = stringVectors[i].trim();
                }

                if (stringVectors.length == 2) {
                    // dashed lines
                    if (command.contains("[dashed]")) {
                        // delete the dashed stuff
                        String str1 = stringVectors[0].replace("[dashed] ", "").replace("[dashed]", "");
                        String str2 = stringVectors[1];

                        Vector2 origin = new Vector2().fromString(str1.trim());
                        Vector2 endPoint = new Vector2().fromString(str2.trim());

                        points.add(new TikTypeStruct(origin, endPoint, DrawType.DOTTED_LINE));
                    } else if (command.contains("[thin, ->]") || command.contains("[thick,->]")) {
                        String str1 = stringVectors[0].replace("[thin, ->] ", "").replace("[thick,->]", "");
                        String str2 = stringVectors[1];

                        Vector2 origin = new Vector2().fromString(str1.trim());
                        Vector2 endPoint = new Vector2().fromString(str2.trim());

                        points.add(new TikTypeStruct(origin, endPoint, DrawType.ARROW));
                    } else if (command.contains("[thin, <->]") || command.contains("[thin,<->]")) {
                        String str1 = stringVectors[0].replace("[thin, <->] ", "").replace("[thin,<->]", "");
                        String str2 = stringVectors[1];

                        Vector2 origin = new Vector2().fromString(str1.trim());
                        Vector2 endPoint = new Vector2().fromString(str2.trim());

                        points.add(new TikTypeStruct(origin, endPoint, DrawType.DOUBLE_ARROW));
                    } else {
                        // Line Case
                        Vector2 origin = new Vector2().fromString(stringVectors[0].trim());
                        Vector2 endPoint = new Vector2().fromString(stringVectors[1].trim());

                        points.add(new TikTypeStruct(origin, endPoint, DrawType.LINE));
                    }
                } else {
                    // POLYGON case (for every vector, try to convert it
                    String[] vectors = command.trim().split("--");
                    Array<Vector2> vs = new Array<>();
                    for (String v : vectors) {
                        v = v.trim();
                        vs.add(new Vector2().fromString(v));
                    }
                    points.add(new TikTypeStruct(vs, DrawType.POLYGON));
                }
            } else if (command.contains("node at")) {
                // Text Case
                String[] str2 = command.replace("node at ", "").split(" ");
                // get the location
                Vector2 loc = new Vector2().fromString(str2[0].trim());

                // make the text string
                StringBuilder text = new StringBuilder();

                for (int i = 1; i < str2.length - 1; i++) {
                    text.append(str2[i].replace("{", "").replace("}", ""));
                    text.append(" ");
                }
                // append the last bit
                text.append(str2[str2.length - 1].replace("{", "").replace("}", ""));

                // add it to points
                points.add(new TikTypeStruct(loc, DrawType.TEXT, text.toString()));
            } else if (command.contains("circle")) {
                System.out.println("\tCircle Implementation is WIP");

                // split everything
                String[] circleStrings = command.split("circle");

                // get the center, and the edge and remove the tik stuff
                Vector2 center = new Vector2().fromString(circleStrings[0].trim());
                circleStrings[1] = circleStrings[1].replace("(", "").replace("cm)", "");

                // get the edge and use it as a radius
                Vector2 dist = center.cpy().add(Float.parseFloat(circleStrings[1].trim()), 0);

                // add it to points
                points.add(new TikTypeStruct(center, dist, DrawType.CIRCLE));
            } else {
                throw new IllegalDrawType("Unknown Tikz Code ("+command+")");
            }
        }
        return points;
    }
}
