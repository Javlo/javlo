/*
 * Created on 17-oct.-2004
 */
package org.javlo.helper;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;


/**
 * @author pvandermaesen
 * help to translate array and other object structure
 */
public class I18NHelper {
    
    
    /**
     * translate a array to a double array
     * @param keys a list of keys
     * @param prefix the prefix of the translation key ( sp. : content.web )
     * @param request the current request for ressource access
     * @return a dbl tab with key in first index and translation in the second index.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static final String[][] translateArray( GlobalContext globalContext, HttpSession session, String[] keys, String prefix ) throws FileNotFoundException, IOException {
        I18nAccess i18n = I18nAccess.getInstance( globalContext, session );
        String[][] res = new String[keys.length][];
        for (int i = 0; i < res.length; i++) {
            res[i] = new String[2];
            res[i][0] = keys[i];
            res[i][1] = i18n.getText(prefix+keys[i]);
        }
        return res;
    }
    
    /**
     * translate a array to a double array
     * @param globalContext the context of current website
     * @param session user session
     * @param keys a list of keys
     * @param prefix the prefix of the translation key ( sp. : content.web ) 
     * @return a dbl tab with key in first index and translation in the second index.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static final String[][] translateArrayInView(  GlobalContext globalContext, HttpSession session, String[] keys, String prefix ) throws FileNotFoundException, IOException {
        I18nAccess i18n = I18nAccess.getInstance( globalContext, session );
        String[][] res = new String[keys.length][];
        for (int i = 0; i < res.length; i++) {
            res[i] = new String[2];
            res[i][0] = keys[i];
            res[i][1] = i18n.getContentViewText(prefix+keys[i]);
        }
        return res;
    }
    
}
