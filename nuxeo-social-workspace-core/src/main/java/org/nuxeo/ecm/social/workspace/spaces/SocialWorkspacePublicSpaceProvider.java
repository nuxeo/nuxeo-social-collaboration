package org.nuxeo.ecm.social.workspace.spaces;

import static org.nuxeo.ecm.social.workspace.SocialConstants.PUBLIC_DASHBOARD_SPACE_NAME;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.spaces.api.AbstractSpaceProvider;
import org.nuxeo.ecm.spaces.api.Space;
import org.nuxeo.ecm.spaces.api.exceptions.SpaceException;

/**
 * Creates the default Public {@link Space} for a Social Workspace.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class SocialWorkspacePublicSpaceProvider extends AbstractSpaceProvider {

    @Override
    protected Space doGetSpace(CoreSession session, DocumentModel contextDocument, String spaceName,
            Map<String, String> parameters) throws SpaceException {
        try {
            if (isSocialWorkspace(contextDocument)) {
                SocialWorkspace socialWorkspace = toSocialWorkspace(contextDocument);
                DocumentModel doc = session.getDocument(new PathRef(socialWorkspace.getPublicDashboardSpacePath()));
                return doc.getAdapter(Space.class);
            } else {
                // assume dashboard spaces root
                DocumentModel doc = session.getDocument(new PathRef(contextDocument.getPathAsString() + "/"
                        + PUBLIC_DASHBOARD_SPACE_NAME));
                return doc.getAdapter(Space.class);
            }
        } catch (ClientException e) {
            throw new SpaceException(e);
        }
    }

    @Override
    public boolean isReadOnly(CoreSession session) {
        return false;
    }

}
