package com.tikz.grid;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXFormula;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GenerateLaTeXImage {
    public static BufferedImage renderLaTexToImage(String latex) throws ParseException {
        // Create a TeXFormula object from the LaTeX string, which will be rendered as an image.
        TeXFormula formula = new TeXFormula(latex);
        formula.setDEBUG(false);
        System.out.printf("LaTeX String: %s\n", latex);
        return (BufferedImage) formula.createBufferedImage(TeXFormula.SERIF, 512f, Color.WHITE, null);
    }

    public static Pixmap bufferedImageToPixMap(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        System.out.printf("buffered image size: %d, %d%n", width, height);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Iterate over the BufferedImage and convert each pixel properly
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = img.getRGB(x, y);

                // Extract color channels
                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                // Convert ARGB to LibGDX RGBA format
                int rgba = (red << 24) | (green << 16) | (blue << 8) | alpha;
                pixmap.drawPixel(x, y, rgba);
            }
        }

        return pixmap;
    }


    public static Texture createLaTeXFormulaImage(String latex) throws ParseException {
        BufferedImage image = renderLaTexToImage("\\text{ " + latex + " }");
        Pixmap map = bufferedImageToPixMap(image);
        System.out.printf("\tpixmap size: %d, %d%n", map.getWidth(), map.getHeight());
        Texture latexTexture = new Texture(map, true); // Enable mipmapping
        latexTexture.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);

        return latexTexture;
    }
}
