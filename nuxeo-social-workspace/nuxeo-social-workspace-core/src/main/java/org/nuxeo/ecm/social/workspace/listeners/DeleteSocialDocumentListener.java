package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.core.api.LifeCycleConstants.DELETE_TRANSITION;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.TRANSTION_EVENT_OPTION_TRANSITION;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocumentAdapter;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;

public class DeleteSocialDocumentListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentModel document = ((DocumentEventContext) ctx).getSourceDocument();
        if (!SocialWorkspaceHelper.isSocialDocument(document)) {
            return;
        }

        if (!ctx.hasProperty(TRANSTION_EVENT_OPTION_TRANSITION)) {
            return;
        }

        if (!DELETE_TRANSITION.equals(ctx.getProperty(TRANSTION_EVENT_OPTION_TRANSITION))) {
            return;
        }

        CoreSession session = ctx.getCoreSession();

        SocialDocumentAdapter socialDocument = document.getAdapter(SocialDocumentAdapter.class);
        cleanProxy(session, socialDocument);
    }

    /**
     * Remove social document publication into social workspace if needed.
     */
    public void cleanProxy(CoreSession session,
            SocialDocumentAdapter socialDocument) throws ClientException {
        DocumentModel publicProxy = socialDocument.getDocumentPublic();
        if (publicProxy != null) {
            session.removeDocument(publicProxy.getRef());
        } else {
            if (!ARTICLE_TYPE.equals(socialDocument.getType())) {
                // if not public then automatically private !!
                session.removeDocument(socialDocument.getDocumentRestrictedToMembers().getRef());
            }
        }
    }

}
