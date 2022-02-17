package kendzi.kendzi3d.editor.example.objects.render;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import kendzi.jogl.util.DrawUtil;
import kendzi.kendzi3d.editor.drawer.SimpleOutlineDrawUtil;
import kendzi.kendzi3d.editor.example.objects.Box;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Util to draw box.
 */
public final class BoxDrawUtil {

    private BoxDrawUtil() {
        //
    }

    /**
     * Draws box.
     *
     * @param box
     *            box
     * @param gl
     *            gl
     */
    public static void draw(Box box, GL2 gl) {

        double size = box.getSize();

        Vector3d max = new Vector3d(box.getPosition());
        max.x += size;
        max.y += size;
        max.z += size;

        Vector3d min = new Vector3d(box.getPosition());
        min.x -= size;
        min.y -= size;
        min.z -= size;

        DrawUtil.drawFullBox(gl, max, min);
    }

    private static void drawSelected(GL2 gl, Vector3dc max, Vector3dc min) {
        drawSelectedFill(gl, max, min);
        drawGreenOutline(gl, max, min);
    }

    private static void drawSelectedFill(GL2 gl, Vector3dc max, Vector3dc min) {

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);

        gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
        // offset polygons to front
        gl.glPolygonOffset(-2.0f, -2.0f);

        DrawUtil.drawFullBox(gl, max, min);

        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);

    }

    private static void drawGreenOutline(GL2 gl, Vector3dc max, Vector3dc min) {

        // green
        gl.glColor3f(0.5f, 1.0f, 0.5f);

        SimpleOutlineDrawUtil.beginSimpleOutlineLine(gl);

        DrawUtil.drawFullBox(gl, max, min);

        SimpleOutlineDrawUtil.endSimpleOutline(gl);

    }

}
