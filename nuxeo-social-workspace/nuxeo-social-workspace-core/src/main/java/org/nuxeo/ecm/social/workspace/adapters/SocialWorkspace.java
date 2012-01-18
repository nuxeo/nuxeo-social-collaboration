package org.nuxeo.ecm.social.workspace.adapters;

import java.security.Principal;
import java.util.List;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;

/**
 * An object that wraps a {@code DocumentModel} having the facet
 * {@code SocialWorkspace}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface SocialWorkspace {

    /**
     * Returns the id of this Social Workspace.
     */
    String getId();

    /**
     * Returns the title of this Social Workspace.
     */
    String getTitle();

    /**
     * Returns the full path of this Social Workspace.
     */
    String getPath();

    /**
     * Returns {@code true} if this Social Workspace is public, {@code false}
     * otherwise.
     */
    boolean isPublic();

    /**
     * Returns {@code true} if this Social Workspace is private, {@code false}
     * otherwise.
     */
    boolean isPrivate();

    /**
     * Make this Social Workspace public.
     */
    void makePublic();

    /**
     * Make this Social Workspace private.
     */
    void makePrivate();

    /**
     * Returns {@code true} if the subscriptions to this Social Workspace need
     * an administrator approval, {@code false} otherwise.
     */
    boolean mustApproveSubscription();

    /**
     * Returns {@code true} if the social workspace won't send a notification to
     * its members when modifications occurred.
     */
    boolean isMembersNotificationEnabled();

    /**
     * Adds a user to this Social Workspace administrators group.
     */
    boolean addAdministrator(Principal principal);

    /**
     * Adds a user to this Social Workspace members group.
     *
     * @return {@code true} if the user was successfully added to the members
     *         group, {@code false} otherwise.
     */
    boolean addMember(Principal principal);

    /**
     * Removes a user from this Social Workspace administrators group.
     *
     * @return {@code true} if the user was successfully added to the
     *         administrators group, {@code false} otherwise.
     */
    void removeAdministrator(Principal principal);

    /**
     * Removes a user from this Social Workspace members group.
     */
    void removeMember(Principal principal);

    /**
     * Returns true if the given {@code principal} is administrator of this
     * Social Workspace, {@code false} otherwise.
     */
    boolean isAdministrator(NuxeoPrincipal principal);

    /**
     * Returns true if the given {@code principal} is member of this Social
     * Workspace, {@code false} otherwise.
     */
    boolean isMember(NuxeoPrincipal principal);

    /**
     * Returns true if the given {@code principal} is administrator or member of
     * this Social Workspace, {@code false} otherwise.
     */
    boolean isAdministratorOrMember(NuxeoPrincipal principal);

    /**
     * Search through members of this social workspace that match with the
     * passed pattern.
     */
    List<String> searchMembers(String pattern);

    /**
     * Search through administrators of this social workspace that match with
     * the passed pattern.
     */
    List<String> searchAdministrators(String pattern);

    /**
     * Search through members and administrators of this social workspace that
     * match with the passed pattern.
     */
    List<String> searchUsers(String pattern);

    /**
     * Gets all members of this social workspace.
     */
    List<String> getMembers();

    /**
     * Gets all administrators of this social workspace.
     */
    List<String> getAdministrators();

    /**
     * Gets all members and administrators of this social workspace
     */
    List<String> getUsers();

    /**
     * Returns this Social Workspace administrators group name.
     */
    String getAdministratorsGroupName();

    /**
     * Returns this Social Workspace administrators group label.
     */
    String getAdministratorsGroupLabel();

    /**
     * Returns this Social Workspace members group name.
     */
    String getMembersGroupName();

    /**
     * Returns this Social Workspace members group label.
     */
    String getMembersGroupLabel();

    /**
     * Returns the path of the public Section of this Social Workspace.
     */
    String getPublicSectionPath();

    /**
     * Returns the path of the private Section of this Social Workspace.
     */
    String getPrivateSectionPath();

    /**
     * Returns the path of the News Items root of this Social Workspace.
     */
    String getNewsItemsRootPath();

    /**
     * Returns the path of the Dashboard spaces root of this Social Workspace.
     */
    String getDashboardSpacesRootPath();

    /**
     * Returns the path of the public Dashboard Space of this Social Workspace.
     */
    String getPublicDashboardSpacePath();

    /**
     * Returns the path of the private Dashboard Space of this Social Workspace.
     */
    String getPrivateDashboardSpacePath();

    /**
     * Handles a Subscription Request for the given {@code principal}.
     */
    void handleSubscriptionRequest(Principal principal);

    /**
     * Returns {@code true} if there is a pending Subscription Request for the
     * given {@code principal}, {@code false} otherwise.
     */
    boolean isSubscriptionRequestPending(Principal principal);

    /**
     * Returns status of subscription request for the given {@code principal}
     * Values returned are: pending, accepted, rejected or null if there is no
     * subscription request
     *
     */
    String getSubscriptionRequestStatus(Principal principal);

    /**
     * Accepts this {@code subscriptionRequest}.
     */
    void acceptSubscriptionRequest(SubscriptionRequest subscriptionRequest);

    /**
     * Rejects this {@code subscriptionRequest}.
     */
    void rejectSubscriptionRequest(SubscriptionRequest subscriptionRequest);

    /**
     * Returns the underlying {@DocumentModel}.
     */
    DocumentModel getDocument();

    /**
     * Sets the underlying {@DocumentModel}.
     * <p>
     * Must be the same document (same id), otherwise throw a RuntimeException.
     */
    void setDocument(DocumentModel doc);

}
