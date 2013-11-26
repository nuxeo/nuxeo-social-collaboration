package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.ScopeType.SESSION;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * Business delegate exposing the {@link SocialWorkspaceService} as a seam
 * component.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Name("socialWorkspaceService")
@Scope(SESSION)
public class SocialWorkspaceServiceBusinessDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SocialWorkspaceServiceBusinessDelegate.class);

    protected SocialWorkspaceService socialWorkspaceService;

    /**
     * Acquires a new {@link SocialWorkspaceService} reference. The related
     * service may be deployed on a local or remote AppServer.
     *
     * @throws org.nuxeo.ecm.core.api.ClientException
     */
    @Unwrap
    public SocialWorkspaceService getService() throws ClientException {
        if (socialWorkspaceService == null) {
            try {
                socialWorkspaceService = Framework.getService(SocialWorkspaceService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to SocialWorkspaceService. "
                        + e.getMessage();
                throw new ClientException(errMsg, e);
            }
            if (socialWorkspaceService == null) {
                throw new ClientException(
                        "SocialWorkspaceService service not bound");
            }
        }
        return socialWorkspaceService;
    }

    @Destroy
    public void destroy() {
        if (socialWorkspaceService != null) {
            socialWorkspaceService = null;
        }
        log.debug("Destroyed the seam component");
    }

}
