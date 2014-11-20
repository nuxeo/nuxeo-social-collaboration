package org.nuxeo.ecm.social.workspace.userregistration;

import static org.nuxeo.ecm.social.workspace.adapters.SocialWorkspaceAdapter.MEMBER_NOTIFICATION_DISABLED;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;
import static org.nuxeo.ecm.user.registration.DocumentRegistrationInfo.DOCUMENT_ID_FIELD;
import static org.nuxeo.ecm.user.registration.DocumentRegistrationInfo.DOCUMENT_RIGHT_FIELD;

import java.security.Principal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.ecm.user.invite.UserRegistrationConfiguration;
import org.nuxeo.ecm.user.registration.DefaultRegistrationUserFactory;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class SocialRegistrationUserFactory extends
    DefaultRegistrationUserFactory {
    public static final String NOT_NOTIFY_MEMBER_FIELD = "socialer:doNotNotifyMembers";

    public static final String ADMINISTRATOR_RIGHT = "administrator";
    public static final String MEMBER_RIGHT = "member";

    private static final Log log = LogFactory.getLog(SocialRegistrationUserFactory.class);

    @Override
    public DocumentModel doAddDocumentPermission(CoreSession session,
            DocumentModel registrationDoc,UserRegistrationConfiguration configuration) throws ClientException {
        String docId = (String) registrationDoc.getPropertyValue(DOCUMENT_ID_FIELD);
        if (StringUtils.isBlank(docId)) {
            throw new ClientException("SocialWorkspace id is missing");
        }
        SocialWorkspace sw = toSocialWorkspace(session.getDocument(new IdRef(
                docId)));
        if (sw == null) {
            throw new ClientException(
                    "Document passed is not a Social Workspace");
        }
        String login = (String) registrationDoc.getPropertyValue(configuration.getUserInfoUsernameField());
        Principal principal = getUserManager().getPrincipal(login);

        // Set if new member notification is needed
        sw.getDocument().putContextData(MEMBER_NOTIFICATION_DISABLED,
                        registrationDoc.getPropertyValue(NOT_NOTIFY_MEMBER_FIELD));

        Boolean isAdded;
        if (ADMINISTRATOR_RIGHT.equals(registrationDoc.getPropertyValue(DOCUMENT_RIGHT_FIELD))) {
            isAdded = getSocialWorkspaceService().addSocialWorkspaceAdministrator(sw, principal);
        } else {
            isAdded = getSocialWorkspaceService().addSocialWorkspaceMember(sw, principal);
        }

        try {
            return isAdded ? sw.getDocument() : null;
        } finally {
            // reset notification flag
            sw.getDocument().putContextData(MEMBER_NOTIFICATION_DISABLED, false);
        }
    }

    @Override
    public void doPostAddDocumentPermission(CoreSession session,
            DocumentModel registrationDoc, DocumentModel document)
            throws ClientException {
        // Nothing to do
    }

    public SocialWorkspaceService getSocialWorkspaceService() {
        return Framework.getLocalService(SocialWorkspaceService.class);
    }

    public UserManager getUserManager() {
        return Framework.getLocalService(UserManager.class);
    }
}
