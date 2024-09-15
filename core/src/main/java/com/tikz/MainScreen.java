package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.*;

public class MainScreen implements Screen {
    private final Main app;
    private GridInterface grid;
    private final Stage stage;
    public final Table t;
    TextField textField;

    public MainScreen(Main app) {
        this.app = app;
        this.grid = new GridInterface(this, app);

        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        t = new Table();
        t.defaults().prefWidth(Value.percentWidth(0.9f, t));
        t.defaults().prefHeight(Value.percentHeight(0.05625f, t));

        t.setSize(200, Gdx.graphics.getHeight());

        // type buttons
        addButton(DrawType.LINE, t, skin, "Line");

        addButton(DrawType.DOTTED_LINE, t, skin, "Dotted Line");

        addButton(DrawType.ARROW, t, skin, "Arrow");

        addButton(DrawType.DOUBLE_ARROW, t, skin, "Double Arrow");

        addButton(DrawType.TEXT, t, skin, "Text");

        addButton(DrawType.CIRCLE, t, skin, "Circle");

        addButton(DrawType.POLYGON, t, skin, "Polygon");

        TextButton importTikz = new TextButton("Import existing Tikz", skin);

        importTikz.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new ImportTikzScreen(app, grid));
            }
        });

        t.add(importTikz).spaceTop(50);
        t.row();

        TextButton convertToTikz = new TextButton("Convert to Tikz", skin);

        convertToTikz.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Generating Tikz Points");
                System.out.println(MakeTikz.convert(grid.points));
                app.setScreen(new ShowTikz(app, grid, MakeTikz.convert(grid.points)));
            }
        });

        t.add(convertToTikz).spaceBottom(50).spaceTop(10);
        t.row();

        // Create TextField
        textField = new TextField("", skin);
        t.add(textField).height(Value.percentHeight(0.0375f, t)).spaceBottom(10);
        t.row();

        // Create Submit Button
        TextButton submitButton = new TextButton("Submit", skin);
        t.add(submitButton);
        t.row();

        // Handle Button Click
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                grid.text = textField.getText();
                stage.setKeyboardFocus(null);
            }
        });

        stage.addActor(t);

        // Set the stage as the input processor
        Gdx.input.setInputProcessor(stage);
    }

    public MainScreen setGrid(GridInterface grid) {
        this.grid = grid;
        return this;
    }

    public void addButton(DrawType type, Table table, Skin skin, String name) {
        TextButton b = new TextButton(name, skin);

        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                grid.setDrawType(type);
                table.getStage().setKeyboardFocus(null);
                if(type == DrawType.TEXT) {
                    grid.editing = new TikTypeStruct(new Vector2(), new Vector2(), DrawType.TEXT);
                    grid.addingPoints = true;
                } else {
                    grid.addingPoints = false;
                }
            }
        });

        table.add(b).spaceBottom(10);
        table.row();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        app.shapeRenderer.setAutoShapeType(true);
        app.shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        app.shapeRenderer.begin();
        app.shapeRenderer.setColor(Color.WHITE);

        grid.drawGrid(app.shapeRenderer);

        app.shapeRenderer.setColor(Color.BLACK);
        app.shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        app.shapeRenderer.rect(0, 0, t.getWidth(), Gdx.graphics.getHeight());
        app.shapeRenderer.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
        t.setSize(200 * width/1200f, height);
        t.defaults().prefWidth(200*width/1200f);
        t.invalidate();
        t.layout();
        float scalingS = Math.min((float) 800 / grid.ROWS, (float) 1200 / grid.COLS);
        float scaling = Math.min((float) Gdx.graphics.getHeight() / grid.ROWS, (float) Gdx.graphics.getWidth() / grid.COLS);
        app.updateFont(scaling/scalingS);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
