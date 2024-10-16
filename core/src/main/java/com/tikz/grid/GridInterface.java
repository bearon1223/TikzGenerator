package com.tikz.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tikz.Main;
import com.tikz.MainScreen;
import org.scilab.forge.jlatexmath.ParseException;

import static java.lang.Math.*;

public class GridInterface {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    private final Main app;
    public float gridSpacing = Math.min((float) Gdx.graphics.getHeight() / ROWS, (float) Gdx.graphics.getWidth() / COLS);
    public float scaling = 1;
    public Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    public boolean addingPoints = false;
    public String text = "Base Text";
    public MainScreen screen;
    public Array<TikTypeStruct> points = new Array<>();
    public TikTypeStruct editing;
    public float zoomLevel = 1f;
    public Vector2 panning = new Vector2();
    public boolean showGrid = true;
    private boolean snapGrid = true;
    private DrawType currentType = DrawType.LINE;
    private float centerOffset = 0f;
    private final float lineWidth = 2f;
    private int arcDrawState = 0;
    public Color selectedColor = Color.GOLDENROD;

    public GridInterface(MainScreen screen, Main app) {
        this.app = app;
        this.screen = screen;
    }

    /**
     * clamps the value of x to be within the minimum and maximum values
     *
     * @param x   value to be clamped
     * @param min minimum value
     * @param max maximum value
     * @return value bound by min and max.
     */
    public static float clamp(float x, float min, float max) {
        return x < min ? min : Math.min(x, max);
    }

    public DrawType getDrawType() {
        return currentType;
    }

    public void setDrawType(DrawType type) {
        this.currentType = type;
    }

    public void drawGrid(ShapeRenderer renderer) {
        // set the center location. Center of the screen minus the pan location and adding the menu if it's there
        centerOffset = screen.t.getWidth() / 2f + screen.tableOffset / 2;
        final Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2f + centerOffset,
            Gdx.graphics.getHeight() / 2f).sub(panning);
        renderer.set(ShapeRenderer.ShapeType.Filled);

        // set the gridSpacing and scaling
        gridSpacing = Math.min((float) Gdx.graphics.getHeight() / ROWS, (float) Gdx.graphics.getWidth() / COLS);
        scaling = gridSpacing / Math.min(1200f / (float) ROWS, 800f / (float) COLS);

        // set the zoom
        gridSpacing *= zoomLevel;

        if (showGrid) {
            final int viewable = 7;
            int count = 10;
            int min = -Math.round(viewable / zoomLevel) + (int) (panning.x / gridSpacing);
            int max = Math.round(viewable / zoomLevel) + (int) (panning.x / gridSpacing);
            // draw small lines
            // Vertical Lines
            if (zoomLevel < 0.5f) {
                count = 4;
            }
            for (int i = min; i <= max - 1; i++) {  // row
                for (int j = 0; j < count; j++) {
                    renderer.setColor(Color.GRAY);
                    renderer.rectLine(new Vector2(center.x + gridSpacing * i + gridSpacing / count * j, 0),
                        new Vector2(center.x + gridSpacing * i + gridSpacing / count * j, Gdx.graphics.getHeight()), 1f);
                }
            }

            // Horizontal Lines
            min = -Math.round(viewable / 1.25f / zoomLevel) + (int) (panning.y / gridSpacing);
            max = Math.round(viewable / 1.25f / zoomLevel) + (int) (panning.y / gridSpacing);
            for (int i = min; i <= max - 1; i++) {  // row
                for (int j = 0; j < count; j++) {
                    renderer.setColor(Color.GRAY);
                    renderer.rectLine(new Vector2(0, center.y + gridSpacing * i + gridSpacing / count * j),
                        new Vector2(Gdx.graphics.getWidth(), center.y + gridSpacing * i + gridSpacing / count * j), 1f);
                }
            }
            for (int i = min; i <= max; i++) {
                renderer.setColor(Color.LIGHT_GRAY);
                renderer.rectLine(new Vector2(0, center.y + gridSpacing * i),
                    new Vector2(Gdx.graphics.getWidth(), center.y + gridSpacing * i), 2f);
            }
            min = -Math.round(viewable / zoomLevel) + (int) (panning.x / gridSpacing);
            max = Math.round(viewable / zoomLevel) + (int) (panning.x / gridSpacing);
            // draw big lines
            for (int i = min; i <= max; i++) {
                renderer.setColor(Color.LIGHT_GRAY);
                renderer.rectLine(new Vector2(center.x + gridSpacing * i, 0),
                    new Vector2(center.x + gridSpacing * i, Gdx.graphics.getHeight()), 2f);
            }
        }

        drawTikz();

        // Clear all points
        if (screen.notTyping() && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
            && Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            points.clear();
        }

        if (showGrid) {
            renderer.setColor(Color.GOLD);
            renderer.circle(center.x, center.y, max(2f*scaling, 2));
        }

        renderer.setColor(selectedColor);
        renderer.circle(mouse.x * gridSpacing + center.x, mouse.y * gridSpacing + center.y, 2f);

        renderAllPoints(renderer, center);

        // Ctrl-Z to remove the latest point
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && points.size > 0) {
                points.removeIndex(points.size - 1);
            }
        }

        renderEditingTik(renderer, center);
    }

    private void renderAllPoints(ShapeRenderer renderer, Vector2 center) {
        // render every point
        for (TikTypeStruct tik : points) {
            Vector2 o = new Vector2();
            Vector2 e = new Vector2(-1, -1);
            if (tik.type != DrawType.FILLED_POLYGON) {
                o = tik.origin.cpy().scl(gridSpacing).add(center);
                e = tik.endPoint.cpy().scl(gridSpacing).add(center);
            }
            renderTikz(tik, tik.type, renderer, o, e, center);
        }
    }

    private void renderEditingTik(ShapeRenderer renderer, Vector2 center) {
        // render the editing point
        if (editing != null && addingPoints) {
            Vector2 o = new Vector2();
            Vector2 e = new Vector2();
            if (currentType != DrawType.FILLED_POLYGON) {
                editing.endPoint = mouse.cpy();
                o = editing.origin.cpy().scl(gridSpacing).add(center);
                e = editing.endPoint.cpy().scl(gridSpacing).add(center);
            }
            if (currentType != DrawType.TEXT) {
                renderTikz(editing, currentType, renderer, o, e, center);
                if (currentType == DrawType.FILLED_POLYGON) {
                    Vector2 vPres = editing.vertices.peek().cpy().scl(gridSpacing).add(center);
                    renderer.rectLine(vPres, mouse.cpy().scl(gridSpacing).add(center), 2f * scaling);
                } else if (currentType == DrawType.DOTTED_POLYGON) {
                    Vector2 vPres = editing.vertices.peek().cpy().scl(gridSpacing).add(center);
                    drawDottedLine(renderer, vPres.x, vPres.y, mouse.cpy().scl(gridSpacing).add(center).x, mouse.cpy().scl(gridSpacing).add(center).y, 20f);
                }
            } else {
                editing = new TikTypeStruct(mouse, currentType, text, editing.latexImg, editing.numericalData);
                editing.color = selectedColor;
                o = editing.origin.cpy().scl(gridSpacing).add(center);
                renderTikz(editing, currentType, renderer, o, e, center);
            }
        }
    }

    private void renderTikz(TikTypeStruct tik, DrawType type, ShapeRenderer renderer, Vector2 o, Vector2 e, Vector2 center) {
        renderer.setColor(tik.color);
        switch (type) {
            case LINE:
                renderer.rectLine(o, e, Math.max(lineWidth * scaling * zoomLevel, 1));
                break;
            case CIRCLE:
                drawCircle(renderer, o.x, o.y, o.dst(e));
                break;
            case ARROW:
                drawArrow(renderer, o.x, o.y, e.x, e.y, 20f);
                break;
            case DOTTED_LINE:
                drawDottedLine(renderer, o.x, o.y, e.x, e.y, 20f);
                break;
            case DOUBLE_ARROW:
                drawTwoHeadedArrow(renderer, o.x, o.y, e.x, e.y, 20f);
                break;
            case FILLED_POLYGON:
                // draw the polygon
                Vector2 vPres = tik.vertices.get(0).cpy().scl(gridSpacing).add(center);
                for (int i = 1; i < tik.vertices.size; i++) {
                    renderer.rectLine(vPres, tik.vertices.get(i).cpy().scl(gridSpacing).add(center), Math.max(lineWidth * scaling * zoomLevel, 1));
                    vPres = tik.vertices.get(i).cpy().scl(gridSpacing).add(center);
                }
                break;
            case DOTTED_POLYGON:
                // draw the polygon
                Vector2 vPres2 = tik.vertices.get(0).cpy().scl(gridSpacing).add(center);
                for (int i = 1; i < tik.vertices.size; i++) {
                    Vector2 a = tik.vertices.get(i).cpy().scl(gridSpacing).add(center);
                    drawDottedLine(renderer, vPres2.x, vPres2.y, a.x, a.y, 20f);
                    vPres2 = tik.vertices.get(i).cpy().scl(gridSpacing).add(center);
                }
                break;
            case TEXT:
                if (tik.data.matches("^\\$.*\\$$") && tik.latexImg == null) {
                    try {
                        tik.latexImg = GenerateTikzImage.createLaTeXFormulaImage(tik.data.replace("$", ""));
                        if (tik.data.contains("\\frac")) {
                            tik.numericalData += 0.5f;
                        }
                        if (tik.data.contains("\\sqrt")) {
                            tik.numericalData += 0.125f;
                        }
                    } catch (ParseException ignored) {
                        System.err.println("Parse Error: " + tik.data);
                        tik.latexImg = new Texture(Gdx.files.internal("Parsing Error.png"));
                    }
                }
                renderer.end();
                app.batch.begin();
                app.batch.setProjectionMatrix(renderer.getProjectionMatrix());
                if (tik.latexImg == null) {
                    app.TikzTextFont.setColor(Color.WHITE);
                    app.TikzTextFont.draw(app.batch, tik.data, o.x, o.y + app.TikzTextFont.getCapHeight() / 2, 1f, Align.center, false);
                } else {
                    float sizeY = app.TikzTextFont.getLineHeight() * tik.numericalData;
                    float sizeX = tik.latexImg.getWidth() * sizeY / (float) (tik.latexImg.getHeight());
                    Vector2 o2 = o.cpy().sub(sizeX / 2, sizeY / 2);
                    app.batch.draw(tik.latexImg, o2.x, o2.y, sizeX, sizeY);
                }
                app.batch.end();
                renderer.begin();
                renderer.set(ShapeRenderer.ShapeType.Filled);
                break;
            case DROPPED_POLYGON:
                // draw the polygon
                Vector2 vOld = editing.vertices.get(0).cpy().add(mouse).scl(gridSpacing).add(center);
                for (int i = 1; i < editing.vertices.size; i++) {
                    renderer.rectLine(vOld, editing.vertices.get(i).cpy().add(mouse).scl(gridSpacing).add(center), Math.max(lineWidth * scaling * zoomLevel, 1));
                    vOld = editing.vertices.get(i).cpy().add(mouse).scl(gridSpacing).add(center);
                }
                break;
            case ELLIPTICAL_ARC:
            case CIRCULAR_ARC:
                drawArc(renderer, o, tik.numericalData*gridSpacing, tik.angles[0], tik.angles[1], tik.arcHeight*gridSpacing);
                break;
            default:
                throw new IllegalDrawType("Unknown Draw Type");
        }
    }

    private void drawTikz() {
        final Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f).sub(panning);
        // do inputs stuff
        mouse = new Vector2(Gdx.input.getX() - screen.t.getWidth() / 2 - screen.tableOffset / 2, Gdx.graphics.getHeight() - Gdx.input.getY());
        mouse.sub(center).scl(1 / gridSpacing);

        if (snapGrid) {
            mouse.x = (float) Math.round(mouse.x * 10f) / 10f;
            mouse.y = (float) Math.round(mouse.y * 10f) / 10f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            snapGrid = !snapGrid;
        }

        // do stuff at the cursor
        if (Gdx.input.isButtonJustPressed(0) && Gdx.input.getX() > centerOffset * 2) {
            switch (currentType) {
                case LINE:
                case CIRCLE:
                case ARROW:
                case DOTTED_LINE:
                case DOUBLE_ARROW:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikTypeStruct(mouse, mouse.cpy().add(0.01f, 0.01f), currentType);
                        editing.color = selectedColor;
                    } else {
                        addingPoints = false;
                        TikTypeStruct a = new TikTypeStruct(editing.origin, mouse, currentType);
                        a.color = selectedColor;
                        points.add(a);
                        editing = null;
                    }
                    break;
                case TEXT:
                    if (addingPoints) {
                        editing = new TikTypeStruct(mouse, currentType, text);
                        editing.color = selectedColor;
                        points.add(editing);
                    }
                    break;
                case DOTTED_POLYGON:
                case FILLED_POLYGON:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikTypeStruct(new Array<>(), currentType);
                        editing.color = selectedColor;
                        editing.vertices.add(mouse);
                    } else {
                        if (mouse.equals(editing.vertices.get(0))) {
                            addingPoints = false;
                            editing.vertices.add(editing.vertices.get(0));
                            points.add(editing);
                        } else
                            editing.vertices.add(mouse);
                    }
                    break;
                case ELLIPTICAL_ARC:
                case CIRCULAR_ARC:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikTypeStruct(mouse, new Vector2(), currentType);
                        editing.color = selectedColor;
                        arcDrawState = 0;
                    } else {
                        if(arcDrawState == 0) {
                            arcDrawState = 2;
                        } else if (arcDrawState == 1) {
                            arcDrawState = 2;
                        } else if(arcDrawState == 2) {
                            arcDrawState = 0;
                            addingPoints = false;
                            points.add(editing);
                        }
                    }
                    break;
                case DROPPED_POLYGON:
                    addingPoints = false;
                    Array<Vector2> verts = editing.vertices;
                    for (int i = 0; i < verts.size; i++) {
                        verts.get(i).add(mouse);
                    }
                    editing.color = selectedColor;
                    points.add(editing);
                    setDrawType(DrawType.FILLED_POLYGON);
                    break;
                default:
                    throw new IllegalDrawType("Unknown Draw Type");
            }
        } else if (Gdx.input.isButtonJustPressed(1)) {
            if ((currentType == DrawType.FILLED_POLYGON || currentType == DrawType.DOTTED_POLYGON)
                && editing.vertices.size > 1) {
                points.add(editing);
            }
            addingPoints = false;
        }

        if (editing != null && addingPoints) {
            if (currentType == DrawType.CIRCULAR_ARC) {
                if (arcDrawState == 0) {
                    // Calculate width and height (circular arc, so height = width)
                    editing.numericalData = editing.origin.x - mouse.x;
                    editing.arcHeight = mouse.x - editing.origin.x; // for circular, height equals width
                } else if (arcDrawState == 1) {
                    // First angle calculation
                    Vector2 start = new Vector2(editing.numericalData, 0); // Start at width along x-axis
                    Vector2 arcCenter = new Vector2(editing.origin).sub(
                        (float) (editing.numericalData * Math.cos(Math.toRadians(editing.angles[0]))),
                        (float) (editing.arcHeight * Math.sin(Math.toRadians(editing.angles[0])))
                    );
                    Vector2 newMouse = new Vector2(mouse).sub(arcCenter); // Calculate new mouse position relative to center
//                    editing.angles[0] = newMouse.angleDeg(start); // Store first angle
                    editing.angles[0] = 0;
                } else if (arcDrawState == 2) {
                    // Second angle calculation
                    Vector2 start = new Vector2(editing.numericalData, 0); // Start at width along x-axis
                    Vector2 arcCenter = new Vector2(editing.origin).sub(
                        (float) (editing.numericalData * Math.cos(Math.toRadians(editing.angles[0]))),
                        (float) (editing.arcHeight * Math.sin(Math.toRadians(editing.angles[0])))
                    );
                    Vector2 newMouse = new Vector2(new Vector2(mouse.x, arcCenter.y-mouse.y)).sub(arcCenter); // Calculate new mouse position relative to center
                    editing.angles[1] = newMouse.angleDeg(start); // Store second angle
                }
            } else if (currentType == DrawType.ELLIPTICAL_ARC) {
                if (arcDrawState == 0) {
                    // Elliptical arc: width and height are different
                    editing.numericalData = editing.origin.x - mouse.x;
                    editing.arcHeight = mouse.y - editing.origin.y;
                } else if (arcDrawState == 1) {
                    // First angle calculation (elliptical)
                    Vector2 start = new Vector2(editing.numericalData, 0); // Start at width along x-axis
                    Vector2 arcCenter = new Vector2(editing.origin).sub(
                        (float) (editing.numericalData * Math.cos(Math.toRadians(editing.angles[0]))),
                        (float) (editing.arcHeight * Math.sin(Math.toRadians(editing.angles[0])))
                    );
                    Vector2 newMouse = new Vector2(mouse).sub(arcCenter); // Calculate new mouse position relative to center
//                    editing.angles[0] = newMouse.angleDeg(start); // Store first angle
                    editing.angles[0] = 0;
                } else if (arcDrawState == 2) {
                    // Second angle calculation (elliptical)
                    Vector2 start = new Vector2(editing.numericalData, 0); // Start at width along x-axis
                    Vector2 arcCenter = new Vector2(editing.origin).sub(
                        (float) (editing.numericalData * Math.cos(Math.toRadians(editing.angles[0]))),
                        (float) (editing.arcHeight * Math.sin(Math.toRadians(editing.angles[0])))
                    );
                    Vector2 newMouse = new Vector2(new Vector2(mouse.x, arcCenter.y-mouse.y)).sub(arcCenter); // Calculate new mouse position relative to center
                    editing.angles[1] = newMouse.angleDeg(start); // Store second angle
                }
            }
        }

    }

    public void drawDottedLine(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float dotSpacing) {
        // Calculate the total distance between the start and end points
        float distance = (float) Math.hypot(x2 - x1, y2 - y1);

        dotSpacing *= scaling * zoomLevel;

        // Calculate the number of dots that fit along the line
        int numDots = (int) (distance / dotSpacing);

        // Calculate the direction of the line (unit vector)
        float directionX = (x2 - x1) / distance;
        float directionY = (y2 - y1) / distance;

        // Draw dots along the line at regular intervals
        Vector2 vPres = new Vector2(x1, y1);
        for (int i = 0; i < numDots; i++) {
            shapeRenderer.rectLine(vPres, vPres.cpy().add(dotSpacing * directionX / 2, dotSpacing * directionY / 2), Math.max(lineWidth * scaling * zoomLevel, 1));
            vPres.add(dotSpacing * directionX, dotSpacing * directionY);
        }

        shapeRenderer.rectLine(vPres.x, vPres.y, x2, y2, Math.max(lineWidth * scaling * zoomLevel, 1));
    }

    public void drawCircle(ShapeRenderer shapeRenderer, float x, float y, float radius) {
        int segments = 360 / 5;
        Vector2 center = new Vector2(x, y);
        Vector2 vPres = new Vector2(x + radius, y);
        for (int i = 0; i <= segments; i++) {
            double alpha = 2 * PI / segments * i;
            Vector2 newPoint = center.cpy().add((float) (radius * cos(alpha)), (float) (radius * sin(alpha)));
            shapeRenderer.rectLine(vPres, newPoint, Math.max(lineWidth * scaling * zoomLevel, 1));
            vPres = newPoint.cpy();
        }
    }

    public void drawArc(ShapeRenderer shapeRenderer, Vector2 start, float width, float startAngle, float endAngle, float height) {
        int segments = (int) abs(endAngle - startAngle) / 5;
        Vector2 center = new Vector2(start).sub(
            (float) (width * Math.cos(Math.toRadians(startAngle))),
            (float) (height * Math.sin(Math.toRadians(startAngle)))
        );

        Vector2 vPres = center.cpy().add((float) (width*cos(toRadians(startAngle))), (float) (height*sin(toRadians(startAngle))));
        for(int i = 0; i <= segments; i++) {
            double alpha = toRadians(endAngle - startAngle) / segments * i + toRadians(startAngle);
            Vector2 newPoint = center.cpy().add((float) (width * cos(alpha)), (float) (height * sin(alpha)));
            shapeRenderer.rectLine(vPres, newPoint, Math.max(lineWidth * scaling * zoomLevel, 1));
            vPres = newPoint.cpy();
        }
    }

    public void drawArrow(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float arrowHeadSize) {
        // Draw the line (shaft of the arrow)
        shapeRenderer.rectLine(x1, y1, x2, y2, Math.max(lineWidth * scaling * zoomLevel, 1));

        // Calculate the angle of the line
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);

        // Calculate the points for the arrowhead triangle
        float arrowX1 = x2 - arrowHeadSize / 2 * scaling * zoomLevel * (float) cos(angle - Math.PI / 6);
        float arrowY1 = y2 - arrowHeadSize / 2 * scaling * zoomLevel * (float) sin(angle - Math.PI / 6);

        float arrowX2 = x2 - arrowHeadSize / 2 * scaling * zoomLevel * (float) cos(angle + Math.PI / 6);
        float arrowY2 = y2 - arrowHeadSize / 2 * scaling * zoomLevel * (float) sin(angle + Math.PI / 6);

        // Draw the arrowhead (a filled triangle)
        shapeRenderer.triangle(x2, y2, arrowX1, arrowY1, arrowX2, arrowY2);
    }

    public void drawTwoHeadedArrow(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float arrowHeadSize) {
        // Draw the line (shaft of the arrow)
        shapeRenderer.rectLine(x1, y1, x2, y2, Math.max(lineWidth * scaling * zoomLevel, 1));

        // Calculate the angle of the line
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);

        // Arrowhead at the end (x2, y2)
        float arrowX1 = x2 - arrowHeadSize * scaling * zoomLevel * (float) cos(angle - Math.PI / 6);
        float arrowY1 = y2 - arrowHeadSize * scaling * zoomLevel * (float) sin(angle - Math.PI / 6);
        float arrowX2 = x2 - arrowHeadSize * scaling * zoomLevel * (float) cos(angle + Math.PI / 6);
        float arrowY2 = y2 - arrowHeadSize * scaling * zoomLevel * (float) sin(angle + Math.PI / 6);

        // Draw the arrowhead at the end
        shapeRenderer.triangle(x2, y2, arrowX1, arrowY1, arrowX2, arrowY2);

        // Arrowhead at the start (x1, y1)
        float arrowX3 = x1 + arrowHeadSize * scaling * zoomLevel * (float) cos(angle - Math.PI / 6);
        float arrowY3 = y1 + arrowHeadSize * scaling * zoomLevel * (float) sin(angle - Math.PI / 6);

        float arrowX4 = x1 + arrowHeadSize * scaling * zoomLevel * (float) cos(angle + Math.PI / 6);
        float arrowY4 = y1 + arrowHeadSize * scaling * zoomLevel * (float) sin(angle + Math.PI / 6);

        // Draw the arrowhead at the start
        shapeRenderer.triangle(x1, y1, arrowX3, arrowY3, arrowX4, arrowY4);
    }

    public void dispose() {
        for (TikTypeStruct tik : points) {
            tik.dispose();
        }
        System.out.println("Disposing of Textures");
    }
}
