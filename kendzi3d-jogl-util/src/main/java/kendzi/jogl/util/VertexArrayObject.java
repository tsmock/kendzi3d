package kendzi.jogl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class VertexArrayObject implements AutoCloseable {
    private final int id;
    private final List<BufferObject> bufferObjectList = new ArrayList<>();
    private BufferObject indices = null;

    public VertexArrayObject() {
        if (GL.getCapabilities().OpenGL30) {
            this.id = GL30C.glGenVertexArrays();
        } else {
            this.id = 0;
        }
    }

    public void bind() {
        if (this.id > 0) {
            GL30C.glBindVertexArray(this.id);
        }
    }

    public void unbind() {
        if (this.id > 0) {
            GL30C.glBindVertexArray(0);
        }
    }

    public void draw() {
        if (this.id > 0) {
            this.bind();
            GL11C.glDrawElements(GL11C.GL_TRIANGLES, this.indices.count(), GL11C.GL_INT, 0);
            this.unbind();
        } else {
            GL11.glEnableClientState(GL11C.GL_VERTEX_ARRAY);
            this.bufferObjectList.get(0).bindBuffer();
            GL11.glVertexPointer(3, GL11C.GL_DOUBLE, 0, 0);
            if (this.bufferObjectList.size() >= 2) {
                GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
                this.bufferObjectList.get(1).bindBuffer();
                GL11.glNormalPointer(GL11C.GL_DOUBLE, 0, 0);
            }
            GL11.glDrawArrays(GL11C.GL_TRIANGLES, 0, this.bufferObjectList.get(0).count());
            this.bufferObjectList.get(0).unbindBuffer();
            GL11.glDisableClientState(GL11C.GL_VERTEX_ARRAY);
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        }
    }

    @Override
    public void close() {
        if (this.id != 0) {
            GL30C.glDeleteVertexArrays(this.id);
        }
        this.bufferObjectList.forEach(BufferObject::close);
        this.indices.close();
    }

    public void add(BufferObject vertexBuffer) {
        if (this.indices == null) {
            this.indices = vertexBuffer;
        } else {
            this.bufferObjectList.add(vertexBuffer);
            this.enable(this.bufferObjectList.indexOf(vertexBuffer));
        }
    }

    public void enable(int... vertexIndices) {
        this.bind();
        for (int i : vertexIndices) {
            GL20C.glEnableVertexAttribArray(i);
        }
        this.unbind();
    }

    public void enableAll() {
        enable(IntStream.of(0, this.bufferObjectList.size()).toArray());
    }

    public void disable(int... vertexIndices) {
        this.bind();
        for (int i : vertexIndices) {
            GL20C.glDisableVertexAttribArray(i);
        }
        this.unbind();
    }

    public void disableAll() {
        disable(IntStream.of(0, this.bufferObjectList.size()).toArray());
    }
}
