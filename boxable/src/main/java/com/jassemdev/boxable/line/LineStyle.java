package com.jassemdev.boxable.line;

import com.tom_roush.harmony.awt.AWTColor;

import java.util.Objects;

/**
 * <p>
 * The <code>LineStyle</code> class defines a basic set of rendering attributes
 * for lines.
 * </p>
 *
 * @author hstimac
 * @author mkuehne
 */
public class LineStyle {

    private final AWTColor color;

    private final float width;

    private float[] dashArray;

    private float dashPhase;

    /**
     * <p>
     * Simple constructor for setting line {@link AWTColor} and line width
     * </p>
     *
     * @param color The line {@link AWTColor}
     * @param width The line width
     */
    public LineStyle(final AWTColor color, final float width) {
        this.color = color;
        this.width = width;
    }

    /**
     * <p>
     * Provides ability to produce dotted line.
     * </p>
     *
     * @param color The {@link AWTColor} of the line
     * @param width The line width
     * @return new styled line
     */
    public static LineStyle produceDotted(final AWTColor color, final int width) {
        final LineStyle line = new LineStyle(color, width);
        line.dashArray = new float[]{1.0f};
        line.dashPhase = 0.0f;

        return line;
    }

    /**
     * <p>
     * Provides ability to produce dashed line.
     * </p>
     *
     * @param color The {@link AWTColor} of the line
     * @param width The line width
     * @return new styled line
     */
    public static LineStyle produceDashed(final AWTColor color, final int width) {
        return produceDashed(color, width, new float[]{5.0f}, 0.0f);
    }

    /**
     * @param color     The {@link AWTColor} of the line
     * @param width     The line width
     * @param dashArray Mimics the behavior of {awt BasicStroke#getDashArray()}
     * @param dashPhase Mimics the behavior of {awt BasicStroke#getDashPhase()}
     * @return new styled line
     */
    public static LineStyle produceDashed(final AWTColor color, final int width, final float[] dashArray,
                                          final float dashPhase) {
        final LineStyle line = new LineStyle(color, width);
        line.dashArray = dashArray;
        line.dashPhase = dashPhase;

        return line;
    }

    public AWTColor getColor() {
        return new AWTColor(color.color);
    }

    public float getWidth() {
        return width;
    }

    public float[] getDashArray() {
        return dashArray;
    }

    public float getDashPhase() {
        return dashPhase;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.color);
        hash = 89 * hash + Float.floatToIntBits(this.width);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LineStyle other = (LineStyle) obj;
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (Float.floatToIntBits(this.width) != Float.floatToIntBits(other.width)) {
            return false;
        }
        return true;
    }


}
