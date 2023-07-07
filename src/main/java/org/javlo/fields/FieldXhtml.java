package org.javlo.fields;

import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;

import java.util.Locale;

public class FieldXhtml extends FieldLargeText {

    public String getType() {
        return "xhtml";
    }

    public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
        String refCode = referenceViewCode(ctx);
        if (refCode != null) {
            return refCode;
        }
        return XHTMLHelper.replaceJSTLData( ctx, super.getDisplayValue(ctx, locale));
    }

}
