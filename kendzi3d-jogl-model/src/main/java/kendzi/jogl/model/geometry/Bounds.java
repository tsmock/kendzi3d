/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.jogl.model.geometry;

import org.joml.Vector3dc;

/**
 * Bounds of model. Minimal border, maximal border, radius and center.
 *
 * @author Tomasz Kędziora (Kendzi)
 */
public class Bounds {
    /**
     * Minimal point of bounds.
     */
    public Vector3dc min;

    /**
     * Maximal point of bounds.
     */
    public Vector3dc max;

    /**
     * Radius of bounds.
     */
    public double radius;
    /**
     * Center of bounds.
     */
    public Vector3dc center;

    /**
     * @return the min
     */
    public Vector3dc getMin() {
        return min;
    }

    /**
     * @param min
     *            the min to set
     */
    public void setMin(Vector3dc min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public Vector3dc getMax() {
        return max;
    }

    /**
     * @param max
     *            the max to set
     */
    public void setMax(Vector3dc max) {
        this.max = max;
    }

    /**
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius
     *            the radius to set
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @return the center
     */
    public Vector3dc getCenter() {
        return center;
    }

    /**
     * @param center
     *            the center to set
     */
    public void setCenter(Vector3dc center) {
        this.center = center;
    }

}
