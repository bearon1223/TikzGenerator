package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.GridInterface;
import com.tikz.grid.ImportFromTikz;

public class ImportTikzScreen implements Screen {
    private final Stage stage;

    public ImportTikzScreen(Main app, GridInterface gridInterface) {
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        Table table = new Table(skin);
        table.setFillParent(true);

        TextArea textArea = new TextArea("", skin);
        textArea.setDisabled(false);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFadeScrollBars(false);
        table.add(textArea).width(Value.percentWidth(1, table))
            .height(Value.percentHeight(1f-60/800f, table)).colspan(2).padBottom(5f);
        table.row();

        TextButton importTik = new TextButton("Import Tikz Code", skin);
        table.add(importTik).width(Value.percentWidth(0.2f, table))
            .height(Value.percentHeight(50/800f, table));

        importTik.addListener(new ClickListener() {
            @Override
        public void clicked(InputEvent event, float x, float y) {
            try {
                gridInterface.points = ImportFromTikz.FromTikToPoints(textArea.getText());
            } catch (NullPointerException | NumberFormatException | GdxRuntimeException e) {
                System.err.println("Error: Improper Tikz Code was imported");

                StringBuilder sb = new StringBuilder();
                sb.append(e.getMessage()).append("\n\n");

                StackTraceElement[] elements = e.getStackTrace();
                for(StackTraceElement stackTraceElement : elements) {
                    sb.append(stackTraceElement.toString()).append("\n");
                }

                // Show the error in a dialog
                Dialog dialog = new Dialog("Error", skin);
                dialog.setClip(false);
                dialog.setSize(table.getWidth()/2, table.getHeight()/2);

                // Create label for error and add padding
                Label errorLabel = new Label(sb.toString(), skin);
                ScrollPane scrollPane = new ScrollPane(errorLabel);
                dialog.getContentTable().add(scrollPane);

                dialog.getContentTable().pad(5);

                TextButton ok = new TextButton("OK", skin);
                ok.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        dialog.hide();
                    }
                });

                dialog.add(ok).width(100f).height(50f).pad(5).center();

                // Show the dialog
                dialog.show(stage);
            }
        }
        });

        TextButton returnToMain = new TextButton("Return to Main Editor", skin);
        table.add(returnToMain).width(Value.percentWidth(0.2f, table))
            .height(Value.percentHeight(50/800f, table));

        returnToMain.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new MainScreen(app).setGrid(gridInterface));
            }
        });

        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0,0,0,1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
