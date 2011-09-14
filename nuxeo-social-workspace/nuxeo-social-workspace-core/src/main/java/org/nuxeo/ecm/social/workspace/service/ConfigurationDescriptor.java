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

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("configuration")
public class ConfigurationDescriptor implements Serializable {
    private static final long serialVersionUID = 1L;

    @XNode("validationTimeInDays")
    private int validationTimeInDays = -1;

    @XNode("socialWorkspaceContainerPath")
    private String socialWorkspaceContainerPath;

    public int getValidationTimeInDays() {
        return validationTimeInDays;
    }

    public String getSocialWorkspaceContainerPath() {
        return socialWorkspaceContainerPath;
    }
}
