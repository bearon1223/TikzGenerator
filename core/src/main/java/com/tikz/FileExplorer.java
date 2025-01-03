package com.tikz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import java.io.File;

public class FileExplorer extends Window {
    private final Array<FileHandle> fileList;
    private final List<String> fileNames;
    private final Label directoryLabel;
    private FileHandle currentDirectory;
    public TextField fileName;

    public FileExplorer(Skin skin, FileExplorerListener listener) {
        super("File Explorer", skin);
        super.setResizable(true);
        directoryLabel = new Label("", skin);
        fileList = new Array<>();
        fileNames = new List<>(skin);
        TextButton backButton = new TextButton("Back", skin);

        this.pad(5f);
        this.padTop(15f * Gdx.graphics.getHeight() / 800f);
        this.setSize(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        this.setPosition(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 4f);

        this.add(directoryLabel).colspan(3).padTop(5f).padBottom(5f).row();
        this.add(new ScrollPane(fileNames)).expand().fill().colspan(3).padBottom(5f).row();

        fileName = new TextField("", skin);
        this.add(fileName).fillX().colspan(3).height(Value.percentHeight(20/400f, this)).pad(5f).row();

        this.add(backButton).left().size(Value.percentWidth(0.25f, this), Value.percentHeight(0.1f, this));

        currentDirectory = Gdx.files.absolute(System.getProperty("user.home") + File.separator + "Desktop");
        if (Gdx.files.absolute(System.getProperty("user.home") + File.separator + "OneDrive" + File.separator + "Desktop").exists()) {
            currentDirectory = Gdx.files.absolute(System.getProperty("user.home") + File.separator + "OneDrive" + File.separator + "Desktop");
        }
        updateFileList();

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (!currentDirectory.file().getParentFile().getPath().equals("/")) {
                        currentDirectory = Gdx.files.absolute(currentDirectory.file().getParent());
                        updateFileList();
                    }
                } catch (NullPointerException e) {
                    Dialog errorDialog = new Dialog("Error", skin) {
                        {
                            this.pad(5f);
                            this.padTop(15f);
                            getContentTable().pad(5f);
                            getButtonTable().defaults().prefWidth(100f).padBottom(5f);
                            button("Ok");
                            text("There is no Parent Directory");
                        }
                    };
                    errorDialog.show(FileExplorer.super.getStage());
                }
            }
        });

        TextButton close = new TextButton("Close", skin);
        close.setWidth(this.getWidth() / 4f);

        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getStage().getActors().removeValue(FileExplorer.this, false);
            }
        });
        this.add(close).center().size(Value.percentWidth(0.25f, this), Value.percentHeight(0.1f, this));

        TextButton selectButton = new TextButton("Submit", skin);
        selectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.submitPressed(getFile(), fileName.getText());
            }
        });

        this.add(selectButton).right().size(Value.percentWidth(0.25f, this), Value.percentHeight(0.1f, this)).row();

        fileNames.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileHandle selected = fileList.get(fileNames.getSelectedIndex());
                if (selected.isDirectory() && this.getTapCount() == 2) {
                    currentDirectory = selected;
                    updateFileList();
                } else if (!selected.isDirectory()) {
                    if (this.getTapCount() == 2) {
                        listener.fileSelected(getFile());
                    } else if (this.getTapCount() == 1) {
                        fileName.setText(getFile().name());
                    }
                }
            }
        });
    }

    public void resize(Main app) {
        this.padTop(20f * Gdx.graphics.getHeight() / 800f);

        this.getSkin().add("default-font", app.editorFont, BitmapFont.class);  // Update the default font

        // Create a new TextButton style and assign the updated font
        TextButton.TextButtonStyle buttonStyle = this.getSkin().get(TextButton.TextButtonStyle.class);
        buttonStyle.font = app.editorFont;

        Label.LabelStyle labelStyle = this.getSkin().get(Label.LabelStyle.class);
        labelStyle.font = app.editorFont;

        List.ListStyle listStyle = this.getSkin().get(List.ListStyle.class);
        listStyle.font = app.editorFont;

        // Update all text buttons to use the new style
        for (Actor actor : this.getChildren()) {
            if (actor instanceof TextButton) {
                ((TextButton) actor).setStyle(buttonStyle);
            } else if (actor instanceof Label) {
                ((Label) actor).setStyle(labelStyle);
            } else if (actor instanceof List) {
                ((List<?>) actor).setStyle(listStyle);
            }
        }

        super.getTitleLabel().setStyle(labelStyle);
    }

    public FileHandle getFile() {
        return fileList.get(fileNames.getSelectedIndex());
    }

    private void updateFileList() {
        directoryLabel.setText("Current Directory: " + currentDirectory.path());
        Array<FileHandle> files = new Array<>(currentDirectory.list());
        fileList.clear();
        Array<String> fileNames = new Array<>();
        for (FileHandle file : files) {
            fileNames.add(file.name());
            fileList.add(file);
        }
        this.fileNames.setItems(fileNames);
    }

    public interface FileExplorerListener {
        void fileSelected(FileHandle file);

        default void submitPressed(FileHandle file, String fileName){
            fileSelected(file);
        }
    }
}
