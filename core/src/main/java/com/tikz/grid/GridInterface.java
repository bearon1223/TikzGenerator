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

import static java.lang.Math.sqrt;

public class GridInterface {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public float scaling = Math.min((float) Gdx.graphics.getHeight() / ROWS, (float) Gdx.graphics.getWidth() / COLS);
    public Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    public boolean addingPoints = false;
    private boolean snapGrid = true;
    public String text = "Base Text";

    private final Main app;
    private final MainScreen screen;

    private DrawType currentType = DrawType.LINE;
    public Array<TikTypeStruct> points = new Array<>();
    public TikTypeStruct editing;

    public GridInterface(MainScreen screen, Main app) {
        this.app = app;
        this.screen = screen;
    }

    public void setDrawType(DrawType type) {
        this.currentType = type;
    }

    public DrawType getDrawType() {
        return currentType;
    }

    public void drawGrid(ShapeRenderer renderer) {
        // Draw grid
        final Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2f + screen.t.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f);
        renderer.set(ShapeRenderer.ShapeType.Filled);
        scaling = Math.min((float) Gdx.graphics.getHeight() / ROWS, (float) Gdx.graphics.getWidth() / COLS);
        // draw small lines
        for (int i = -4; i <= 3; i++) {  // row
            for (int j = 0; j < 10; j++) {
                renderer.setColor(Color.GRAY);
                renderer.rectLine(new Vector2(0, center.y + scaling * i + scaling / 10 * j),
                    new Vector2(Gdx.graphics.getWidth(), center.y + scaling * i + scaling / 10 * j), 1f);
                renderer.setColor(Color.GRAY);
                renderer.rectLine(new Vector2(center.x + scaling * i + scaling / 10 * j, 0),
                    new Vector2(center.x + scaling * i + scaling / 10 * j, Gdx.graphics.getHeight()), 1f);
            }
        }
        // draw big lines
        for (int i = -4; i <= 4; i++) {
            renderer.setColor(Color.WHITE);
            renderer.rectLine(new Vector2(center.x + scaling * i, 0),
                new Vector2(center.x + scaling * i, Gdx.graphics.getHeight()), 1f);
            renderer.rectLine(new Vector2(0, Gdx.graphics.getHeight() / 2f + scaling * i),
                new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2f + scaling * i), 1f);
        }

        draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            points.clear();
        }

        renderer.setColor(Color.GOLD);
        renderer.circle(center.x, center.y, 4f);

        renderer.setColor(Color.GOLD);
        renderer.circle(mouse.x * scaling + center.x, mouse.y * scaling + center.y, 2f);

        for (TikTypeStruct tik : points) {
            Vector2 o = new Vector2();
            Vector2 e = new Vector2();
            if (tik.type != DrawType.POLYGON) {
                o = tik.origin.cpy().scl(scaling).add(center);
                e = tik.endPoint.cpy().scl(scaling).add(center);
            }
            switch (tik.type) {
                case LINE:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    renderer.rectLine(o, e, 3f);
                    break;
                case CIRCLE:
                    renderer.set(ShapeRenderer.ShapeType.Line);
                    renderer.circle(o.x, o.y, o.dst(e));
                    break;
                case ARROW:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    drawArrow(renderer, o.x, o.y, e.x, e.y, 20f);
                    break;
                case TEXT:
                    renderer.end();
                    app.batch.begin();
                    app.batch.setProjectionMatrix(renderer.getProjectionMatrix());
                    app.TikzTextFont.setColor(Color.WHITE);
                    app.TikzTextFont.draw(app.batch, tik.data, o.x, o.y + app.TikzTextFont.getCapHeight() / 2, 1f, Align.center, false);
                    app.batch.end();
                    renderer.begin();
                    renderer.setAutoShapeType(true);
                    break;
                case DOTTED_LINE:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    drawDottedLine(renderer, o.x, o.y, e.x, e.y, 20f);
                    break;
                case DOUBLE_ARROW:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    drawTwoHeadedArrow(renderer, o.x, o.y, e.x, e.y, 20f);
                    break;
                case POLYGON:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    // draw the polygon
                    Vector2 vPres = tik.vertices.get(0).cpy().scl(scaling).add(center);
                    for (int i = 1; i < tik.vertices.size; i++) {
                        renderer.rectLine(vPres, tik.vertices.get(i).cpy().scl(scaling).add(center), 2f);
                        vPres = tik.vertices.get(i).cpy().scl(scaling).add(center);
                    }
                    renderer.rectLine(vPres, tik.vertices.get(0).cpy().scl(scaling).add(center), 2f);
                    break;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && points.size > 0) {
                points.removeIndex(points.size - 1);
            }
        }

        if (editing != null && addingPoints) {
            Vector2 o = new Vector2();
            Vector2 e = new Vector2();
            if (currentType != DrawType.POLYGON) {
                editing.endPoint = mouse.cpy();
                o = editing.origin.cpy().scl(scaling).add(center);
                e = editing.endPoint.cpy().scl(scaling).add(center);
            }
            switch (currentType) {
                case LINE:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    renderer.rectLine(o, e, 3f);
                    break;
                case CIRCLE:
                    renderer.set(ShapeRenderer.ShapeType.Line);
                    renderer.circle(o.x, o.y, o.dst(e));
                    break;
                case POLYGON:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    // draw the polygon
                    Vector2 vPres = editing.vertices.get(0).cpy().scl(scaling).add(center);
                    for (int i = 1; i < editing.vertices.size; i++) {
                        renderer.rectLine(vPres, editing.vertices.get(i).cpy().scl(scaling).add(center), 2f);
                        vPres = editing.vertices.get(i).cpy().scl(scaling).add(center);
                    }
                    renderer.rectLine(vPres, mouse.cpy().scl(scaling).add(center), 2f);
                    break;
                case TEXT:
                    o = editing.origin.cpy().scl(scaling).add(center);
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
                    break;
                case ARROW:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    drawArrow(renderer, o.x, o.y, e.x, e.y, 20f);
                    break;
                case DOTTED_LINE:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    drawDottedLine(renderer, o.x, o.y, e.x, e.y, 20f);
                    break;
                case DOUBLE_ARROW:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    drawTwoHeadedArrow(renderer, o.x, o.y, e.x, e.y, 20f);
                    break;
                case ARC:
                    renderer.set(ShapeRenderer.ShapeType.Line);
                    renderer.setColor(Color.GOLD);
                    renderer.arc(e.x, e.y, 100, 0, 180f);
                    break;
                case DROPPED_POLYGON:
                    renderer.set(ShapeRenderer.ShapeType.Filled);
                    // draw the polygon
                    Vector2 vOld = editing.vertices.get(0).cpy().add(mouse).scl(scaling).add(center);
                    for (int i = 1; i < editing.vertices.size; i++) {
                        renderer.rectLine(vOld, editing.vertices.get(i).cpy().add(mouse).scl(scaling).add(center), 2f);
                        vOld = editing.vertices.get(i).cpy().add(mouse).scl(scaling).add(center);
                    }
                    break;
                default:
                    throw new IllegalDrawType("Unknown Draw Type");
            }
        }
    }

    public void draw() {
        final Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        // do inputs stuff
        mouse = new Vector2(Gdx.input.getX() - screen.t.getWidth() / 2, Gdx.graphics.getHeight() - Gdx.input.getY());
        mouse.sub(center).scl(1 / scaling);

        if (snapGrid) {
            mouse.x = (float) Math.round(mouse.x * 10f) / 10f;
            mouse.y = (float) Math.round(mouse.y * 10f) / 10f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            snapGrid = !snapGrid;
        }

        // do stuff at the cursor
        if (Gdx.input.isButtonJustPressed(0) && Gdx.input.getX() > screen.t.getWidth()) {
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
                case POLYGON:
                    if (!addingPoints) {
                        addingPoints = true;
                        editing = new TikTypeStruct(new Array<Vector2>(), currentType);
                        editing.vertices.add(mouse);
                    } else {
                        if (mouse.equals(editing.vertices.get(0))) {
                            addingPoints = false;
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
                    setDrawType(DrawType.POLYGON);
                    break;
                default:
                    throw new IllegalDrawType("Unknown Draw Type");
            }
        } else if (Gdx.input.isButtonJustPressed(1)) {
            addingPoints = false;
        }
    }

    public void drawDottedLine(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float dotSpacing) {
        // Calculate the total distance between the start and end points
        float distance = (float) Math.hypot(x2 - x1, y2 - y1);

        // Calculate the number of dots that fit along the line
        int numDots = (int) (distance / dotSpacing);

        // Calculate the direction of the line (unit vector)
        float directionX = (x2 - x1) / distance;
        float directionY = (y2 - y1) / distance;

        // Draw dots along the line at regular intervals
        for (int i = 0; i <= numDots; i++) {
            float dotX = x1 + i * dotSpacing * directionX;
            float dotY = y1 + i * dotSpacing * directionY;

            shapeRenderer.circle(dotX, dotY, 3f);
        }

        shapeRenderer.circle(x2, y2, 3f);
    }

    ShapeRenderer shapeRenderer = new ShapeRenderer();

    public void drawArrow(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float arrowHeadSize) {
        // Draw the line (shaft of the arrow)
        shapeRenderer.rectLine(x1, y1, x2, y2, 3f);

        // Calculate the angle of the line
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);

        // Calculate the points for the arrowhead triangle
        float arrowX1 = x2 - arrowHeadSize * (float) Math.cos(angle - Math.PI / 6);
        float arrowY1 = y2 - arrowHeadSize * (float) Math.sin(angle - Math.PI / 6);

        float arrowX2 = x2 - arrowHeadSize * (float) Math.cos(angle + Math.PI / 6);
        float arrowY2 = y2 - arrowHeadSize * (float) Math.sin(angle + Math.PI / 6);

        // Draw the arrowhead (a filled triangle)
        shapeRenderer.triangle(x2, y2, arrowX1, arrowY1, arrowX2, arrowY2);
    }

    public void NACA0012(Array<TikTypeStruct> points) {
        points.add(new TikTypeStruct(new Vector2(), new Vector2(), DrawType.LINE));
        float c = 50;
        for (int i = 1; i <= c; i++) {
            float z = (float) (0.12f / 0.2f * (0.296 * sqrt((double) i / c) - 0.126f * ((double) i / c) - 0.3516 * Math.pow((double) i / c, 2) + 0.2843 * Math.pow((double) i / c, 3) - 0.1015 * Math.pow((double) i / c, 4)));
            points.add(new TikTypeStruct(points.peek().endPoint, new Vector2((float) i / c * 2, 2 * z), DrawType.LINE));
        }

        for (int i = (int) c; i > 0; i--) {
            float z = (float) -(0.12f / 0.2f * (0.296 * sqrt((double) i / c) - 0.126f * ((double) i / c) - 0.3516 * Math.pow((double) i / c, 2) + 0.2843 * Math.pow((double) i / c, 3) - 0.1015 * Math.pow((double) i / c, 4)));
            points.add(new TikTypeStruct(points.peek().endPoint, new Vector2((float) i / c * 2, 2 * z), DrawType.LINE));
        }
    }

    public void drawTwoHeadedArrow(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float arrowHeadSize) {
        // Draw the line (shaft of the arrow)
        shapeRenderer.rectLine(x1, y1, x2, y2, 3f);

        // Calculate the angle of the line
        float angle = (float) Math.atan2(y2 - y1, x2 - x1);

        // Arrowhead at the end (x2, y2)
        float arrowX1 = x2 - arrowHeadSize * (float) Math.cos(angle - Math.PI / 6);
        float arrowY1 = y2 - arrowHeadSize * (float) Math.sin(angle - Math.PI / 6);
        float arrowX2 = x2 - arrowHeadSize * (float) Math.cos(angle + Math.PI / 6);
        float arrowY2 = y2 - arrowHeadSize * (float) Math.sin(angle + Math.PI / 6);

        // Draw the arrowhead at the end
        shapeRenderer.triangle(x2, y2, arrowX1, arrowY1, arrowX2, arrowY2);

        // Arrowhead at the start (x1, y1)
        float arrowX3 = x1 + arrowHeadSize * (float) Math.cos(angle - Math.PI / 6);
        float arrowY3 = y1 + arrowHeadSize * (float) Math.sin(angle - Math.PI / 6);
        float arrowX4 = x1 + arrowHeadSize * (float) Math.cos(angle + Math.PI / 6);
        float arrowY4 = y1 + arrowHeadSize * (float) Math.sin(angle + Math.PI / 6);

        // Draw the arrowhead at the start
        shapeRenderer.triangle(x1, y1, arrowX3, arrowY3, arrowX4, arrowY4);
    }


}
