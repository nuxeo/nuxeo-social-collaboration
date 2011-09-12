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

import java.security.Principal;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.usermanager.exceptions.UserAlreadyExistsException;
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
        "org.nuxeo.ecm.user.relationships",
        "org.nuxeo.ecm.platform.htmlsanitizer",
        "org.nuxeo.ecm.social.mini.message", "org.nuxeo.ecm.platform.url.api",
        "org.nuxeo.ecm.platform.url.core",
        "org.nuxeo.ecm.platform.ui:OSGI-INF/urlservice-framework.xml",
        "org.nuxeo.ecm.user.center:OSGI-INF/urlservice-contrib.xml" })
@LocalDeploy("org.nuxeo.ecm.social.mini.message:mini-message-test.xml")
public abstract class AbstractMiniMessageTest {

    public static RelationshipKind CIRCLE_RELATION = RelationshipKind.fromGroup("circle");

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
    protected UserManager userManager;

    @Inject
    protected CoreSession session;



    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    @Override
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                    }
                });
    }

    protected void initializeSomeMiniMessagesAndRelations()
            throws ClientException {
        DateTime now = new DateTime();
        Principal bender = createUser("Bender");
        Principal leela = createUser("Leela");
        Principal fry = createUser("Fry");
        Principal zapp = createUser("Zapp");

        miniMessageService.addMiniMessage(bender,
                "Of all the friends I've had... you're the first.",
                now.toDate());
        miniMessageService.addMiniMessage(
                bender,
                "This is the worst kind of discrimination: the kind against me!",
                now.plusMinutes(1).toDate());
        miniMessageService.addMiniMessage(bender, "Lies, lies and slander!",
                now.plusMinutes(2).toDate());
        miniMessageService.addMiniMessage(bender,
                "Oh wait, your serious. Let me laugh even harder.",
                now.plusMinutes(3).toDate());
        miniMessageService.addMiniMessage(
                bender,
                "I don't tell you how to tell me what to do, so don't tell me how to do what you tell me to do!",
                now.plusMinutes(4).toDate());
        miniMessageService.addMiniMessage(leela,
                "At the risk of sounding negative, no.",
                now.plusMinutes(5).toDate());
        miniMessageService.addMiniMessage(fry,
                "When I'm with you, every day feels like double soup Tuesday.",
                now.plusMinutes(10).toDate());
        miniMessageService.addMiniMessage(
                fry,
                "We've lost power of the forward Gameboy! Mario not responding!",
                now.plusMinutes(15).toDate());
        miniMessageService.addMiniMessage(zapp,
                "Kif, I have made it with a woman. Inform the men.",
                now.plusMinutes(20).toDate());
        miniMessageService.addMiniMessage(zapp,
                "I surrender, and volunteer for treason!",
                now.plusMinutes(21).toDate());

        RelationshipKind friends = RelationshipKind.newInstance("circle",
                "friends");
        RelationshipKind coworkers = RelationshipKind.newInstance("circle",
                "coworkers");

        String leelaActivityObject = ActivityHelper.createUserActivityObject(leela.getName());
        String benderActivityObject = ActivityHelper.createUserActivityObject(bender.getName());
        String fryActivityObject = ActivityHelper.createUserActivityObject(fry.getName());
        String zappActivityObject = ActivityHelper.createUserActivityObject(zapp.getName());
        userRelationshipService.addRelation(leelaActivityObject,
                benderActivityObject, friends);
        userRelationshipService.addRelation(leelaActivityObject,
                fryActivityObject, friends);
        userRelationshipService.addRelation(leelaActivityObject,
                zappActivityObject, coworkers);
    }

    protected void changeUser(String username) {
        featuresRunner.getFeature(CoreFeature.class).getRepository().switchUser(
                username);
    }

    protected Principal createUser(String username) throws ClientException {
        DocumentModel user = userManager.getBareUserModel();
        user.setPropertyValue("user:username", username);
        try {
            userManager.createUser(user);
        } catch (UserAlreadyExistsException e) {
            // do nothing
        } finally {
            session.save();
        }
        return userManager.getPrincipal(username);
    }

}
