package kendzi.kendzi3d.buildings.model;

import java.util.ArrayList;
import java.util.List;

import kendzi.math.geometry.polygon.PolygonList2d;
import kendzi.math.geometry.polygon.PolygonWithHolesList2d;
import org.joml.Vector2dc;

public class BuildingUtil {
    private BuildingUtil() {
        // Hide constructor
    }

    public static PolygonWithHolesList2d buildingPartToPolygonWithHoles(BuildingPart bp) {

        PolygonList2d outerPolygon = wallToOuterPolygon(bp.getWall());

        List<PolygonList2d> innerList = new ArrayList<>();
        if (bp.getInlineWalls() != null) {
            for (Wall innerWall : bp.getInlineWalls()) {
                PolygonList2d innerPolygon = wallToOuterPolygon(innerWall);
                innerList.add(innerPolygon);
            }
        }

        return new PolygonWithHolesList2d(outerPolygon, innerList);
    }

    public static PolygonList2d wallToOuterPolygon(Wall wall) {
        return wallPartsToPolygon(wall.getWallParts());
    }

    private static PolygonList2d wallPartsToPolygon(List<WallPart> wallParts) {

        List<Vector2dc> points = new ArrayList<>();

        for (WallPart p : wallParts) {
            if (!points.isEmpty() && p.getNodes().size() > 0) {
                Vector2dc lastAdded = points.get(points.size() - 1);
                Vector2dc newToAdd = p.getNodes().get(0).point;
                if (lastAdded.equals(newToAdd)) {
                    points.remove(points.size() - 1);
                }
            }

            for (WallNode n : p.getNodes()) {
                points.add(n.point);
            }
        }

        // if (points.size() > 1) {
        if (points.get(0).equals(points.get(points.size() - 1))) {
            points.remove(points.size() - 1);
        } else {
            throw new IllegalArgumentException("wall is not closed!!!");
        }

        return new PolygonList2d(points);
    }

}
