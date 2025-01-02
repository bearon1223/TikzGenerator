package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikz.grid.*;

import java.io.File;
import java.util.Objects;

import static com.tikz.grid.ProgramState.*;

public class MainScreen implements Screen {
    public Table t;
    private final Main app;
    private Stage stage;
    public float tableOffset = 0f;
    TextField textField;
    private GridInterface grid;
    private float time = 1f;
    private boolean hiddenMenu = false;
    private Vector2 startingPan = new Vector2();
    private TextButton bezierButton;
    private FileExplorer fileExplorer;

    Skin skin = new Skin(Gdx.files.internal(ProgramState.lightMode ? "ui/light/uiskin.json" : "ui/uiskin.json"));

    public MainScreen(Main app) {
        this.app = app;
        this.grid = new GridInterface(this, app);

        setUI();
    }

    /**
     * an easing function where f(x) = -x^{4}+2x^{2} when 0 < x < 1
     *
     * @param x (x) in f(x)
     * @return f(x)
     */
    public static float ease(float x) {
        x = clamp(x);
        return (float) (x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2);
    }

    public void openFile(FileHandle file) throws ImproperFileType {
        if(file.exists()) {
            if(!Objects.equals(file.extension(), "txt")) {
                throw new ImproperFileType("The File Type must be .txt");
            }
            try {
                app.setScreen(new ImportTikzScreen(app, grid, file.readString().replaceAll("\\n+", "\n")));
            } catch (GdxRuntimeException e) {
                Dialog errorDialog = new Dialog("Error", skin) {
                    {
                        this.pad(5f);
                        this.padTop(15f);
                        getContentTable().pad(5f);
                        getButtonTable().defaults().prefWidth(100f).padBottom(5f);
                        button("Ok");
                        text(String.format("The File at %s was unable to be loaded", file));
                    }
                };
                errorDialog.show(stage);
            }
        }
        stage.getActors().removeValue(fileExplorer, true);
        fileExplorer = null;
    }

    /**
     * clamps the value of x to be within the minimum and maximum values
     *
     * @param x value to be clamped
     * @return value bound by 0 and 1
     */
    private static float clamp(float x) {
        return x < (float) 0 ? (float) 0 : Math.min(x, (float) 1);
    }

    public MainScreen setGrid(GridInterface grid) {
        this.grid = grid;
        this.grid.screen = this;
        if (this.grid.getDrawType() != DrawType.DROPPED_POLYGON)
            ProgramState.addingPoints = false;
        this.textField.setText(ProgramState.text);
        return this;
    }

    public void addButton(DrawType type, Table table, Skin skin, String name) {
        TextButton b = new TextButton(name, skin);

        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                grid.setDrawType(type);
                table.getStage().setKeyboardFocus(null);
                stage.setKeyboardFocus(null);
                if (type == DrawType.TEXT) {
                    grid.editing = new TikType(new Vector2(), new Vector2(), DrawType.TEXT);
                    ProgramState.addingPoints = true;
                } else if (type == DrawType.BEZIER) {
                    ProgramState.addingPoints = false;
                } else {
                    ProgramState.addingPoints = false;
                }
            }
        });

        table.add(b).spaceBottom(Value.percentHeight(0.0083f, table));
        table.row();
    }

    public void addBezButton(DrawType type, Table table, Skin skin, String name) {
        bezierButton = new TextButton(name, skin);
        bezierButton.setText("Bezier Line (Control: " + bezierControlPointCount + ")");
        bezierButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                grid.setDrawType(type);
                table.getStage().setKeyboardFocus(null);
                stage.setKeyboardFocus(null);
                if (type == DrawType.TEXT) {
                    grid.editing = new TikType(new Vector2(), new Vector2(), DrawType.TEXT);
                    ProgramState.addingPoints = true;
                } else if (type == DrawType.BEZIER) {
                    ProgramState.addingPoints = false;
                } else {
                    ProgramState.addingPoints = false;
                }
            }
        });

        table.add(bezierButton).spaceBottom(Value.percentHeight(0.0083f, table));
        table.row();
    }

    @Override
    public void show() {

    }

    /**
     * Creates an Error Dialog that will display the stacktrace back to the source
     *
     * @param e Exception to be printed to the error dialog (Can be Null)
     */
    public void ErrorDialog(Exception e) {
        System.err.println("Error: Unable to overwrite file");
        StringBuilder sb = new StringBuilder();

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
    public void render(float delta) {
        if(lightMode)
            ScreenUtils.clear(Color.WHITE);
        else
            ScreenUtils.clear(Color.BLACK);
        t.setPosition(tableOffset, 0);
        app.shapeRenderer.setAutoShapeType(true);
        app.shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        app.shapeRenderer.begin();
        app.shapeRenderer.setColor(Color.WHITE);

        grid.render(app.shapeRenderer);

        if(lightMode)
            app.shapeRenderer.setColor(new Color(0xDDDDDDFF));
        else
            app.shapeRenderer.setColor(new Color(Color.BLACK));
        app.shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        app.shapeRenderer.rect(tableOffset, 0, t.getWidth(), Gdx.graphics.getHeight());
        app.shapeRenderer.end();

        if (!hiddenMenu || time < 0.5f) {
            stage.act(delta);
            stage.draw();
        }

        handleInputs();

        if (time < 0.5f) {
            time += Gdx.graphics.getDeltaTime();
            if (hiddenMenu) {
                tableOffset = -t.getWidth() * ease(time * 2);
            } else {
                tableOffset = -t.getWidth() * (1 - ease(time * 2));
            }
        }
    }

    private void handleInputs() {
        // Toggle hiding main menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            hiddenMenu = !hiddenMenu;
            time = 0;
            // remove the keyboard focus from the text box
            stage.setKeyboardFocus(null);
        }

        if (Gdx.input.isButtonJustPressed(0) && Gdx.input.getX() > tableOffset + t.getWidth()) {
            if(isNotFileExplorer())
                stage.setKeyboardFocus(null);
        }

        if (notTyping() && Gdx.input.isKeyJustPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            snapGrid = !snapGrid;
        }

        // change zoom
        float scalingS = Math.min((float) 800 / GridInterface.ROWS, (float) 1200 / GridInterface.COLS);
        float scaling = Math.min((float) Gdx.graphics.getHeight() / GridInterface.ROWS,
            (float) Gdx.graphics.getWidth() / GridInterface.COLS);

        if (stage.getKeyboardFocus() == null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
                zoomLevel += 0.125f;
                zoomLevel = GridInterface.clamp(ProgramState.zoomLevel, 0.25f, 2f);

                // Update tikz font using the gridSpacing factor from the grid
                app.updateTikFont(scaling / scalingS * zoomLevel);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
                zoomLevel -= 0.125f;
                zoomLevel = GridInterface.clamp(zoomLevel, 0.25f, 2f);

                // Update tikz font using the gridSpacing factor from the grid
                app.updateTikFont(scaling / scalingS * zoomLevel);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                zoomLevel = 1;
                grid.panning.set(0, 0);

                // Update tikz font using the gridSpacing factor from the grid
                app.updateTikFont(scaling / scalingS * zoomLevel);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                showGrid = !showGrid;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
                bezierControlPointCount--;
                if (bezierControlPointCount < 1) {
                    bezierControlPointCount = 1;
                }
                bezierButton.setText("Bezier Line (Control: " + bezierControlPointCount + ")");
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
                bezierControlPointCount++;
                if (bezierControlPointCount > 6) {
                    bezierControlPointCount = 6;
                }
                bezierButton.setText("Bezier Line (Control: " + bezierControlPointCount + ")");
            }
        }

        if (MainScreen.checkPanMode()) {
            if (MainScreen.checkJustPanMode()) {
                startingPan = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()).add(grid.panning);
            }
            grid.panning.set(startingPan.cpy().sub(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP) && notTyping()) {
            grid.panning.add(0, scaling / scalingS * 5f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && notTyping()) {
            grid.panning.add(0, -scaling / scalingS * 5f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && notTyping()) {
            grid.panning.add(-scaling / scalingS * 5f, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && notTyping()) {
            grid.panning.add(scaling / scalingS * 5f, 0);
        }
    }

    public static boolean checkPanMode() {
        return Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) ||(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            && Gdx.input.isButtonPressed(Input.Buttons.LEFT));
    }

    public static boolean checkJustPanMode() {
        return Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE) ||(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT));
    }

    public boolean notTyping() {
        return stage.getKeyboardFocus() == null;
    }

    public boolean isNotFileExplorer() {
        return fileExplorer == null;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setWorldSize(width, height);
        stage.getViewport().update(width, height, true);

        // Resize table
        float tableScaling = Math.min(Gdx.graphics.getWidth() / 1200f, Gdx.graphics.getHeight() / 800f);
        t.setSize(225 * tableScaling, height);
        t.defaults().prefWidth(225 * tableScaling);
        t.invalidate();
        t.layout();

        // Calculate gridSpacing and update font
        float scalingS = Math.min((float) 800 / GridInterface.ROWS, (float) 1200 / GridInterface.COLS);
        float scaling = Math.min((float) Gdx.graphics.getHeight() / GridInterface.ROWS,
            (float) Gdx.graphics.getWidth() / GridInterface.COLS);

        app.updateFont(scaling / scalingS);
        app.updateTikFont(scaling * ProgramState.zoomLevel / scalingS);

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
                ((TextButton) actor).setStyle(buttonStyle);
            } else if (actor instanceof TextField) {
                ((TextField) actor).setStyle(fieldStyle);
            }
        }

        // Invalidate hierarchy to ensure layout refresh
        t.invalidateHierarchy();

        // scale the table offset as well
        if (hiddenMenu) {
            tableOffset = -t.getWidth() * ease(time * 2);
        } else {
            tableOffset = -t.getWidth() * (1 - ease(time * 2));
        }

        if(fileExplorer != null) {
            fileExplorer.resize(app);
        }
    }

    public void setUI() {
        if(stage != null) {
            stage.clear();
            stage.dispose();
        }
        stage = new Stage(new ScreenViewport());

        t = new Table();
        t.setSkin(skin);
        t.defaults().prefWidth(Value.percentWidth(0.9f, t));
//        t.defaults().prefHeight(Value.percentHeight(0.05625f, t));
        t.defaults().prefHeight(Value.percentHeight(0.052f, t));

        t.setSize(0.16667f*Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // type buttons
        addButton(DrawType.LINE, t, skin, "Line");
        addButton(DrawType.CIRCLE, t, skin, "Circle");
        addButton(DrawType.MULTI_LINE, t, skin, "Multi-Line / Polygon");
        addBezButton(DrawType.BEZIER, t, skin, "Bezier Line");

        TextButton colorButton = new TextButton("Tik Color: " + getColorName(selectedColor), skin);

        colorButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedColor.equals(Color.BLACK)) {
                    selectedColor = Color.RED;
                } else if (selectedColor.equals(Color.RED)) {
                    selectedColor = Color.BLUE;
                } else if (selectedColor.equals(Color.BLUE)) {
                    selectedColor = Color.ORANGE;
                } else if (selectedColor.equals(Color.ORANGE)) {
                    selectedColor = Color.YELLOW;
                } else if (selectedColor.equals(Color.YELLOW)) {
                    selectedColor = Color.CYAN;
                } else if (selectedColor.equals(Color.CYAN)) {
                    selectedColor = Color.GRAY;
                } else {
                    selectedColor = Color.BLACK;
                }
                colorButton.setText("Tik Color: " + (getColorName(selectedColor)));
            }
        });

        t.add(colorButton).spaceTop(Value.percentHeight(0.025f, t)).spaceBottom(Value.percentHeight(0.0083f, t));
        t.row();

        TextButton dashed = new TextButton("Dashed: " + (ProgramState.dashed ? "True" : "False"), skin);

        dashed.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ProgramState.dashed = !ProgramState.dashed;
                dashed.setText("Dashed: " + (ProgramState.dashed ? "True" : "False"));
            }
        });

        t.add(dashed).spaceBottom(Value.percentHeight(0.0083f, t));
        t.row();

        String arrowState;
        if(frontArrow && backArrow) {
            arrowState = "Front and Back";
        } else if (frontArrow) {
            arrowState = "Front";
        } else if (backArrow){
            arrowState = "Back";
        } else {
            arrowState = "None";
        }

        TextButton arrow = new TextButton("Arrow Heads: " + arrowState, skin);

        arrow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(frontArrow && backArrow) {
                    frontArrow = false;
                    backArrow = false;
                } else if (!(frontArrow || backArrow)) {
                    frontArrow = true;
                    backArrow = false;
                } else if (frontArrow) {
                    frontArrow = false;
                    backArrow = true;
                } else {
                    frontArrow = true;
                    backArrow = true;
                }

                String arrowState;
                if(frontArrow && backArrow) {
                    arrowState = "Front and Back";
                } else if (frontArrow) {
                    arrowState = "Front";
                } else if (backArrow){
                    arrowState = "Back";
                } else {
                    arrowState = "None";
                }
                arrow.setText("Arrow Heads: " + arrowState);
            }
        });

        t.add(arrow).spaceBottom(Value.percentHeight(0.0083f, t));
        t.row();

        TextButton filled = new TextButton("Is Filled: " + (isFilled ? "True" : "False"), skin);

        filled.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isFilled = !isFilled;
                filled.setText("Is Filled: " + (isFilled ? "True" : "False"));
            }
        });

        t.add(filled).spaceBottom(Value.percentHeight(0.0083f, t));
        t.row();

        // Create TextField
        textField = new TextField(ProgramState.text, skin);
        t.add(textField).height(Value.percentHeight(0.0375f, t)).
            spaceTop(Value.percentHeight(0.025f, t)).
            spaceBottom(Value.percentHeight(0.0083f, t)).
            width(Value.percentWidth(0.9f, t));
        t.row();

        addButton(DrawType.TEXT, t, skin, "Insert Text");

        textField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ProgramState.text = textField.getText();
            }
        });

        TextButton importTikz = new TextButton("Import existing Tikz", skin);

        importTikz.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.setScreen(new ImportTikzScreen(app, grid));
            }
        });

        t.add(importTikz).spaceTop(Value.percentHeight(20 / 800f, t));
        t.row();

        TextButton importFromFileTikz = new TextButton("Import from File", skin);

        importFromFileTikz.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fileExplorer = new FileExplorer(skin, MainScreen.this, file -> {
                    try {
                        openFile(file);
                    } catch (ImproperFileType e) {
                        Dialog errorDialog = new Dialog("Error", skin) {
                            {
                                this.pad(5f);
                                this.padTop(15f);
                                getContentTable().pad(5f);
                                getButtonTable().defaults().prefWidth(100f).padBottom(5f);
                                button("Ok");
                                text("The file must be a txt file");
                            }
                        };
                        errorDialog.show(stage);
                    }
                });
                fileExplorer.resize(app);
                stage.addActor(fileExplorer);
            }
        });

        t.add(importFromFileTikz).spaceTop(Value.percentHeight(0.0083f, t));
        t.row();

        TextButton saveToFile = new TextButton("Save to File", skin);

        saveToFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fileExplorer = new FileExplorer(skin, MainScreen.this, new FileExplorer.FileExplorerListener() {
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
                                openFile(new FileHandle(""));
                                app.setScreen(new ShowTikz(app, grid, output));
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

        TextButton convertToTikz = new TextButton("Convert to Tikz", skin);

        convertToTikz.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Generating Tikz Points");
                app.setScreen(new ShowTikz(app, grid, MakeTikz.convert(grid.points)));
            }
        });

        t.add(convertToTikz).spaceTop(Value.percentHeight(0.0083f, t));
        t.row();

        TextButton lightMode = new TextButton("Toggle Theme", skin);

        lightMode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ProgramState.lightMode = !ProgramState.lightMode;
                skin = new Skin(Gdx.files.internal(ProgramState.lightMode ? "ui/light/uiskin.json" : "ui/uiskin.json"));
                setUI();
            }
        });

        t.add(lightMode).spaceBottom(Value.percentHeight(20 / 800f, t)).
            spaceTop(Value.percentHeight(0.0083f, t));
        t.row();

        stage.addActor(t);

        // Set the stage as the input processor
        Gdx.input.setInputProcessor(stage);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public static String getColorName(Color color) {
        if (color.equals(Color.RED)) {
            return "Red";
        } else if (color.equals(Color.BLUE)) {
            return "Blue";
        } else if (color.equals(Color.ORANGE)) {
            return "Orange";
        } else if (color.equals(Color.YELLOW)) {
            return "Yellow";
        } else if (color.equals(Color.CYAN)) {
            return "Cyan";
        } else if (color.equals(Color.BLACK)){
            return "Black";
        } else if (color.equals(Color.GRAY)) {
            return "Gray";
        }
        return color.toString();
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
        grid.dispose();
        stage.dispose();
    }
}
