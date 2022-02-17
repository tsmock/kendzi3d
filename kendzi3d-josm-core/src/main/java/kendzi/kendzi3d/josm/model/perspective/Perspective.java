package kendzi.kendzi3d.josm.model.perspective;

import org.joml.Vector2dc;
import org.openstreetmap.josm.data.osm.Node;

public interface Perspective {

    Vector2dc calcPoint(Node node);

}
