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

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.user.relationships", "org.nuxeo.ecm.social.mini.message" })
@LocalDeploy("org.nuxeo.ecm.social.mini.message:mini-message-test.xml")
public abstract class AbstractMiniMessageTest {

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected UserRelationshipService userRelationshipService;

    @Inject
    protected MiniMessageService miniMessageService;

    @Inject
    protected EventService eventService;

    @Inject
    protected CoreSession session;

    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                    }
                });
    }

    protected void initializeSomeMiniMessagesAndRelations()
            throws ClientException {
        DateTime now = new DateTime();
        miniMessageService.addMiniMessage("Bender",
                "Of all the friends I've had... you're the first.",
                now.toDate());
        miniMessageService.addMiniMessage(
                "Bender",
                "This is the worst kind of discrimination: the kind against me!",
                now.plusMinutes(1).toDate());
        miniMessageService.addMiniMessage("Bender", "Lies, lies and slander!",
                now.plusMinutes(2).toDate());
        miniMessageService.addMiniMessage("Bender",
                "Oh wait, your serious. Let me laugh even harder.",
                now.plusMinutes(3).toDate());
        miniMessageService.addMiniMessage(
                "Bender",
                "I don't tell you how to tell me what to do, so don't tell me how to do what you tell me to do!",
                now.plusMinutes(4).toDate());
        miniMessageService.addMiniMessage("Leela",
                "At the risk of sounding negative, no.",
                now.plusMinutes(5).toDate());
        miniMessageService.addMiniMessage("Fry",
                "When I'm with you, every day feels like double soup Tuesday.",
                now.plusMinutes(10).toDate());
        miniMessageService.addMiniMessage(
                "Fry",
                "We've lost power of the forward Gameboy! Mario not responding!",
                now.plusMinutes(15).toDate());
        miniMessageService.addMiniMessage("Zapp Brannigan",
                "Kif, I have made it with a woman. Inform the men.",
                now.plusMinutes(20).toDate());
        miniMessageService.addMiniMessage("Zapp Brannigan",
                "I surrender, and volunteer for treason!",
                now.plusMinutes(21).toDate());

        RelationshipKind friends = RelationshipKind.newInstance("circle",
                "friends");
        RelationshipKind coworkers = RelationshipKind.newInstance("circle",
                "coworkers");
        userRelationshipService.addRelation("Leela", "Bender", friends);
        userRelationshipService.addRelation("Leela", "Fry", friends);
        userRelationshipService.addRelation("Leela", "Zapp Brannigan",
                coworkers);
    }

    protected void changeUser(String username) {
        featuresRunner.getFeature(CoreFeature.class).getRepository().switchUser(
                username);
    }

}
