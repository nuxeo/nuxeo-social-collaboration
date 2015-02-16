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
import static org.nuxeo.ecm.social.mini.message.AbstractMiniMessagePageProvider.ACTOR_PROPERTY;
import static org.nuxeo.ecm.social.mini.message.AbstractMiniMessagePageProvider.RELATIONSHIP_KIND_PROPERTY;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.runtime.test.runner.Deploy;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Deploy("org.nuxeo.ecm.platform.query.api:OSGI-INF/pageprovider-framework.xml")
public class TestMiniMessagePageProviders extends AbstractMiniMessageTest {

    public static final String PROVIDER_NAME = "mini_messages";

    @Inject
    protected PageProviderService pageProviderService;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRetrieveMiniMessagesForUser() throws ClientException {
        initializeSomeMiniMessagesAndRelations();

        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put(ACTOR_PROPERTY, "Leela");
        properties.put(RELATIONSHIP_KIND_PROPERTY, "circle:");
        PageProvider<MiniMessage> miniMessagePageProvider = (PageProvider<MiniMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, null, null, properties);
        assertNotNull(miniMessagePageProvider);
        List<MiniMessage> miniMessages = miniMessagePageProvider.getCurrentPage();
        assertNotNull(miniMessages);
        assertEquals(10, miniMessages.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPaginateMiniMessages() throws ClientException {
        initializeSomeMiniMessagesAndRelations();

        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put(ACTOR_PROPERTY, "Leela");
        properties.put(RELATIONSHIP_KIND_PROPERTY, "circle:");
        PageProvider<MiniMessage> miniMessagePageProvider = (PageProvider<MiniMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, null, null, properties);
        assertNotNull(miniMessagePageProvider);
        miniMessagePageProvider.setPageSize(3);

        List<MiniMessage> miniMessages = miniMessagePageProvider.getCurrentPage();
        assertNotNull(miniMessages);
        assertEquals(3, miniMessages.size());
        miniMessagePageProvider.setCurrentPage(1);
        miniMessagePageProvider.refresh();
        miniMessages = miniMessagePageProvider.getCurrentPage();
        assertNotNull(miniMessages);
        assertEquals(3, miniMessages.size());
        miniMessagePageProvider.setCurrentPage(2);
        miniMessagePageProvider.refresh();
        miniMessages = miniMessagePageProvider.getCurrentPage();
        assertNotNull(miniMessages);
        assertEquals(3, miniMessages.size());
        miniMessagePageProvider.setCurrentPage(3);
        miniMessagePageProvider.refresh();
        miniMessages = miniMessagePageProvider.getCurrentPage();
        assertNotNull(miniMessages);
        assertEquals(1, miniMessages.size());
    }

}
