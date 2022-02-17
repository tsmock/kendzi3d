/*
 * This software is provided "AS IS" without a warranty of any kind. You use it
 * on your own risk and responsibility!!! This file is shared under BSD v3
 * license. See readme.txt and BSD3 file for details.
 */

package kendzi.josm.kendzi3d.jogl.model.trees;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import kendzi.jogl.camera.Camera;
import kendzi.jogl.model.geometry.Bounds;
import kendzi.jogl.model.geometry.Model;
import kendzi.jogl.model.render.ModelRender;
import kendzi.josm.kendzi3d.jogl.model.export.ExportItem;
import kendzi.josm.kendzi3d.jogl.model.export.ExportModelConf;
import kendzi.josm.kendzi3d.jogl.model.lod.DLODSuport;
import kendzi.josm.kendzi3d.jogl.model.lod.LOD;
import kendzi.josm.kendzi3d.jogl.model.tmp.AbstractWayModel;
import kendzi.josm.kendzi3d.service.MetadataCacheService;
import kendzi.josm.kendzi3d.service.ModelCacheService;
import kendzi.josm.kendzi3d.util.ModelUtil;
import kendzi.kendzi3d.josm.model.perspective.Perspective;
import kendzi.kendzi3d.world.MultiPointWorldObject;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Representing trees in row model.
 * 
 * @author Tomasz Kedziora (Kendzi)
 */
public class TreeRow extends AbstractWayModel implements DLODSuport, MultiPointWorldObject {

    private static final double EPSILON = 0.001;

    /**
     * Renderer of model.
     */
    private final ModelRender modelRender;

    /**
     * Model cache service
     */
    private final ModelCacheService modelCacheService;

    /**
     * Metadata cache service.
     */
    private final MetadataCacheService metadataCacheService;

    private final EnumMap<LOD, Model> modelLod;

    private String type;
    private String genus;
    private String species;

    Vector3d scale;

    private List<Vector2dc> hookPoints;

    private Integer numOfTrees;

    /**
     * @param pWay
     *            way
     * @param perspective
     *            perspective
     */
    public TreeRow(Way pWay, Perspective perspective, ModelRender pModelRender, ModelCacheService modelCacheService,
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

        buildModel = true;
    }

    @Override
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
        }
    }

    private List<Vector2dc> calsHookPoints(List<Vector2dc> points, Integer numOfTrees) {

        double distance = calcDistance(points);

        if (numOfTrees == null) {
            numOfTrees = (int) Math.round(distance / 5d);
        }

        List<Vector2dc> ret = new ArrayList<>();

        double repeatEvery = distance / numOfTrees;

        double lastOffset = 0;

        Vector2dc b = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Vector2dc e = points.get(i);
            distance = e.distance(b);

            lastOffset = splitVector(b, e, lastOffset, repeatEvery, ret);

            b = e;

        }

        return ret;
    }

    private double splitVector(Vector2dc b, Vector2dc e, double left, double every, List<Vector2dc> ret) {
        Vector2d v = new Vector2d(e);
        v.sub(b);
        double distance = v.length();
        if (distance + EPSILON < left) {
            return left - distance;
        }

        v.normalize();

        Vector2d beginVector = new Vector2d(v).mul(left);

        Vector2d everyVector = new Vector2d(v).mul(every);

        Vector2d repeat = beginVector;

        do {
            ret.add(new Vector2d(b.x() + repeat.x(), b.y() + repeat.y()));

            repeat.add(everyVector);

        } while (distance + EPSILON >= repeat.length());

        return repeat.length() - distance;
        // return distance - (repeat.length() - everyVector.length());

    }

    private double calcDistance(List<Vector2dc> points) {

        if (points == null || points.size() < 2) {
            return 0d;
        }
        double distance = 0;

        Vector2dc b = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Vector2dc e = points.get(i);
            distance = distance + e.distance(b);

            b = e;
        }
        return distance;
    }

    private void setupScale(Model model2, double height) {

        Bounds bounds = model2.getBounds();

        double modelHeight = bounds.max.y();

        double modelWidht = Math.max(bounds.max.x() - bounds.min.x(), bounds.max.z() - bounds.min.z());

        double modelScaleHeight = height / modelHeight;

        double modelScaleWidht = modelScaleHeight;

        scale.x = modelScaleWidht;
        scale.y = modelScaleHeight;
        scale.z = modelScaleWidht;

        // model2.useScale = true;
    }

    @Override
    public boolean isModelBuild(LOD pLod) {

        if (modelLod.get(pLod) != null) {
            return true;
        }
        return false;
    }

    @Override
    public void draw(GL2 gl, Camera camera, LOD pLod) {
        Model model2 = modelLod.get(pLod);
        if (model2 != null) {

            gl.glEnable(GLLightingFunc.GL_NORMALIZE);

            for (Vector2dc hook : hookPoints) {

                gl.glPushMatrix();

                gl.glTranslated(getGlobalX() + hook.x(), 0, -(getGlobalY() + hook.y()));

                gl.glScaled(scale.x(), scale.y(), scale.z());

                modelRender.render(gl, model2);

                gl.glPopMatrix();
            }

            gl.glDisable(GLLightingFunc.GL_NORMALIZE);
        }
    }

    @Override
    public void draw(GL2 gl, Camera camera, boolean selected) {
        draw(gl, camera);
    }

    @Override
    public void draw(GL2 gl, Camera camera) {
        draw(gl, camera, LOD.LOD1);
    }

    @Override
    public List<ExportItem> export(ExportModelConf conf) {
        if (modelLod.get(LOD.LOD1) == null) {
            buildModel(LOD.LOD1);
        }

        List<ExportItem> ret = new ArrayList<>();

        for (Vector2dc hook : hookPoints) {

            Vector3d p = new Vector3d(getGlobalX() + hook.x(), 0, -(getGlobalY() + hook.y()));

            Vector3d s = new Vector3d(scale.x(), scale.y(), scale.z());
            ret.add(new ExportItem(modelLod.get(LOD.LOD1), p, s));

        }
        return ret;
    }

    @Override
    public List<Vector3dc> getPoints() {
        List<Vector3dc> ret = new ArrayList<>();

        for (Vector2dc hook : hookPoints) {
            ret.add(new Vector3d(hook.x(), 0, -hook.y()));
        }
        return ret;
    }

    @Override
    public Model getModel() {
        return modelLod.get(LOD.LOD1);
    }

    @Override
    public Vector3dc getPosition() {
        return getPoint();
    }
}
