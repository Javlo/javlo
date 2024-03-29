/*
 * Created on 11-juin-2004
 */
package org.javlo.tracking;

import java.util.Date;

import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

/**
 * @author pvandermaesen
 * a element of tracking
 */
public class Track implements Comparable<Track> {
	
	public static final String UNDEFINED_ACTION =  "undefined";
	
	String userName;
	String action;
	String path;
	String refered;
	String userAgent;
	String range;
	boolean view = false;
	
	String sessionId;
	String inputTrackingKey;
	String IP;
	long time;
	
	public Track() {
	}
	
    /**
     * @return Returns the iP.
     */
    public String getIP() {
        return IP;
    }
    /**
     * @param ip The iP to set.
     */
    public void setIP(String ip) {
        IP = ip;
    }
    /**
     * @return Returns the sessionId.
     */
    public String getSessionId() {
        return sessionId;
    }
    /**
     * @param sessionId The sessionId to set.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getInputTrackingKey() {
		return inputTrackingKey;
	}
    
    public void setInputTrackingKey(String inputTrackingKey) {
		this.inputTrackingKey = inputTrackingKey;
	}

	/**
	 * @param userName current user name
	 * @param action current action ( sample : login, click )
	 * @param path the current path
	 * @param time the time of the action
	 */
	public Track(String userName, String action, String path, long time, String inReferer, String agent, String range) {		
		this.userName = userName;
		this.action = action;
		if (path.contains(";jsessionid")) {
			path = path.substring(0, path.indexOf(";jsessionid"));
		}
		this.path = path;
		this.time = time;
		this.refered = inReferer;
		this.userAgent = agent;
		this.range = StringHelper.neverEmpty(range, "");
	}
	/**
	 * @return Returns the action.
	 */
	public String getAction() {
		return action;
	}
	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @return Returns the time.
	 */
	public long getTime() {
		return time;
	}
	/**
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return userName;
	}
	
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append ("userName=");
		res.append (userName);
		res.append (" - ");
		res.append ("path=");
		res.append (path);
		res.append (" - ");
		res.append ("action=");
		res.append (action);
		res.append (" - ");
		res.append ("time=");
		res.append (time);
		res.append (" - ");
		res.append ("range=");
		res.append (range);
		return res.toString();
	}
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Track track) {
        if ( getSessionId().equals( track.getSessionId() ) ) {
            return (int)(getTime() - track.getTime());
        } else {
            return getSessionId().compareTo(track.getSessionId());
        }        
    }
	public void setAction(String action) {
		if ((action == null)||action.equals("null")) {
			this.action = null;
		} else {
			this.action = action;
		}
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRefered() {
		return refered;
	}
	
	public String getReferedHost() {
		return URLHelper.extractName(refered);
	}

	public void setRefered(String refered) {
		this.refered = refered;
	}

	public String getUserAgent() {
		return userAgent;
	}
	
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = StringHelper.neverEmpty(range,"");
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	public String getSortableTime() {
		return StringHelper.renderSortableTime(new Date(time));
	}
	public void setView(boolean view) {
		this.view = view;
	}
	public boolean isView() {
		return view;
	}
}
