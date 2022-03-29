/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.jogl.model.geometry;

import java.util.stream.IntStream;

import kendzi.jogl.util.BufferObject;
import kendzi.jogl.util.VertexArrayObject;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;

public class Mesh implements AutoCloseable {
    private VertexArrayObject bufferObject;

    private Face[] face;

    /** An array of vertex points (point) */
    private Vector3dc[] vertices;

    /** An array of vertex normals (vector) */
    private Vector3dc[] normals;

    private TextCoord[] texCoords;

    private String name;

    private int materialID;

    private boolean hasTexture;

    public VertexArrayObject getArrayObject() {
        if (this.bufferObject != null) {
            return this.bufferObject;
        }
        this.generateBufferObject();
        return this.bufferObject;
    }

    private synchronized void generateBufferObject() {
        if (this.bufferObject != null) {
            return;
        }
        VertexArrayObject newBufferObject = new VertexArrayObject();
        newBufferObject.bind();
        for (Face currentFace : this.face) {
            BufferObject indices = new BufferObject(GL15C.GL_ELEMENT_ARRAY_BUFFER, currentFace.vertIndex.length / 3, type -> GL15C
                    .glBufferData(type, IntStream.of(0, currentFace.vertIndex.length).toArray(), GL15C.GL_STATIC_DRAW));
            indices.bindBuffer();

            BufferObject vertexBuffer = new BufferObject(GL15C.GL_ARRAY_BUFFER, currentFace.vertIndex.length,
                    type -> GL15C.glBufferData(type, currentFace.vertIndex, GL15C.GL_STATIC_DRAW));
            vertexBuffer.bindBuffer();
            GL20C.glVertexAttribPointer(0, 3, GL11C.GL_INT, false, 0, 0);

            BufferObject normalBuffer = new BufferObject(GL15C.GL_ARRAY_BUFFER, currentFace.normalIndex.length,
                    type -> GL15C.glBufferData(type, currentFace.normalIndex, GL15C.GL_STATIC_DRAW));
            normalBuffer.bindBuffer();
            GL20C.glVertexAttribPointer(1, 3, GL11C.GL_INT, false, 0, 0);
            normalBuffer.unbindBuffer();

            newBufferObject.add(indices);
            newBufferObject.add(vertexBuffer);
            newBufferObject.add(normalBuffer);
            newBufferObject.enableAll();
        }
        newBufferObject.unbind();
        this.bufferObject = newBufferObject;
    }

    public TextCoord[] getTexCoords() {
        return this.texCoords;
    }

    public void setTexCoords(final TextCoord[] texCoords) {
        this.texCoords = texCoords;
        this.resetBufferObject();
    }

    public Vector3dc[] getNormals() {
        return this.normals;
    }

    public void setNormals(final Vector3dc[] normals) {
        this.normals = normals;
        this.resetBufferObject();
    }

    public Vector3dc[] getVertices() {
        return this.vertices;
    }

    public void setVertices(final Vector3dc[] vertices) {
        this.vertices = vertices;
        this.resetBufferObject();
    }

    public Face[] getFaces() {
        return this.face;
    }

    public void setFaces(final Face[] face) {
        this.face = face;
        this.resetBufferObject();
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getMaterialID() {
        return this.materialID;
    }

    public void setMaterialID(final int materialID) {
        this.materialID = materialID;
    }

    public boolean hasTexture() {
        return this.hasTexture;
    }

    public void setHasTexture(final boolean hasTexture) {
        this.hasTexture = hasTexture;
    }

    private synchronized void resetBufferObject() {
        if (this.bufferObject != null) {
            this.bufferObject.close();
            this.bufferObject = null;
        }
    }

    @Override
    public void close() {
        this.resetBufferObject();
    }
}
