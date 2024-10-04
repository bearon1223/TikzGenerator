package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
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

    TextArea textArea;

    public ImportTikzScreen(Main app, GridInterface gridInterface) {
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        Table table = new Table(skin);
        table.setFillParent(true);

        textArea = new TextArea("", skin);
        textArea.setDisabled(false);

        ScrollPane scrollPane = new ScrollPane(textArea);

        table.add(scrollPane).width(Value.percentWidth(1, table))
            .height(Value.percentHeight(1f-60/800f, table)).colspan(3).padBottom(10f);
        table.row();

        TextButton importTik = new TextButton("Import Tikz Code", skin);
        table.add(importTik).width(Value.percentWidth(0.2f, table))
            .height(Value.percentHeight(50/800f, table));

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
                for(StackTraceElement stackTraceElement : elements) {
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
        table.add(importVectors).width(Value.percentWidth(0.2f, table))
            .height(Value.percentHeight(50/800f, table));

        importVectors.addListener(new ClickListener(){
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
                    for(StackTraceElement stackTraceElement : elements) {
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

    public ImportTikzScreen(Main app, GridInterface gridInterface, String tikzCode) {
        this(app, gridInterface);
        textArea.setText(tikzCode);
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
