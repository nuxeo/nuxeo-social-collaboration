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

package org.nuxeo.ecm.social.mini.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class TestMiniMessageService extends AbstractMiniMessageTest {

    @Test
    public void serviceRegistration() throws IOException {
        assertNotNull(miniMessageService);
        assertNotNull(userRelationshipService);
    }

    @Test
    public void shouldStoreAMiniMessageActivity() {
        Activity activity = new ActivityImpl();
        activity.setActor("bender");
        activity.setVerb(MiniMessageActivityStreamFilter.VERB);
        activity.setObject("My first message");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertEquals(1, activities.size());
        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());
    }

    @Test
    public void shouldRetrieveUserMiniMessages() throws ClientException {
        initializeSomeMiniMessagesAndRelations();
        String benderActivityObject = ActivityHelper.createUserActivityObject("Bender");

        List<MiniMessage> messages = miniMessageService.getMiniMessageFrom(
                "Bender", 0, 0);
        assertNotNull(messages);
        assertEquals(5, messages.size());
        MiniMessage miniMessage = messages.get(0);
        assertEquals(
                "I don't tell you how to tell me what to do, so don't tell me how to do what you tell me to do!",
                miniMessage.getMessage());
        assertEquals("Bender", miniMessage.getActor());
        assertNotNull(miniMessage.getPublishedDate());
        miniMessage = messages.get(1);
        assertEquals("Oh wait, your serious. Let me laugh even harder.",
                miniMessage.getMessage());
        assertEquals("Bender", miniMessage.getActor());
        assertNotNull(miniMessage.getPublishedDate());
        miniMessage = messages.get(2);
        assertEquals("Lies, lies and slander!", miniMessage.getMessage());
        assertEquals("Bender", miniMessage.getActor());
        assertNotNull(miniMessage.getPublishedDate());
        miniMessage = messages.get(3);
        assertEquals(
                "This is the worst kind of discrimination: the kind against me!",
                miniMessage.getMessage());
        assertEquals("Bender", miniMessage.getActor());
        assertNotNull(miniMessage.getPublishedDate());
        miniMessage = messages.get(4);
        assertEquals("Of all the friends I've had... you're the first.",
                miniMessage.getMessage());
        assertEquals("Bender", miniMessage.getActor());
        assertNotNull(miniMessage.getPublishedDate());
    }

    @Test
    public void shouldRetrieveMiniMessagesForUser() throws ClientException {
        initializeSomeMiniMessagesAndRelations();

        List<MiniMessage> messages = miniMessageService.getMiniMessageFor(
                "Leela", RelationshipKind.fromGroup("circle"), 0, 0);
        assertNotNull(messages);
        assertEquals(10, messages.size());
    }

}
