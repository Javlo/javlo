package org.javlo.macro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.layout.ContentSeparation;
import org.javlo.component.multimedia.Multimedia;
import org.javlo.component.text.Description;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.Heading;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImportExternalPage implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportExternalPage.class.getName());

	@Override
	public String getName() {
		return "import-external-page";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-external-page";
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/import-external-page.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	public static String performImport(RequestService rs, ContentContext ctx, EditContext editCtx, ContentService content, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String url = ctx.getRequest().getParameter("url");
		Document doc = Jsoup.parse(NetHelper.readPage(new URL(url)));
		String title = doc.select("title").text();
		if (title.contains(",")) {
			title = title.substring(0, title.indexOf(','));
		}
		String code = doc.select("#id_form-0-product_id").text();
		String description = null;
		String text = "";
		for (Element paragraph : doc.select("#leftCntr p")) {
			if (description == null) {
				description = paragraph.text();
			} else {
				text = text + "<p>" + paragraph.text() + "</p>";
			}
		}
		String finalPrice = "";
		int minPrice = Integer.MAX_VALUE;
		for (Element price : doc.select(".para .right strong")) {
			String cleanPrice = price.text().replace('€', ' ').trim();
			if (StringHelper.isLikeNumber(cleanPrice)) {
				int localPrice = 0;
				try {
					localPrice = Integer.parseInt(cleanPrice);
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage());
				}
				if (localPrice < minPrice) {
					finalPrice = ""+localPrice;
					minPrice = localPrice;
				}
			}
		}

		Elements factBox = doc.select(".factBox li");
		Map<String, String> data = new HashMap<String, String>();
		for (Element elem : factBox) {
			data.put(elem.select("span").text(), elem.select("strong").text());
		}
		Elements photoBox = doc.select(".photoBox li a.fancyBox");
		Collection<String> images = new LinkedList<String>();
		for (Element elem : photoBox) {
			images.add(elem.attr("href"));
		}

		String country = data.get("Waar?");
		String city = "";
		if (country.contains(",")) {
			city = StringHelper.stringToArray(country, ",")[0];
			country = StringHelper.stringToArray(country, ",")[1];
		}

		for (String img : images) {
			System.out.println("   " + img);
		}
		String folder = "";
		String image = null;
		for (String imageURL : images) {
			String imageRelativeFolder = URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), ctx.getCurrentTemplate().getImportImageFolder(), ctx.getCurrentPage().getName());
			folder = URLHelper.mergePath(ctx.getCurrentTemplate().getImportImageFolder(), ctx.getCurrentPage().getName());
			File targetFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), imageRelativeFolder, StringHelper.createFileName(StringHelper.getFileNameFromPath(imageURL))));
			StaticInfo si = StaticInfo.getInstance(ctx, targetFile);
			si.setTitle(ctx, title);
			targetFile.getParentFile().mkdirs();
			ResourceHelper.writeUrlToFile(new URL(imageURL), targetFile);
			if (image == null) {
				image = StringHelper.getFileNameFromPath(imageURL);
			}
		}

		String wanner = data.get("Wanneer?");

		String startDate = "";
		String endDate = "";
		if (wanner.contains("-")) {
			SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("nl"));
			SimpleDateFormat formatSuffix = new SimpleDateFormat("MMMM yyyy", new Locale("nl"));
			String startDateStr = wanner.split("-")[0].trim();
			String endDateStr = wanner.split("-")[1].trim();
			Date endDateDate = null;
			try {
				endDateDate = format.parse(endDateStr);
				endDate = StringHelper.renderDate(endDateDate);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			try {
				if (StringHelper.isDigit(startDateStr.trim()) && endDateDate != null) {
					Calendar endDateCal = Calendar.getInstance();
					endDateCal.setTime(endDateDate);
					startDateStr = startDateStr.trim()+" "+formatSuffix.format(endDateDate);
				}
				Date startDateDate = format.parse(startDateStr);
				startDate = StringHelper.renderDate(startDateDate);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
	
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("component.renderer=/components/destination.jsp");
		out.println("component.type=destination");
		out.println("component.label=destination");
		out.println("image.priority=9");
		out.println("field.code.label=code");
		out.println("field.code.value=" + code);
		out.println("field.location.label.fr=Localisation");
		out.println("field.type.label.fr=Genre");
		out.println("field.title.search=true");
		out.println("field.location.search=true");
		out.println("field.title.type=h1");
		out.println("field.photo.order=400");
		out.println("field.location.order=700");		
		out.println("field.location.value=" + data.get("Locatie"));		
		out.println("field.photo.label=photo");
		out.println("field.photo.value.file=" + image);
		out.println("field.location.label=location");		
		out.println("field.location.label.nl=Locatie");
		out.println("field.url.type=text");
		out.println("field.photo.image.filter=bloc-6-6");
		out.println("field.photo.value.folder=" + folder.replaceFirst("/images/", ""));
		out.println("field.type.label.nl=Genre");		
		out.println("field.code.search=true");		
		out.println("field.type.order=600");
		out.println("field.url.order=800");
		out.println("field.url.label.fr=r\u00E9server");
		out.println("field.title.order=100");
		out.println("field.url.value=" + url + "#tickets");
		out.println("field.type.value=" + data.get("Genre"));
		out.println("field.type.label=type");
		out.println("field.url.label=book");
		out.println("field.photo.type=image");
		out.println("field.title.value=" + title);
		out.println("field.code.type=text");
		out.println("field.code.i18n=false");
		out.println("field.type.type=text");
		out.println("field.title.label=Title");
		out.println("field.code.order=10");
		out.println("field.location.type=text");		

		out.println("field.country.value=" + country);
		out.println("field.country.type=open-list");
		out.println("field.country.label=Country");
		out.println("field.country.label.fr=Pays");
		out.println("field.country.label.nl=Land");
		out.println("field.country.search=true");
		out.println("field.country.order=150");

		out.println("field.city.value=" + city);
		out.println("field.city.type=text");
		out.println("field.city.label=City");
		out.println("field.city.label.fr=Ville");
		out.println("field.city.label.nl=Stad");
		out.println("field.city.search=true");
		out.println("field.city.order=170");

		out.println("field.startdate.type=date");
		out.println("field.startdate.label=Start");
		out.println("field.startdate.label.fr=Début");
		out.println("field.startdate.label.nl=Begin");
		out.println("field.startdate.search=true");
		out.println("field.startdate.search.type=month");
		out.println("field.startdate.order=200");
		out.println("field.startdate.value=" + startDate);

		out.println("field.enddate.type=date");
		out.println("field.enddate.label=End");
		out.println("field.enddate.label.fr=Fin");
		out.println("field.enddate.label.nl=Einde");
		out.println("field.enddate.search.type=month");
		out.println("field.enddate.order=220");
		out.println("field.enddate.value=" + endDate);

		out.println("field.price.type=number");
		out.println("field.price.label=Max Price (&euro;)");
		out.println("field.price.label.fr=Budget max (&euro;)");
		out.println("field.price.label.nl=Max Budgets (&euro;)");
		out.println("field.price.search=true");
		out.println("field.price.search.type=<=");
		out.println("field.price.order=650");
		out.println("field.price.value="+finalPrice);

		out.println("component.list-renderer=/components/destination_list.jsp");
		out.close();

		String parent = MacroHelper.addContentIfNotExist(ctx, ctx.getCurrentPage(), "0", Description.TYPE, description);
		parent = MacroHelper.addContentIfNotExist(ctx, ctx.getCurrentPage(), parent, "destination", new String(outStream.toByteArray()));
		IContentVisualComponent comp = content.getComponent(ctx, parent);
		((DynamicComponent) comp).reloadProperties();
		
		parent = MacroHelper.addContentIfNotExist(ctx, ctx.getCurrentPage(), parent, WysiwygParagraph.TYPE, text);
		parent = MacroHelper.addContentIfNotExist(ctx, ctx.getCurrentPage(), parent, ContentSeparation.TYPE, "");
		if (ctx.getContextRequestLanguage().equals("fr")) {
			parent = MacroHelper.addContentIfNotExist(ctx, ctx.getCurrentPage(), parent, Heading.TYPE, "depth=2\ntext=Gallerie");
		} else {
			parent = MacroHelper.addContentIfNotExist(ctx, ctx.getCurrentPage(), parent, Heading.TYPE, "depth=2\ntext=Gallery");
		}
		parent = MacroHelper.addContentIfNotExist(ctx, ctx.getCurrentPage(), parent, Multimedia.TYPE, "%%0,16%" + folder + "%%%Images");
		ContentService.getInstance(ctx.getGlobalContext()).getComponent(ctx, parent).setRenderer(ctx, "blocs");

		ctx.setClosePopup(true);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	public static void main(String[] args) throws Exception {
		URL url = new URL("https://festival.travel/festivals/snowattack/");
		Document doc = Jsoup.parse(NetHelper.readPage(url));
		String where = doc.select("h2 strong").text();
		String description = doc.select("#leftCntr p").first().text();

		String finalPrice = "";
		int minPrice = Integer.MAX_VALUE;
		for (Element price : doc.select(".para .right strong")) {
			String cleanPrice = price.text().replace('€', ' ').trim();
			if (StringHelper.isLikeNumber(cleanPrice)) {
				int localPrice = Integer.parseInt(cleanPrice);
				if (localPrice < minPrice) {
					finalPrice = price.text().trim();
					minPrice = localPrice;
				}
			}
		}

		Elements factBox = doc.select(".factBox li");
		Map<String, String> data = new HashMap<String, String>();
		for (Element elem : factBox) {
			data.put(elem.select("span").text(), elem.select("strong").text());
		}

		Elements photoBox = doc.select(".photoBox li a.fancyBox");
		Collection<String> images = new LinkedList<String>();
		for (Element elem : photoBox) {
			images.add(elem.attr("href"));
		}

		System.out.println("where=" + where);
		System.out.println("description=" + description);
		System.out.println("waar=" + data.get("Waar?"));
		System.out.println("Wanneer=" + data.get("Wanneer?"));
		System.out.println("Genre=" + data.get("Genre"));
		System.out.println("Locatie=" + data.get("Locatie"));
		System.out.println("images :");
		for (String img : images) {
			System.out.println("   " + img);
		}

	}
}
