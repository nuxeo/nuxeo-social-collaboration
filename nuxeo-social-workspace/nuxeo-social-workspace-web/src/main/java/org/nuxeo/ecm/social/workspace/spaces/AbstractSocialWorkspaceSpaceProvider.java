package org.nuxeo.ecm.social.workspace.spaces;

import static org.nuxeo.ecm.spaces.api.Constants.SPACE_DOCUMENT_TYPE;

import java.util.Map;import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.spaces.api.AbstractSpaceProvider;
import org.nuxeo.ecm.spaces.api.Space;
import org.nuxeo.ecm.spaces.api.exceptions.SpaceException;import org.nuxeo.opensocial.container.shared.layout.api.LayoutHelper;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public abstract class AbstractSocialWorkspaceSpaceProvider extends
        AbstractSpaceProvider {

    public static final String DASHBOARD_SPACES_ROOT_NAME = "dashboardSpacesRoot";

    public static final String DASHBOARD_SPACES_ROOT_TYPE = "HiddenFolder";

    @Override
    public boolean isReadOnly(CoreSession session) {
        return true;
    }

    @Override
    protected Space doGetSpace(CoreSession session,
            DocumentModel contextDocument, String spaceName,
            Map<String, String> parameters) throws SpaceException {
        if (spaceName == null || spaceName.isEmpty()) {
            spaceName = getSpaceName();
        }
        try {
            return getOrCreateSpace(session, contextDocument, spaceName);
        } catch (ClientException e) {
            throw new SpaceException(e);
        }
    }

    protected abstract String getSpaceName();

    protected Space getOrCreateSpace(CoreSession session,
            DocumentModel contextDocument, String spaceName)
            throws ClientException {
        DocumentModel dashboardSpacesRoot = getOrCreateDashboardSpacesRoot(
                session, contextDocument);

        DocumentRef spaceRef = new PathRef(
                dashboardSpacesRoot.getPathAsString(), spaceName);
        if (session.exists(spaceRef)) {
            return session.getDocument(spaceRef).getAdapter(Space.class);
        } else {
            DocumentModel spaceDoc = session.createDocumentModel(
                    dashboardSpacesRoot.getPathAsString(), spaceName,
                    SPACE_DOCUMENT_TYPE);
            spaceDoc.setPropertyValue("dc:title", spaceName);
            spaceDoc = session.createDocument(spaceDoc);
            session.save();

            Space space = spaceDoc.getAdapter(Space.class);
            space.initLayout(LayoutHelper.buildLayout(LayoutHelper.Preset.X_2_66_33));
            return space;
        }
    }

    protected DocumentModel getOrCreateDashboardSpacesRoot(CoreSession session,
            DocumentModel contextDocument) throws ClientException {
        DocumentRef dashboardSpacesRootRef = new PathRef(
                contextDocument.getPathAsString(), DASHBOARD_SPACES_ROOT_NAME);
        if (session.exists(dashboardSpacesRootRef)) {
            return session.getDocument(dashboardSpacesRootRef);
        } else {
            DocumentModel dashboardSpacesRoot = session.createDocumentModel(
                    contextDocument.getPathAsString(),
                    DASHBOARD_SPACES_ROOT_NAME, DASHBOARD_SPACES_ROOT_TYPE);
            dashboardSpacesRoot.setPropertyValue("dc:title",
                    "Dashboard Spaces Root");
            return session.createDocument(dashboardSpacesRoot);
        }
    }

}
