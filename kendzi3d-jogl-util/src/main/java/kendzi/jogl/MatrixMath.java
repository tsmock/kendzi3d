package kendzi.jogl;

import java.util.ArrayDeque;
import java.util.Deque;

import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

/**
 * A class to help ease the transition off of deprecated GL matrix functions.
 * Note: If the context under which this class is initialized does not have
 * deprecated functions, the returned matrices <i>must</i> be used to change the
 * data.
 */
public class MatrixMath {
    private MatrixMath() {
        /* Do nothing */
    };

    private static class MatrixType {
        final Deque<Matrix4d> stack = new ArrayDeque<>(64);
        final Matrix4d currentMatrix = new Matrix4d();
        final int type;

        MatrixType(int type) {
            this.type = type;
        }
    }

    /** See {@link org.lwjgl.opengl.GLCapabilities#forwardCompatible} */
    private static final boolean forwardCompatible = GL.getCapabilities().forwardCompatible;
    private static final MatrixType MODEL_VIEW = new MatrixType(GL11.GL_MODELVIEW);
    private static final MatrixType PROJECTION = new MatrixType(GL11.GL_PROJECTION);
    private static final MatrixType TEXTURE = new MatrixType(GL11.GL_TEXTURE);
    private static final MatrixType COLOR = new MatrixType(GL11.GL_COLOR);
    private static MatrixType currentType = MODEL_VIEW;

    public static Matrix4dc glTranslated(double x, double y, double z) {
        if (!forwardCompatible) {
            GL11.glTranslated(x, y, z);
        }
        return currentType.currentMatrix.translate(x, y, z);
    }

    public static Matrix4dc glTranslatef(float x, float y, float z) {
        if (!forwardCompatible) {
            GL11.glTranslatef(x, y, z);
        }
        return currentType.currentMatrix.translate(x, y, z);
    }

    public static Matrix4dc glPushMatrix() {
        if (!forwardCompatible) {
            GL11.glPushMatrix();
        }
        final Matrix4d newMatrix = new Matrix4d(currentType.currentMatrix);
        currentType.stack.push(newMatrix);
        return newMatrix;
    }

    public static Matrix4dc glPopMatrix() {
        if (!forwardCompatible) {
            GL11.glPopMatrix();
        }
        return currentType.currentMatrix.set(currentType.stack.pop());
    }

    public static Matrix4dc glRotated(double angle, double x, double y, double z) {
        if (!forwardCompatible) {
            GL11.glRotated(angle, x, y, z);
        }
        return currentType.currentMatrix.rotate(angle, x, y, z);
    }

    public static Matrix4dc glRotatef(float angle, float x, float y, float z) {
        if (!forwardCompatible) {
            GL11.glRotatef(angle, x, y, z);
        }
        return currentType.currentMatrix.rotate(angle, x, y, z);
    }

    public static Matrix4dc glScaled(double x, double y, double z) {
        if (!forwardCompatible) {
            GL11.glScaled(x, y, z);
        }
        return currentType.currentMatrix.scale(x, y, z);
    }

    public static Matrix4dc glScalef(float x, float y, float z) {
        if (!forwardCompatible) {
            GL11.glScalef(x, y, z);
        }
        return currentType.currentMatrix.scale(x, y, z);
    }

    public static Matrix4dc glLoadIdentity() {
        if (!forwardCompatible) {
            GL11.glLoadIdentity();
        }
        return currentType.currentMatrix.identity();
    }

    public static Matrix4dc glOrtho(float left, double right, double bottom, float top, float zNear, float zFar) {
        if (!forwardCompatible) {
            GL11.glOrtho(left, right, bottom, top, zNear, zFar);
        }
        return currentType.currentMatrix.ortho(left, right, bottom, top, zNear, zFar);
    }

    public static Matrix4dc glMultMatrixd(double[] doubles) {
        if (!forwardCompatible) {
            GL11.glMultMatrixd(doubles);
        }
        return currentType.currentMatrix.mul(doubles[0], doubles[1], doubles[2], doubles[3], doubles[4], doubles[5], doubles[6],
                doubles[7], doubles[8], doubles[9], doubles[10], doubles[11], doubles[12], doubles[13], doubles[14], doubles[15]);
    }

    public static void glMatrixMode(int glMatrixMode) {
        if (!forwardCompatible) {
            GL11.glMatrixMode(glMatrixMode);
        }
        switch (glMatrixMode) {
        case GL11.GL_MODELVIEW:
            currentType = MODEL_VIEW;
            break;
        case GL11.GL_PROJECTION:
            currentType = PROJECTION;
            break;
        case GL11.GL_TEXTURE:
            currentType = TEXTURE;
            break;
        case GL11.GL_COLOR:
            currentType = COLOR;
            break;
        default:
            throw new RuntimeException("Unknown glMatrixMode: " + glMatrixMode);
        }
    }
}
