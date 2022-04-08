/*
 * This software is provided "AS IS" without a warranty of any kind. You use it
 * on your own risk and responsibility!!! This file is shared under BSD v3
 * license. See readme.txt and BSD3 file for details.
 */

package kendzi.josm.kendzi3d.jogl.model.trees;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kendzi.jogl.MatrixMath;
import kendzi.jogl.camera.Camera;
import kendzi.jogl.model.geometry.Bounds;
import kendzi.jogl.model.geometry.Model;
import kendzi.jogl.model.render.ModelRender;
import kendzi.jogl.util.DrawUtil;
import kendzi.josm.kendzi3d.jogl.model.export.ExportItem;
import kendzi.josm.kendzi3d.jogl.model.export.ExportModelConf;
import kendzi.josm.kendzi3d.jogl.model.lod.LOD;
import kendzi.josm.kendzi3d.jogl.model.tmp.AbstractWayModel;
import kendzi.josm.kendzi3d.service.MetadataCacheService;
import kendzi.josm.kendzi3d.service.ModelCacheService;
import kendzi.josm.kendzi3d.util.ModelUtil;
import kendzi.kendzi3d.josm.model.perspective.Perspective;
import kendzi.kendzi3d.world.MultiPointWorldObject;
import kendzi.math.geometry.Triangulate;
import kendzi.math.geometry.polygon.PolygonList2d;
import kendzi.math.geometry.polygon.PolygonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Representing trees in row model.
 *
 * @author Tomasz Kedziora (Kendzi)
 */
public class Forest extends AbstractWayModel implements MultiPointWorldObject {

    /** Log. */
    private static final Logger log = LogManager.getLogger(Forest.class);

    ModelCacheService modelCacheService;
    MetadataCacheService metadataCacheService;

    public static double lod1 = 20 * 20;

    private static final double lod2 = 100 * 100;

    private static final double lod3 = 500 * 500;

    private static final double lod4 = 1000 * 1000;

    /**
     * Renderer of model.
     */
    private final ModelRender modelRender;

    private final EnumMap<LOD, Model> modelLod;

    private String type;
    private String genus;
    private String species;

    Vector3d scale;

    private List<Vector2dc> hookPoints;

    private Integer numOfTrees;

    private List<HeightCluster> clusterHook;

    /**
     * @param pWay
     *            way
     * @param perspective
     *            perspective
     */
    public Forest(Way pWay, Perspective perspective, ModelRender pModelRender, ModelCacheService modelCacheService,
            MetadataCacheService metadataCacheService) {
        super(pWay, perspective);

        modelLod = new EnumMap<>(LOD.class);

        scale = new Vector3d(1d, 1d, 1d);

        modelRender = pModelRender;
        this.modelCacheService = modelCacheService;
        this.metadataCacheService = metadataCacheService;
    }

    @Override
    public void buildWorldObject() {

        buildModel(LOD.LOD1);
        buildModel(LOD.LOD2);
        buildModel(LOD.LOD3);
        buildModel(LOD.LOD4);
        buildModel(LOD.LOD5);

        buildModel = true;
    }

    public void buildModel(LOD pLod) {

        type = way.get("type");
        if (type == null) {
            type = "unknown";
        }
        genus = way.get("genus");
        species = way.get("species");

        double height = Tree.getHeight(way, species, genus, type, metadataCacheService);

        Model model = null;

        model = Tree.findSimpleModel(species, genus, type, pLod, metadataCacheService, modelCacheService);

        setupScale(model, height);

        modelLod.put(pLod, model);

        numOfTrees = ModelUtil.parseInteger(way.get("tree"), null);

        if (hookPoints == null) {
            hookPoints = calsHookPoints(points, numOfTrees);

            log.info("***** num of tree: " + hookPoints.size());

            double CLUSTER_SIZE = 50;

            Vector2dc minBound = minBound(points);

            clusterHook = calcClusterHooks(hookPoints, minBound, CLUSTER_SIZE, HeightCluster.class);

            calcHeight(clusterHook);

        }

    }

    private void calcHeight(List<HeightCluster> clusterHooks) {

        for (HeightCluster heightCluster : clusterHooks) {
            List<Vector2dc> hook = heightCluster.getHook();

            double[] heights = new double[hook.size()];
            for (int i = 0; i < hook.size(); i++) {
                heights[i] = randHeight();
            }
            heightCluster.setHeight(heights);
        }
    }

    private double randHeight() {
        Random randomNumberGenerator = new Random();

        return randomNumberGenerator.nextDouble() / 2d + 0.5;
    }

    private <T extends Cluster> ArrayList<T> calcClusterHooks(List<Vector2dc> pHookPoints2, Vector2dc pMinBound,
            double pClusterSize, Class<T> clazz) {

        Vector2dc minBound = pMinBound;

        if (minBound == null) {
            minBound = minBound(pHookPoints2);
        }
        Vector2dc maxBound = maxBound(pHookPoints2);

        double minX = minBound.x();
        double minY = minBound.y();

        double width = maxBound.x() - minBound.x();
        double height = maxBound.y() - minBound.y();

        int clusterXMax = (int) Math.ceil(width / pClusterSize);
        int clusterYMax = (int) Math.ceil(height / pClusterSize);

        T[] clusters = (T[]) Array.newInstance(clazz, clusterYMax * clusterXMax);

        for (int y = 0; y < clusterYMax; y++) {
            for (int x = 0; x < clusterXMax; x++) {

                T c = null;
                try {
                    c = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error(e, e);
                }

                c.setCenter(new Vector3d(minX + pClusterSize / 2 + pClusterSize * x, 0,
                        -(minY + pClusterSize / 2 + pClusterSize * y)));

                clusters[clusterXMax * y + x] = c;
            }
        }

        for (Vector2dc p : pHookPoints2) {
            int clusterX = (int) Math.floor((p.x() - minX) / pClusterSize);
            int clusterY = (int) Math.floor((p.y() - minY) / pClusterSize);

            clusters[clusterXMax * clusterY + clusterX].getHook().add(p);

        }

        ArrayList<T> ret = new ArrayList<>();
        for (int y = 0; y < clusterYMax; y++) {
            ret.addAll(Arrays.asList(clusters).subList(0 + clusterXMax * y + 0, clusterXMax + clusterXMax * y + 0));
        }

        return ret;
    }

    static class HeightCluster extends Cluster {
        double[] height;

        /**
         * @return the height
         */
        public double[] getHeight() {
            return height;
        }

        /**
         * @param height
         *            the height to set
         */
        public void setHeight(double[] height) {
            this.height = height;
        }

    }

    static class Cluster {
        List<Vector2dc> hook;
        Vector3dc center;

        public Cluster() {
            super();
            hook = new ArrayList<>();
            center = new Vector3d();
        }

        /**
         * @return the hook
         */
        public List<Vector2dc> getHook() {
            return hook;
        }

        /**
         * @param hook
         *            the hook to set
         */
        public void setHook(List<Vector2dc> hook) {
            this.hook = hook;
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

    private List<Vector2dc> calsHookPoints(List<Vector2dc> points, Integer numOfTrees) {

        double area = Math.abs(Triangulate.area(points));

        if (numOfTrees == null) {
            // 1 tree on 100 square meters
            numOfTrees = (int) Math.round(area / 100d);
        }

        if (numOfTrees > 1000) {
            // XXX
            numOfTrees = 1000;
        }

        PolygonList2d polygon = new PolygonList2d(points);

        return monteCarloHookGenerator(polygon, numOfTrees);

    }

    /**
     * Minimal values in polygon. Minimal coordinates of bounding box.
     *
     * @param pPolygon
     *            polygon
     * @return minimal values
     */
    public static Vector2dc minBound(List<Vector2dc> points) {

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (Vector2dc p : points) {
            if (p.x() < minX) {
                minX = p.x();
            }
            if (p.y() < minY) {
                minY = p.y();
            }
        }

        return new Vector2d(minX, minY);
    }

    /**
     * Maximal values in polygon. Maximal coordinates of bounding box.
     *
     * @param pPolygon
     *            polygon
     * @return maximal values
     */
    public static Vector2dc maxBound(List<Vector2dc> points) {

        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Vector2dc p : points) {
            if (p.x() > maxX) {
                maxX = p.x();
            }
            if (p.y() > maxY) {
                maxY = p.y();
            }
        }

        return new Vector2d(maxX, maxY);
    }

    /**
     * Generate hook for trees using monte carlo method.
     *
     * @param polygon
     *            polygon
     * @param numOfTrees
     *            num of trees
     * @return hooks for trees
     */
    private List<Vector2dc> monteCarloHookGenerator(PolygonList2d polygon, Integer numOfTrees) {

        List<Vector2dc> ret = new ArrayList<>(numOfTrees);

        Vector2dc minBound = PolygonUtil.minBound(polygon);
        Vector2dc maxBound = PolygonUtil.maxBound(polygon);

        double minX = minBound.x();
        double minY = minBound.y();

        double width = maxBound.x() - minBound.x();
        double height = maxBound.y() - minBound.y();

        Random randomNumberGenerator = new Random();

        for (int i = 0; i < numOfTrees * 10; i++) {

            double x = minX + randomNumberGenerator.nextDouble() * width;
            double y = minY + randomNumberGenerator.nextDouble() * height;

            Vector2dc hook = new Vector2d(x, y);

            if (PolygonUtil.isPointInsidePolygon(hook, polygon)) {
                ret.add(hook);
            }

            if (ret.size() >= numOfTrees) {
                return ret;
            }
        }

        return ret;
    }

    private void setupScale(Model model2, double height) {

        Bounds bounds = model2.getBounds();

        double modelHeight = bounds.max.y();

        double modelScaleHeight = height / modelHeight;

        double modelScaleWidht = modelScaleHeight;

        scale.x = modelScaleWidht;
        scale.y = modelScaleHeight;
        scale.z = modelScaleWidht;

    }

    public boolean isModelBuild(LOD pLod) {

        return modelLod.get(pLod) != null;
    }

    public void draw(Camera camera, LOD pLod) {
        Model model2 = modelLod.get(pLod);

        if (model2 != null) {

            Integer dl = getDisplayList(model2);

            if (dl == null) {
                dl = createDisplayList(model2);
            }

            GL11C.glEnable(GL11.GL_NORMALIZE);

            for (Vector2dc hook : hookPoints) {

                MatrixMath.glPushMatrix();

                MatrixMath.glTranslated(getGlobalX() + hook.x(), 0, -(getGlobalY() + hook.y()));

                MatrixMath.glScaled(scale.x(), scale.y(), scale.z());

                GL11.glCallList(dl);

                MatrixMath.glPopMatrix();
            }

            GL11C.glDisable(GL11.GL_NORMALIZE);
        }
    }

    private int createDisplayList(Model model2) {

        // create one display list
        int index = GL11.glGenLists(1);

        // XXX for texture download
        modelRender.render(model2);

        // compile the display list, store a triangle in it
        GL11.glNewList(index, GL11.GL_COMPILE);

        modelRender.resetFaceCount();
        modelRender.render(model2);
        log.info("***> face count: " + modelRender.getFaceCount());

        GL11.glEndList();

        displayList.put(model2, index == 0 ? null : index);

        return index;
    }

    Map<Model, Integer> displayList = new HashMap<>();

    private Integer getDisplayList(Model model2) {
        return displayList.get(model2);
    }

    @Override
    public void draw(Camera camera, boolean selected) {
        draw(camera);
    }

    @Override
    public void draw(Camera camera) {

        Vector3dc localCamera = new Vector3d(camera.getPoint().x() - getGlobalX(), camera.getPoint().y(),
                camera.getPoint().z() + getGlobalY());

        for (HeightCluster c : clusterHook) {

            if (modelRender.isDebugging()) {
                MatrixMath.glPushMatrix();

                MatrixMath.glTranslated(c.getCenter().x() + getGlobalX(), 2, c.getCenter().z() - getGlobalY());

                DrawUtil.drawDotY(6d, 6);

                MatrixMath.glPopMatrix();
            }

            LOD lod = getLods(c.getCenter(), localCamera);
            List<Vector2dc> hookPoints = c.getHook();
            double[] heights = c.getHeight();

            Model model2 = modelLod.get(lod);

            if (model2 != null) {

                Integer dl = getDisplayList(model2);

                if (dl == null) {
                    dl = createDisplayList(model2);
                }

                GL11C.glEnable(GL11.GL_NORMALIZE);

                int i = 0;
                for (Vector2dc hook : hookPoints) {

                    double height = heights[i];

                    MatrixMath.glPushMatrix();

                    MatrixMath.glTranslated(getGlobalX() + hook.x(), 0, -(getGlobalY() + hook.y()));

                    MatrixMath.glScaled(scale.x() * height, scale.y() * height, scale.z() * height);

                    GL11.glCallList(dl);

                    MatrixMath.glPopMatrix();
                    i++;
                }

                GL11C.glDisable(GL11.GL_NORMALIZE);
            }

        }

    }

    public static LOD getLods(Vector3dc point, Vector3dc camera) {

        double dx = camera.x() - point.x();
        double dy = camera.y() - point.y();
        double dz = camera.z() - point.z();

        double distance = dx * dx + dy * dy + dz * dz;

        if (distance < lod1) {
            return LOD.LOD1;
        } else if (distance < lod2) {
            return LOD.LOD2;
        } else if (distance < lod3) {
            return LOD.LOD3;
        } else if (distance < lod4) {
            return LOD.LOD4;
        }
        return LOD.LOD5;

    }

    @Override
    public List<ExportItem> export(ExportModelConf conf) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Model getModel() {
        return modelLod.get(LOD.LOD1);
    }

    @Override
    public List<Vector3dc> getPoints() {
        List<Vector3dc> ret = new ArrayList<>();
        for (HeightCluster cluster : clusterHook) {

            List<Vector2dc> hookPoints = cluster.getHook();

            for (Vector2dc hook : hookPoints) {
                ret.add(new Vector3d(hook.x(), 0, -hook.y()));
            }
        }
        return ret;
    }

    @Override
    public Vector3dc getPosition() {
        return getPoint();
    }
}
