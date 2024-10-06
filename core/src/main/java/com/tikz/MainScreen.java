package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.DrawType;
import com.tikz.grid.GridInterface;
import com.tikz.grid.MakeTikz;
import com.tikz.grid.TikTypeStruct;

import javax.swing.*;
import java.io.File;

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
        t.setSkin(skin);
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
        addButton(DrawType.POLYGON, t, skin, "Multi-Line / Polygon");

        TextButton importTikz = new TextButton("Import existing Tikz", skin);

        importTikz.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new ImportTikzScreen(app, grid));
            }
        });

        t.add(importTikz).spaceTop(Value.percentHeight(0.0416f, t));
        t.row();

        TextButton importFromFileTikz = new TextButton("Import From File", skin);

        importFromFileTikz.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                File file = openFile();
                if (file != null)
                    if (file.exists())
                        try {
                            app.setScreen(new ImportTikzScreen(app, grid,
                                Gdx.files.absolute(file.getAbsolutePath()).readString().replace("\n\n", "")));
                        } catch (GdxRuntimeException e) {
                            Dialog errorDialog = new Dialog("", skin) {
                                {
                                    getContentTable().pad(5f);
                                    getButtonTable().defaults().prefWidth(100f).padBottom(5f);
                                    button("Ok");
                                    text(String.format("The File at %s was unable to be loaded", file.getAbsolutePath()));
                                }
                            };

                            errorDialog.show(stage);
                        }
            }
        });

        t.add(importFromFileTikz).spaceTop(Value.percentHeight(0.0083f, t));
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

        t.add(convertToTikz).spaceBottom(Value.percentHeight(0.0416f, t)).spaceTop(Value.percentHeight(0.0083f, t));
        t.row();

        // Create TextField
        textField = new TextField("", skin);
        t.add(textField).height(Value.percentHeight(0.0375f, t)).spaceBottom(Value.percentHeight(0.0083f, t));
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

    public File openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public MainScreen setGrid(GridInterface grid) {
        this.grid = grid;
        if (this.grid.getDrawType() != DrawType.DROPPED_POLYGON)
            this.grid.addingPoints = false;
        return this;
    }

    public void addButton(DrawType type, Table table, Skin skin, String name) {
        TextButton b = new TextButton(name, skin);

        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                grid.setDrawType(type);
                table.getStage().setKeyboardFocus(null);
                if (type == DrawType.TEXT) {
                    grid.editing = new TikTypeStruct(new Vector2(), new Vector2(), DrawType.TEXT);
                    grid.addingPoints = true;
                } else {
                    grid.addingPoints = false;
                }
            }
        });

        table.add(b).spaceBottom(Value.percentHeight(0.0083f, table));
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

        // Resize table
        t.setSize(200 * width / 1200f, height);
        t.defaults().prefWidth(200 * width / 1200f);
        t.invalidate();
        t.layout();

        // Calculate scaling and update font
        float scalingS = Math.min((float) 800 / GridInterface.ROWS, (float) 1200 / GridInterface.COLS);
        float scaling = Math.min((float) Gdx.graphics.getHeight() / GridInterface.ROWS,
            (float) Gdx.graphics.getWidth() / GridInterface.COLS);

        app.updateFont(scaling / scalingS);

        // Update skin with the new editor font
        Skin skin = t.getSkin();
        skin.add("default-font", app.editorFont, BitmapFont.class);  // Update the default font

        // Create a new TextButton style and assign the updated font
        TextButton.TextButtonStyle buttonStyle = skin.get(TextButton.TextButtonStyle.class);
        buttonStyle.font = app.editorFont;

        TextField.TextFieldStyle fieldStyle = skin.get(TextField.TextFieldStyle.class);
        fieldStyle.font = app.editorFont;

        // Update all text buttons to use the new style
        for (Actor actor : t.getChildren()) {
            if (actor instanceof TextButton) {
                ((TextButton) actor).setStyle(buttonStyle);  // Apply new style with updated font
            } else if (actor instanceof TextField) {
                ((TextField) actor).setStyle(fieldStyle);
            }
        }

        // Invalidate hierarchy to ensure layout refresh
        t.invalidateHierarchy();
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
