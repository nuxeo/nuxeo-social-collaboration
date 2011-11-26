package org.nuxeo.ecm.social.user.relationship;

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
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * User Relationship service business delegate
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
 */
@Name("relationshipService")
@Scope(CONVERSATION)
public class UserRelationshipServiceBusinessDelegate implements Serializable {
    private static final long serialVersionUID = -1;

    private static final Log log = LogFactory.getLog(UserRelationshipServiceBusinessDelegate.class);

    protected RelationshipService relationshipService;

    @Unwrap
    public RelationshipService getRelationshipService() throws ClientException {
        if (null == relationshipService) {
            try {
                relationshipService = Framework.getService(RelationshipService.class);
            } catch (Exception e) {
                throw new ClientException(
                        "Error while trying to acquire RelationshipService", e);
            }

            if (null == relationshipService) {
                throw new ClientException("RelationshipService not bound");
            }
        }
        return relationshipService;
    }

    @Destroy
    @PermitAll
    public void destroy() {
        if (null != relationshipService) {
            relationshipService = null;
        }
    }
}
