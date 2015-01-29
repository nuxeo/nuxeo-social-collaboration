package org.nuxeo.ecm.social.workspace;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.ui.web.component.file.InputFileInfo;

import com.sun.faces.util.MessageFactory;

public class ImageBlobValidator implements Validator {

    private static final String IMAGE_MIME_TYPE_PREFIX = "image/";

    private static final String ERROR_INPUT_FILE_NOT_AN_IMAGE = "error.inputFile.notAnImage";

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Blob blob = (Blob) ((InputFileInfo) value).getBlob();
        String mimeType = null;
        if (blob != null) {
            mimeType = blob.getMimeType();
        }
        if (mimeType != null && !mimeType.startsWith(IMAGE_MIME_TYPE_PREFIX)) {
            throw new ValidatorException(MessageFactory.getMessage(ERROR_INPUT_FILE_NOT_AN_IMAGE,
                    FacesMessage.SEVERITY_ERROR));
        }
    }

}
