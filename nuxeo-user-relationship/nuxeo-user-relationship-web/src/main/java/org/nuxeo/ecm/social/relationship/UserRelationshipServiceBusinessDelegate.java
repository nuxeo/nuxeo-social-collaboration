package org.nuxeo.ecm.social.relationship;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;

import javax.annotation.security.PermitAll;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * User Relationship service business delegate
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@Name("userRelationshipService")
@Scope(CONVERSATION)
public class UserRelationshipServiceBusinessDelegate implements Serializable {
    private static final long serialVersionUID = -1;

    private static final Log log = LogFactory.getLog(UserRelationshipServiceBusinessDelegate.class);

    protected UserRelationshipService userRelationshipService;

    @Unwrap
    public UserRelationshipService getUserRelationshipService()
            throws ClientException {
        if (null == userRelationshipService) {
            try {
                userRelationshipService = Framework.getService(UserRelationshipService.class);
            } catch (Exception e) {
                throw new ClientException(
                        "Error while trying to acquire UserRelationshipService",
                        e);
            }

            if (null == userRelationshipService) {
                throw new ClientException("UserRelationshipService not bound");
            }
        }
        return userRelationshipService;
    }

    @Destroy
    @PermitAll
    public void destroy() {
        if (null != userRelationshipService) {
            userRelationshipService = null;
        }
    }
}
