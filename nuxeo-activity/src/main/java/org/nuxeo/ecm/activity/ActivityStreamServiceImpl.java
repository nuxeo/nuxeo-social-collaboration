/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import static org.nuxeo.ecm.activity.ActivityHelper.getDocumentLink;
import static org.nuxeo.ecm.activity.ActivityHelper.getUserProfileLink;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.persistence.PersistenceProviderFactory;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@link ActivityStreamService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivityStreamServiceImpl extends DefaultComponent implements
        ActivityStreamService {

    private static final Log log = LogFactory.getLog(ActivityStreamServiceImpl.class);

    public static final String ACTIVITIES_PROVIDER = "nxactivities";

    public static final String ACTIVITY_STREAM_FILTER_EP = "activityStreamFilters";

    public static final String ACTIVITY_MESSAGE_LABELS_EP = "activityMessageLabels";

    public static final String ACTIVITY_STREAMS_EP = "activityStreams";

    protected final ThreadLocal<EntityManager> localEntityManager = new ThreadLocal<EntityManager>();

    protected final Map<String, ActivityStreamFilter> activityStreamFilters = new HashMap<String, ActivityStreamFilter>();

    protected final Map<String, String> activityMessageLabels = new HashMap<String, String>();

    protected ActivityStreamRegistry activityStreamRegistry;

    protected PersistenceProvider persistenceProvider;

    @Override
    public ActivitiesList query(String filterId,
            final Map<String, Serializable> parameters) {
        return query(filterId, parameters, 0, 0);
    }

    @Override
    public ActivitiesList query(String filterId,
            final Map<String, Serializable> parameters, final long offset,
            final long limit) {
        if (ALL_ACTIVITIES.equals(filterId)) {
            return queryAll(offset, limit);
        }

        final ActivityStreamFilter filter = activityStreamFilters.get(filterId);
        if (filter == null) {
            throw new ClientRuntimeException(String.format(
                    "Unable to retrieve '%s' ActivityStreamFilter", filterId));
        }

        return query(filter, parameters, offset, limit);
    }

    protected ActivitiesList query(final ActivityStreamFilter filter,
            final Map<String, Serializable> parameters, final long offset,
            final long limit) {
        try {
            return getOrCreatePersistenceProvider().run(false,
                    new PersistenceProvider.RunCallback<ActivitiesList>() {
                        @Override
                        public ActivitiesList runWith(EntityManager em) {
                            return query(em, filter, parameters, offset, limit);
                        }
                    });
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected ActivitiesList query(EntityManager em,
            ActivityStreamFilter filter, Map<String, Serializable> parameters,
            long offset, long limit) {
        try {
            localEntityManager.set(em);
            return filter.query(this, parameters, offset, limit);
        } finally {
            localEntityManager.remove();
        }

    }

    protected ActivitiesList queryAll(final long offset, final long limit) {
        try {
            return getOrCreatePersistenceProvider().run(false,
                    new PersistenceProvider.RunCallback<ActivitiesList>() {
                        @Override
                        public ActivitiesList runWith(EntityManager em) {
                            return queryAll(em, offset, limit);
                        }
                    });
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected ActivitiesList queryAll(EntityManager em, long offset, long limit) {
        Query query = em.createQuery("from Activity activity order by activity.id asc");
        if (limit > 0) {
            query.setMaxResults((int) limit);
            if (offset > 0) {
                query.setFirstResult((int) offset);
            }
        }
        return new ActivitiesListImpl(query.getResultList());
    }

    @Override
    public Activity addActivity(final Activity activity) {
        if (activity.getPublishedDate() == null) {
            activity.setPublishedDate(new Date());
        }
        try {
            getOrCreatePersistenceProvider().run(true,
                    new PersistenceProvider.RunVoid() {
                        @Override
                        public void runWith(EntityManager em) {
                            addActivity(em, activity);
                        }
                    });
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
        return activity;
    }

    protected void addActivity(EntityManager em, Activity activity) {
        try {
            localEntityManager.set(em);
            em.persist(activity);
            for (ActivityStreamFilter filter : activityStreamFilters.values()) {
                if (filter.isInterestedIn(activity)) {
                    filter.handleNewActivity(this, activity);
                }
            }
        } finally {
            localEntityManager.remove();
        }
    }

    @Override
    public void removeActivities(final Collection<Serializable> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return;
        }

        try {
            getOrCreatePersistenceProvider().run(true,
                    new PersistenceProvider.RunVoid() {
                        @Override
                        public void runWith(EntityManager em) {
                            removeActivities(em, activityIds);
                        }
                    });
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected void removeActivities(EntityManager em,
            Collection<Serializable> activityIds) {
        try {
            localEntityManager.set(em);
            for (ActivityStreamFilter filter : activityStreamFilters.values()) {
                filter.handleRemovedActivities(this, activityIds);
            }

            Query query = em.createQuery("delete from Activity activity where activity.id in (:ids)");
            query.setParameter("ids", activityIds);
            query.executeUpdate();
        } finally {
            localEntityManager.remove();
        }
    }

    @Override
    public ActivityMessage toActivityMessage(final Activity activity,
            Locale locale) {
        Map<String, String> fields = activity.toMap();

        if (!activityMessageLabels.containsKey(activity.getVerb())) {
            return new ActivityMessage(activity, activity.toString());
        }

        String labelKey = activityMessageLabels.get(activity.getVerb());
        String messageTemplate;
        try {
            messageTemplate = I18NUtils.getMessageString("messages", labelKey,
                    null, locale);
        } catch (MissingResourceException e) {
            log.error(e.getMessage());
            log.debug(e, e);
            // just return the labelKey if we have no resource bundle
            return new ActivityMessage(activity, labelKey);
        }

        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher m = pattern.matcher(messageTemplate);
        while (m.find()) {
            String param = m.group().replaceAll("[\\|$\\|{\\}]", "");
            if (fields.containsKey(param)) {
                String value = fields.get(param);
                final String displayValue = fields.get("display"
                        + StringUtils.capitalize(param));
                if (ActivityHelper.isDocument(value)) {
                    value = getDocumentLink(value, displayValue);
                } else if (ActivityHelper.isUser(value)) {
                    value = getUserProfileLink(value, displayValue);
                }
                messageTemplate = messageTemplate.replace(m.group(), value);
            }
        }
        return new ActivityMessage(activity, messageTemplate);
    }

    @Override
    public ActivityStream getActivityStream(String name) {
        return activityStreamRegistry.get(name);
    }

    public EntityManager getEntityManager() {
        return localEntityManager.get();
    }

    public PersistenceProvider getOrCreatePersistenceProvider() {
        if (persistenceProvider == null) {
            activatePersistenceProvider();
        }
        return persistenceProvider;
    }

    protected void activatePersistenceProvider() {
        Thread thread = Thread.currentThread();
        ClassLoader last = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(PersistenceProvider.class.getClassLoader());
            PersistenceProviderFactory persistenceProviderFactory = Framework.getLocalService(PersistenceProviderFactory.class);
            persistenceProvider = persistenceProviderFactory.newProvider(ACTIVITIES_PROVIDER);
            persistenceProvider.openPersistenceUnit();
        } finally {
            thread.setContextClassLoader(last);
        }
    }

    protected void deactivatePersistenceProvider() {
        if (persistenceProvider != null) {
            persistenceProvider.closePersistenceUnit();
            persistenceProvider = null;
        }
    }

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        activityStreamRegistry = new ActivityStreamRegistry();
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        deactivatePersistenceProvider();
        super.deactivate(context);
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (ACTIVITY_STREAM_FILTER_EP.equals(extensionPoint)) {
            registerActivityStreamFilter((ActivityStreamFilterDescriptor) contribution);
        } else if (ACTIVITY_MESSAGE_LABELS_EP.equals(extensionPoint)) {
            registerActivityMessageLabel((ActivityMessageLabelDescriptor) contribution);
        } else if (ACTIVITY_STREAMS_EP.equals(extensionPoint)) {
            registerActivityStream((ActivityStream) contribution);
        }
    }

    private void registerActivityStreamFilter(
            ActivityStreamFilterDescriptor descriptor) throws ClientException {
        ActivityStreamFilter filter = descriptor.getActivityStreamFilter();

        String filterId = filter.getId();

        boolean enabled = descriptor.isEnabled();
        if (activityStreamFilters.containsKey(filterId)) {
            log.info("Overriding activity stream filter with id " + filterId);
            if (!enabled) {
                activityStreamFilters.remove(filterId);
                log.info("Disabled activity stream filter with id " + filterId);
            }
        }
        if (enabled) {
            log.info("Registering activity stream filter with id " + filterId);
            activityStreamFilters.put(filterId,
                    descriptor.getActivityStreamFilter());
        }
    }

    private void registerActivityMessageLabel(
            ActivityMessageLabelDescriptor descriptor) {
        String activityVerb = descriptor.getActivityVerb();
        if (activityMessageLabels.containsKey(activityVerb)) {
            log.info("Overriding activity message label for verb "
                    + activityVerb);
        }
        log.info("Registering activity message label for verb" + activityVerb);
        activityMessageLabels.put(activityVerb, descriptor.getLabelKey());
    }

    private void registerActivityStream(ActivityStream activityStream) {
        log.info(String.format("Registering activity stream '%s'",
                activityStream.getName()));
        activityStreamRegistry.addContribution(activityStream);
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (ACTIVITY_STREAM_FILTER_EP.equals(extensionPoint)) {
            unregisterActivityStreamFilter((ActivityStreamFilterDescriptor) contribution);
        } else if (ACTIVITY_MESSAGE_LABELS_EP.equals(extensionPoint)) {
            unregisterActivityMessageLabel((ActivityMessageLabelDescriptor) contribution);
        } else if (ACTIVITY_STREAMS_EP.equals(extensionPoint)) {
            unregisterActivityStream((ActivityStream) contribution);
        }
    }

    private void unregisterActivityStreamFilter(
            ActivityStreamFilterDescriptor descriptor) throws ClientException {
        ActivityStreamFilter filter = descriptor.getActivityStreamFilter();
        String filterId = filter.getId();
        activityStreamFilters.remove(filterId);
        log.info("Unregistering activity stream filter with id " + filterId);
    }

    private void unregisterActivityMessageLabel(
            ActivityMessageLabelDescriptor descriptor) {
        String activityVerb = descriptor.getActivityVerb();
        activityMessageLabels.remove(activityVerb);
        log.info("Unregistering activity message label for verb "
                + activityVerb);
    }

    private void unregisterActivityStream(ActivityStream activityStream) {
        activityStreamRegistry.removeContribution(activityStream);
        log.info(String.format("Unregistering activity stream '%s'",
                activityStream.getName()));
    }

}
