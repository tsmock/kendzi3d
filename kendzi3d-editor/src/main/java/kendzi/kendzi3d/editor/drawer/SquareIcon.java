/*
 * This software is provided "AS IS" without a warranty of any kind. You use it
 * on your own risk and responsibility!!! This file is shared under BSD v3
 * license. See readme.txt and BSD3 file for details.
 */

package kendzi.kendzi3d.editor.drawer;

import javax.inject.Inject;

import kendzi.jogl.Gl2Draw;
import kendzi.jogl.texture.TextureCacheService;
import kendzi.jogl.util.DrawUtil;
import kendzi.jogl.util.texture.Texture;
import kendzi.jogl.util.texture.TextureCoords;
import kendzi.kendzi3d.editor.selection.ViewportProvider;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

/**
 * Displays given icon.
 */
public class SquareIcon implements Gl2Draw {

    /**
     * File with icon.
     */
    private String icon;

    /**
     * Display icon size.
     */
    private final double size = 64;

    private final double x = 10;

    private final double y = 10;

    /**
     * Texture cache service.
     */
    @Inject
    private TextureCacheService textureCacheService;

    /**
     * Provider for current viewport settings.
     */
    @Inject
    private ViewportProvider viewportProvider;

    /**
     * Adds icon to display.
     *
     * @param iconName
     *            resource path for file with icon
     */
    public void addIcon(String iconName) {
        icon = iconName;
    }

    /**
     * Draws icon.
     *
     */

    @Override
    public void draw() {

        if (icon == null) {
            return;
        }

        // Calculate icon location on screen.
        double width = viewportProvider.getViewport().getWidth();
        double height = viewportProvider.getViewport().getHeight();

        double maxx = x + size;
        double minx = maxx - size;

        double miny = y;
        double maxy = miny + size;

        Texture texture = textureCacheService.getTexture(icon);
        if (texture == null) {
            return;
        }
        texture.enable();
        texture.bind();

        GL11C.glDisable(GL11.GL_LIGHTING);
        GL11C.glEnable(GL11C.GL_BLEND);
        GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);

        // Set transparency for texture
        GL11.glColor4f(1f, 1f, 1f, 0.8f);

        // Mix transparency color with texture
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        // No depth.
        GL11C.glDisable(GL11C.GL_DEPTH_TEST);

        // Draw icon in 2d mode.
        DrawUtil.begin2D(width, height);

        TextureCoords tc = texture.getImageTexCoords();

        GL11.glBegin(GL11.GL_POLYGON);

        GL11.glTexCoord2d(tc.left(), tc.bottom());
        GL11.glVertex2d(minx, maxy);
        GL11.glTexCoord2d(tc.right(), tc.bottom());
        GL11.glVertex2d(maxx, maxy);
        GL11.glTexCoord2d(tc.right(), tc.top());
        GL11.glVertex2d(maxx, miny);
        GL11.glTexCoord2d(tc.left(), tc.top());
        GL11.glVertex2d(minx, miny);

        GL11.glEnd();

        GL11C.glEnable(GL11.GL_LIGHTING);

        texture.disable();

        DrawUtil.end2D();

        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
    }
}
