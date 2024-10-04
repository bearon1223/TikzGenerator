package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.DrawType;
import com.tikz.grid.GridInterface;
import com.tikz.grid.IllegalDrawType;
import com.tikz.grid.ImportFromTikz;

public class ImportTikzScreen implements Screen {
    private final Stage stage;
    private Table t;
    private final Main app;
    TextArea textArea;

    public ImportTikzScreen(Main app, GridInterface gridInterface) {
        this.app = app;
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        t = new Table(skin);
        t.setFillParent(true);

        textArea = new TextArea("", skin);
        textArea.setDisabled(false);

        ScrollPane scrollPane = new ScrollPane(textArea);

        t.add(scrollPane).width(Value.percentWidth(1, t))
            .height(Value.percentHeight(1f - 60 / 800f, t)).colspan(3).padBottom(10f);
        t.row();

        TextButton importTik = new TextButton("Import Tikz Code", skin);
        t.add(importTik).width(Value.percentWidth(0.2f, t))
            .height(Value.percentHeight(50 / 800f, t));

        importTik.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    gridInterface.points = ImportFromTikz.FromTikToPoints(textArea.getText());
                } catch (NullPointerException | NumberFormatException | GdxRuntimeException | IllegalDrawType e) {
                    System.err.println("Error: Improper Tikz Code was imported");

                    StringBuilder sb = new StringBuilder();
                    sb.append(e.getMessage()).append("\n\n");

                    StackTraceElement[] elements = e.getStackTrace();
                    for (StackTraceElement stackTraceElement : elements) {
                        sb.append(stackTraceElement.toString()).append("\n");
                    }

                    Dialog errorDialog = new Dialog("", skin) {
                        {
                            getContentTable().pad(5f);
                            getButtonTable().defaults().prefWidth(100f).padBottom(5f);
                            button("Ok");
                            text(sb.toString());
                        }
                    };

                    errorDialog.show(stage);
                }
            }
        });

        TextButton importVectors = new TextButton("Import List of Vectors", skin);
        t.add(importVectors).width(Value.percentWidth(0.2f, t))
            .height(Value.percentHeight(50 / 800f, t));

        importVectors.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    gridInterface.editing = ImportFromTikz.FromVectorsToPoints(textArea.getText());
                    gridInterface.setDrawType(DrawType.DROPPED_POLYGON);
                    gridInterface.addingPoints = true;
                } catch (NullPointerException | NumberFormatException | GdxRuntimeException | IllegalDrawType e) {
                    System.err.println("Error: Improper Tikz Code was imported");

                    StringBuilder sb = new StringBuilder();
                    sb.append(e.getMessage()).append("\n\n");

                    StackTraceElement[] elements = e.getStackTrace();
                    for (StackTraceElement stackTraceElement : elements) {
                        sb.append(stackTraceElement.toString()).append("\n");
                    }

                    Dialog errorDialog = new Dialog("", skin) {
                        {
                            getContentTable().pad(5f);
                            getButtonTable().defaults().prefWidth(100f).padBottom(5f);
                            button("Ok");
                            text(sb.toString());
                        }
                    };

                    errorDialog.show(stage);
                }
            }
        });

        TextButton returnToMain = new TextButton("Return to Main Editor", skin);
        t.add(returnToMain).width(Value.percentWidth(0.2f, t))
            .height(Value.percentHeight(50 / 800f, t));

        returnToMain.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new MainScreen(app).setGrid(gridInterface));
            }
        });

        stage.addActor(t);

        Gdx.input.setInputProcessor(stage);
    }

    public ImportTikzScreen(Main app, GridInterface gridInterface, String tikzCode) {
        this(app, gridInterface);
        textArea.setText(tikzCode);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
        t.setSize(width - 200, height);
        t.invalidate();
        t.layout();

        // Calculate scaling and update font
        float scalingS = Math.min((float) 800 / GridInterface.ROWS, (float) 1200 / GridInterface.COLS);
        float scaling = Math.min((float) Gdx.graphics.getHeight() / GridInterface.ROWS, (float) Gdx.graphics.getWidth() / GridInterface.COLS);
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

    }
}
