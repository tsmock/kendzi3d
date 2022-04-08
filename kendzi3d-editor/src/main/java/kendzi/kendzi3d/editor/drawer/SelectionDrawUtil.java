package kendzi.kendzi3d.editor.drawer;

import kendzi.jogl.MatrixMath;
import kendzi.jogl.util.DrawUtil;
import kendzi.kendzi3d.editor.selection.Selectable;
import kendzi.kendzi3d.editor.selection.Selection;
import kendzi.kendzi3d.editor.selection.SphereSelection;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11C;

public class SelectionDrawUtil {

    public static void drawSphereSelection(Selectable r) {

        for (Selection selection : r.getSelection()) {
            if (selection instanceof SphereSelection) {

                SphereSelection s = (SphereSelection) selection;
                MatrixMath.glPushMatrix();

                Vector3dc p = s.getCenter();

                double dx = p.x();
                double dy = p.y();
                double dz = p.z();

                GL11C.glLineWidth(1);
                MatrixMath.glTranslated(dx, dy, dz);

                DrawUtil.drawDotOuterY(s.getRadius(), 24);

                MatrixMath.glRotated(90d, 1d, 0, 0);
                DrawUtil.drawDotOuterY(s.getRadius(), 24);

                MatrixMath.glRotated(90d, 0, 0, 1d);
                DrawUtil.drawDotOuterY(s.getRadius(), 24);

                MatrixMath.glPopMatrix();
            }
        }
    }
}
