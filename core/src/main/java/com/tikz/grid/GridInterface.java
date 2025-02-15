package com.tikz.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tikz.ColorHolder;
import com.tikz.Main;
import com.tikz.MainScreen;
import com.tikz.ProgramState;
import org.scilab.forge.jlatexmath.ParseException;

import static com.tikz.ProgramState.*;
import static java.lang.Math.*;

public class GridInterface {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    private final Main app;
    public float gridSpacing = 1;
    public float scaling = 1;
    public Vector2 mouse = new Vector2();
    public Vector2 panning = new Vector2();
    public Array<TikType> points = new Array<>();
    public TikType editing;
    public MainScreen screen;
    private float centerOffset = 0f;

    public ColorHolder selectedColor = colors[0].clone();

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
        currentType = type;
    }

    public void render(ShapeRenderer renderer) {
        // set the center location. Center of the screen minus the pan location and adding the menu if it's there
        centerOffset = screen.t.getWidth() / 2f + screen.tableOffset / 2;
        final Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2f + centerOffset,
            Gdx.graphics.getHeight() / 2f).sub(panning);
        renderer.set(wireframe ? ShapeRenderer.ShapeType.Line : ShapeRenderer.ShapeType.Filled);

        // set the gridSpacing and scaling
        gridSpacing = Math.min((float) Gdx.graphics.getHeight() / ROWS, (float) Gdx.graphics.getWidth() / COLS);
        scaling = gridSpacing / Math.min(1200f / (float) ROWS, 800f / (float) COLS);

        // set the zoom
        gridSpacing *= zoomLevel;

        if (showGrid) {
            drawGridLines(renderer, center);
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
            renderer.circle(center.x, center.y, max(2f * scaling, 2));
        }

        // If we are not in light mode, and the color is black, render white, else render the selected color
        renderer.setColor(!lightMode && selectedColor.name.equalsIgnoreCase("black") ? Color.WHITE : selectedColor.color);
        renderer.circle(mouse.x * gridSpacing + center.x, mouse.y * gridSpacing + center.y, 2f);

        renderAllPoints(renderer, center);

        // Ctrl-Z to remove the latest point
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && screen.notTyping()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && points.size > 0) {
                points.peek().dispose();
                points.removeIndex(points.size - 1);
            }
        }

        renderEditingTik(renderer, center);
    }

    private void drawGridLines(ShapeRenderer renderer, Vector2 center) {
        final int viewable = 7;
        int count = 10;
        int halfLineCount = Math.round(viewable / zoomLevel) + (int) (panning.x / gridSpacing);

        if (zoomLevel < 0.5f) {
            count = 4;
        }

        // Vertical Lines
        for (int i = -halfLineCount + (int) (2 * panning.x / gridSpacing); i < halfLineCount; i++) {
            renderer.setColor(Color.GRAY);
            // Draw Minor Grid Lines
            for (int j = 0; j < count; j++) {
                renderer.rectLine(new Vector2(center.x + gridSpacing * i + gridSpacing / count * j, 0),
                    new Vector2(center.x + gridSpacing * i + gridSpacing / count * j, Gdx.graphics.getHeight()), 1f);
            }
            // Draw Major Grid Lines
            renderer.setColor(lightMode ? Color.GRAY : Color.LIGHT_GRAY);
            renderer.rectLine(new Vector2(center.x + gridSpacing * i, 0),
                new Vector2(center.x + gridSpacing * i, Gdx.graphics.getHeight()), 2f);
        }

        // Horizontal Lines
        halfLineCount = Math.round(viewable / 1.25f / zoomLevel) + (int) (panning.y / gridSpacing);
        for (int i = -halfLineCount + (int) (2 * panning.y / gridSpacing); i < halfLineCount; i++) {
            // Draw Minor Grid Lines
            renderer.setColor(lightMode ? Color.LIGHT_GRAY : Color.GRAY);
            for (int j = 0; j < count; j++) {
                renderer.rectLine(new Vector2(0, center.y + gridSpacing * i + gridSpacing / count * j),
                    new Vector2(Gdx.graphics.getWidth(), center.y + gridSpacing * i + gridSpacing / count * j), 1f);
            }
            // Draw Major Grid Lines
            renderer.setColor(lightMode ? Color.GRAY : Color.LIGHT_GRAY);
            renderer.rectLine(new Vector2(0, center.y + gridSpacing * i),
                new Vector2(Gdx.graphics.getWidth(), center.y + gridSpacing * i), 2f);
        }
    }

    private void renderAllPoints(ShapeRenderer renderer, Vector2 center) {
        // render every point
        for (TikType tik : points) {
            Vector2 o = new Vector2();
            Vector2 e = new Vector2(-1, -1);
            if (tik.type != DrawType.MULTI_LINE) {
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

            if (currentType != DrawType.MULTI_LINE) {
                if (currentType != DrawType.BEZIER)
                    editing.endPoint = mouse.cpy();
                o = editing.origin.cpy().scl(gridSpacing).add(center);
                e = editing.endPoint.cpy().scl(gridSpacing).add(center);
            }
            if (currentType != DrawType.TEXT) {
                renderTikz(editing, currentType, renderer, o, e, center);
                if (currentType == DrawType.MULTI_LINE) {
                    Vector2 vPres = editing.vertices.peek().cpy().scl(gridSpacing).add(center);
                    drawLine(renderer, vPres, mouse.cpy().scl(gridSpacing).add(center), editing.dashed,
                        editing.frontArrow, false, editing.lineThickness);
                }
            } else {
                editing = new TikType(mouse, currentType, text, editing.latexImg, editing.upscale);
                editing.color = selectedColor;
                o = editing.origin.cpy().scl(gridSpacing).add(center);
                renderTikz(editing, currentType, renderer, o, e, center);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                points.add(editing);
                addingPoints = false;
            }
            if (addingPoints && currentType == DrawType.BEZIER) {
                handleBezierDrawing(renderer, o, e, center);
            }
        }
    }

    private void handleBezierDrawing(ShapeRenderer renderer, Vector2 o, Vector2 e, Vector2 center) {
        Array<Vector2> controlPoints = new Array<>();

        for (Vector2 c : editing.vertices) {
            controlPoints.add(c.cpy().scl(gridSpacing).add(center));
        }

        editing.dashed = dashed;
        editing.frontArrow = frontArrow;
        editing.backArrow = backArrow;

        // Render the Bezier Control Points
        renderer.setColor(lightMode ? Color.GRAY : Color.GOLDENROD);
        renderer.circle(o.x, o.y, 5f * scaling);
        Vector2 vPres = new Vector2(o);
        for (Vector2 c : controlPoints) {
            renderer.circle(c.x, c.y, 5f * scaling);
            drawDashedLine(renderer, vPres.x, vPres.y, c.x, c.y, 10f, 2f);
            vPres = c.cpy();
        }
        renderer.circle(e.x, e.y, 5f * scaling);
        drawDashedLine(renderer, vPres.x, vPres.y, e.x, e.y, 10f, 2f);

        // Drag the Bezier Control Points
        Vector2 mouseReal = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        if (((mouseReal.dst2(o) < 100 * scaling * scaling && draggingState == 0) || draggingState == 1) && (Gdx.input.isButtonPressed(Input.Buttons.LEFT))) {
            editing.origin.set(mouse.cpy());
            draggingState = 1;
        } else if (((mouseReal.dst2(e) < 100 * scaling * scaling && draggingState == 0) || draggingState == 2) && (Gdx.input.isButtonPressed(Input.Buttons.LEFT))) {
            editing.endPoint.set(mouse.cpy());
            draggingState = 2;
        }

        for (int i = 0; i < controlPoints.size; i++) {
            Vector2 c = controlPoints.get(i);
            if ((mouseReal.dst2(c) < 100 * scaling * scaling && draggingState == 0 || draggingState == i + 3) && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                editing.vertices.get(i).set(mouse.cpy());
                draggingState = i + 3;
            }
        }

        if (draggingState != 0 && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            draggingState = 0;
        }
    }

    private void renderTikz(TikType tik, DrawType type, ShapeRenderer renderer, Vector2 o, Vector2 e, Vector2 center) {
        renderer.setColor(new Color(
            (tik.color.r != 0 || lightMode ? tik.color.r : 1) * tik.color.percentValue + (lightMode ? 1 : 0x00) * (1 - tik.color.percentValue),
            (tik.color.g != 0 || lightMode ? tik.color.g : 1) * tik.color.percentValue + (lightMode ? 1 : 0x00) * (1 - tik.color.percentValue),
            (tik.color.b != 0 || lightMode ? tik.color.b : 1) * tik.color.percentValue + (lightMode ? 1 : 0x00) * (1 - tik.color.percentValue)
                , 255));
        switch (type) {
            case LINE:
                drawLine(renderer, o, e, tik.dashed, tik.frontArrow, tik.backArrow, tik.lineThickness);
                break;
            case CIRCLE:
                drawCircle(renderer, o.x, o.y, o.dst(e), tik.dashed, tik.isFilled, tik.lineThickness);
                break;
            case MULTI_LINE:
                // draw the polygon
                if (tik.isFilled) {
                    if (tik.triangleLocations == null) {
                        tik.triangulate();
                    }
                    for (int i = 0; i < tik.triangleLocations.length; i += 3) {
                        int v1 = tik.triangleLocations[i] * 2;
                        int v2 = tik.triangleLocations[i + 1] * 2;
                        int v3 = tik.triangleLocations[i + 2] * 2;

                        renderer.triangle(
                            tik.flatVertices[v1] * gridSpacing + center.x, tik.flatVertices[v1 + 1] * gridSpacing + center.y,
                            tik.flatVertices[v2] * gridSpacing + center.x, tik.flatVertices[v2 + 1] * gridSpacing + center.y,
                            tik.flatVertices[v3] * gridSpacing + center.x, tik.flatVertices[v3 + 1] * gridSpacing + center.y
                        );
                    }
                }
                Vector2 vPres = tik.vertices.get(0).cpy().scl(gridSpacing).add(center);
                if (tik.vertices.size > 1) {
                    drawLine(renderer, vPres, tik.vertices.get(1).cpy().scl(gridSpacing).add(center), tik.dashed,
                        false, tik.backArrow, tik.lineThickness);
                    vPres = tik.vertices.get(1).cpy().scl(gridSpacing).add(center);
                }
                if (tik.vertices.size > 2) {
                    for (int i = 2; i < tik.vertices.size - 1; i++) {
                        boolean isShort = vPres.cpy().sub(tik.vertices.get(i).cpy().scl(gridSpacing).add(center)).len() < 20f * scaling;
                        if (isShort && tik.dashed) {
                            if (i % 2 == 1) {
                                drawLine(renderer, vPres, tik.vertices.get(i).cpy().scl(gridSpacing).add(center), tik.dashed,
                                    false, false, tik.lineThickness);
                            }
                        } else
                            drawLine(renderer, vPres, tik.vertices.get(i).cpy().scl(gridSpacing).add(center), tik.dashed,
                                false, false, tik.lineThickness);
                        vPres = tik.vertices.get(i).cpy().scl(gridSpacing).add(center);
                    }
                }
                drawLine(renderer, vPres, tik.vertices.get(tik.vertices.size - 1).cpy().scl(gridSpacing).add(center),
                    tik.dashed, tik.frontArrow && (!addingPoints || tik != editing), false, tik.lineThickness);
                break;
            case TEXT:
                // Check if a texture needs to be generated
                if (tik.text.matches("^\\$.*\\$$") && tik.latexImg == null) {
                    try {
                        tik.latexImg = GenerateLaTeXImage.createLaTeXFormulaImage(tik.text);
                    } catch (ParseException ignored) {
                        System.err.println("Parse Error: " + tik.text);
                        tik.latexImg = new Texture(Gdx.files.internal("Parsing Error.png"));
                    }
                }
                renderer.end();
                app.batch.begin();
                app.batch.setProjectionMatrix(renderer.getProjectionMatrix());
                // If there is no texture, render the text, otherwise render the image
                if (tik.latexImg == null) {
                    app.TikzTextFont.setColor(!lightMode && tik.color.name.equalsIgnoreCase("black") ? Color.WHITE : tik.color.color);
                    app.TikzTextFont.draw(app.batch, tik.text, o.x, o.y + app.TikzTextFont.getCapHeight() / 2, 1f, Align.center, false);
                } else {
                    float sizeX = tik.latexImg.getWidth() / 512f * zoomLevel * scaling * (40f - 9f);
                    float sizeY = tik.latexImg.getHeight() / 512f * zoomLevel * scaling * (40f - 9f);
                    Vector2 o2 = o.cpy().sub(sizeX / 2, sizeY / 2);
                    app.batch.setColor(!lightMode && tik.color.name.equalsIgnoreCase("black") ? Color.WHITE : tik.color.color);
                    app.batch.draw(tik.latexImg, o2.x, o2.y, sizeX, sizeY);
                }
                app.batch.end();
                renderer.begin();
                renderer.set(wireframe ? ShapeRenderer.ShapeType.Line : ShapeRenderer.ShapeType.Filled);
                break;
            case DROPPED_POLYGON:
                // draw the polygon
                Vector2 vOld = editing.vertices.get(0).cpy().add(mouse).scl(gridSpacing).add(center);
                for (int i = 1; i < editing.vertices.size; i++) {
                    drawLine(renderer, vOld, editing.vertices.get(i).cpy().add(mouse).scl(gridSpacing).add(center), dashed, frontArrow, backArrow, lineThickness);
                    vOld = editing.vertices.get(i).cpy().add(mouse).scl(gridSpacing).add(center);
                }
                break;
            case BEZIER:
                Array<Vector2> points = new Array<>();
                for (Vector2 p : tik.vertices) {
                    points.add(p.cpy().scl(gridSpacing).add(center));
                }
                drawBezier(renderer, o, e, tik.dashed, tik.frontArrow, tik.backArrow, points, lineThickness);
                break;
            default:
                throw new IllegalDrawType("Unknown Draw Type");
        }
    }

    /**
     * Enable drawing the tikz geometry using the mouse inputs
     */
    private void drawTikz() {
        final Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f).sub(panning);
        // do inputs stuff
        mouse = new Vector2(Gdx.input.getX() - screen.t.getWidth() / 2 - screen.tableOffset / 2, Gdx.graphics.getHeight() - Gdx.input.getY());
        mouse.sub(center).scl(1 / gridSpacing);

        if (snapGrid) {
            mouse.x = (float) Math.round(mouse.x * 10f) / 10f;
            mouse.y = (float) Math.round(mouse.y * 10f) / 10f;
        }

        // do stuff at the cursor
        if (Gdx.input.isButtonJustPressed(0) && Gdx.input.getX() > centerOffset * 2 && screen.isNotFileExplorer() && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            switch (currentType) {
                case LINE:
                case CIRCLE:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikType(mouse, mouse.cpy().add(0.01f, 0.01f), currentType);
                        editing.color = selectedColor.clone();
                        editing.dashed = dashed;
                        editing.frontArrow = frontArrow;
                        editing.backArrow = backArrow;
                        editing.lineThickness = lineThickness;
                        if (currentType == DrawType.CIRCLE)
                            editing.isFilled = isFilled;
                    } else {
                        addingPoints = false;
                        editing.endPoint = mouse.cpy();
                        points.add(editing);
                        editing = null;
                    }
                    break;
                case TEXT:
                    if (addingPoints) {
                        editing = new TikType(mouse, currentType, text);
                        editing.color = selectedColor.clone();
                        points.add(editing);
                    }
                    break;
                case MULTI_LINE:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikType(new Array<>(), currentType);
                        editing.color = selectedColor.clone();
                        editing.vertices.add(mouse);
                        editing.dashed = dashed;
                        editing.frontArrow = frontArrow;
                        editing.backArrow = backArrow;
                        editing.lineThickness = lineThickness;
                    } else {
                        if (mouse.equals(editing.vertices.get(0))) {
                            addingPoints = false;
                            editing.vertices.add(editing.vertices.get(0));
                            editing.frontArrow = false;
                            editing.backArrow = false;
                            editing.isFilled = isFilled;
                            editing.lineThickness = lineThickness;
                            points.add(editing);
                        } else
                            editing.vertices.add(mouse);
                    }
                    break;
                case DROPPED_POLYGON:
                    Array<Vector2> verts = new Array<>(editing.vertices.size);
                    for (Vector2 v : editing.vertices) {
                        verts.add(v.cpy().add(mouse));
                    }
                    TikType temp = new TikType(verts, DrawType.MULTI_LINE);
                    temp.color = selectedColor.clone();
                    temp.dashed = editing.dashed;
                    temp.frontArrow = editing.frontArrow;
                    temp.backArrow = editing.backArrow;
                    temp.isFilled = isFilled;
                    temp.lineThickness = lineThickness;
                    points.add(temp);
                    break;
                case BEZIER:
                    if (!addingPoints) {
                        addingPoints = true;
                        Vector2[] v = new Vector2[bezierControlPointCount];
                        for (int i = 0; i < bezierControlPointCount; i++) {
                            v[i] = mouse.cpy().add(2f / (bezierControlPointCount + 1) * (i + 1), (float) pow(-1, i));
                        }
                        editing = new TikType(new Vector2(mouse.cpy()), mouse.cpy().add(2, 0), DrawType.BEZIER, v);
                        editing.color = selectedColor.clone();
                        editing.dashed = dashed;
                        editing.frontArrow = frontArrow;
                        editing.backArrow = backArrow;
                        editing.lineThickness = lineThickness;
                    }
                    break;
                default:
                    throw new IllegalDrawType("Unknown Draw Type");
            }
        } else if (Gdx.input.isButtonJustPressed(1)) {
            if ((currentType == DrawType.MULTI_LINE)
                && editing.vertices.size > 1) {
                if (editing.vertices.size == 2) {
                    TikType temp = editing;
                    editing = new TikType(temp.vertices.get(0), temp.vertices.get(1), DrawType.LINE);
                    editing.backArrow = temp.backArrow;
                    editing.frontArrow = temp.frontArrow;
                    editing.dashed = temp.dashed;
                    editing.isFilled = isFilled;
                    editing.color = selectedColor.clone();
                }
                points.add(editing);
            } else if (currentType == DrawType.DROPPED_POLYGON) {
                setDrawType(DrawType.MULTI_LINE);
            }
            addingPoints = false;
        }
    }

    public void drawCircle(ShapeRenderer shapeRenderer, float x, float y, float radius, boolean isDashed, boolean isFilled,
                           DrawType.LineThickness thickness) {
        if (isFilled) {
            shapeRenderer.circle(x, y, radius);
            return;
        }
        int segments = Math.max(1, (int) (6 * (float) Math.cbrt(radius)));
        Vector2 center = new Vector2(x, y);
        Vector2 vPres = new Vector2(x + radius, y);
        double angularSeparation = 2 * PI / segments;
        for (int i = 0; i <= segments; i++) {
            double alpha = angularSeparation * i;
            Vector2 newPoint = center.cpy().add((float) (radius * cos(alpha)), (float) (radius * sin(alpha)));
            if ((angularSeparation * radius > 30f || i % 2 == 0) || !isDashed)
                drawLine(shapeRenderer, vPres, newPoint, isDashed, false, false, thickness);
            vPres = newPoint.cpy();
        }
    }

    public void drawLine(ShapeRenderer shapeRenderer, Vector2 origin, Vector2 end, boolean isDashed, boolean frontArrow
        , boolean backArrow, DrawType.LineThickness thickness) {
        float x1 = origin.x;
        float x2 = end.x;
        float y1 = origin.y;
        float y2 = end.y;
        float lineWidth = getLineWidth(thickness);

        // Calculate the angle of the line
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);
        float arrowHeadSize = 20f * scaling * zoomLevel;

        if (isDashed) {
            drawDashedLine(shapeRenderer, origin.x, origin.y, end.x, end.y, 20f, lineWidth);
        } else {
            shapeRenderer.rectLine(origin, end, Math.max(lineWidth * scaling * zoomLevel, 1f));
        }

        if (frontArrow) {
            // Calculate the points for the arrowhead triangle
            float arrowX1 = x2 - arrowHeadSize / 2 * (float) cos(angle - Math.PI / 6);
            float arrowY1 = y2 - arrowHeadSize / 2 * (float) sin(angle - Math.PI / 6);

            float arrowX2 = x2 - arrowHeadSize / 2 * (float) cos(angle + Math.PI / 6);
            float arrowY2 = y2 - arrowHeadSize / 2 * (float) sin(angle + Math.PI / 6);

            // Draw the arrowhead (a filled triangle)
            shapeRenderer.triangle(x2, y2, arrowX1, arrowY1, arrowX2, arrowY2);
        }
        if (backArrow) {
            // Arrowhead at the start (x1, y1)
            float arrowX3 = x1 + arrowHeadSize / 2f * (float) cos(angle - Math.PI / 6);
            float arrowY3 = y1 + arrowHeadSize / 2f * (float) sin(angle - Math.PI / 6);

            float arrowX4 = x1 + arrowHeadSize / 2f * (float) cos(angle + Math.PI / 6);
            float arrowY4 = y1 + arrowHeadSize / 2f * (float) sin(angle + Math.PI / 6);

            // Draw the arrowhead at the start
            shapeRenderer.triangle(x1, y1, arrowX3, arrowY3, arrowX4, arrowY4);
        }
    }

    public float getLineWidth(DrawType.LineThickness thickness) {
        switch (thickness) {
            case ULTRA_THIN:
                return 0.5f;
            case VERY_THIN:
                return 1.25f;
            case THIN:
                return 1.55f;
            case THICK:
                return 2f;
            case VERY_THICK:
                return 3f;
            case ULTRA_THICK:
                return 5f;
        }
        return 2f;
    }

    public void drawDashedLine(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float dashSpacing, float lineWidth) {
        // Calculate the total distance between the start and end points
        float distance = (float) Math.hypot(x2 - x1, y2 - y1);

        dashSpacing *= scaling * zoomLevel;

        // Calculate the number of dots that fit along the line
        int numDots = (int) (distance / dashSpacing);

        // Calculate the direction of the line (unit vector)
        float directionX = (x2 - x1) / distance;
        float directionY = (y2 - y1) / distance;

        // Draw dots along the line at regular intervals
        Vector2 vPres = new Vector2(x1, y1);
        for (int i = 0; i < numDots; i++) {
            shapeRenderer.rectLine(vPres, vPres.cpy().add(dashSpacing * directionX / 2,
                dashSpacing * directionY / 2), Math.max(lineWidth * scaling * zoomLevel, 1f));
            vPres.add(dashSpacing * directionX, dashSpacing * directionY);
        }

        shapeRenderer.rectLine(vPres.x, vPres.y, x2, y2, Math.max(lineWidth * scaling * zoomLevel, 1f));
    }

    public void drawBezier(ShapeRenderer renderer, Vector2 start, Vector2 end, boolean isDashed, boolean frontArrow,
                           boolean backArrow, Array<Vector2> controlPoints, DrawType.LineThickness thickness) {
        int lineCount = 50;
        Vector2 vPres = start.cpy();
        Array<Vector2> vectors = new Array<>();
        vectors.add(start);
        for (Vector2 controlPoint : controlPoints) {
            vectors.add(controlPoint);
        }
        vectors.add(end);
        int n = vectors.size - 1;
        float t = 1f / lineCount;
        Vector2 point = new Vector2();
        for (int i = 0; i <= n; i++) {
            double scl = binomialCoefficient(n, i) * pow(1 - t, n - i) * pow(t, i);
            point.add(vectors.get(i).cpy().scl((float) scl));
        }
        drawLine(renderer, vPres, point, false, false, backArrow, thickness);
        vPres = point.cpy();

        // \sum_{i=0}^n*\frac{n!}{i!(n-i)!}(1-t)^{n-i}t^iP_i
        for (int line = 1; line < lineCount; line++) {
            t = (float) line / lineCount;
            point = new Vector2();
            for (int i = 0; i <= n; i++) {
                double scl = binomialCoefficient(n, i) * pow(1 - t, n - i) * pow(t, i);
                point.add(vectors.get(i).cpy().scl((float) scl));
            }
            if (line % 2 == 0 || !isDashed)
                drawLine(renderer, vPres, point, false, false, false, thickness);
            vPres = point.cpy();
        }
        t = 1;
        point = new Vector2();
        for (int i = 0; i <= n; i++) {
            double scl = binomialCoefficient(n, i) * pow(1 - t, n - i) * pow(t, i);
            point.add(vectors.get(i).cpy().scl((float) scl));
        }
        drawLine(renderer, vPres, point, false, frontArrow, false, thickness);
    }

    private int binomialCoefficient(int n, int i) {
        return factorial(n) / (factorial(i) * factorial(n - i));
    }

    private int factorial(int n) {
        int fac = 1;
        if (n == 0)
            return 1;
        for (int i = 1; i <= n; i++) {
            fac *= i;
        }
        return fac;
    }

    public void dispose() {
        for (TikType tik : points) {
            tik.dispose();
        }
        System.out.println("Disposing of Textures");
    }
}
