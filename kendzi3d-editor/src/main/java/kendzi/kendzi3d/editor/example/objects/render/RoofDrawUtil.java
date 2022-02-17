package kendzi.kendzi3d.editor.example.objects.render;

import com.jogamp.opengl.GL2;
import kendzi.jogl.util.DrawUtil;
import kendzi.kendzi3d.editor.example.objects.Roof;
import org.joml.Vector3d;

/**
 * Util to draw roof.
 */
public final class RoofDrawUtil {

    private RoofDrawUtil() {
        //
    }

    /**
     * Draws roof.
     *
     * @param box
     *            box
     * @param gl
     *            gl
     */
    public static void draw(Roof roof, GL2 gl) {

        double height = roof.getHeight();
        double width = roof.getWidth();
        double roofHeigth = roof.getRoofHeight();

        Vector3d max = new Vector3d(roof.getPosition());
        max.x += width;
        max.y += height - roofHeigth;
        max.z += width;

        Vector3d min = new Vector3d(roof.getPosition());
        min.x -= width;
        min.y -= 0;
        min.z -= width;

        // roof base
        DrawUtil.drawFullBox(gl, max, min);

        max.set(roof.getPosition());
        max.x += width / 2;
        max.y += height;
        max.z += width / 2;

        min.set(roof.getPosition());
        min.x -= width / 2;
        min.y -= height - roofHeigth;
        min.z -= width / 2;

        // roof top
        DrawUtil.drawFullBox(gl, max, min);
    }

}
