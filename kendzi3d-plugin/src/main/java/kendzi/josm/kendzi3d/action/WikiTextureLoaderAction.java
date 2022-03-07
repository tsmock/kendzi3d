/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.josm.kendzi3d.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import com.google.inject.Inject;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import kendzi.josm.kendzi3d.service.WikiTextureLoaderService;
import kendzi.josm.kendzi3d.service.WikiTextureLoaderService.LoadRet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Texture filter toggle action.
 *
 * @author Tomasz Kędziora (Kendzi)
 *
 */
public class WikiTextureLoaderAction extends JosmAction {

    /** Log. */
    private static final Logger log = LogManager.getLogger(WikiTextureLoaderAction.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Wiki texture loader service.
     */
    private final WikiTextureLoaderService wikiTextureLoaderService;

    /**
     * Constructor of wiki texture loader toggle action.
     * 
     * @param pWikiTextureLoaderService
     *            wiki texture loader service
     */
    @Inject
    public WikiTextureLoaderAction(WikiTextureLoaderService pWikiTextureLoaderService) {
        super(tr("Load textures from wiki"), "1323558253_wikipedia-icon_24", tr("Load textures from wiki"), null, false);

        this.wikiTextureLoaderService = pWikiTextureLoaderService;
    }

    /**
     *
     */
    public void loadFromWiki() {
        List<String> errors = null;
        String timestamp = null;
        try {
            LoadRet load = this.wikiTextureLoaderService.load();
            errors = load.getErrors();
            timestamp = load.getTimestamp();

        } catch (JAXBException | IOException e) {
            log.error(e, e);
            showError(e);
        }

        if (errors != null && !errors.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            for (String err : errors) {
                sb.append(err);
                sb.append("\n");
            }

            JOptionPane.showMessageDialog(null, tr("Error downloding textures from urls: ") + "\n" + sb, "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, tr("Downloded textures from wiki timestamp: " + timestamp + " to path: ") + "\n"
                    + this.wikiTextureLoaderService.getTexturesPath(), "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showError(Exception e) {
        // custom title, error icon
        JOptionPane.showMessageDialog(null, "Error downloding textures from wiki: " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent pE) {
        loadFromWiki();
    }

    @Override
    protected void updateEnabledState() {
        // setEnabled(Main.map != null && Main.main.getEditLayer() != null);
    }
}
