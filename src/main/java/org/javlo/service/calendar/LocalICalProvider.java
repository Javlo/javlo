package org.javlo.service.calendar;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;

public class LocalICalProvider extends AbstractICalProvider {

	private static final String KEY = LocalICalProvider.class.getName();

	public static LocalICalProvider getInstance(ContentContext ctx) {
		LocalICalProvider outService = (LocalICalProvider) ctx.getGlobalContext().getAttribute(KEY);
		if (outService == null) {
			outService = new LocalICalProvider();
			ctx.getGlobalContext().setAttribute(KEY, outService);
			outService.init(ctx);
		}
		return outService;
	}

	@Override
	public String getName() {
		return "local";
	}	

	@Override
	public List<ICal> getICals() {
		// TODO Auto-generated method stub
		return null;
	}

	private File getICalFolder(ICal ical) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(ical.getDate());
		if (ical.isOneMonth()) {
			return new File(URLHelper.mergePath(folder.getAbsolutePath(), "" + cal.get(Calendar.YEAR),
					"" + cal.get(Calendar.MONTH)));
		} else if (ical.isOneYear()) {
			return new File(URLHelper.mergePath(folder.getAbsolutePath(), "" + cal.get(Calendar.YEAR)));
		} else {
			return new File(URLHelper.mergePath(folder.getAbsolutePath()));
		}
	}
	
	@Override
	public void reset(ContentContext ctx) throws Exception {
		ctx.getGlobalContext().setAttribute(KEY, null);
	}

	private File getICalFolder(int y, int m) {
		return new File(URLHelper.mergePath(folder.getAbsolutePath(), "" + y, "" + m));
	}

	@Override
	public List<ICal> getICals(int year, int month) throws Exception {
		List<ICal> outICal = new LinkedList<ICal>();
		File dir = getICalFolder(year, month);
		List<File> files = new LinkedList<File>();
		if (dir.listFiles() != null) {
			files.addAll(Arrays.asList(dir.listFiles()));
		}
		/** load multi month **/
		if (dir.getParentFile().listFiles() != null) {
			files.addAll(Arrays.asList(dir.getParentFile().listFiles()));
		}
		/** load multi year **/
		if (dir.getParentFile().getParentFile().listFiles() != null) {
			files.addAll(Arrays.asList(dir.getParentFile().getParentFile().listFiles()));
		}
		for (File f : files) {
			if (FilenameUtils.getExtension(f.getName()).equals("ical")) {
				ICal ical = new ICal(isEditable());
				ical.setId(FilenameUtils.getBaseName(f.getName()));
				ical.loadFromString(ResourceHelper.loadStringFromFile(f));
				if (ical.isSameMonth(year, month)) {
					outICal.add(ical);
				}
				cache(ical);

				if (!ical.isOneDay()) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(ical.getStartDate());
					Calendar endCal = Calendar.getInstance();
					endCal.setTime(ical.getEndDate());
					if (endCal.getTimeInMillis() > cal.getTimeInMillis()) {
						GhostICal gcal = null;
						while (!DateUtils.isSameDay(cal, endCal)) {
							cal.add(Calendar.DAY_OF_YEAR, 1);
							gcal = new GhostICal(ical, cal.getTime());
							if (gcal.isSameMonth(year, month)) {
								outICal.add(gcal);
							}
							// cache(gcal);
						}
						/* set end date if time in day defined */
						gcal.setDate(ical.getEndDate());
						gcal.setNext(false);
					}
				}
			}
		}
		return outICal;
	}

	public void store(ICal ical) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(ical.getDate());
		File icalFolder = getICalFolder(ical);
		icalFolder.mkdirs();
		if (!ical.isOneYear()) {
			icalFolder = icalFolder.getParentFile().getParentFile();
		} else if (!ical.isOneMonth()) {
			icalFolder = icalFolder.getParentFile();
		}
		File icalFile = new File(URLHelper.mergePath(icalFolder.getAbsolutePath(), ical.getId() + ".ical"));
		try {
			ResourceHelper.writeStringToFile(icalFile, ical.storeToString(), ContentContext.CHARACTER_ENCODING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteICal(ICal ical) throws Exception {
		if (ical == null) {
			return;
		}
		File icalFolder = getICalFolder(ical);
		File icalFile = new File(URLHelper.mergePath(icalFolder.getAbsolutePath(), ical.getId() + ".ical"));
		if (icalFile.exists()) {
			icalFile.delete();
		}
		removeFromCache(ical.getId());
	}

	@Override
	public void deleteICal(String id) throws Exception {
		deleteICal(getInCache(id));		
	}
	
	@Override
	public boolean isEditable() {	
		return true;
	}

}
