package com.tikz.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tikz.Main;
import com.tikz.MainScreen;

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
    private boolean snapGrid = true;
    private DrawType currentType = DrawType.LINE;
    private float centerOffset = 0f;

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
        // set the center location. Center of the screen minus the pan location and adding the menu if its there
        centerOffset = screen.t.getWidth() / 2f + screen.tableOffset / 2;
        final Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2f + centerOffset,
            Gdx.graphics.getHeight() / 2f).sub(panning);
        renderer.set(ShapeRenderer.ShapeType.Filled);

        // set the gridSpacing and scaling
        gridSpacing = Math.min((float) Gdx.graphics.getHeight() / ROWS, (float) Gdx.graphics.getWidth() / COLS);
        scaling = gridSpacing / Math.min(1200f / (float) ROWS, 800f / (float) COLS);

        // set the zoom
        zoomLevel = clamp(zoomLevel, 0.5f, 2f);
        gridSpacing *= zoomLevel;
        // draw small lines
        for (int i = -6; i <= 5; i++) {  // row
            for (int j = 0; j < 10; j++) {
                renderer.setColor(Color.GRAY);
                renderer.rectLine(new Vector2(0, center.y + gridSpacing * i + gridSpacing / 10 * j),
                    new Vector2(Gdx.graphics.getWidth(), center.y + gridSpacing * i + gridSpacing / 10 * j), 1f);
                renderer.setColor(Color.GRAY);
                renderer.rectLine(new Vector2(center.x + gridSpacing * i + gridSpacing / 10 * j, 0),
                    new Vector2(center.x + gridSpacing * i + gridSpacing / 10 * j, Gdx.graphics.getHeight()), 1f);
            }
        }
        // draw big lines
        for (int i = -6; i <= 6; i++) {
            renderer.setColor(Color.WHITE);
            renderer.rectLine(new Vector2(center.x + gridSpacing * i, 0),
                new Vector2(center.x + gridSpacing * i, Gdx.graphics.getHeight()), 1f);
            renderer.rectLine(new Vector2(0, center.y + gridSpacing * i),
                new Vector2(Gdx.graphics.getWidth(), center.y + gridSpacing * i), 1f);
        }

        drawTikz();

        // Clear all points
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            points.clear();
        }

        renderer.setColor(Color.GOLD);
        renderer.circle(center.x, center.y, 4f);

        if (editing != null) {
            renderer.setColor(editing.color);
        }
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
            Vector2 e = new Vector2();
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
                o = editing.origin.cpy().scl(gridSpacing).add(center);
                editing = new TikTypeStruct(mouse, currentType, text);
                renderer.end();
                app.batch.begin();
                app.batch.setProjectionMatrix(renderer.getProjectionMatrix());
                app.TikzTextFont.setColor(Color.WHITE);
                app.TikzTextFont.draw(app.batch, editing.data, o.x, o.y + app.TikzTextFont.getCapHeight() / 2, 1f,
                    Align.center, false);
                app.batch.end();
                renderer.begin();
                renderer.setAutoShapeType(true);
            }
        }
    }

    private void renderTikz(TikTypeStruct tik, DrawType type, ShapeRenderer renderer, Vector2 o, Vector2 e, Vector2 center) {
        renderer.setColor(tik.color);
        switch (type) {
            case LINE:
                renderer.rectLine(o, e, 2f * scaling);
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
                    renderer.rectLine(vPres, tik.vertices.get(i).cpy().scl(gridSpacing).add(center), 2f * scaling);
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
                renderer.end();
                app.batch.begin();
                app.batch.setProjectionMatrix(renderer.getProjectionMatrix());
                app.TikzTextFont.setColor(Color.WHITE);
                app.TikzTextFont.draw(app.batch, tik.data, o.x, o.y + app.TikzTextFont.getCapHeight() / 2, 1f, Align.center, false);
                app.batch.end();
                renderer.begin();
                renderer.set(ShapeRenderer.ShapeType.Filled);
                break;
            case DROPPED_POLYGON:
                // draw the polygon
                Vector2 vOld = editing.vertices.get(0).cpy().add(mouse).scl(gridSpacing).add(center);
                for (int i = 1; i < editing.vertices.size; i++) {
                    renderer.rectLine(vOld, editing.vertices.get(i).cpy().add(mouse).scl(gridSpacing).add(center), 2f * scaling);
                    vOld = editing.vertices.get(i).cpy().add(mouse).scl(gridSpacing).add(center);
                }
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
                    } else {
                        addingPoints = false;
                        points.add(new TikTypeStruct(editing.origin, mouse, currentType));
                        editing = null;
                    }
                    break;
                case TEXT:
                    if (addingPoints) {
                        editing = new TikTypeStruct(mouse, currentType, text);
                        points.add(editing);
                    }
                    break;
                case DOTTED_POLYGON:
                case FILLED_POLYGON:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikTypeStruct(new Array<>(), currentType);
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
                case ARC:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikTypeStruct(new Vector2(), new Vector2(), DrawType.ARC);
                    } else {
                        addingPoints = false;
                    }
                    break;
                case DROPPED_POLYGON:
                    addingPoints = false;
                    Array<Vector2> verts = editing.vertices;
                    for (int i = 0; i < verts.size; i++) {
                        verts.get(i).add(mouse);
                    }
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
            shapeRenderer.rectLine(vPres, vPres.cpy().add(dotSpacing * directionX / 2, dotSpacing * directionY / 2), 2f * scaling);
            vPres.add(dotSpacing * directionX, dotSpacing * directionY);
        }

        shapeRenderer.rectLine(vPres.x, vPres.y, x2, y2, 2f * scaling);
    }

    public void drawCircle(ShapeRenderer shapeRenderer, float x, float y, float radius) {
        int segments = 360 / 5;
        Vector2 center = new Vector2(x, y);
        Vector2 vPres = new Vector2(x + radius, y);
        for (int i = 0; i <= segments; i++) {
            double alpha = 2 * PI / segments * i;
            Vector2 newPoint = center.cpy().add((float) (radius * cos(alpha)), (float) (radius * sin(alpha)));
            shapeRenderer.rectLine(vPres, newPoint, 2f * scaling);
            vPres = newPoint.cpy();
        }
    }

    public void drawArrow(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float arrowHeadSize) {
        // Draw the line (shaft of the arrow)
        shapeRenderer.rectLine(x1, y1, x2, y2, 2f * scaling);

        // Calculate the angle of the line
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);

        // Calculate the points for the arrowhead triangle
        float arrowX1 = x2 - arrowHeadSize / 2 * scaling * (float) cos(angle - Math.PI / 6);
        float arrowY1 = y2 - arrowHeadSize / 2 * scaling * (float) sin(angle - Math.PI / 6);

        float arrowX2 = x2 - arrowHeadSize / 2 * scaling * (float) cos(angle + Math.PI / 6);
        float arrowY2 = y2 - arrowHeadSize / 2 * scaling * (float) sin(angle + Math.PI / 6);

        // Draw the arrowhead (a filled triangle)
        shapeRenderer.triangle(x2, y2, arrowX1, arrowY1, arrowX2, arrowY2);
    }

    public void drawTwoHeadedArrow(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float arrowHeadSize) {
        // Draw the line (shaft of the arrow)
        shapeRenderer.rectLine(x1, y1, x2, y2, 2f * scaling);

        // Calculate the angle of the line
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);

        // Arrowhead at the end (x2, y2)
        float arrowX1 = x2 - arrowHeadSize * (float) cos(angle - Math.PI / 6);
        float arrowY1 = y2 - arrowHeadSize * (float) sin(angle - Math.PI / 6);
        float arrowX2 = x2 - arrowHeadSize * (float) cos(angle + Math.PI / 6);
        float arrowY2 = y2 - arrowHeadSize * (float) sin(angle + Math.PI / 6);

        // Draw the arrowhead at the end
        shapeRenderer.triangle(x2, y2, arrowX1, arrowY1, arrowX2, arrowY2);

        // Arrowhead at the start (x1, y1)
        float arrowX3 = x1 + arrowHeadSize * (float) cos(angle - Math.PI / 6);
        float arrowY3 = y1 + arrowHeadSize * (float) sin(angle - Math.PI / 6);
        float arrowX4 = x1 + arrowHeadSize * (float) cos(angle + Math.PI / 6);
        float arrowY4 = y1 + arrowHeadSize * (float) sin(angle + Math.PI / 6);

        // Draw the arrowhead at the start
        shapeRenderer.triangle(x1, y1, arrowX3, arrowY3, arrowX4, arrowY4);
    }

}
