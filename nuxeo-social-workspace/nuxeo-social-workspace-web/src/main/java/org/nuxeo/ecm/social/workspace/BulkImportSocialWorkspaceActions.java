/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo
 */

package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.jboss.seam.international.StatusMessage.Severity.ERROR;
import static org.jboss.seam.international.StatusMessage.Severity.INFO;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.userregistration.SocialRegistrationUserFactory.ADMINISTRATOR_RIGHT;
import static org.nuxeo.ecm.social.workspace.userregistration.SocialRegistrationUserFactory.MEMBER_RIGHT;
import static org.nuxeo.ecm.user.registration.UserRegistrationService.ValidationMethod.EMAIL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.user.registration.UserRegistrationInfo;
import org.nuxeo.ecm.user.registration.actions.UserRegistrationActions;

/**
 * Action bean to manage bulk user import in a Social Workspace
 * 
 * @author Arnaud KERVERN <akervern@nuxeo.com>
 * @since 5.5
 */
@Name("bulkImportSocialWorkspaceActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class BulkImportSocialWorkspaceActions extends UserRegistrationActions {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(BulkImportSocialWorkspaceActions.class);

    @In(create = true)
    protected transient UserManager userManager;

    protected boolean doNotNotifyMembers = false;

    protected List<Map<String, String>> rightsMenuEntries = null;

    public boolean isDoNotNotifyMembers() {
        return doNotNotifyMembers;
    }

    public void setDoNotNotifyMembers(boolean doNotNotifyMembers) {
        this.doNotNotifyMembers = doNotNotifyMembers;
    }

    public List<Map<String, String>> getRightsMenuEntries() {
        if (rightsMenuEntries == null) {
            rightsMenuEntries = new ArrayList<Map<String, String>>();
            rightsMenuEntries.add(buildEntry("label.social.workspace.member",
                    MEMBER_RIGHT));
            rightsMenuEntries.add(buildEntry(
                    "label.social.workspace.administrator", ADMINISTRATOR_RIGHT));
        }
        return rightsMenuEntries;
    }

    protected static Map<String, String> buildEntry(String label, String value) {
        Map<String, String> entry = new HashMap<String, String>();
        entry.put("value", value);
        entry.put("label", label);
        return entry;
    }

    public void importUserFromListOfEmail() {
        throw new ClientRuntimeException("Deprecated");
    }

    public void importUserFromGroups() {
        throw new ClientRuntimeException("Deprecated");
    }

    @Override
    protected Map<String, Serializable> getAdditionalsParameters() {
        Map<String, Serializable> additionalsParameters = super.getAdditionalsParameters();
        additionalsParameters.put("doNotNotifyMembers", doNotNotifyMembers);
        return additionalsParameters;
    }

    @Override
    public void resetPojos() {
        super.resetPojos();
        doNotNotifyMembers = false;
    }

    @Override
    protected void doSubmitUserRegistration(String configurationName) {
        if (StringUtils.isBlank(configurationName)) {
            configurationName = "social_collaboration";
        }

        try {
            userinfo.setPassword(RandomStringUtils.randomAlphanumeric(6));

            SocialWorkspace sw = toSocialWorkspace(navigationContext.getCurrentDocument());
            if (isInvitationPossible(sw, userinfo)) {
                boolean autoAccept = !(StringUtils.isBlank(multipleEmails)
                        && sw.mustApproveSubscription());

                userRegistrationService.submitRegistrationRequest(
                        configurationName, userinfo, docinfo,
                        getAdditionalsParameters(), EMAIL, autoAccept);

                facesMessages.add(
                        INFO,
                        resourcesAccessor.getMessages().get(
                                "label.user.invited.success"));
            }
        } catch (ClientException e) {
            log.info("Unable to register user: " + e.getMessage());
            log.debug(e, e);
            facesMessages.add(
                    ERROR,
                    resourcesAccessor.getMessages().get(
                            "label.unable.invite.user"));
        }
    }

    protected boolean isInvitationPossible(SocialWorkspace sw,
            UserRegistrationInfo userInfo) {

        try {
            // Build userManager filters
            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            String emailKey = userManager.getUserEmailField();
            filter.put(emailKey, userInfo.getEmail());
            Set<String> pattern = new HashSet<String>();
            pattern.add(emailKey);

            DocumentModelList users = userManager.searchUsers(filter, pattern);
            if (users.size() > 0) {
                userInfo.setLogin(users.get(0).getId());
                NuxeoPrincipal nxp = userManager.getPrincipal(users.get(0).getId());
                return sw.shouldRequestSubscription(nxp);
            } else {
                return StringUtils.isBlank(sw.getSubscriptionRequestStatus(new NuxeoPrincipalImpl(
                        userInfo.getLogin())));
            }
        } catch (ClientException e) {
            log.debug(e, e);
        }
        return true;
    }
}
