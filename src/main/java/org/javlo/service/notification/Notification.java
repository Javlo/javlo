package org.javlo.service.notification;

import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;

import java.util.Date;

public final class Notification {
    private String message;
    private String url;
    private int type;
    private Date creationDate;
    private String userId;
    private String receiver;
    private boolean admin = false;

    public String getMessage() {
        return message;
    }

    public String getDisplayMessage() {
        String out = getMessage();
        if (isForAll() && getUserId() != null) {
            out = out + " (" + getUserId() + ')';
        }
        return out;
    }

    public boolean isForAll() {
        return getReceiver() == null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * get the type, same time than in GenericMessage
     *
     * @return
     */
    public int getType() {
        return type;
    }

    public String getTypeLabel() {
        return GenericMessage.getTypeLabel(getType());
    }

    public String getBootstrapIcon() {
        return GenericMessage.getBootstrapIcon(getType());
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDiplayCreationDate() {
        return StringHelper.renderTime(getCreationDate());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimeLabel() {
        return StringHelper.renderTime(getCreationDate());
    }

    public String getSortableTimeLabel() {
        return StringHelper.renderSortableTime(getCreationDate());
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
