package org.nuxeo.ecm.social.workspace.adapters;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;

/**
 * An object that wraps a {@code DocumentModel} having the facet
 * {@code SocialWorkspace}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public interface SocialWorkspace {

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
    String getNewsRootPath();

}
