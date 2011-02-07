/*
 * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_PICTURE_FIELD;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.runtime.api.Framework;

public class ResizeArticlePictureListener implements EventListener {



    private static final int RESIZED_IMAGE_WIDTH = 300;

    private static final int RESIZED_IMAGE_HEIGHT = 200;

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!event.getName().equals(DocumentEventTypes.ABOUT_TO_CREATE)
                && !event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
            return;
        }

        if (!(event.getContext() instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext ctx = (DocumentEventContext) event.getContext();
        DocumentModel doc = ctx.getSourceDocument();

        if (!ARTICLE_TYPE.equals(doc.getType())) {
            return;
        }

        Blob image = (Blob) doc.getPropertyValue(ARTICLE_PICTURE_FIELD);
        if (image == null) {
            return;
        }

        ImagingService service;
        try {
            service = Framework.getService(ImagingService.class);
        } catch (Exception e) {
            throw new ClientException("Failed to get ImagingService", e);
        }

        ImageInfo info = service.getImageInfo(image);
        int width = info.getWidth();
        int height = info.getHeight();
        float wScale = (float) RESIZED_IMAGE_WIDTH / width;
        float hscale = (float) RESIZED_IMAGE_HEIGHT / height;
        float scale = Math.min(wScale, hscale);

        if (scale < 1) {
            image = service.resize(image, "jpg", (int) (width * scale),
                    (int) (height * scale), info.getDepth());
            image.setMimeType("image/jpeg"); // XXX : Should be automatic
            doc.setPropertyValue(ARTICLE_PICTURE_FIELD, (Serializable) image);
        }
    }

}
