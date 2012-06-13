package org.javlo.module.template.remote.freecsstemplates;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.module.template.remote.IRemoteTemplateFactory;
import org.javlo.module.template.remote.RemoteTemplate;
import org.javlo.remote.IRemoteResource;

public class FreeCSSTemplateFactory implements IRemoteTemplateFactory {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "freecsstemplate";

	private static Logger logger = Logger.getLogger(FreeCSSTemplateFactory.class.getName());

	private static final String BASE_URL = "http://www.freecsstemplates.org/css-templates/";

	private List<IRemoteResource> templates = new ArrayList<IRemoteResource>();

	private Date latestUpdate = null;
	
	private String sponsors;
	
	private String createAbsoluteURL(String uri) {
		URL url = null;
		try {
			url = new URL(BASE_URL);			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "bad base URL";
		}
		return URLHelper.mergePath("http://", url.getHost(), uri);
	}

	private List<IRemoteResource> extractTemplate(String page) throws BadXMLException {
		List<IRemoteResource> templates = new LinkedList<IRemoteResource>();
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(page, false);
		RemoteTemplate template = null;
		for (TagDescription tag : tags) {
			if (tag.getName().equals("div") && "ad".equalsIgnoreCase(tag.getAttributes().get("class"))) {				
				setSponsors(tag.getInside(page));
			}
			if (tag.getName().equals("div") && "thumbnail".equalsIgnoreCase(tag.getAttributes().get("class"))) {
				if (template != null) {
					template.setLicence("Creative Commons Attribution 3.0 Unported License");
					template.setAuthors("Free CSS template.");
					templates.add(template);
				}
				template = new RemoteTemplate();
				List<TagDescription> children = XMLManipulationHelper.searchChildren(tags, tag);
				String fieldName = null;
				for (TagDescription child : children) {
					if (child.getName().equals("img")) {
						template.setImageURL(createAbsoluteURL(child.getAttributes().get("src")));
					}
					if (child.getName().equals("th")) {
						fieldName = child.getInside(page).trim();
					}
					if (child.getName().equals("td") && fieldName != null) {
						String value = child.getInside(page).trim();
						if (fieldName.equalsIgnoreCase("name")) {
							template.setName(value);
						} else if (fieldName.equalsIgnoreCase("Added")) {
							DateFormat df = new SimpleDateFormat("MM.dd.yyyy");
							try {
								template.setCreationDate(df.parse(value));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}						
					}
					if (child.getName().equalsIgnoreCase("a") && child.getInside(page).trim().equalsIgnoreCase("download")) {
						template.setDownloadURL(createAbsoluteURL(child.getAttributes().get("href")));
					}
					if (child.getName().equalsIgnoreCase("a") && child.getInside(page).trim().equalsIgnoreCase("preview")) {
						template.setURL(createAbsoluteURL(child.getAttributes().get("href")));
					}
				}
			}
		}
		if (template != null) {
			template.setLicence("Creative Commons Attribution 3.0 Unported License");
			template.setAuthors("Free CSS template.");
			templates.add(template);
		}
		return templates;
	}

	@Override
	public void refresh() throws Exception {
		if (templates.size() > 0 || !checkConnection()) {
			return;
		}
		templates.clear();
		int i = 1;

		URL url = new URL(BASE_URL + i);
		logger.info("read page : "+url);
		String page = NetHelper.readPage(url);
		while (page != null) {			
			templates.addAll(extractTemplate(page));
			i++;
			url = new URL(BASE_URL + i);
			logger.info("read page : "+url);
			page = NetHelper.readPage(url);
		}
	}

	@Override
	public boolean checkConnection() {
		try {
			return NetHelper.isURLValid(new URL(BASE_URL));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<IRemoteResource> getTemplates() {
		return templates;
	}
	
	@Override
	public void setTemplates(List<IRemoteResource> templates) {
		this.templates = templates;
	}

	@Override
	public Date latestUpdate() {
		return latestUpdate;
	}

	public static void main(String[] args) {
		FreeCSSTemplateFactory fact = new FreeCSSTemplateFactory();
		System.out.println("checkConnection ? = " + fact.checkConnection());
		try {
			fact.refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collection<IRemoteResource> templates = fact.getTemplates();
		for (IRemoteResource iRemoteTemplate : templates) {
			System.out.println("");
			System.out.println("  name=" + iRemoteTemplate.getName());
			System.out.println("  date=" + StringHelper.renderDate(iRemoteTemplate.getDate()));
			System.out.println("  preview=" + iRemoteTemplate.getImageURL());
			System.out.println("  zip=" +iRemoteTemplate.getDownloadURL() );
		}
		System.out.println("");
		System.out.println("END.");
	}

	public String getSponsors() {
		return sponsors;
	}

	public void setSponsors(String sponsors) {
		this.sponsors = sponsors;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public IRemoteResource getTemplate(String name) {
		Collection<IRemoteResource> templates = getTemplates();
		for (IRemoteResource template : templates) {
			if (template.getName().equals(name)) {
				return template;
			}
		}
		return null;
	}

}
