package org.javlo.macro.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.macro.ActiveAllChildren;
import org.javlo.macro.AddChildMacro;
import org.javlo.macro.CleanDuplicatedId;
import org.javlo.macro.CleanImportFolder;
import org.javlo.macro.CleanPersistenceFolder;
import org.javlo.macro.CleanResourceNameMacro;
import org.javlo.macro.CleanStaticInfoPersistence;
import org.javlo.macro.ClearContext;
import org.javlo.macro.ClearDataAccessCount;
import org.javlo.macro.ClearImageCache;
import org.javlo.macro.ClearTransformURLCache;
import org.javlo.macro.CommitTemplate;
import org.javlo.macro.ConvertUserForComansys;
import org.javlo.macro.CopyLanguageStructureMacro;
import org.javlo.macro.CreateAllContentSample;
import org.javlo.macro.CreateAlphabeticChildrenHereMacro;
import org.javlo.macro.CreateArticleComposition;
import org.javlo.macro.CreateChildrenStructureMacro;
import org.javlo.macro.CreateDefaultPageStructure;
import org.javlo.macro.CreateFakeUsers;
import org.javlo.macro.CreateRolesFromUserList;
import org.javlo.macro.CurrentLgExpandMacro;
import org.javlo.macro.DashOnImageMacro;
import org.javlo.macro.DeleteBadTemplate;
import org.javlo.macro.DeleteChildren;
import org.javlo.macro.DeleteChildrenAndContent;
import org.javlo.macro.DeleteChildrenContent;
import org.javlo.macro.DeleteComponentBadArea;
import org.javlo.macro.DeleteComponentWithBadResourceReference;
import org.javlo.macro.DeleteContentFiles;
import org.javlo.macro.DeletePageFromSpecificUser;
import org.javlo.macro.DeleteSameComponent;
import org.javlo.macro.DeleteSmartExternalLinkMacro;
import org.javlo.macro.DeleteTestPage;
import org.javlo.macro.DeleteTrackerCache;
import org.javlo.macro.DetectAllComponentsType;
import org.javlo.macro.DownDateMacro;
import org.javlo.macro.DuplicateChildren;
import org.javlo.macro.DuplicatePage;
import org.javlo.macro.DuplicatePageIndexed;
import org.javlo.macro.EncryptPasswordComponent;
import org.javlo.macro.EncryptVisitorsPasswordMacro;
import org.javlo.macro.FileInListMacro;
import org.javlo.macro.ImageAfterDescriptionChildrenMacro;
import org.javlo.macro.ImageAfterDescriptionMacro;
import org.javlo.macro.ImportAndTranslateDefaultLanguageMacro;
import org.javlo.macro.ImportDataBase;
import org.javlo.macro.ImportDefaultLanguageMacro;
import org.javlo.macro.IncreaseSubtitleLevelMacro;
import org.javlo.macro.InitContentMacro;
import org.javlo.macro.MacroRendererCorrection;
import org.javlo.macro.MergeDynamicComponent;
import org.javlo.macro.MergeGalleryAndGalleries;
import org.javlo.macro.MergeImagesInGallery;
import org.javlo.macro.NoClickableImageMacro;
import org.javlo.macro.NoDashOnImageMacro;
import org.javlo.macro.PasteCopiedElementInAllLanguageMacro;
import org.javlo.macro.PublishMacro;
import org.javlo.macro.RebuitTemplate;
import org.javlo.macro.RebuitTemplateHtml;
import org.javlo.macro.ReduceSubtitleLevelMacro;
import org.javlo.macro.ResetRecaptcha;
import org.javlo.macro.SortChildren;
import org.javlo.macro.StaticInfoAutoFill;
import org.javlo.macro.TransfertComponentBadAreaToContent;
import org.javlo.macro.UndeletePage;
import org.javlo.macro.UnlinkMirrorComponent;
import org.javlo.macro.UpDateMacro;
import org.javlo.macro.UpdateTemplate;
import org.javlo.macro.ValidAllChildren;
import org.javlo.macro.bean.MacroGoHome;
import org.javlo.macro.interactive.ChangeImageFilter;
import org.javlo.macro.interactive.CreateArticle;
import org.javlo.macro.interactive.CreateArticleWidthTemplates;
import org.javlo.macro.interactive.CreateBusinessComponent;
import org.javlo.macro.interactive.CreateChildren;
import org.javlo.macro.interactive.CreateContentChildren;
import org.javlo.macro.interactive.CreateRedirectionForAllLanguages;
import org.javlo.macro.interactive.DeleteComponent;
import org.javlo.macro.interactive.DeleteDynamicComponent;
import org.javlo.macro.interactive.DisplayDashBoard;
import org.javlo.macro.interactive.DisplaySeoReport;
import org.javlo.macro.interactive.ImportContent;
import org.javlo.macro.interactive.ImportExternalPage;
import org.javlo.macro.interactive.ImportGalleryMacro;
import org.javlo.macro.interactive.ImportHTMLPageMacro;
import org.javlo.macro.interactive.LogAsUser;
import org.javlo.macro.interactive.MailingStat;
import org.javlo.macro.interactive.PushStaticOnFtp;
import org.javlo.macro.interactive.RenameChildren;
import org.javlo.macro.interactive.SmartImport;
import org.javlo.macro.interactive.SocialShare;
import org.javlo.macro.interactive.UpdateUserRole;
import org.javlo.macro.interactive.UploadGallery;
import org.javlo.macro.interactive.module.ComponentsMacroModule;
import org.javlo.macro.interactive.module.MailingMacroModule;
import org.javlo.macro.interactive.module.TaxonomyMacroModule;
import org.javlo.macro.interactive.module.TemplateMacroModule;

import CleanResourceNameMacro.CleanResourceImageMacro;

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
		macros.add(new CreateAlphabeticChildrenHereMacro());
		macros.add(new CreateArticleComposition());
		macros.add(new ImportHTMLPageMacro());
		// macros.add(new ImportJCRPageMacro());
		macros.add(new ImportGalleryMacro());
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
		macros.add(new SocialShare());
		macros.add(new ValidAllChildren());
		macros.add(new CleanPersistenceFolder());
		macros.add(new FileInListMacro());
		macros.add(new MergeGalleryAndGalleries());
		macros.add(new DeleteContentFiles());
		macros.add(new PushStaticOnFtp());
		macros.add(new ConvertUserForComansys());
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
