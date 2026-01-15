package org.javlo.data;

import org.javlo.helper.StringHelper;

import java.util.Locale;

public class LanguageBean {

    private Locale locale;
    private String url;

    public LanguageBean(Locale locale, String url) {
        this.locale = locale;
        this.url = url;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDisplayCountry() {
        if (locale.getLanguage().contentEquals("en") && StringHelper.isEmpty(locale.getCountry())) {
            return "international";
        } else {
            return locale.getDisplayCountry(locale);
        }
    }
}
