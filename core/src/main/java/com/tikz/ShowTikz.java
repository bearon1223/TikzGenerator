package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.GridInterface;

public class ShowTikz implements Screen {
    private final Stage stage;
    private final Main app;
    Table t;

    public ShowTikz(Main app, GridInterface grid, String tikz) {
        this.app = app;

        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());
        t = new Table();
        t.setSkin(skin);
        t.setPosition(200, 0);
        t.setSize(Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight());

        TextArea textArea = new TextArea(tikz, skin);
        textArea.setDisabled(false);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFadeScrollBars(false);
        t.add(textArea).width(Value.percentWidth(1, t)).height(Value.percentHeight(1, t));

        TextButton b = new TextButton("Return", skin);
        b.setPosition(10, 10);
        b.setSize(180, 50);

        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new MainScreen(app).setGrid(grid));
            }
        });

        stage.addActor(t);
        stage.addActor(b);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
        // TODO: Add methods to show Tikz in a text field
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
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
    }
}
