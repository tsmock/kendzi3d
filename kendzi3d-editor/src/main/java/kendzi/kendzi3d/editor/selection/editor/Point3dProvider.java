package kendzi.kendzi3d.editor.selection.editor;

import org.joml.Vector3dc;

/**
 * Simple wrapper for Point3d class. Point3d don't have setters and getters so
 * we need simple proxy to control values before they are read.
 */
public class Point3dProvider<P extends Vector3dc> {

    private final P point;

    /**
     * Constructor.
     * 
     * @param point
     *            provided point
     */
    public Point3dProvider(P point) {
        this.point = point;
    }

    /**
     * When value of point is provided
     * 
     * @return point
     */
    public P provide() {
        return point;
    }
}