package org.nuxeo.ecm.social.user.relationship;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.webapp.security.UserManagementActions.USER_SELECTED_CHANGED;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.ecm.webapp.security.UserManagementActions;

/**
 * Social User Relationship action bean.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.3
 */
@Name("userRelationshipAction")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class UserRelationshipActions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(UserRelationshipActions.class);

    private static final String RELATIONSHIP_KIND_GROUP = "circle";

    @In(create = true)
    protected transient UserRelationshipService userRelationshipService;

    @In(create = true)
    protected transient UserManager userManager;

    @In
    protected transient UserManagementActions userManagementActions;

    @In
    protected transient CoreSession documentManager;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected transient NuxeoPrincipal currentUser;

    @RequestParameter
    protected String selectedKind;

    public static final String USER_RELATIONSHIP_CHANGED = "UserRelationshipChanged";

    protected List<RelationshipKind> relationshipsWithSelectedUser;

    protected Map<RelationshipKind, Boolean> allRelationshipsState;

    public boolean isAlreadyConnected() throws ClientException {
        return !isCurrentUser()
                && !getRelationshipsWithSelectedUser().isEmpty();
    }

    public boolean isCurrentUser() {
        return getCurrentUser().equals(getSelectedUser());
    }

    public List<RelationshipKind> getRelationshipsWithSelectedUser() {
        if (relationshipsWithSelectedUser == null) {
            relationshipsWithSelectedUser = userRelationshipService.getRelationshipKinds(
                    getCurrentUser(), getSelectedUser());
        }
        return relationshipsWithSelectedUser;
    }

    protected void addRelationshipWithSelectedUser(String kind) {
        if (userRelationshipService.addRelation(getCurrentUser(),
                getSelectedUser(), RelationshipKind.fromString(kind))) {
            setFacesMessage("label.social.user.relationship.addRelation.success");
            Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
        }
    }

    protected void removeRelationship(String kind) {
        if (userRelationshipService.removeRelation(getCurrentUser(),
                getSelectedUser(), RelationshipKind.fromString(kind))) {
            setFacesMessage("label.social.user.relationship.removeRelation.success");
            Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
        }
    }

    public boolean isActiveRelationship(String type) throws ClientException {
        return getRelationshipsWithSelectedUser().contains(type);
    }

    public Map<RelationshipKind, Boolean> getAllRelationshipsState()
            throws ClientException {
        if (allRelationshipsState == null) {
            allRelationshipsState = new HashMap<RelationshipKind, Boolean>();
            for (RelationshipKind kind : userRelationshipService.getRegisteredKinds(
                    null)) {
                allRelationshipsState.put(kind,
                        isActiveRelationship(kind.toString()));
            }
        }
        return allRelationshipsState;
    }

    public List<RelationshipKind> getKinds() {
        return userRelationshipService.getRegisteredKinds(RELATIONSHIP_KIND_GROUP);
    }

    public List<String> getRelationshipsFromSelectedUser() {
        return userRelationshipService.getTargets(getSelectedUser());
    }

    public void relationshipCheckboxChanged(ValueChangeEvent event) {
        if (!StringUtils.isBlank(selectedKind)) {
            if ((Boolean) event.getNewValue()) {
                addRelationshipWithSelectedUser(selectedKind);
            } else {
                removeRelationship(selectedKind);
            }
        }
    }

    @Observer( { USER_RELATIONSHIP_CHANGED, USER_SELECTED_CHANGED })
    public void resetUserRelationship() {
        relationshipsWithSelectedUser = null;
        allRelationshipsState = null;
    }

    protected String getCurrentUser() {
        return currentUser.getModel().getId();
    }

    protected String getSelectedUser() {
        return userManagementActions.getSelectedUser().getId();
    }

    protected void setFacesMessage(String msg) {
        facesMessages.add(StatusMessage.Severity.INFO,
                resourcesAccessor.getMessages().get(msg));
    }
}
