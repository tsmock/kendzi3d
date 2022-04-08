package kendzi.kendzi3d.editor.drawer;

import java.awt.Color;

import kendzi.jogl.Gl2Draw;
import kendzi.jogl.util.ColorUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

/**
 * Simple highlight drawer for object.
 *
 */
public class HighlightDrawer {

    private static final float[] selectionColor = ColorUtil.colorToArray(new Color(0.5f, 1.0f, 0.5f));

    /**
     * Draw object with highlight.
     *
     * @param object
     *            object to draw
     */
    public static void drawHighlight(Gl2Draw object) {

        drawSelectedFill(object);
        drawGreenOutline(object);
    }

    private static void drawSelectedFill(Gl2Draw drawer) {

        GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_FILL);

        GL11C.glEnable(GL11C.GL_POLYGON_OFFSET_FILL);
        // offset polygons to front
        GL11C.glPolygonOffset(-2.0f, -2.0f);

        drawer.draw();

        GL11C.glDisable(GL11C.GL_POLYGON_OFFSET_FILL);

    }

    private static void drawGreenOutline(Gl2Draw drawer) {

        // selection color
        GL11.glColor4fv(selectionColor);
        GL11C.glDisable(GL11C.GL_TEXTURE_2D);

        SimpleOutlineDrawUtil.beginSimpleOutlineLine();
        drawer.draw();

        SimpleOutlineDrawUtil.beginSimpleOutlinePoint();
        drawer.draw();

        SimpleOutlineDrawUtil.endSimpleOutline();
    }
}
