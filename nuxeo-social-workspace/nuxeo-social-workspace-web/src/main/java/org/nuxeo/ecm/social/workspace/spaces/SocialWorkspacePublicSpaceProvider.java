package org.nuxeo.ecm.social.workspace.spaces;

import org.nuxeo.ecm.spaces.api.Space;

/**
 * Creates the default Public {@link Space} for a Social Workspace.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SocialWorkspacePublicSpaceProvider extends
        AbstractSocialWorkspaceSpaceProvider {

    public static final String PUBLIC_DASHBOARD_SPACE_NAME = "publicDashboardSpace";

    @Override
    protected String getSpaceName() {
        return PUBLIC_DASHBOARD_SPACE_NAME;
    }

}
