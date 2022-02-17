/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.jogl.model.factory;

import kendzi.jogl.model.geometry.Bounds;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class BoundsFactory {
    Bounds bounds;

    public BoundsFactory() {
        this.bounds = new Bounds();
        this.bounds.min = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.bounds.max = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    public void addPoint(Vector3dc point) {
        this.bounds.min = this.bounds.min.min(point, new Vector3d());
        this.bounds.max = this.bounds.max.max(point, new Vector3d());
    }

    public void addPoint(double x, double y, double z) {
        this.addPoint(new Vector3d(x, y, z));
    }

    public Bounds toBounds() {
        this.bounds.center = this.bounds.max.add(this.bounds.min, new Vector3d()).div(2);

        double dx = this.bounds.max.x() - this.bounds.min.x();
        double dy = this.bounds.max.y() - this.bounds.min.y();
        double dz = this.bounds.max.z() - this.bounds.min.z();
        this.bounds.radius = 0.5d * Math.sqrt(dx * dx + dy * dy + dz * dz);

        return this.bounds;

    }
}
