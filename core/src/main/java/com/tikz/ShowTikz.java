package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.GridInterface;

public class ShowTikz implements Screen {
    private final Stage stage;
    Table t;

    public ShowTikz(Main app, GridInterface grid, String tikz) {
        stage = new Stage(new ScreenViewport());
        t = new Table();
        t.setPosition(200, 0);
        t.setSize(Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight());

        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

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
        stage.getViewport().update(width, height, true);
        t.setSize(width - 200, height);
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
