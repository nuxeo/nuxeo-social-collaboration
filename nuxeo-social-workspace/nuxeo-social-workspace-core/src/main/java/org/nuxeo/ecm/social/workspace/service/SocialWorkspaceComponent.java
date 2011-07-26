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
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see SocialWorkspaceService} service.
 */
public class SocialWorkspaceComponent extends DefaultComponent implements
        SocialWorkspaceService {
    public static final String CONFIGURATION_EP = "configuration";

    private int validationDays = 15;

    @Override
    public int getValidationDays() {
        return validationDays;
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (CONFIGURATION_EP.equals(extensionPoint)) {
            ConfigurationDescriptor config = (ConfigurationDescriptor) contribution;
            if (config.getValidationTimeInDays() > 0) {
                validationDays = config.getValidationTimeInDays();
            }
        }
    }

    @Override
    public void makeSocialWorkspacePublic(SocialWorkspace socialWorkspace) {
    }

    @Override
    public void makeSocialWorkspacePrivate(SocialWorkspace socialWorkspace) {
    }

}
