/*
 * Created on 11-juin-2004
 */
package org.javlo.tracking;

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
	
	String sessionId;
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


	/**
	 * @param userName current user name
	 * @param action current action ( sample : login, click )
	 * @param path the current path
	 * @param time the time of the action
	 */
	public Track(String userName, String action, String path, long time, String inReferer, String agent) {		
		this.userName = userName;
		this.action = action;
		this.path = path;
		this.time = time;
		this.refered = inReferer;
		this.userAgent = agent;
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

	public void setRefered(String refered) {
		this.refered = refered;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	

}
