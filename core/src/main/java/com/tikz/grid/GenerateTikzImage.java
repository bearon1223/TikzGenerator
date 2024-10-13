package com.tikz.grid;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXFormula;

public class GenerateTikzImage {
    public static BufferedImage renderLaTexToImage(String latex)  throws ParseException {
        // Create a TeXFormula object from the LaTeX string, which will be rendered as an image.
        TeXFormula formula = new TeXFormula(latex);
        formula.setDEBUG(false);
        System.out.printf("LaTeX String: %s\n", latex);
        return (BufferedImage) formula.createBufferedImage(TeXFormula.SERIF, 120f, Color.WHITE, null);
    }

    public static Pixmap bufferedImageToPixMap(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        System.out.printf("buffered image size: %d, %d%n", width, height);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // For each pixel in the BufferedImage, convert the ARGB color format to RGBA
        // and draw it in the Pixmap.
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = img.getRGB(x, y);
                // Check if the pixel is not transparent (alpha > 0)
                int alpha = (argb >> 24) & 0xFF;
                if (alpha > 0) {
                    // Set pixel to white with full opacity
                    pixmap.drawPixel(x, y, 0xFFFFFFFF); // White pixel
                } else {
                    // Optionally set transparent pixels to be transparent in Pixmap
                    pixmap.drawPixel(x, y, 0x00000000); // Fully transparent
                }
            }
        }
        return pixmap;
    }

    public static Texture createLaTeXFormulaImage(String latex) throws ParseException {
        BufferedImage image = renderLaTexToImage(latex);
        Pixmap map = bufferedImageToPixMap(image);
        System.out.printf("\tpixmap size: %d, %d%n", map.getWidth(), map.getHeight());
        return new Texture(map);
    }
}
