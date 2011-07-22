package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.helper.SocialDocumentPublicationHandler;

public class DeleteSocialDocumentListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentModel socialDocument = ((DocumentEventContext) ctx).getSourceDocument();
        if (socialDocument == null || socialDocument.isProxy()) {
            return;
        }
        if (socialDocument.hasFacet(SOCIAL_DOCUMENT_FACET)) {

            // CoreSession session = ctx.getCoreSession();

            if (ctx.hasProperty(LifeCycleConstants.TRANSTION_EVENT_OPTION_TRANSITION)
                    && LifeCycleConstants.DELETE_TRANSITION.equals(ctx.getProperty(LifeCycleConstants.TRANSTION_EVENT_OPTION_TRANSITION))) {
                CoreSession session = ctx.getCoreSession();
                SocialDocumentPublicationHandler remover = new SocialDocumentPublicationHandler(
                        session, socialDocument);
                remover.unpublishSocialDocument();
            }
        }
    }

}
