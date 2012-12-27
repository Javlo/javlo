/*
 * Created on 12-mai-2004
 */
package org.javlo.i18n;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.message.GenericMessage;


/**
 * @author pvandermaesen
 * internationalisation of GenericMessage
 */
public class I18nMessage {
    
    I18nAccess i18nAccess = null;
    GenericMessage message;
    
    public I18nMessage ( GenericMessage newMessage, HttpServletRequest request) throws FileNotFoundException, IOException, ConfigurationException {
        i18nAccess = I18nAccess.getInstance(request);
        message = newMessage;
    }
    
    public GenericMessage getMessage() {
        return message;
    }
    
    public String getI18nMessage () {
        if ( ( message != null ) && (message.getMessage().trim().length() > 0) ) {
            return i18nAccess.getText(message.getMessage());
        } else {
            return "";
        }
    }
    
    public String toString() {
        return getI18nMessage();
    }

}
