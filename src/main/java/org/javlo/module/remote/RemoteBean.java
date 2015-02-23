package org.javlo.module.remote;

import java.beans.Transient;
import java.io.Serializable;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.javlo.actions.DataAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetException;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.JSONMap;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class RemoteBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int PRIORITY_LOW = 1;
	public static final int PRIORITY_MIDDLE = 2;
	public static final int PRIORITY_HIGH = 3;

	private String id = StringHelper.getRandomId();
	private String url;
	private String synchroCode;
	private String authors;
	private String latestEditor;
	private String text;
	private int priority = PRIORITY_LOW;
	private Date latestValid;
	private Date latestUnvalid;	
	private String error;	
	private Date creationDate = new Date();
	private int errorCount = 0;
	private int validCount = 0;

	transient long latestHashStore = -1;

	private Map<String, Object> serverInfo;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSynchroCode() {
		return synchroCode;
	}
	public void setSynchroCode(String synchroCode) {
		this.synchroCode = synchroCode;
	}
	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	public String getLatestEditor() {
		return latestEditor;
	}
	public void setLatestEditor(String latestEditor) {
		this.latestEditor = latestEditor;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Date getLatestValid() {
		return latestValid;
	}
	public void setLatestValid(Date latestValid) {
		this.latestValid = latestValid;
	}
	public Date getLatestUnvalid() {
		return latestUnvalid;
	}
	public void setLatestUnvalid(Date latestUnvalid) {
		this.latestUnvalid = latestUnvalid;
	}	
	public boolean check(String defaulSynchroCode) {
		return check(defaulSynchroCode, false);
	}
	public boolean check(String defaulSynchroCode, boolean forceLoadServerInfo) {
		if (serverInfo == null || forceLoadServerInfo) {
			Map<String, Object> serverInfoOut = new LinkedHashMap<String, Object>();
			if (serverInfo != null) {
				serverInfoOut.putAll(serverInfo);
			}
			String synchroCodeLocal = null;
			try {
				synchroCodeLocal = StringHelper.trimAndNullify(this.synchroCode);
				if (synchroCodeLocal == null) {
					synchroCodeLocal = defaulSynchroCode;
				}
				String srvUrl = url;
				srvUrl = URLHelper.addParam(srvUrl, ContentContext.FORWARD_AJAX, "true");
				srvUrl = URLHelper.addParam(srvUrl, "webaction", "data.serverInfo");
				srvUrl = URLHelper.addParam(srvUrl, DataAction.SYNCHRO_CODE_PARAM, synchroCodeLocal);
				String content = NetHelper.readPageGet(new URL(srvUrl));
				if (content == null) {
					serverInfoOut.put("message", "Error: No content read (The url targets a javlo server? Synchro code is correct?)");
				} else {
					JSONMap ajaxMap = JSONMap.parseMap(content);
					JSONMap dataMap = ajaxMap.getMap("data");
					if (dataMap == null) {
						serverInfoOut.put("message", "No data map.");
					} else {
						Map<String, Object> serverInfo = dataMap.getValue("serverInfo",
								new TypeToken<LinkedHashMap<String, Object>>() {
								}.getType());
						Map<String, List<String>> requestHeaders = dataMap.getValue("requestHeaders",
								new TypeToken<LinkedHashMap<String, LinkedList<String>>>() {
								}.getType());
						serverInfoOut.put("loaded", true);
						serverInfoOut.remove("message");
						serverInfoOut.putAll(serverInfo);
						serverInfoOut.put("requestHeaders", requestHeaders);
					}
				}
			} catch (JsonSyntaxException ex) {
				serverInfoOut.put("message", "No data.");
			} catch (NetException ex) {
				serverInfoOut.put("message", "Http error: " + ex.getMessage());
			} catch (Exception ex) {
				ex.printStackTrace();
				String msg = ex.getMessage();
				if (synchroCodeLocal != null) {
					msg = msg.replace(synchroCodeLocal, "[synchro-code]");
				}
				serverInfoOut.put("message", "Exception when retrieving server info: " + msg);
			}
			this.serverInfo = serverInfoOut;
		}
		try {
			String content = NetHelper.readPage(new URL(url));
			if (content.contains(text)) {
				latestValid = new Date();
				setError("");
				validCount++;
				return true;
			} else {
				setError("'" + text + "' not found.");
				latestUnvalid = new Date();
				errorCount++;
				return false;
			}
		} catch (Exception e) { 
			setError(e.getMessage());
			latestUnvalid = new Date();
			errorCount++;
			return false;
		}
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}	
	public int getErrorCount() {
		return errorCount;
	}
	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public int getValidCount() {
		return validCount;
	}
	public void setValidCount(int validCount) {
		this.validCount = validCount;
	}
	public boolean isValid() {
		if (getLatestValid() == null) {
			return false;
		} else if (getLatestUnvalid() == null) {
			return true;
		}
		Calendar errorCal = Calendar.getInstance();
		errorCal.setTime(getLatestUnvalid());
		Calendar validCal = Calendar.getInstance();
		validCal.setTime(getLatestValid());
		return validCal.after(errorCal);
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public String getLatestChangeDisplay() {
		if (isValid()) {
			return StringHelper.renderTime(getLatestUnvalid());
		} else {
			return StringHelper.renderTime(getLatestValid());
		}
	}

	@Transient
	public Map<String, Object> getServerInfo() {
		return serverInfo;
	}
	public void setServerInfo(Map<String, Object> serverInfo) {
		this.serverInfo = serverInfo;
	}

	public int getStoreHashCode() {
		 return new HashCodeBuilder(17, 37).
			       append(latestValid).
			       append(latestUnvalid).
			       append(priority).
			       append(validCount).
			       append(errorCount).
			       append(url).
			       append(authors).
			       append(latestEditor).
			       toHashCode();			   
	}
}
