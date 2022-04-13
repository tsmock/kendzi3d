package kendzi.jogl.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertexArrayObject implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertexArrayObject.class);
    private final int id;
    private final boolean glBindVertexArray;
    private final boolean ARB_vertex_array_object;
    private BufferObject indices = null;
    private BufferObject vertex = null;
    private BufferObject normal = null;
    private BufferObject color = null;
    private BufferObject texture = null;

    VertexArrayObject() {
        this.glBindVertexArray = GL.getCapabilities().glBindVertexArray != MemoryUtil.NULL;
        this.ARB_vertex_array_object = GL.getCapabilities().GL_ARB_vertex_array_object;
        if (this.glBindVertexArray) {
            this.id = GL30C.glGenVertexArrays();
        } else if (this.ARB_vertex_array_object) {
            this.id = ARBVertexArrayObject.glGenVertexArrays();
        } else {
            this.id = 0;
            // throw new UnsupportedOperationException("Vertex Array Objects are not
            // supported in the current GL context");
        }
    }

    public void bind() {
        if (this.id > 0) {
            if (this.glBindVertexArray) {
                GL30C.glBindVertexArray(this.id);
            } else if (this.ARB_vertex_array_object) {
                ARBVertexArrayObject.glBindVertexArray(this.id);
            }
        }
    }

    public void unbind() {
        if (this.glBindVertexArray) {
            GL30C.glBindVertexArray(0);
        } else if (this.ARB_vertex_array_object) {
            ARBVertexArrayObject.glBindVertexArray(0);
        }
    }

    public void draw() {
        draw(GL11C.GL_TRIANGLES);
    }

    /**
     *
     * @param mode
     *            See {@link GL11C#glDrawElements(int, int, int, long)} for more
     *            information
     */
    public void draw(int mode) {
        if (this.id > 0) {
            this.bind();
            GL11C.glDrawArrays(mode, 0, this.indices.count());
            // GL11C.glDrawElements(mode, this.indices.count(), GL11C.GL_UNSIGNED_INT, 0);
            this.unbind();
        } else {
            GL11.glEnableClientState(GL11.GL_INDEX_ARRAY);
            this.indices.bindBuffer();
            GL11.glIndexPointer(this.indices.getDataType(), 0, 0);

            GL11.glEnableClientState(GL11C.GL_VERTEX_ARRAY);
            this.vertex.bindBuffer();
            GL11.glVertexPointer(3, this.vertex.getDataType(), 0, 0);

            if (this.normal != null) {
                GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
                this.normal.bindBuffer();
                GL11.glNormalPointer(this.normal.getDataType(), 0, 0);
            }
            if (this.color != null) {
                GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                this.color.bindBuffer();
                GL11.glColorPointer(this.color.count(), this.color.getDataType(), 0, 0);
            }
            if (this.texture != null) {
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                this.texture.bindBuffer();
                GL11.glTexCoordPointer(this.texture.count(), this.texture.getDataType(), 0, 0);
            }
            GL11.glDrawArrays(mode, 0, this.vertex.count());
            this.vertex.unbindBuffer();
            GL11.glDisableClientState(GL11.GL_INDEX_ARRAY);
            GL11.glDisableClientState(GL11C.GL_VERTEX_ARRAY);
            if (this.normal != null) {
                GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
            }
            if (this.color != null) {
                GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
            }
            if (this.texture != null) {
                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            }
        }
    }

    @Override
    public void close() {
        if (this.id != 0) {
            GL30C.glDeleteVertexArrays(this.id);
        }
        for (BufferObject bo : Arrays.asList(this.indices, this.texture, this.color, this.vertex, this.normal)) {
            if (bo != null) {
                bo.close();
            }
        }
    }

    public void enable(int... vertexIndices) {
        if (this.id == 0) {
            return;
        }
        this.bind();
        for (int i : vertexIndices) {
            GL20C.glEnableVertexAttribArray(i);
        }
        this.unbind();
    }

    public void enableAll() {
        enable(IntStream.of(0, count()).toArray());
    }

    private int count() {
        return (int) Stream.of(this.indices, this.vertex, this.normal, this.color, this.texture).filter(Objects::nonNull).count();
    }

    public void disable(int... vertexIndices) {
        if (this.id == 0) {
            return;
        }
        this.bind();
        for (int i : vertexIndices) {
            GL20C.glDisableVertexAttribArray(i);
        }
        this.unbind();
    }

    public void disableAll() {
        disable(IntStream.of(0, count()).toArray());
    }

    public int getId() {
        return this.id;
    }

    public static VertexArrayObject createVertexArrayObject(final int[] indices, final double[] vertex, final double[] normal,
            final double[] color, final double[] texture) {
        return getVertexArrayObject(indices, createBufferObject(vertex), createBufferObject(normal), createBufferObject(color),
                createBufferObject(texture));
    }

    public static VertexArrayObject createVertexArrayObject(final int[] indices, final int[] vertex, final int[] normal,
            final int[] color, final int[] texture) {
        return getVertexArrayObject(indices, createBufferObject(vertex), createBufferObject(normal), createBufferObject(color),
                createBufferObject(texture));
    }

    private static VertexArrayObject getVertexArrayObject(int[] indices, BufferObject vertex, BufferObject normal,
            BufferObject color, BufferObject texture) {
        // Indices must all be positive. Ideally, this would only be checked if we were
        // in debug mode
        assert Arrays.stream(indices).allMatch(i -> i >= 0);
        final VertexArrayObject vao = new VertexArrayObject();
        vao.bind();
        final BufferObject indexBuffer = new BufferObject(GL15C.GL_ELEMENT_ARRAY_BUFFER, GL11C.GL_INT, indices.length,
                type -> GL15C.glBufferData(type, indices, GL15C.GL_STATIC_DRAW));
        indexBuffer.bindBuffer();
        vao.indices = indexBuffer;
        if (vertex != null) {
            vao.vertex = vertex;
            vertex.bindBuffer();
        }
        if (normal != null) {
            vao.normal = normal;
            normal.bindBuffer();
        }
        if (color != null) {
            vao.color = color;
            color.bindBuffer();
        }
        if (texture != null) {
            vao.texture = texture;
            texture.bindBuffer();
        }

        vao.enableAll();
        vao.unbind();
        return vao;
    }

    private static BufferObject createBufferObject(final int[] data) {
        if (data == null) {
            return null;
        }
        return new BufferObject(GL15C.GL_ARRAY_BUFFER, GL11C.GL_INT, data.length,
                type -> GL15C.glBufferData(type, data, GL15C.GL_STATIC_DRAW));
    }

    private static BufferObject createBufferObject(final double[] data) {
        if (data == null) {
            return null;
        }
        return new BufferObject(GL15C.GL_ARRAY_BUFFER, GL11C.GL_DOUBLE, data.length,
                type -> GL15C.glBufferData(type, data, GL15C.GL_STATIC_DRAW));
    }
}
