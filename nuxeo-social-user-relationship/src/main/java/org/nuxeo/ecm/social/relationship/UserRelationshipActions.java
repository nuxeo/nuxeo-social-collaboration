package org.nuxeo.ecm.social.relationship;

import static org.nuxeo.ecm.webapp.security.UserManagementActions.USER_SELECTED_CHANGED;

import java.io.Serializable;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.user.relationship.service.UserRelationshipService;
import org.nuxeo.ecm.webapp.base.InputController;
import org.nuxeo.ecm.webapp.security.UserManagementActions;

/**
 * Social Friendships action bean
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@Name("userRelationshipAction")
@Scope(value = ScopeType.CONVERSATION)
public class UserRelationshipActions extends InputController implements
        Serializable {

    private static final Log log = LogFactory.getLog(UserRelationshipActions.class);

    @In(create = true)
    protected transient UserRelationshipService userRelationshipService;

    @In(create = true)
    protected transient UserManager userManager;

    @In
    protected transient UserManagementActions userManagementActions;

    @In
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NuxeoPrincipal currentUser;

    @RequestParameter
    protected String selectedKind;

    public static final String USER_RELATIONSHIP_CHANGED = "UserRelationshipChanged";

    protected List<String> relationshipsWithSelectedUser = null;

    public boolean isAlreadyConnected() throws ClientException {
        return !isYou() && getRelationshipsWithSelectedUser().size() > 0;
    }

    public boolean isYou() {
        return getCurrentUser().equals(getFriend());
    }

    public List<String> getRelationshipsWithSelectedUser() throws ClientException {
        if (relationshipsWithSelectedUser == null) {
            relationshipsWithSelectedUser = userRelationshipService.getRelationshipKinds(
                    getCurrentUser(), getFriend());
        }
        return relationshipsWithSelectedUser;
    }

    protected void addRelationshipWithSelectedUser(String type)
            throws ClientException {
        userRelationshipService.addRelation(getCurrentUser(), getFriend(), type);
        setFacesMessage("label.social.user.relationship.addRelation.success");
        Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
    }

    protected void removeRelationship(String type) throws ClientException {
        userRelationshipService.removeRelation(getCurrentUser(), getFriend(),
                type);
        Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
    }

    public boolean isActiveRelationship(String type) throws ClientException {
        return getRelationshipsWithSelectedUser().contains(type);
    }

    public List<String> getRelationshipsFromSelectedUser()
            throws ClientException {
        return userRelationshipService.getTargets(getFriend());
    }

    public void relationshipCheckboxChanged(ValueChangeEvent event) {
        if (!StringUtils.isEmpty(selectedKind)) {
            try {
                if ((Boolean) event.getNewValue()) {
                    addRelationshipWithSelectedUser(selectedKind);
                } else {
                    removeRelationship(selectedKind);
                }
            } catch (ClientException e) {
                log.error(e.getMessage(), e);
                facesMessages.add(StatusMessage.Severity.ERROR,
                        resourcesAccessor.getMessages().get(
                                "label.social.user.relationship.error"));
            }
        }
    }

    @Observer( { USER_RELATIONSHIP_CHANGED, USER_SELECTED_CHANGED })
    public void resetUserRelationship() {
        relationshipsWithSelectedUser = null;
    }

    protected String getCurrentUser() {
        return currentUser.getModel().getId();
    }

    protected String getFriend() {
        return userManagementActions.getSelectedUser().getId();
    }
}
