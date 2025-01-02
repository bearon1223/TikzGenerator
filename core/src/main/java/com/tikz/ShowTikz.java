package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.GridInterface;
import com.tikz.grid.MakeTikz;
import com.tikz.grid.ProgramState;

import java.io.File;
import java.util.Objects;

public class ShowTikz implements Screen {
    private final Stage stage;
    private final Main app;
    Table t;
    Skin skin;
    private final GridInterface grid;

    public ShowTikz(Main app, GridInterface grid, String tikz) {
        this.app = app;
        this.grid = grid;

        skin = new Skin(Gdx.files.internal(ProgramState.lightMode ? "ui/light/uiskin.json" : "ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());
        t = new Table();
        t.setSkin(skin);
        t.setPosition(0, 0);
        t.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        TextArea textArea = new TextArea(tikz, skin);
        textArea.setPrefRows(textArea.getText().split("\\n").length);
        textArea.setDisabled(false);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFlickScroll(false);
        scrollPane.layout();

        TextButton returnButton = new TextButton("Return", skin);

        returnButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new MainScreen(app).setGrid(grid));
            }
        });

        TextButton saveToFile = new TextButton("Save to File", skin);

        saveToFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileExplorer fileExplorer = new FileExplorer(skin, new FileExplorer.FileExplorerListener() {
                    @Override
                    public void fileSelected(FileHandle file) {
                    }

                    @Override
                    public void submitPressed(FileHandle file, String fileName) {
                        if(fileName.equals(file.name())) {
                            // If we are overwriting a file, confirm if they truly want to overwrite.
                            final Dialog[] confirmDialog = new Dialog[1]; // Get around Java Restrictions
                            confirmDialog[0] = new Dialog("Confirmation", skin) {
                                {
                                    this.pad(5f);
                                    this.padTop(15f);
                                    getContentTable().pad(5f);
                                    getButtonTable().defaults().prefWidth(100f).padBottom(5f);

                                    text("Are you Sure?");

                                    TextButton submit = new TextButton("Submit", skin);

                                    // if yes, try to overwrite, if it fails throw an error dialog
                                    submit.addListener(new ClickListener() {
                                        @Override
                                        public void clicked(InputEvent event, float x, float y) {
                                            confirmDialog[0].hide();
                                            try {
                                                if(!Objects.equals(file.extension(), "txt")){
                                                    throw new ImproperFileType("The file must end with a txt extension");
                                                }
                                                file.writeString(MakeTikz.convert(grid.points), false);
                                            } catch (Exception e) {
                                                ErrorDialog(e);
                                            }
                                        }
                                    });
                                    this.getButtonTable().add(submit);
                                    button("Cancel");
                                }
                            };
                            confirmDialog[0].show(stage);
                        } else {
                            try {
                                if(!fileName.endsWith(".txt")){
                                    fileName += ".txt";
                                }
                                String output = MakeTikz.convert(grid.points);
                                FileHandle newFile = Gdx.files.absolute(file.file().getParent() + File.separator + fileName);
                                newFile.writeString(output, false);
                                app.setScreen(new MainScreen(app).setGrid(grid));
//                                app.setScreen(new ShowTikz(app, grid, output));
                            } catch (Exception e) {
                                ErrorDialog(e);
                            }
                        }
                    }
                });
                fileExplorer.resize(app);
                stage.addActor(fileExplorer);
            }
        });

        t.add(saveToFile).spaceTop(Value.percentHeight(0.0083f, t));
        t.row();

        t.add(returnButton).width(Value.percentWidth(0.15f, t)).height(Value.percentHeight(0.0625f, t))
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

    /**
     * Creates an Error Dialog that will display the stacktrace back to the source
     *
     * @param e Exception to be printed to the error dialog (Can be Null)
     */
    public void ErrorDialog(Exception e) {
        System.err.println("Error: Unable to overwrite file");
        com.badlogic.gdx.utils.StringBuilder sb = new StringBuilder();

        if(e != null) {
            sb.append(e.getMessage()).append("\n\n");

            StackTraceElement[] elements = e.getStackTrace();
            for (StackTraceElement stackTraceElement : elements) {
                sb.append(stackTraceElement.toString()).append("\n");
            }
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

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        stage.clear();
        stage.dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        grid.dispose();
    }
}
