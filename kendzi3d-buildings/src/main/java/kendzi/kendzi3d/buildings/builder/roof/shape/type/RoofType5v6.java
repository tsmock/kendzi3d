/*
 * This software is provided "AS IS" without a warranty of any kind. You use it
 * on your own risk and responsibility!!! This file is shared under BSD v3
 * license. See readme.txt and BSD3 file for details.
 */

package kendzi.kendzi3d.buildings.builder.roof.shape.type;

import java.util.Arrays;
import java.util.List;

import kendzi.jogl.model.factory.FaceFactory;
import kendzi.jogl.model.factory.FaceFactory.FaceType;
import kendzi.jogl.model.factory.MeshFactory;
import kendzi.jogl.model.factory.MeshFactoryUtil;
import kendzi.jogl.model.geometry.TextCoord;
import kendzi.jogl.texture.dto.TextureData;
import kendzi.kendzi3d.buildings.builder.dto.RoofMaterials;
import kendzi.kendzi3d.buildings.builder.dto.RoofTypeOutput;
import kendzi.kendzi3d.buildings.builder.dto.TextureQuadIndex;
import kendzi.kendzi3d.buildings.builder.roof.shape.measurement.Measurement;
import kendzi.kendzi3d.buildings.builder.roof.shape.measurement.MeasurementKey;
import kendzi.kendzi3d.buildings.builder.roof.shape.measurement.MeasurementUnit;
import kendzi.kendzi3d.buildings.model.roof.shape.DormerRoofModel;
import kendzi.math.geometry.Plane3d;
import kendzi.math.geometry.point.TransformationMatrix2d;
import kendzi.math.geometry.point.TransformationMatrix3d;
import kendzi.math.geometry.point.Vector2dUtil;
import kendzi.math.geometry.polygon.CircleInsidePolygon;
import kendzi.math.geometry.polygon.CircleInsidePolygon.Circle;
import kendzi.math.geometry.polygon.MultiPolygonList2d;
import kendzi.math.geometry.polygon.PolygonList2d;
import kendzi.math.geometry.polygon.PolygonWithHolesList2d;
import org.ejml.simple.SimpleMatrix;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Roof type 5.6.
 * 
 * @author Tomasz Kędziora (Kendzi)
 */
public class RoofType5v6 extends AbstractRoofTypeBuilder {

    @Override
    public RoofTypeOutput buildRoof(Vector2dc pStartPoint, PolygonWithHolesList2d buildingPolygon, DormerRoofModel pRoof,
            double height, RoofMaterials roofTextureData) {

        SimpleMatrix transformLocal = TransformationMatrix2d.tranA(-pStartPoint.x(), -pStartPoint.y());

        List<Vector2dc> polygon = buildingPolygon.getOuter().getPoints();

        polygon = TransformationMatrix2d.transformList(polygon, transformLocal);

        Double h1 = null;
        Double angle = null;
        Measurement measurement = pRoof.getMeasurements().get(MeasurementKey.HEIGHT_1);
        if (isUnit(measurement, MeasurementUnit.DEGREES)) {
            angle = measurement.getValue();
        } else {
            h1 = getHeightMeters(pRoof.getMeasurements(), MeasurementKey.HEIGHT_1, DEFAULT_ROOF_HEIGHT);
        }

        RoofTypeOutput rto = build(polygon, h1, angle, roofTextureData);

        SimpleMatrix transformGlobal = TransformationMatrix3d.tranA(pStartPoint.x(), height - rto.getHeight(), -pStartPoint.y());
        rto.setTransformationMatrix(transformGlobal);

        return rto;
    }

    protected RoofTypeOutput build(List<Vector2dc> borderList, Double height, Double angle, RoofMaterials roofTextureData) {

        MeshFactory meshDome = createRoofMesh(roofTextureData);
        MeshFactory meshRoof = createRoofMesh(roofTextureData);

        TextureData roofTexture = roofTextureData.getRoof().getTextureData();

        PolygonList2d borderPolygon = new PolygonList2d(borderList);

        buildFlatRoof(borderPolygon, meshRoof, roofTexture);

        // build circle
        Circle circle = CircleInsidePolygon.iterativeNonConvex(borderPolygon, 0.01);
        circle.setRadius(Math.min(height, circle.getRadius()));

        int pIcross = 5;
        int pIsection = 9;
        buildRotaryShape(meshDome, circle, pIcross, pIsection, true);

        RoofTypeOutput rto = new RoofTypeOutput();
        rto.setHeight(circle.getRadius());

        rto.setMesh(Arrays.asList(meshDome, meshRoof));

        rto.setRoofHooksSpaces(null);

        rto.setRectangle(RoofTypeUtil.findRectangle(borderList, 0));

        return rto;
    }

    public static void buildFlatRoof(PolygonList2d borderPolygon, MeshFactory meshRoof, TextureData roofTexture) {

        MultiPolygonList2d topMP = new MultiPolygonList2d(borderPolygon);
        // build flat
        Vector3dc planeRightTopPoint = new Vector3d(0, 0, 0);

        Vector3dc nt = new Vector3d(0, 1, 0);

        Plane3d planeTop = new Plane3d(planeRightTopPoint, nt);

        Vector3dc roofTopLineVector = new Vector3d(-1d, 0, 0);

        MeshFactoryUtil.addPolygonToRoofMesh(meshRoof, topMP, planeTop, roofTopLineVector, roofTexture);

    }

    private void buildRotaryShape(MeshFactory meshFactory, Circle circle, int numberOfCrossSplits, int pIsection, boolean soft) {

        int crossCount = numberOfCrossSplits + 1;

        // create cross section
        Vector2dc[] crossSection = new Vector2dc[crossCount];
        for (int i = 0; i < crossCount; i++) {
            double a = Math.toRadians(90) / (crossCount - 1) * i;

            crossSection[i] = new Vector2d(Math.cos(a) * circle.getRadius(), Math.sin(a) * circle.getRadius());
        }

        buildRotaryShape(meshFactory, circle.getPoint(), pIsection, crossSection, soft);
    }

    public static void buildRotaryShape(MeshFactory meshFactory, Vector2dc center, int sectionCount, Vector2dc[] crossSection,
            boolean soft) {

        int crossCount = crossSection.length;

        // create points
        Vector3dc[][] mesh = buildMesh(center, crossSection, sectionCount);

        TextureQuadIndex[] tc = buildRotaryShapeTextureMapping(meshFactory, crossCount, sectionCount, mesh);

        FaceFactory face = meshFactory.addFace(FaceType.QUADS);

        // add vertex to mesh
        int[][] pointsIntex = addVertexToMeshFactory(meshFactory, mesh, sectionCount, crossCount);

        // add soft normals vectors
        int[][] softNormalsIntex = new int[sectionCount][];
        if (soft) {
            softNormalsIntex = buildSoftNormalsIndexs(meshFactory, sectionCount, crossSection, crossCount);
        }

        // add faces to mesh
        for (int i = 0; i < sectionCount; i++) {
            Vector3dc[] c1 = mesh[i];
            Vector3dc[] c2 = mesh[(i + 1) % sectionCount];

            int i2 = (i + 1) % sectionCount;

            for (int j = 0; j < crossCount - 1; j++) {

                int ic1p1 = pointsIntex[i][j];
                int ic2p1 = pointsIntex[i2][j];
                int ic1p2 = pointsIntex[i][j + 1];
                int ic2p2 = pointsIntex[i2][j + 1];

                int ic1p1n;
                int ic2p1n;
                int ic1p2n;
                int ic2p2n;

                if (!soft) {
                    // hard normals

                    Vector3dc c1p1 = c1[j];
                    Vector3dc c2p1 = c2[j];
                    Vector3dc c1p2 = c1[j + 1];

                    Vector3dc n = calcNormal(c1p1, c2p1, c1p2);
                    int in = meshFactory.addNormal(n);

                    ic1p1n = in;
                    ic2p1n = in;
                    ic1p2n = in;
                    ic2p2n = in;

                } else {
                    // soft normals
                    ic1p1n = softNormalsIntex[i][j];
                    ic2p1n = softNormalsIntex[i2][j];
                    ic1p2n = softNormalsIntex[i][j + 1];
                    ic2p2n = softNormalsIntex[i2][j + 1];
                }

                TextureQuadIndex tq = tc[j];

                face.addVert(ic1p1, tq.getLd(), ic1p1n);
                face.addVert(ic2p1, tq.getRd(), ic2p1n);
                face.addVert(ic2p2, tq.getRt(), ic2p2n);
                face.addVert(ic1p2, tq.getLt(), ic1p2n);
            }
        }
    }

    public static int[][] addVertexToMeshFactory(MeshFactory meshFactory, Vector3dc[][] mesh, int pointCount, int crossCount) {
        int[][] pointsIntex = new int[pointCount][];
        for (int i = 0; i < pointCount; i++) {
            pointsIntex[i] = new int[crossCount];
            for (int j = 0; j < crossCount; j++) {
                Vector3dc p = mesh[i][j];
                int ip = meshFactory.addVertex(p);
                pointsIntex[i][j] = ip;
            }
        }
        return pointsIntex;
    }

    private static Vector3dc[][] buildMesh(Vector2dc center, Vector2dc[] crossSection, int sectionCount) {

        int crossCount = crossSection.length;

        Vector3dc[][] mesh = new Vector3dc[sectionCount][];
        for (int i = 0; i < sectionCount; i++) {
            double a = Math.toRadians(360) / sectionCount * i;

            SimpleMatrix tranA = TransformationMatrix3d.tranA(center.x(), 0, -center.y());
            SimpleMatrix rotY = TransformationMatrix3d.rotYA(a);

            SimpleMatrix trans = tranA.mult(rotY);

            Vector3dc[] crossMesh = new Vector3dc[crossCount];

            for (int j = 0; j < crossSection.length; j++) {
                // point
                Vector2dc cross = crossSection[j];
                Vector3dc p = new Vector3d(cross.x(), cross.y(), 0);

                crossMesh[j] = TransformationMatrix3d.transform(p, trans, true);

            }
            mesh[i] = crossMesh;
        }
        return mesh;
    }

    private static int[][] buildSoftNormalsIndexs(MeshFactory meshFactory, int sectionCount, Vector2dc[] crossSection,
            int crossCount) {

        Vector2dc[] crossSectionSoftNormals = calsSoftNormals(crossSection);

        int[][] softNormalsIntex = new int[sectionCount][];
        for (int i = 0; i < sectionCount; i++) {
            double a = Math.toRadians(360) / sectionCount * i;

            SimpleMatrix rotY = TransformationMatrix3d.rotYA(a);
            softNormalsIntex[i] = new int[crossCount];

            for (int j = 0; j < crossSection.length; j++) {
                // point
                Vector2dc n2d = crossSectionSoftNormals[j];
                Vector3dc n = new Vector3d(n2d.x(), n2d.y(), 0);

                Vector3dc transform = TransformationMatrix3d.transform(n, rotY, false);

                int in = meshFactory.addNormal(transform);
                softNormalsIntex[i][j] = in;
            }
        }
        return softNormalsIntex;
    }

    private static Vector2dc[] calsSoftNormals(Vector2dc[] crossSection) {

        Vector2d[] ret = new Vector2d[crossSection.length];

        Vector2dc[] normals = new Vector2dc[crossSection.length - 1];
        for (int i = 0; i < crossSection.length - 1; i++) {
            Vector2d n = new Vector2d(crossSection[i + 1]);
            n.sub(crossSection[i]);
            n.normalize();
            normals[i] = n;
        }

        for (int i = 1; i < crossSection.length - 1; i++) {
            Vector2dc n1 = normals[i - 1];
            Vector2dc n2 = normals[i];

            Vector2d n = new Vector2d(Vector2dUtil.bisectorNormalized(n1, n2));
            n.normalize();

            ret[i] = n;
        }

        ret[0] = new Vector2d(Vector2dUtil.orthogonalLeft(normals[0]));

        ret[crossSection.length - 1] = new Vector2d(Vector2dUtil.orthogonalLeft(normals[normals.length - 1]));

        for (Vector2d element : ret) {
            element.negate();
        }

        return ret;
    }

    private static TextureQuadIndex[] buildRotaryShapeTextureMapping(MeshFactory meshFactory, int crossCount, int sectionCount,
            Vector3dc[][] mesh) {
        // texture mapping only for one section, all others are the same
        int i = 0;

        return buildTextureMappingForCross(meshFactory, mesh, sectionCount, crossCount, i);
    }

    private static Vector3dc calcNormal(Vector3dc p1, Vector3dc p2, Vector3dc p3) {
        Vector3d n = new Vector3d(p2).sub(p1);

        Vector3d n2 = new Vector3d(p3).sub(p2);

        n.cross(n2).normalize();
        return n;
    }

    public static TextureQuadIndex[] buildTextureMappingForCross(MeshFactory meshFactory, Vector3dc[][] mesh, int pointCount,
            int crossCount, int i) {

        TextureQuadIndex[] crossTc = new TextureQuadIndex[crossCount];

        double textHeightDown = 0;

        Vector3dc[] c1 = mesh[i];
        Vector3dc[] c2 = mesh[(i + 1) % pointCount];

        Vector3d middleDown = new Vector3d((c1[0].x() + c2[0].x()) / 2d, (c1[0].y() + c2[0].y()) / 2d,
                (c1[0].z() + c2[0].z()) / 2d);

        double widthDown = middleDown.distance(c2[0]);

        for (int j = 1; j < crossCount; j++) {
            Vector3d middleTop = new Vector3d((c1[j].x() + c2[j].x()) / 2d, (c1[j].y() + c2[j].y()) / 2d,
                    (c1[j].z() + c2[j].z()) / 2d);
            double widthTop = middleTop.distance(c2[j]);
            double height = middleDown.distance(middleTop);
            double textHeightTop = textHeightDown + height;
            TextureQuadIndex tq = new TextureQuadIndex();

            tq.setLd(meshFactory.addTextCoord(new TextCoord(-widthDown, textHeightDown)));
            tq.setRd(meshFactory.addTextCoord(new TextCoord(widthDown, textHeightDown)));

            tq.setRt(meshFactory.addTextCoord(new TextCoord(widthTop, textHeightTop)));
            tq.setLt(meshFactory.addTextCoord(new TextCoord(-widthTop, textHeightTop)));

            crossTc[j - 1] = tq;

            middleDown = middleTop;
            widthDown = widthTop;
            textHeightDown = textHeightTop;
        }
        return crossTc;
    }

}
