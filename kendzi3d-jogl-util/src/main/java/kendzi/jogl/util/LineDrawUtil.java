package kendzi.jogl.util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Util for drawing lines.
 */
public class LineDrawUtil {
    private LineDrawUtil() {
        // Hide constructor
    }

    /**
     * Draw dotted line, which segments size depends on distance from camera.
     *
     * @param gl
     *            gl
     * @param begin
     *            begin point
     * @param end
     *            end point
     * @param segmentLength
     *            length of line segment
     */
    public static void drawDottedLine(GL2 gl, Vector3dc begin, Vector3dc end, double segmentLength) {

        double distance = begin.distance(end);

        Vector3d segmentVector = begin.sub(end, new Vector3d()).normalize().mul(segmentLength);

        boolean fill = true;
        double drawedDistance = 0;

        Vector3d drawPoint = new Vector3d(begin);

        gl.glBegin(GL.GL_LINES);

        while (distance > drawedDistance + segmentLength) {
            drawedDistance += segmentLength;

            if (fill) {
                gl.glVertex3d(drawPoint.x, drawPoint.y, drawPoint.z);
                gl.glVertex3d(drawPoint.x + segmentVector.x, //
                        drawPoint.y + segmentVector.y, //
                        drawPoint.z + segmentVector.z);
            }
            fill = !fill;
            drawPoint.add(segmentVector);
        }

        if (fill) {
            gl.glVertex3d(drawPoint.x, drawPoint.y, drawPoint.z);
            gl.glVertex3d(end.x(), end.y(), end.z());

        }

        gl.glEnd();
    }
}
