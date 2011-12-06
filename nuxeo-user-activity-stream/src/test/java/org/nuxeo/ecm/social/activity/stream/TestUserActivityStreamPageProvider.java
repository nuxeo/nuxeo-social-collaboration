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

package org.nuxeo.ecm.social.activity.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.ACTOR_PROPERTY;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.CORE_SESSION_PROPERTY;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.FOR_ACTOR_STREAM_TYPE;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.LOCALE_PROPERTY;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamPageProvider.STREAM_TYPE_PROPERTY;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.runtime.test.runner.Deploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Deploy("org.nuxeo.ecm.platform.query.api:OSGI-INF/pageprovider-framework.xml")
public class TestUserActivityStreamPageProvider extends
        AbstractUserActivityTest {

    public static final String PROVIDER_NAME = "user_activity_stream";

    @Inject
    protected PageProviderService pageProviderService;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFilterDocumentsRelatedActivities() throws ClientException {
        initializeSomeRelations();
        initializeDummyDocumentRelatedActivities();

        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put(ACTOR_PROPERTY, "Leela");
        properties.put(STREAM_TYPE_PROPERTY, FOR_ACTOR_STREAM_TYPE);
        properties.put(LOCALE_PROPERTY, new Locale("en"));
        properties.put(CORE_SESSION_PROPERTY, (Serializable) session);
        PageProvider<ActivityMessage> userActivityStreamPageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, null, null, properties);
        assertNotNull(userActivityStreamPageProvider);
        List<ActivityMessage> activityMessages = userActivityStreamPageProvider.getCurrentPage();
        assertNotNull(activityMessages);
        assertEquals(2, activityMessages.size());
    }

    @Test
    public void shouldFilterActivitiesBasedOnACLs() throws ClientException {
        initializeSomeRelations();
        createDocumentsWithBender();

        CoreSession newSession = openSessionAs("Leela");
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put(ACTOR_PROPERTY, "Leela");
        properties.put(STREAM_TYPE_PROPERTY, FOR_ACTOR_STREAM_TYPE);
        properties.put(LOCALE_PROPERTY, new Locale("en"));
        properties.put(CORE_SESSION_PROPERTY, (Serializable) newSession);
        PageProvider<ActivityMessage> userActivityStreamPageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, null, null, properties);
        assertNotNull(userActivityStreamPageProvider);
        List<ActivityMessage> activityMessages = userActivityStreamPageProvider.getCurrentPage();
        assertNotNull(activityMessages);
        assertEquals(4, activityMessages.size());

        CoreInstance.getInstance().close(newSession);
    }

}
