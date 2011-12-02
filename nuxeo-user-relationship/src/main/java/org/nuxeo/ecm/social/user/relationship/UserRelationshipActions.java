package org.nuxeo.ecm.social.user.relationship;

import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.CIRCLE_RELATIONSHIP_KIND_GROUP;
import static org.nuxeo.ecm.webapp.security.UserManagementActions.USER_SELECTED_CHANGED;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
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
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ui.web.tag.fn.Functions;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.ecm.webapp.security.UserManagementActions;
import org.nuxeo.runtime.api.Framework;

/**
 * Social User Relationship action bean.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
 */
@Name("userRelationshipActions")
@Scope(ScopeType.PAGE)
@Install(precedence = FRAMEWORK)
public class UserRelationshipActions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(UserRelationshipActions.class);

    public static final String USER_RELATIONSHIP_CHANGED = "UserRelationshipChanged";

    private static final String PUBLICPROFILE_FIELD = "socialprofile:publicprofile";

    @In(create = true)
    protected transient RelationshipService relationshipService;

    @In
    protected transient UserManagementActions userManagementActions;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected transient NuxeoPrincipal currentUser;

    @In(create = true)
    protected transient UserManager userManager;

    @RequestParameter
    protected String selectedKind;

    @RequestParameter
    protected String selectedUser;

    protected transient ActivityStreamService activityStreamService;

    protected Map<String, List<RelationshipKind>> relationshipsWithUser;

    protected Map<RelationshipKind, Boolean> allRelationshipsState;

    public boolean isAlreadyConnected() {
        return !isCurrentUser()
                && !getRelationshipsWithSelectedUser().isEmpty();
    }

    public boolean isAlreadyConnected(String userName) {
        return !isCurrentUser(userName)
                && !getRelationshipsWithUser(userName).isEmpty();
    }

    public boolean isCurrentUser() {
        return isCurrentUser(getSelectedUser());
    }

    public boolean isCurrentUser(String userName) {
        return userName == null || getCurrentUser().equals(userName);
    }

    public List<RelationshipKind> getRelationshipsWithUser(String username) {
        if (relationshipsWithUser == null) {
            relationshipsWithUser = new HashMap<String, List<RelationshipKind>>();
        }
        if (!relationshipsWithUser.containsKey(username)) {
            List<RelationshipKind> relations = relationshipService.getRelationshipKinds(
                    ActivityHelper.createUserActivityObject(getCurrentUser()),
                    ActivityHelper.createUserActivityObject(username));
            relationshipsWithUser.put(username, relations);
        }
        return relationshipsWithUser.get(username);
    }

    public List<RelationshipKind> getRelationshipsWithSelectedUser() {
        return getRelationshipsWithUser(getSelectedUser());
    }

    protected void addRelationshipWithSelectedUser(String userName, String kind) {
        String currentUser = ActivityHelper.createUserActivityObject(getCurrentUser());
        String selectedUser = ActivityHelper.createUserActivityObject(userName);
        RelationshipKind relationshipKind = RelationshipKind.fromString(kind);
        if (relationshipService.addRelation(currentUser, selectedUser,
                relationshipKind)) {
            setFacesMessage("label.social.user.relationship.addRelation.success");
            addNewRelationActivity(currentUser, selectedUser, relationshipKind);
            Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
        }
    }

    protected void addNewRelationActivity(String actorActivityObject,
            String targetActivityObject, RelationshipKind relationshipKind) {
        Activity activity = new ActivityBuilder().actor(actorActivityObject).displayActor(
                Functions.userFullName(ActivityHelper.getUsername(actorActivityObject))).verb(
                relationshipKind.getGroup()).object(targetActivityObject).displayObject(
                Functions.userFullName(ActivityHelper.getUsername(targetActivityObject))).build();
        Framework.getLocalService(ActivityStreamService.class).addActivity(
                activity);
    }

    protected void removeRelationship(String userName, String kind) {
        if (relationshipService.removeRelation(
                ActivityHelper.createUserActivityObject(getCurrentUser()),
                ActivityHelper.createUserActivityObject(userName),
                RelationshipKind.fromString(kind))) {
            setFacesMessage("label.social.user.relationship.removeRelation.success");
            Events.instance().raiseEvent(USER_RELATIONSHIP_CHANGED);
        }
    }

    public boolean isActiveRelationship(RelationshipKind relationshipKind) {
        return getRelationshipsWithSelectedUser().contains(relationshipKind);
    }

    public Map<RelationshipKind, Boolean> getAllRelationshipsState()
            throws ClientException {
        if (allRelationshipsState == null) {
            allRelationshipsState = new HashMap<RelationshipKind, Boolean>();
            for (RelationshipKind kind : relationshipService.getRegisteredKinds(null)) {
                allRelationshipsState.put(kind, isActiveRelationship(kind));
            }
        }
        return allRelationshipsState;
    }

    public List<RelationshipKind> getKinds() {
        return relationshipService.getRegisteredKinds(CIRCLE_RELATIONSHIP_KIND_GROUP);
    }

    public List<String> getRelationshipsFromSelectedUser() {
        List<String> targets = relationshipService.getTargetsOfKind(
                ActivityHelper.createUserActivityObject(getSelectedUser()),
                RelationshipKind.fromGroup(CIRCLE_RELATIONSHIP_KIND_GROUP));

        return ActivityHelper.getUsernames(targets);
    }

    public void relationshipCheckboxChanged(ValueChangeEvent event) {
        if (!StringUtils.isBlank(selectedKind)) {
            if ((Boolean) event.getNewValue()) {
                addRelationshipWithSelectedUser(selectedUser, selectedKind);
            } else {
                removeRelationship(selectedUser, selectedKind);
            }
        }
    }

    @Observer(USER_RELATIONSHIP_CHANGED)
    public void resetUserRelationship() {
        // XXX Should be more intelligent to remove only impacted relations
        relationshipsWithUser = null;
    }

    @Observer({ USER_RELATIONSHIP_CHANGED, USER_SELECTED_CHANGED })
    public void resetUserRelationshipStates() {
        allRelationshipsState = null;
    }

    protected String getCurrentUser() {
        return currentUser.getModel().getId();
    }

    protected String getSelectedUser() {
        DocumentModel selectedUser = userManagementActions.getSelectedUser();
        if (selectedUser == null) {
            return null;
        }
        return selectedUser.getId();
    }

    protected void setFacesMessage(String msg) {
        facesMessages.add(StatusMessage.Severity.INFO,
                resourcesAccessor.getMessages().get(msg));
    }

    public boolean canViewProfile(DocumentModel userProfile) {
        try {
            return currentUser.isAdministrator() || isCurrentUser()
                    || isPublicProfile(userProfile)
                    || isInCirclesOf(userProfile);
        } catch (ClientException e) {
            log.error("Failed to test profile visibility", e);
        }
        return false;
    }

    protected boolean isPublicProfile(DocumentModel userProfile)
            throws ClientException {
        Boolean publicProfile = (Boolean) userProfile.getPropertyValue(PUBLICPROFILE_FIELD);
        return publicProfile.booleanValue();
    }

    protected boolean isInCirclesOf(DocumentModel userProfile)
            throws ClientException {
        String currentUsrActObj = ActivityHelper.createUserActivityObject(currentUser);
        String selectedUsrActObj = ActivityHelper.createUserActivityObject((String) userProfile.getProperty(
                userManager.getUserSchemaName(), userManager.getUserIdField()));
        for (RelationshipKind kind : getKinds()) {
            List<String> targetsOfKind = relationshipService.getTargetsOfKind(
                    selectedUsrActObj, kind);
            if (targetsOfKind.contains(currentUsrActObj)) {
                return true;
            }
        }
        return false;
    }

}
