/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *      Nuxeo
 */

package org.nuxeo.ecm.social.workspace.service;

import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;

/**
 * Service interface for Social Workspace.
 */
public interface SocialWorkspaceService {

    /**
     * Gets the number of days before a social workspace expires without
     * validation.
     *
     * @return number of days
     */
    int getValidationDays();

    void initializeSocialWorkspace(SocialWorkspace socialWorkspace,
            String principalName);

    void makeSocialWorkspacePublic(SocialWorkspace socialWorkspace);

    void makeSocialWorkspacePrivate(SocialWorkspace socialWorkspace);

}
