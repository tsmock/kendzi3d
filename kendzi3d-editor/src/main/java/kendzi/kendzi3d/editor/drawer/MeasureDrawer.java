package kendzi.kendzi3d.editor.drawer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import kendzi.jogl.camera.Viewport;
import kendzi.jogl.util.DrawUtil;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Drawer for measure tap.
 */
public class MeasureDrawer {

    private final GLU glu = new GLU();

    private final GLUT glut = new GLUT();

    /**
     * Draws measure tap with distance arrows and distance value. Measure tap is
     * draw in Y direction always rotated to camera. XXX add measure begin point
     *
     * @param gl
     *            gl
     * @param begin
     *            measure begin
     * @param end
     *            measure end
     * @param value
     *            measure value
     * @param viewport
     *            viewport
     * @param horizontalDistance
     *            horizontal distance
     * @param arrowHeight
     *            arrow height
     * @param arrowWidth
     *            arrow width
     * @param lineWidth
     *            line width
     */
    public void drawYMeasureWithArrows(GL2 gl, Vector3dc begin, Vector3dc end, double value, Viewport viewport,
            double horizontalDistance, double arrowHeight, double arrowWidth, float lineWidth) {

        gl.glLineWidth(lineWidth);

        Vector3d screenHorizontally = new Vector3d(viewport.getScreenHorizontally()).normalize();

        Vector3d arrowheadBaseWidthVector = new Vector3d(screenHorizontally).mul(arrowWidth);

        screenHorizontally.mul(horizontalDistance);

        // top horizontal line
        drawLine(gl, end.x(), end.y(), end.z(), //
                end.x() + screenHorizontally.x, end.y() + screenHorizontally.y, end.z() + screenHorizontally.z);

        // bottom horizontal line
        drawLine(gl, begin.x(), begin.y(), begin.z(), //
                begin.x() + screenHorizontally.x, begin.y() + screenHorizontally.y, begin.z() + screenHorizontally.z);

        screenHorizontally.mul(0.5);

        Vector3d bottomArrowhead = new Vector3d(screenHorizontally);
        bottomArrowhead.add(begin);
        Vector3d topArrowhead = new Vector3d(screenHorizontally);
        topArrowhead.add(end);

        // vertical line
        drawLine(gl, bottomArrowhead, topArrowhead);

        // vertical line arrows
        Vector3d arrowVector = bottomArrowhead.sub(topArrowhead, new Vector3d()).normalize().mul(arrowHeight);

        // bottom arrow
        drawFlatArrowhead(gl, bottomArrowhead, arrowVector, arrowheadBaseWidthVector);

        arrowVector.negate();
        arrowheadBaseWidthVector.negate();
        // top arrow
        drawFlatArrowhead(gl, topArrowhead, arrowVector, arrowheadBaseWidthVector);

        Vector3d center = new Vector3d(bottomArrowhead).add(topArrowhead).mul(0.5);

        drawNumberBox(gl, glu, glut, center, value, viewport);
    }

    private void drawFlatArrowhead(GL2 gl, Vector3dc arrowheadPoint, Vector3d arrowheadVector, Vector3d arrowheadWidthVector) {
        gl.glBegin(GL.GL_TRIANGLES);

        gl.glVertex3d(arrowheadPoint.x(), arrowheadPoint.y(), arrowheadPoint.z());
        gl.glVertex3d(//
                arrowheadPoint.x() + arrowheadVector.x + arrowheadWidthVector.x, //
                arrowheadPoint.y() + arrowheadVector.y + arrowheadWidthVector.y, //
                arrowheadPoint.z() + arrowheadVector.z + arrowheadWidthVector.z);
        gl.glVertex3d( //
                arrowheadPoint.x() + arrowheadVector.x - arrowheadWidthVector.x, //
                arrowheadPoint.y() + arrowheadVector.y - arrowheadWidthVector.y, //
                arrowheadPoint.z() + arrowheadVector.z - arrowheadWidthVector.z);

        gl.glEnd();
    }

    private void drawLine(GL2 gl, double beginX, double beginY, double beginZ, double endX, double endY, double endZ) {

        gl.glBegin(GL.GL_LINES);
        gl.glVertex3d(beginX, beginY, beginZ);
        gl.glVertex3d(endX, endY, endZ);
        gl.glEnd();
    }

    private void drawLine(GL2 gl, Vector3dc begin, Vector3dc end) {

        gl.glBegin(GL.GL_LINES);
        gl.glVertex3d(begin.x(), begin.y(), begin.z());
        gl.glVertex3d(end.x(), end.y(), end.z());
        gl.glEnd();
    }

    private void drawNumberBox(GL2 gl, GLU glu, GLUT glut, Vector3dc point, Double value, Viewport viewport) {

        gl.glDisable(GLLightingFunc.GL_LIGHTING);
        String msg = String.format("%.2f m", (double) value);

        Vector2d p = viewport.project(gl, glu, point);
        int fontSize = 18;
        int msgWidth = glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_18, msg);

        // Use a bitmap font (since no scaling required)
        // get (x,y) for centering the text on screen
        int x = (int) p.x + 18;
        int y = (int) p.y + fontSize / 2;

        // Switch to 2D viewing
        DrawUtil.begin2D(gl, viewport.getWidth(), viewport.getHeight());

        // Draw a background rectangle
        gl.glColor4f(1f, 1f, 1f, 0.6f);
        gl.glBegin(GL2GL3.GL_QUADS);
        int border = 7;
        gl.glVertex3i(x - border, y + border, 0);
        gl.glVertex3i(x + msgWidth + border, y + border, 0);
        gl.glVertex3i(x + msgWidth + border, y - fontSize - border, 0);
        gl.glVertex3i(x - border, y - fontSize - border, 0);
        gl.glEnd();
        // Write the message in the center of the screen
        gl.glColor3f(0.1f, 0.1f, 0.1f);

        gl.glRasterPos2i(x, y - 2);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, msg);
        // Switch back to 3D viewing
        DrawUtil.end2D(gl);
        gl.glEnable(GLLightingFunc.GL_LIGHTING);
    }
}
