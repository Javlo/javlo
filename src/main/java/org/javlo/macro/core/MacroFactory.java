package org.javlo.macro.core;

import CleanResourceNameMacro.CleanResourceImageMacro;
import org.javlo.context.ContentContext;
import org.javlo.macro.*;
import org.javlo.macro.bean.MacroGoHome;
import org.javlo.macro.interactive.*;
import org.javlo.macro.interactive.module.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MacroFactory {

	private static final String KEY = MacroFactory.class.getName();

	private final List<IMacro> macros = new LinkedList<IMacro>();

	public static final MacroFactory getInstance(ContentContext ctx) {
		MacroFactory instance = (MacroFactory) ctx.getGlobalContext().getAttribute(KEY);
		if (instance == null) {
			instance = new MacroFactory();
			if (ctx.getGlobalContext().getStaticConfig().getSpecialMacros().size() > 0) {
				instance.macros.addAll(ctx.getGlobalContext().getStaticConfig().getSpecialMacros());
			}
			ctx.getGlobalContext().setAttribute(KEY, instance);
		}
		if (ctx.getAttribute(KEY) == null) {
			ctx.setAttribute(KEY,1);
			for (IMacro m : instance.macros) {
				m.init(ctx);
			}
		}
		return instance;
	}

	private MacroFactory() {
		macros.add(new CreateFakeUsers());
		macros.add(new DetectImageFileForUser());
		macros.add(new CreateContentChildren());
		macros.add(new DeleteDynamicComponent());
		macros.add(new DeleteComponent());
		macros.add(new AddChildMacro());
		macros.add(new DuplicatePage());
		macros.add(new DuplicatePageIndexed());
		macros.add(new CreateChildren());
		macros.add(new ImportContent());
		macros.add(new SmartImport());
		macros.add(new CreateAllContentSample());
		macros.add(new CreateArticle());
		macros.add(new CreateArticleWidthTemplates());
		macros.add(new CreateArticleWidthTemplatesYear());
		macros.add(new CreateAlphabeticChildrenHereMacro());
		macros.add(new CreateArticleComposition());
		macros.add(new ImportHTMLPageMacro());
		// macros.add(new ImportJCRPageMacro());
		macros.add(new ImportWordpressMacro());
		macros.add(new ImportGalleryMacro());
		macros.add(new ImportImageFromUrl());
		macros.add(new CreateDefaultPageStructure());
		// macros.add(new CreatePressReleaseTodayMacro());
		// macros.add(new CreatePressReleaseHereMacro());
		// macros.add(new CreateExternalNewsMacro());
		// macros.add(new CreateWeekHereMacro());
		// macros.add(new CreateMonthHereMacro());
		// macros.add(new CreatePressReleaseTodayHereMacro());
		// macros.add(new CreateMonthStructureMacro());
		// macros.add(new CreateBaseStructureMacro());
		macros.add(new CreateChildrenStructureMacro());
		macros.add(new DeletePageFromSpecificUser());
		macros.add(new MergeDynamicComponent());
		macros.add(new DetectAllComponentsType());
		macros.add(new ReduceSubtitleLevelMacro());
		macros.add(new IncreaseSubtitleLevelMacro());
		macros.add(new CleanResourceImageMacro());
		macros.add(new CleanResourceNameMacro());
		macros.add(new CopyLanguageStructureMacro());
		macros.add(new ImportDefaultLanguageMacro());
		macros.add(new ImportAndTranslateDefaultLanguageMacro());
		macros.add(new DeleteSmartExternalLinkMacro());
		macros.add(new ImageAfterDescriptionMacro());
		macros.add(new MacroRendererCorrection());
		macros.add(new ImageAfterDescriptionChildrenMacro());
		macros.add(new NoClickableImageMacro());
		macros.add(new CurrentLgExpandMacro());
		macros.add(new EncryptPasswordComponent());
		macros.add(new EncryptVisitorsPasswordMacro());
		macros.add(new CreateRolesFromUserList());
		macros.add(new PasteCopiedElementInAllLanguageMacro());
		macros.add(new ImportDataBase());
		macros.add(new DeleteChildren());
		macros.add(new SortChildren());
		macros.add(new DeleteChildrenContent());
		macros.add(new DuplicateChildren());
		macros.add(new DeleteBadTemplate());
		macros.add(new DownDateMacro());
		macros.add(new UpDateMacro());
		macros.add(new DashOnImageMacro());
		macros.add(new NoDashOnImageMacro());
		macros.add(new DeleteChildrenAndContent());
		macros.add(new DeleteSameComponent());
		macros.add(new DeleteComponentBadArea());
		macros.add(new TransfertComponentBadAreaToContent());
		macros.add(new UploadGallery());
		macros.add(new CreateBusinessComponent());
		macros.add(new ImportExternalPage());
		macros.add(new UndeletePage());
		macros.add(new DeleteTestPage());
		macros.add(new UnlinkMirrorComponent());
		macros.add(new RenameChildren());
		macros.add(new ActiveAllChildren());
		macros.add(new CreateRedirectionForAllLanguages());
		macros.add(new ClearDataAccessCount());
		macros.add(new DeleteComponentWithBadResourceReference());
		macros.add(new CleanImportFolder());
		macros.add(new CleanDuplicatedId());
		macros.add(new ClearContext());
		macros.add(new UpdateUserRole());
		macros.add(new MergeImagesInGallery());
		macros.add(new ClearTransformURLCache());
		macros.add(new ChangeImageFilter());
		macros.add(new MailingStat());
		macros.add(new ValidAllChildren());
		macros.add(new CleanPersistenceFolder());
		macros.add(new FileInListMacro());
		macros.add(new MergeGalleryAndGalleries());
		macros.add(new DeleteContentFiles());
		macros.add(new PushStaticOnFtp());
		//macros.add(new ConvertUserForComansys());
		macros.add(new InitContentMacro());
		macros.add(new CommitTemplate());
		macros.add(new UpdateTemplate());		
		macros.add(new RebuitTemplate());
		macros.add(new RebuitTemplateHtml());
		macros.add(new CleanStaticInfoPersistence());
		macros.add(new StaticInfoAutoFill());
		macros.add(new DeleteTrackerCache());
		macros.add(new LogAsUser());
		macros.add(new DisplayDashBoard());
		macros.add(new DisplaySeoReport());
		macros.add(new ResetRecaptcha());
		macros.add(new PublishMacro());
		macros.add(new TaxonomyMacroModule());
		macros.add(new TemplateMacroModule());
		macros.add(new ComponentsMacroModule());
		macros.add(new MailingMacroModule());
		macros.add(new ClearImageCache());
		macros.add(new DeletePageContent());

		macros.add(new UserMacroModule(true));
		macros.add(new UserMacroModule(false));

		macros.add(new RegistrationMacroModule());
		macros.add(new CreateRedirections());

		macros.add(new ResolveLinkToPageName(true));
		macros.add(new ResolveLinkToPageName(false));

		macros.add(new SplitOnTitle("h2"));
		macros.add(new SplitOnTitle("h3"));

		macros.add(new ChildrenRolesInherited());
		
		/** macro bean **/
		initMacroBean(macros);
		
		sort(macros);
	}
	
	public static void sort(List<IMacro> macros) {
		Collections.sort(macros, new Comparator<IMacro>() {
			@Override
			public int compare(IMacro o1, IMacro o2) {
				return o1.getPriority() - o2.getPriority();
			}
		});
	}

	private static void initMacroBean(List<IMacro> macros) {
		MacroBean mailingMacro = new MacroBean("mailling", "module=mailing&previewEdit=true&wizardStep=2&box=sendwizard&webaction=mailing.wizard", "fa fa-envelope-open-o");
		mailingMacro.setModalSize(IMacro.LARGE_MODAL_SIZE);
		macros.add(mailingMacro);
		MacroBean homeMacro = new MacroGoHome();
		macros.add(homeMacro);
	}

	public IMacro getMacro(String name) {
		for (IMacro macro : macros) {
			if (macro.getName().equals(name)) {
				return macro;
			}
		}
		return null;
	}

	public List<IMacro> getMacros() {
		return macros;
	}

	public void clear(ContentContext ctx) {
		ctx.getGlobalContext().setAttribute(KEY, null);
	}

}
