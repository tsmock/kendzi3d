package kendzi.kendzi3d.editor.example.objects;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kendzi.kendzi3d.editor.EditableObject;
import kendzi.kendzi3d.editor.selection.Selection;
import kendzi.kendzi3d.editor.selection.SphereSelection;
import kendzi.kendzi3d.editor.selection.editor.ArrowEditorImp;
import kendzi.kendzi3d.editor.selection.editor.CachePoint3dProvider;
import kendzi.kendzi3d.editor.selection.editor.Editor;
import kendzi.kendzi3d.editor.selection.editor.EditorType;
import kendzi.kendzi3d.editor.selection.event.ArrowEditorChangeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Roof implements EditableObject {

    private static final Logger LOG = LogManager.getLogger(Roof.class);

    private final Vector3dc position = new Vector3d(0, 0, 1);

    private double height = 2;

    private double roofHeight = 1;

    private double width = 1;

    private List<Selection> selections;

    /**
     * Constructor.
     */
    public Roof() {
        createSelections();
    }

    private void createSelections() {

        final CachePoint3dProvider heightPointProvider = new CachePoint3dProvider() {
            @Override
            public void beforeProvide(Vector3d point) {
                point.set(position).add(0, height, 0);
            }
        };

        final ArrowEditorImp editorHeight = new ArrowEditorImp(position, new Vector3d(0, 1, 0), height);
        editorHeight.setOffset(0.1);

        final ArrowEditorImp editorRoofHeight = new ArrowEditorImp();
        editorRoofHeight.setEditorOrigin(heightPointProvider);
        editorRoofHeight.setVector(new Vector3d(0, -1, 0));
        editorRoofHeight.setLength(roofHeight);
        editorRoofHeight.setEditorType(EditorType.BOX_SMALL);

        editorHeight.addChangeListener(event -> {
            if (event instanceof ArrowEditorChangeEvent) {
                ArrowEditorChangeEvent arrow = (ArrowEditorChangeEvent) event;

                double newHeight = arrow.getLength();
                LOG.info("editor height changed: " + newHeight);

                if (newHeight < 0) {
                    setHeight(0);
                    setRoofHeight(0);

                    editorHeight.setLength(0);
                    editorRoofHeight.setLength(0);

                } else if (newHeight - roofHeight < 0) {

                    setHeight(newHeight);
                    setRoofHeight(newHeight);

                    editorRoofHeight.setLength(newHeight);
                } else {
                    setHeight(newHeight);
                }
            }
        });

        editorRoofHeight.addChangeListener(event -> {
            if (event instanceof ArrowEditorChangeEvent) {
                ArrowEditorChangeEvent arrow = (ArrowEditorChangeEvent) event;

                double length = arrow.getLength();
                LOG.info("editor roof changed: " + length);

                if (length < 0) {
                    double newHeigth = getHeight() - length;

                    setHeight(newHeigth);
                    setRoofHeight(0);

                    editorHeight.setLength(newHeigth);
                    editorRoofHeight.setLength(0);
                } else if (length > height) {

                    setRoofHeight(height);

                    editorRoofHeight.setLength(height);
                } else {

                    setRoofHeight(length);
                }
            }
        });

        final List<Editor> editors = Arrays.asList(editorHeight, editorRoofHeight);

        selections = Collections.singletonList(new SphereSelection(position, height) {

            @Override
            public List<Editor> getEditors() {
                return editors;
            }

            @Override
            public Object getSource() {
                return Roof.this;
            }

            @Override
            public double getRadius() {
                return height;
            }
        });
    }

    @Override
    public Vector3dc getPosition() {
        return position;
    }

    @Override
    public List<Selection> getSelection() {
        return selections;
    }

    public double getWidth() {
        return width;
    }

    public double getRoofHeight() {
        return roofHeight;
    }

    public void setRoofHeight(double roofHeight) {
        this.roofHeight = roofHeight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }
}