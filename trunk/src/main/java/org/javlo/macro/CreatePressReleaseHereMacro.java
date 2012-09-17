package org.javlo.macro;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;

import org.javlo.component.image.GlobalImage;
import org.javlo.component.meta.DateComponent;
import org.javlo.component.text.Description;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class CreatePressReleaseHereMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "create-press-release-here";
	}

	public String getPageStructureName() {
		return "press-release";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		MenuElement monthPage = currentPage;

		if ((monthPage != null) && (monthPage.getParent() != null) && (monthPage.getParent().getParent() != null)) {
			MenuElement groupPage = currentPage.getParent().getParent();
			String[] splittedName = monthPage.getName().split("-");
			String year = null;
			String mount = null;
			if (splittedName.length == 3) {
				year = splittedName[1];
				mount = splittedName[2];
			}
			if (splittedName.length == 2) {
				year = splittedName[0];
				mount = splittedName[1];
			}
			try {
				Integer.parseInt(year);
			} catch (Throwable t) {
				year = null;
			}
			if (year != null && mount != null) {
				MenuElement[] children = currentPage.getChildMenuElements();

				int maxNumber = 0;
				for (MenuElement child : children) {
					splittedName = child.getName().split("-");

					try {
						int currentNumber = Integer.parseInt(splittedName[splittedName.length - 1]);
						if (currentNumber > maxNumber) {
							maxNumber = currentNumber;
						}
					} catch (NumberFormatException e) {
					}
				}
				maxNumber = maxNumber + 1;
				MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, monthPage.getName(), groupPage.getName() + "-" + year + "-" + mount + "-" + maxNumber, true);
				newPage.setVisible(true);

				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

				Calendar cal = GregorianCalendar.getInstance();
				cal.setTime(new Date());

				Properties pressReleaseStructure = ctx.getCurrentTemplate().getMacroProperties(globalContext, getPageStructureName());
				if (pressReleaseStructure == null) {
					Collection<String> lgs = globalContext.getContentLanguages();
					for (String lg : lgs) {
						String parentId = "0";
						parentId = MacroHelper.addContent(lg, newPage, parentId, DateComponent.TYPE, "");
						parentId = MacroHelper.addContent(lg, newPage, parentId, Title.TYPE, "");
						parentId = MacroHelper.addContent(lg, newPage, parentId, Description.TYPE, "");
						parentId = MacroHelper.addContent(lg, newPage, parentId, GlobalImage.TYPE, "");
						parentId = MacroHelper.addContent(lg, newPage, parentId, Paragraph.TYPE, "");
					}
				} else {
					createPageStructure(ctx, newPage, pressReleaseStructure, StringHelper.isTrue(pressReleaseStructure.get("fake-content")));
				}

				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
			} else {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				String msg = i18nAccess.getText("action.add.new-news-today");
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
			}
		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-today");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}

		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}
};
