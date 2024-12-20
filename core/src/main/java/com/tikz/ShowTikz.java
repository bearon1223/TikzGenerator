package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.GridInterface;
import com.tikz.grid.ProgramState;

public class ShowTikz implements Screen {
    private final Stage stage;
    private final Main app;
    Table t;
    private final GridInterface grid;

    public ShowTikz(Main app, GridInterface grid, String tikz) {
        this.app = app;
        this.grid = grid;

        Skin skin = new Skin(Gdx.files.internal(ProgramState.lightMode ? "ui/light/uiskin.json" : "ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());
        t = new Table();
        t.setSkin(skin);
        t.setPosition(0, 0);
        t.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        TextArea textArea = new TextArea(tikz, skin);
        textArea.setPrefRows(textArea.getText().split("\n").length);
        textArea.setDisabled(false);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFlickScroll(false);
        scrollPane.layout();
//        scrollPane.setForceScroll(false, true);

//        textArea.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent changeEvent, Actor actor) {
//                textArea.setPrefRows(textArea.getText().split("\n").length);
//                System.out.println(textArea.getText().split("\n").length);
//                scrollPane.layout();
//            }
//        });

        TextButton b = new TextButton("Return", skin);

        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new MainScreen(app).setGrid(grid));
            }
        });

        t.add(b).width(Value.percentWidth(0.15f, t)).height(Value.percentHeight(0.0625f, t))
            .padRight(Value.percentWidth(0.00417f, t)).bottom().padBottom(Value.percentWidth(0.00417f, t));

        t.add(scrollPane).width(Value.percentWidth(0.833f, t)).height(Value.percentHeight(1, t));
        stage.addActor(t);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ProgramState.lightMode ? new Color(0.5f, 0.5f, 0.5f, 1) : Color.BLACK);
        try {
            stage.act(delta);
            stage.draw();
        } catch (ArrayIndexOutOfBoundsException ignored){
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
        t.setSize(width, height);
        t.invalidate();
        t.layout();

        // Calculate gridSpacing and update font
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
            } else if (actor instanceof  TextField) {
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
        grid.dispose();
    }
}
