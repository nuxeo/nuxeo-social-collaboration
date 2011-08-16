package org.nuxeo.ecm.social.user.relationship;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.CIRCLE_RELATIONSHIP_KIND_GROUP;
import static org.nuxeo.ecm.webapp.security.UserManagementActions.USER_SELECTED_CHANGED;

import java.io.Serializable;
import java.util.Date;
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
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ui.web.tag.fn.Functions;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.ecm.webapp.security.UserManagementActions;
import org.nuxeo.runtime.api.Framework;

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

    public static final String USER_RELATIONSHIP_CHANGED = "UserRelationshipChanged";

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

    protected transient ActivityStreamService activityStreamService;

    protected List<RelationshipKind> relationshipsWithSelectedUser;

    protected Map<RelationshipKind, Boolean> allRelationshipsState;

    public boolean isAlreadyConnected() {
        return !isCurrentUser()
                && !getRelationshipsWithSelectedUser().isEmpty();
    }

    public boolean isCurrentUser() {
        return getCurrentUser().equals(getSelectedUser());
    }

    public List<RelationshipKind> getRelationshipsWithSelectedUser() {
        if (relationshipsWithSelectedUser == null) {
            relationshipsWithSelectedUser = userRelationshipService.getRelationshipKinds(
                    ActivityHelper.createUserEntity(getCurrentUser()), ActivityHelper.createUserEntity(getSelectedUser()));
        }
        return relationshipsWithSelectedUser;
    }

    protected void addRelationshipWithSelectedUser(String kind) {
        String currentUser = ActivityHelper.createUserEntity(getCurrentUser());
        String selectedUser = ActivityHelper.createUserEntity(getSelectedUser());
        RelationshipKind relationshipKind = RelationshipKind.fromString(kind);
        if (userRelationshipService.addRelation(currentUser,
                selectedUser, relationshipKind)) {
            setFacesMessage("label.social.user.relationship.addRelation.success");
            addNewRelationActivity(currentUser, selectedUser, relationshipKind);
            Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
        }
    }

    protected void addNewRelationActivity(String actorId, String targetId, RelationshipKind relationshipKind) {
        Activity activity = new ActivityImpl();
        activity.setActor(actorId);
        activity.setDisplayActor(Functions.userFullName(actorId));
        activity.setObject(Functions.userFullName(targetId));
        activity.setVerb(relationshipKind.getGroup());
        activity.setPublishedDate(new Date());
        getActivityStreamService().addActivity(activity);
    }

    protected void removeRelationship(String kind) {
        if (userRelationshipService.removeRelation(ActivityHelper.createUserEntity(getCurrentUser()),
                ActivityHelper.createUserEntity(getSelectedUser()), RelationshipKind.fromString(kind))) {
            setFacesMessage("label.social.user.relationship.removeRelation.success");
            Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
        }
    }

    public boolean isActiveRelationship(String type) {
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
        return userRelationshipService.getRegisteredKinds(CIRCLE_RELATIONSHIP_KIND_GROUP);
    }

    public List<String> getRelationshipsFromSelectedUser() {
        return userRelationshipService.getTargets(ActivityHelper.createUserEntity(getSelectedUser()));
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

    protected ActivityStreamService getActivityStreamService()
            throws ClientRuntimeException {
        if (activityStreamService == null) {
            try {
                activityStreamService = Framework.getService(ActivityStreamService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to ActivityStreamService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (activityStreamService == null) {
                throw new ClientRuntimeException(
                        "ActivityStreamService service not bound");
            }
        }
        return activityStreamService;
    }
}
