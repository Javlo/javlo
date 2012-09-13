package org.javlo.service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.javlo.context.ContentContext;
import org.javlo.portlet.filter.MultiReadRequestWrapper;

public class RequestService {

	FileCleaningTracker tracker = new FileCleaningTracker();

	private static final String KEY = RequestService.class.getName();

	private final Map<String, String[]> parameters = new HashMap<String, String[]>();

	private final Map<String, FileItem[]> fileItems = new HashMap<String, FileItem[]>();

	private HttpServletRequest request;

	// todo: use this method for optimisation.
	private void putParameter(String key, String value) {
		String[] values = parameters.get(key);
		if (values == null) {
			values = new String[0];
		}
		String[] newValues = new String[values.length + 1];
		System.arraycopy(values, 0, newValues, 0, values.length);
		newValues[values.length] = value;
		parameters.put(key, newValues);
	}

	@SuppressWarnings("unchecked")
	public static RequestService getInstance(HttpServletRequest request) {

		try {
			request.setCharacterEncoding(ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		RequestService instance = (RequestService) request.getAttribute(KEY);
		if (instance == null) {
			instance = new RequestService();
			request.setAttribute(KEY, instance);

			try {
				instance.parameters.putAll(request.getParameterMap());
			} catch (RuntimeException e1) {
				// TODO Auto-generated catch block
				System.out.println(e1.getMessage());
				// e1.printStackTrace();
			}
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setFileSizeMax(MultiReadRequestWrapper.MAX_UPLOAD_SIZE);
				List<FileItem> items = null;

				try {
					items = upload.parseRequest(request);
				} catch (FileUploadException e) {
					e.printStackTrace();
				}
				if (items != null) {
					for (FileItem item : items) {
						if (item.isFormField()) {
							String[] values = instance.parameters.get(item.getFieldName());
							if (values == null) {
								try {
									values = new String[] { item.getString(ContentContext.CHARACTER_ENCODING) };
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							} else {
								List<String> list = new LinkedList<String>(Arrays.asList(values));
								try {
									list.add(item.getString(ContentContext.CHARACTER_ENCODING));
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								values = list.toArray(values);
							}
							instance.parameters.put(item.getFieldName(), values);
						} else {
							FileItem[] fileItems = instance.fileItems.get(item.getFieldName());
							if (fileItems == null) {
								fileItems = new FileItem[] { item };
							} else {
								List<FileItem> list = new LinkedList<FileItem>(Arrays.asList(fileItems));
								list.add(item);
								fileItems = list.toArray(fileItems);
							}
							instance.fileItems.put(item.getFieldName(), fileItems);
							instance.parameters.put(item.getFieldName(), new String[] { item.getName() });
						}
					}
				}
				factory.setFileCleaningTracker(instance.tracker);
			}
		}

		instance.request = request;
		return instance;
	}

	public String getParameter(String key, String outDefault) {
		String[] values = parameters.get(key);
		if (values == null || values.length == 0 || values[0] == null) {
			return outDefault;
		} else {
			return values[0];
		}
	}

	public String[] getParameterValues(String key, String[] defaultValue) {
		String[] res = parameters.get(key);
		if (res == null) {
			res = defaultValue;
		}
		return res;
	}

	public List<String> getParameterListValues(String key, List<String> defaultValue) {
		String[] res = parameters.get(key);
		if (res == null) {
			return defaultValue;
		} else {
			List<String> outList = new LinkedList<String>();
			for (String param : res) {
				outList.add(param);
			}
			return outList;
		}
	}

	public Map<String, String[]> getParameterMap() {
		return Collections.unmodifiableMap(parameters);
	}

	public FileItem getFileItem(String key) {
		FileItem[] items = fileItems.get(key);
		if (items == null || items.length == 0) {
			return null;
		} else {
			return items[0];
		}
	}

	public FileItem[] getFileItems(String key) {
		return fileItems.get(key);
	}

	public Collection<FileItem> getAllFileItem() {
		Collection<FileItem> res = new LinkedList<FileItem>();
		Collection<FileItem[]> fileItemArray = fileItems.values();
		for (FileItem[] items : fileItemArray) {
			for (FileItem item : items) {
				res.add(item);
			}
		}
		return res;
	}

	public Map<String, FileItem[]> getFileItemMap() {
		return Collections.unmodifiableMap(fileItems);
	}

	public FileItem getFirstFileItem() {
		if (fileItems.values().size() > 0) {
			FileItem[] items = fileItems.values().iterator().next();
			if (items != null && items.length > 0) {
				return items[0];
			}
		}
		return null;
	}

	public static String getAttribute(HttpServletRequest request, String key, String defaultValue) {
		Object value = request.getAttribute(key);
		if (value == null) {
			value = defaultValue;
		}
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

	public static String getURI(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String uri = request.getRequestURI();
		if (contextPath.length() > 0) {
			uri = uri.substring(contextPath.length());
		}
		return uri;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

}
