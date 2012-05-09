package org.javlo.service;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoFile;

public class CompatibiltyService {

	protected static Logger logger = Logger.getLogger(CompatibiltyService.class.getName());

	/**
	 * convert a former version of content, context, config to the current cms version
	 * 
	 * @param out
	 *            for output
	 * @param ctx
	 *            current context
	 * @throws ParseException 
	 */
	public static void convertCMS(PrintStream out, ContentContext ctx) throws ParseException {
		importStaticInfoFile(out, ctx);
		moveViewBackup(out, ctx);
	}

	private static void importStaticInfoFile(PrintStream out, ContentContext ctx) {		
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		File baseStaticInfoFolder = new File(globalContext.getDataFolder() + '/' + StaticInfoFile._STATIC_INFO_DIR);
		if (baseStaticInfoFolder.exists()) {
			File baseStaticFolder = new File(globalContext.getDataFolder() + '/' + staticConfig.getStaticFolder());
			Collection<String> langs = globalContext.getContentLanguages();
			ContentContext langCtx = new ContentContext(ctx);
			for (String lang : langs) {
				langCtx.setContentLanguage(lang);
				importStaticFile(out, langCtx, baseStaticFolder);	
			}

		}

	}

	private static void importStaticFile(PrintStream out, ContentContext ctx, File inFile) {
		if (inFile.isFile()) {
			try {
				StaticInfoFile staticInfoFile = StaticInfoFile.getInstance(ctx, inFile);
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, inFile);

				/* date */
				if (staticInfoFile.getManualDate() != null) {
					if (staticInfo.getManualDate(ctx) == null) {
						staticInfo.setDate(ctx, staticInfoFile.getManualDate());
					}
				}

				/* shared */
				if (!staticInfoFile.isShared()) {
					staticInfo.setShared(ctx, false);
				}

				/* description */
				if (staticInfoFile.getManualDescription(ctx) != null && staticInfoFile.getManualDescription(ctx).trim().length() > 0) {
					if (staticInfo.getManualDescription(ctx) == null || staticInfo.getManualDescription(ctx).trim().length() == 0) {
						staticInfo.setDescription(ctx, staticInfoFile.getManualDescription(ctx));
					}
				}

				/* title */
				if (staticInfoFile.getManualTitle(ctx) != null && staticInfoFile.getManualTitle(ctx).trim().length() > 0) {
					if (staticInfo.getManualTitle(ctx) == null || staticInfo.getManualTitle(ctx).trim().length() == 0) {
						staticInfo.setTitle(ctx, staticInfoFile.getManualTitle(ctx));
					}
				}

				/* location */
				if (staticInfoFile.getManualLocation(ctx) != null && staticInfoFile.getManualLocation(ctx).trim().length() > 0) {
					if (staticInfo.getManualLocation(ctx) == null || staticInfo.getManualLocation(ctx).trim().length() == 0) {
						staticInfo.setLocation(ctx, staticInfoFile.getManualLocation(ctx));
					}
				}

				/* linked page */
				if (staticInfoFile.getLinkedPageId() != null && staticInfoFile.getLinkedPageId().trim().length() > 0) {
					if (staticInfo.getLinkedPageId(ctx) == null || staticInfo.getLinkedPageId(ctx).trim().length() == 0) {
						staticInfo.setLinkedPageId(ctx, staticInfoFile.getLinkedPageId());
					}
				}

				/* interest */
				if (staticInfo.getFocusZoneX(ctx) == StaticInfo.DEFAULT_FOCUS_X) {
					staticInfo.setFocusZoneX(ctx, staticInfoFile.getFocusZoneX());
				}
				if (staticInfo.getFocusZoneY(ctx) == StaticInfo.DEFAULT_FOCUS_Y) {
					staticInfo.setFocusZoneY(ctx, staticInfoFile.getFocusZoneY());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			File[] childen = inFile.listFiles();
			for (File file : childen) {
				importStaticFile(out, ctx, file);
			}
		}
	}

	private static void moveViewBackup(PrintStream out, ContentContext ctx) throws ParseException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Date presentPublishDate = globalContext.getPublishDate();

		File persistenceDirectory = new File(URLHelper.mergePath(globalContext.getDataFolder(), "/persitence"));
		File[] backups = persistenceDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				boolean out = false;
				if (pathname.getName().startsWith("content_2") && !pathname.getName().equals("content_2.xml")) {
					out = true;
				}
				return out;
			}
		});
		String backupFolder = URLHelper.mergePath(globalContext.getDataFolder(), "/" + globalContext.getStaticConfig().getBackupFolder());
		new File(backupFolder).mkdirs();
		Date lastPublish = null;
		for (File zip : backups) {
			String timeCode = zip.getName().replaceAll("content_" + ContentContext.VIEW_MODE + ".", "").replaceAll(".xml", "").replaceAll(".zip", "");
			try {
				Date saveTime = StringHelper.parseSecondFileTime(timeCode);
				if (lastPublish == null || saveTime.after(lastPublish)) {
					lastPublish = saveTime;
				}
			} catch (ParseException e) {
				logger.warning(e.getMessage());
			}
			File dest = new File(URLHelper.mergePath(backupFolder, zip.getName()));
			zip.renameTo(dest);
		}
		if (lastPublish != null && presentPublishDate == null) {
			// Add 1 second to be sure that the next future backup (based on publish date)
			// will be after the last backup (based on save time)
			Calendar cal = Calendar.getInstance();
			cal.setTime(lastPublish);
			cal.add(Calendar.SECOND, 1);
			globalContext.setPublishDate(cal.getTime());
		}
	}

}
