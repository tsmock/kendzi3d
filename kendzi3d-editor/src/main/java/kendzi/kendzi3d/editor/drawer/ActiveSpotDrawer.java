package kendzi.kendzi3d.editor.drawer;

import java.awt.Color;

import kendzi.jogl.MatrixMath;
import kendzi.jogl.glu.GLU;
import kendzi.jogl.util.ColorUtil;
import kendzi.jogl.util.DrawUtil;
import kendzi.jogl.util.VertexArrayObject;
import kendzi.kendzi3d.editor.selection.editor.EditorType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

/**
 * Util for drawing editor active spots.
 */
public class ActiveSpotDrawer implements AutoCloseable {

    private static final int NUMBER_OF_SECTIONS = 16;

    private final float[] highlightOutlineColor = ColorUtil.colorToArray(Color.BLACK.brighter());

    private final float[] highlightFillColor1 = ColorUtil.colorToArray(Color.RED.darker().darker());

    private final float[] highlightFillColor2 = ColorUtil.colorToArray(Color.GRAY.darker().darker().darker());

    private final float[] normalColor = ColorUtil.colorToArray(Color.GRAY.brighter());

    /**
     * Type of editor highlighting.
     */
    public enum EditorMode {
        NORMAL, HIGHLIGHT_1, HIGHLIGHT_2
    }

    /**
     * Initiate drawer.
     *
     */
    public void init() {
        // do nothing right now -- GLU_SMOOTH is default
    }

    /**
     * Draws editor. Editor center point is at origin.
     *
     * @param editorRadius
     *            editor radius
     * @param type
     *            type of editor
     * @param mode
     *            editor mode
     */
    public void drawEditor(double editorRadius, EditorType type, EditorMode mode) {

        switch (mode) {
        case HIGHLIGHT_2:

            drawHighlightEditor(type, editorRadius, highlightFillColor2);
            break;

        case HIGHLIGHT_1:

            drawHighlightEditor(type, editorRadius, highlightFillColor1);
            break;

        case NORMAL:

            GL11.glColor3fv(normalColor);
            drawEditor(editorRadius, type);
            break;

        default:
            throw new IllegalArgumentException("Unknown editor mode: " + mode);
        }
        GL11C.glEnable(GL11.GL_LIGHTING);

    }

    @Override
    public void close() {
        // Close cached items
    }

    private void drawHighlightEditor(EditorType type, double editorRadius, float[] fillColor) {
        GL11.glColor3fv(highlightOutlineColor);

        SimpleOutlineDrawUtil.beginSimpleOutlineLine();
        drawEditor(editorRadius, type);

        SimpleOutlineDrawUtil.beginSimpleOutlinePoint();
        drawEditor(editorRadius, type);
        SimpleOutlineDrawUtil.endSimpleOutline();

        GL11C.glDisable(GL11.GL_LIGHTING);
        GL11.glColor3fv(fillColor);
        drawEditor(editorRadius, type);
    }

    private void drawEditor(double editorRadius, EditorType type) {
        switch (type) {
        case ARROW:
            drawArrow(editorRadius);
            break;
        case ARROW_HEAD:
            drawArrowhead(editorRadius);
            break;
        case SPHERE:
            drawSphere(editorRadius);
            break;
        case BOX:
            try (VertexArrayObject vao = DrawUtil.drawBox(editorRadius)) {
                vao.draw();
            }
            break;
        case BOX_SMALL:
            try (VertexArrayObject vao = DrawUtil.drawBox(editorRadius / 2)) {
                vao.draw();
            }
            break;

        default:
            throw new IllegalArgumentException("Unknown editor type: " + type);
        }

    }

    private static void drawSphere(double editorRadius) {
        GLU.gluSphere((float) editorRadius, NUMBER_OF_SECTIONS, NUMBER_OF_SECTIONS);
    }

    private static void drawArrowhead(double editorRadius) {

        double length = 2d * editorRadius;

        MatrixMath.glPushMatrix();
        MatrixMath.glTranslated(0, -editorRadius, 0);

        ArrowDrawUtil.drawArrowheadSimple(length, editorRadius, NUMBER_OF_SECTIONS);
        MatrixMath.glPopMatrix();
    }

    private static void drawArrow(double editorRadius) {

        double length = 2d * editorRadius;

        double arrowheadLength = 1d * editorRadius;
        double baseRadius = 0.1d * editorRadius;
        double arrowheadRadius = 0.6d * editorRadius;

        MatrixMath.glPushMatrix();
        MatrixMath.glTranslated(0, -length / 2d, 0);
        ArrowDrawUtil.drawArrow(length, arrowheadLength, baseRadius, arrowheadRadius, NUMBER_OF_SECTIONS);
        MatrixMath.glPopMatrix();
    }
}
