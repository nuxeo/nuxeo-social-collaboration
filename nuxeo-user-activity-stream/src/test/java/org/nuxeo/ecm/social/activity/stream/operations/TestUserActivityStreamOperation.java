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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.social.activity.stream.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.social.activity.stream.AbstractUserActivityTest;
import org.nuxeo.runtime.test.runner.Deploy;
import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.platform.query.api:OSGI-INF/pageprovider-framework.xml" })
public class TestUserActivityStreamOperation extends AbstractUserActivityTest {

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldFilterActivitiesBasedOnACLs() throws Exception {
        initializeSomeRelations();
        createDocumentsWithBender();
        try (CoreSession newSession = openSessionAs("Leela")) {
            OperationContext ctx = new OperationContext(newSession);
            assertNotNull(ctx);

            OperationChain chain = new OperationChain("testUserActivityStreamOperation");
            chain.add(GetActivityStream.ID);
            Blob result = (Blob) automationService.run(ctx, chain);
            assertNotNull(result);
            String json = result.getString();
            assertNotNull(json);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> m = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            List<Map<String, Object>> activities = (List<Map<String, Object>>) m.get("activities");
            assertEquals(4, activities.size());
        }
    }
}
