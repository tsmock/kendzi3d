package kendzi.jogl;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import kendzi.jogl.util.GLEventListener;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapt GLEventListener from JOGL
 */
public class GLAutoDrawable extends AWTGLCanvas implements ComponentListener {
    private static final Logger log = LoggerFactory.getLogger(GLAutoDrawable.class);
    /**
     * Indicates if there is a debug agent loaded. Defaults to true, but if the
     * debug agent cannot be found, is switched to false
     */
    private static boolean DEBUG_AGENT_LOADED = true;

    protected Collection<GLEventListener> listeners = new LinkedHashSet<>();

    public GLAutoDrawable() {
        super();
    }

    public GLAutoDrawable(GLData glData) {
        super(glData);
    }

    /**
     * Add a listener to be called on repaint
     *
     * @param listener
     *            The listener to be called
     * @return See {@link Collection#add(Object)}
     */
    public boolean addGLEventListener(GLEventListener listener) {
        if (this.initCalled) {
            listener.init();
        }
        return listeners.add(listener);
    }

    /**
     * Remove a listener to be called on repaint
     *
     * @param listener
     *            The listener to be removed
     * @return See {@link Collection#remove(Object)}
     */
    public boolean removeGLEventListener(GLEventListener listener) {
        listener.dispose();
        return listeners.remove(listener);
    }

    private static void fixDebugAgent() {
        if (DEBUG_AGENT_LOADED) {
            try {
                Class<?> c = Class.forName("org.lwjglx.debug.org.lwjgl.opengl.Context");
                {
                    Method m = c.getMethod("create", long.class, long.class);
                    m.setAccessible(true);
                    m.invoke(null, 0L, 0L);
                }
                Object ctx;
                {
                    Field f = c.getField("CONTEXTS");
                    f.setAccessible(true);
                    Map<Long, Object> contexts = (Map<Long, Object>) f.get(null);
                    ctx = contexts.get(0L);
                    if (ctx == null)
                        throw new IllegalStateException();
                }
                {
                    Field f = c.getField("CURRENT_CONTEXT");
                    f.setAccessible(true);
                    ThreadLocal<Object> v = (ThreadLocal<Object>) f.get(null);
                    v.set(ctx);
                }
            } catch (ClassNotFoundException e) {
                log.error("GLAutoDrawable", e);
                DEBUG_AGENT_LOADED = false;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void initGL() {
        fixDebugAgent();
        GL.createCapabilities(this.data.forwardCompatible);
        Color color = Color.LIGHT_GRAY;
        GL11C.glClearColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        this.runCommand(GLEventListener::init);
        // Ensure that the viewport is initialized (requires GL.createCapabilities to
        // have been called)
        this.addComponentListener(this);
        SwingUtilities.invokeLater(() -> this.componentResized(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED)));
    }

    @Override
    public void paintGL() {
        fixDebugAgent();
        try {
            this.runCommand(GLEventListener::display);
            this.swapBuffers();
        } catch (Exception e) {
            log.error("Exception", e);
            System.exit(-1);
        }
    }

    @Override
    public void disposeCanvas() {
        this.componentHidden(new ComponentEvent(this, ComponentEvent.COMPONENT_HIDDEN));
        this.removeComponentListener(this);
        this.runCommand(GLEventListener::dispose);
        super.disposeCanvas();
    }

    @Override
    public void componentResized(ComponentEvent event) {
        if (this.initCalled) {
            final int x = event.getComponent().getX();
            final int y = event.getComponent().getY();
            final int width = event.getComponent().getWidth();
            final int height = event.getComponent().getHeight();
            this.runInContext(() -> this.runCommand(listener -> listener.reshape(x, y, width, height)));
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        this.componentResized(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        this.componentResized(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        this.componentResized(e);
    }

    private void runCommand(Consumer<GLEventListener> callable) {
        for (GLEventListener listener : this.listeners) {
            callable.accept(listener);
        }
    }

    @Override
    public void repaint() {
        super.repaint();
        if (SwingUtilities.isEventDispatchThread()) {
            this.render();
        } else {
            SwingUtilities.invokeLater(this::render);
        }
    }
}
