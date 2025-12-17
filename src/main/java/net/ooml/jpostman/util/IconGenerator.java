package net.ooml.jpostman.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Icon Generator for JPostman
 * Generates PNG icons from code
 */
public class IconGenerator {

    public static void main(String[] args) throws IOException {
        int[] sizes = {16, 32, 48, 64, 128, 256, 512};
        String outputDir = "src/main/resources/icons";

        new File(outputDir).mkdirs();

        for (int size : sizes) {
            BufferedImage icon = generateIcon(size);
            File outputFile = new File(outputDir, "jpostman_" + size + ".png");
            ImageIO.write(icon, "PNG", outputFile);
            System.out.println("✓ Created " + size + "x" + size + " icon: " + outputFile.getName());
        }

        System.out.println("\nDone! Icons created in " + outputDir);
    }

    public static BufferedImage generateIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Enable antialiasing for smooth edges
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw background with gradient
        int margin = size / 16;
        int bgSize = size - 2 * margin;
        int cornerRadius = size / 6;

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(37, 99, 235),  // Blue
                size, size, new Color(30, 64, 175)  // Darker blue
        );
        g.setPaint(gradient);
        g.fill(new RoundRectangle2D.Double(margin, margin, bgSize, bgSize, cornerRadius, cornerRadius));

        // Draw "JPM" text
        g.setColor(Color.WHITE);

        // Calculate font size based on icon size
        float fontSize = size * 0.35f;
        Font font = new Font("Arial", Font.BOLD, (int) fontSize);
        g.setFont(font);

        // Center text
        FontMetrics fm = g.getFontMetrics();
        String text = "JPM";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();

        int x = (size - textWidth) / 2;
        int y = (size + textHeight) / 2 - fm.getDescent();

        g.drawString(text, x, y);

        // Add a small accent dot at the bottom
        if (size >= 64) {
            g.setColor(new Color(96, 165, 250));  // Light blue
            int dotSize = size / 24;
            int dotX = size / 2 - dotSize / 2;
            int dotY = size - margin - dotSize * 3;
            g.fillOval(dotX, dotY, dotSize, dotSize);
        }

        g.dispose();
        return image;
    }
}
