package com.tikz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * {@link com.badlogic.gdx.Game} implementation shared by all platforms.
 */
public class Main extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont TikzTextFont;
    public BitmapFont editorFont;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
        updateFont(1);
        updateTikFont(1);
        setScreen(new MainScreen(this));
    }

    public void updateFont(float scale) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Times New Roman.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = (int) (14f*scale);
        try {
            editorFont = generator.generateFont(parameter);
        } catch (GdxRuntimeException ignored){
        }
    }

    public void updateTikFont(float scale) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Times New Roman.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int) (60f*scale);
        try {
            TikzTextFont = generator.generateFont(parameter);
        } catch (GdxRuntimeException ignored){
        }
    }

    @Override
    public void render() {
        try {
            super.render();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        screen.dispose();
        shapeRenderer.dispose();
        TikzTextFont.dispose();
        editorFont.dispose();
    }
}
