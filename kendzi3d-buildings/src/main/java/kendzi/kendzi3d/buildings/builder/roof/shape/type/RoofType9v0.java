/*
 * This software is provided "AS IS" without a warranty of any kind. You use it
 * on your own risk and responsibility!!! This file is shared under BSD v3
 * license. See readme.txt and BSD3 file for details.
 */

package kendzi.kendzi3d.buildings.builder.roof.shape.type;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import kendzi.jogl.model.factory.MeshFactory;
import kendzi.jogl.model.factory.MeshFactoryUtil;
import kendzi.jogl.texture.dto.TextureData;
import kendzi.kendzi3d.buildings.builder.dto.RoofMaterials;
import kendzi.kendzi3d.buildings.builder.dto.RoofTypeOutput;
import kendzi.kendzi3d.buildings.builder.roof.shape.measurement.Measurement;
import kendzi.kendzi3d.buildings.builder.roof.shape.measurement.MeasurementKey;
import kendzi.kendzi3d.buildings.builder.roof.shape.measurement.MeasurementUnit;
import kendzi.kendzi3d.buildings.model.roof.shape.DormerRoofModel;
import kendzi.math.geometry.Algebra;
import kendzi.math.geometry.Plane3d;
import kendzi.math.geometry.line.LineSegment2d;
import kendzi.math.geometry.point.TransformationMatrix2d;
import kendzi.math.geometry.point.TransformationMatrix3d;
import kendzi.math.geometry.point.Vector2dUtil;
import kendzi.math.geometry.polygon.MultiPolygonList2d;
import kendzi.math.geometry.polygon.PolygonList2d;
import kendzi.math.geometry.polygon.PolygonWithHolesList2d;
import kendzi.math.geometry.polygon.PolygonWithHolesList2dUtil;
import kendzi.math.geometry.skeleton.EdgeOutput;
import kendzi.math.geometry.skeleton.Skeleton;
import kendzi.math.geometry.skeleton.SkeletonOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ejml.simple.SimpleMatrix;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Roof type 9.0.
 * 
 * @author Tomasz Kędziora (Kendzi)
 * 
 *         TODO rename to RoofTypeComplexHipped
 */
public class RoofType9v0 extends AbstractRoofTypeBuilder {

    /** Log. */
    private static final Logger log = LogManager.getLogger(RoofType9v0.class);

    @Override
    public RoofTypeOutput buildRoof(Vector2dc startPoint, PolygonWithHolesList2d buildingPolygon, DormerRoofModel roof,
            double height, RoofMaterials roofTextureData) {

        SimpleMatrix transformLocal = TransformationMatrix2d.tranA(-startPoint.x(), -startPoint.y());
        PolygonWithHolesList2d buildingTransformed = PolygonWithHolesList2dUtil.transform(buildingPolygon, transformLocal);

        Measurement measurement = roof.getMeasurements().get(MeasurementKey.HEIGHT_1);

        Double h1 = null;
        Double angle = null;

        if (isUnit(measurement, MeasurementUnit.DEGREES)) {
            angle = measurement.getValue();
        } else {
            h1 = getHeightMeters(roof.getMeasurements(), MeasurementKey.HEIGHT_1, 2.5d);
        }

        RoofTypeOutput rto = build(buildingTransformed, h1, angle, 0, 0, roofTextureData);

        SimpleMatrix transformGlobal = TransformationMatrix3d.tranA(startPoint.x(), height - rto.getHeight(), -startPoint.y());
        rto.setTransformationMatrix(transformGlobal);

        return rto;
    }

    protected RoofTypeOutput build(PolygonWithHolesList2d buildingTransformed, Double h1, Double angle, double l1, double l2,
            RoofMaterials roofTextureData) {

        log.info(debugPolygon(buildingTransformed));

        MeshFactory meshBorder = createFacadeMesh(roofTextureData);
        MeshFactory meshRoof = createRoofMesh(roofTextureData);

        TextureData roofTexture = roofTextureData.getRoof().getTextureData();

        List<Vector2dc> outer = buildingTransformed.getOuter().getPoints();
        List<List<Vector2dc>> inners = PolygonWithHolesList2dUtil.getListOfHolePoints(buildingTransformed);

        SkeletonOutput sk = Skeleton.skeleton(outer, inners);

        // List<PolygonRoofHooksSpace> polygonRoofHooksSpace = new
        // ArrayList<PolygonRoofHooksSpace>();
        Map<Vector2dc, Double> distance = new IdentityHashMap<>();

        calcDistances(sk, distance);

        double heightFactor = calcDistanceToHeight(distance, h1, angle);

        for (EdgeOutput edgeOutput : sk.getEdgeOutputs()) {
            PolygonList2d polygon = edgeOutput.getPolygon();
            List<Vector2dc> points = polygon.getPoints();

            if (points.size() < 3) {
                log.error("not enought vertex for face");
                continue;
            }

            LineSegment2d edge = sk.getEdges().get(polygon);

            Vector3d edgeNormal = new Vector3d(edge.getEnd().x() - edge.getBegin().x(), 0,
                    -(edge.getEnd().y() - edge.getBegin().y()));

            Plane3d plane = createEdgePlane(edge, heightFactor);

            MeshFactoryUtil.addPolygonToRoofMesh(meshRoof, new MultiPolygonList2d(polygon), plane, edgeNormal, roofTexture);

            // Vector2d v1 = new Vector2d(edge.getEnd());
            // v1.sub(edge.getBegin());
            //
            // PolygonRoofHooksSpace hookSpace =
            // RectangleRoofTypeBuilder.buildRecHookSpace(edge.getBegin(), v1,
            // new PolygonPlane(
            // multiPolygonList2d, plane));
            // polygonRoofHooksSpace.add(hookSpace);
        }

        RoofTypeOutput rto = new RoofTypeOutput();

        rto.setHeight(findMaxDistance(distance));

        rto.setMesh(Arrays.asList(meshBorder, meshRoof));

        rto.setRectangle(RoofTypeUtil.findRectangle(outer, 0));

        return rto;
    }

    private Plane3d createEdgePlane(LineSegment2d edge, double heightFactor) {

        Vector3dc faceNormal = calcFaceNormal(edge, heightFactor);

        return new Plane3d(new Vector3d(edge.getBegin().x(), 0, -edge.getBegin().y()), faceNormal);
    }

    private String debugPolygon(PolygonWithHolesList2d buildingTransformed) {
        StringBuffer sb = new StringBuffer();
        sb.append("** Debug for polygon **\n");

        List<Vector2dc> outer = buildingTransformed.getOuter().getPoints();
        sb.append("List<Vector2d> polygon = new ArrayList<Vector2d>();\n");

        for (Vector2dc p : outer) {
            sb.append("polygon.add(new Vector2d(" + p.x() + ",  " + p.y() + "));\n");
        }

        List<List<Vector2dc>> inners = PolygonWithHolesList2dUtil.getListOfHolePoints(buildingTransformed);

        int holeCount = 0;
        for (List<Vector2dc> polygonList2d : inners) {
            holeCount++;
            sb.append("\nList<Vector2d> hole" + holeCount + " = new ArrayList<Vector2d>();\n");
            for (Vector2dc p : polygonList2d) {
                sb.append("hole" + holeCount + ".add(new Vector2d(" + p.x() + ",  " + p.y() + "));\n");
            }
        }

        sb.append("****");
        return sb.toString();
    }

    private double calcDistanceToHeight(Map<Vector2dc, Double> distance, Double h1, Double angle) {

        final double correction;

        if (angle != null) {
            correction = Math.tan(Math.toRadians(angle));

        } else if (h1 != null) {
            double maxDistance = findMaxDistance(distance);

            correction = h1 / maxDistance;
        } else {
            correction = 1;
        }

        for (Vector2dc p : distance.keySet()) {
            distance.computeIfPresent(p, (key, value) -> value * correction);
        }

        return correction;
    }

    private double findMaxDistance(Map<Vector2dc, Double> distance) {
        return distance.values().stream().mapToDouble(Double::valueOf).max().orElse(0);
    }

    private void calcDistances(SkeletonOutput sk, Map<Vector2dc, Double> distance) {

        for (EdgeOutput edgeOutput : sk.getEdgeOutputs()) {
            PolygonList2d polygon = edgeOutput.getPolygon();
            LineSegment2d edge = sk.getEdges().get(polygon);
            List<Vector2dc> points = polygon.getPoints();
            calcDistance(edge, points, distance);
        }
    }

    private void calcDistance(LineSegment2d edge, List<Vector2dc> points, Map<Vector2dc, Double> distance) {

        for (Vector2dc p : points) {
            distance.computeIfAbsent(p, key -> calcDistance(key, edge));
        }
    }

    private Vector3dc calcFaceNormal(LineSegment2d edge, double heightFactor) {
        Vector2d edgeVector = Vector2dUtil.fromTo(edge.getBegin(), edge.getEnd());
        edgeVector.normalize();

        Vector2dc edgeOrthogonal = Vector2dUtil.orthogonalLeft(edgeVector);

        Vector3d v2 = new Vector3d(edgeOrthogonal.x(), heightFactor, -edgeOrthogonal.y());
        return new Vector3d(edgeVector.x, 0, -edgeVector.y).cross(v2).normalize();

    }

    private static double calcDistance(Vector2dc pIntersect, LineSegment2d edgeLine) {
        Vector2d edge = new Vector2d(edgeLine.getEnd()).sub(edgeLine.getBegin());

        Vector2d intersect = new Vector2d(pIntersect).sub(edgeLine.getBegin());

        Vector2dc pointOnVector = Algebra.orthogonalProjection(edge, intersect);

        return intersect.distance(pointOnVector);
    }

}
