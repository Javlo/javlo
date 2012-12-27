package org.javlo.macro.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.config.StaticConfig;
import org.javlo.macro.CleanResourceNameMacro;
import org.javlo.macro.CopyLanguageStructureHereMacro;
import org.javlo.macro.CopyLanguageStructureMacro;
import org.javlo.macro.CreateBaseStructureMacro;
import org.javlo.macro.CreateChildrenStructureMacro;
import org.javlo.macro.CreateDefaultPageStructure;
import org.javlo.macro.CreateExternalNewsMacro;
import org.javlo.macro.CreateMonthHereMacro;
import org.javlo.macro.CreateMonthStructureMacro;
import org.javlo.macro.CreatePressReleaseHereMacro;
import org.javlo.macro.CreatePressReleaseTodayHereMacro;
import org.javlo.macro.CreatePressReleaseTodayMacro;
import org.javlo.macro.CreateWeekHereMacro;
import org.javlo.macro.CurrentLgExpandMacro;
import org.javlo.macro.DeleteChildren;
import org.javlo.macro.DeleteChildrenAndContent;
import org.javlo.macro.DeletePageFromSpecificUser;
import org.javlo.macro.DeleteSmartExternalLinkMacro;
import org.javlo.macro.EncryptPasswordComponent;
import org.javlo.macro.ImageAfterDescriptionChildrenMacro;
import org.javlo.macro.ImageAfterDescriptionMacro;
import org.javlo.macro.ImportDataBase;
import org.javlo.macro.ImportDefaultLanguageMacro;
import org.javlo.macro.ImportGalleryMacro;
import org.javlo.macro.ImportHTMLPageMacro;
import org.javlo.macro.IncreaseSubtitleLevelMacro;
import org.javlo.macro.MergeDynamicComponent;
import org.javlo.macro.NoClickableImageMacro;
import org.javlo.macro.PasteCopiedElementInAllLanguageMacro;
import org.javlo.macro.ReduceSubtitleLevelMacro;
import org.javlo.macro.deleteComponentInBadArea;

public class MacroFactory {

	private final List<IMacro> macros = new LinkedList<IMacro>();

	private static final List<IMacro> defaultMacros = Arrays.asList(new IMacro[] { new ImportHTMLPageMacro(), new ImportGalleryMacro(), new CreateDefaultPageStructure(), new CreatePressReleaseTodayMacro(), new CreatePressReleaseHereMacro(), new CreateExternalNewsMacro(), new CreateWeekHereMacro(), new CreateMonthHereMacro(), new CreatePressReleaseTodayHereMacro(), new CreateMonthStructureMacro(), new CreateBaseStructureMacro(), new CreateChildrenStructureMacro(), new DeletePageFromSpecificUser(), new MergeDynamicComponent(), new ReduceSubtitleLevelMacro(), new IncreaseSubtitleLevelMacro(), new CleanResourceNameMacro(), new CopyLanguageStructureMacro(), new CopyLanguageStructureHereMacro(), new ImportDefaultLanguageMacro(), new DeleteSmartExternalLinkMacro(), new ImageAfterDescriptionMacro(), new ImageAfterDescriptionChildrenMacro(), new NoClickableImageMacro(), new CurrentLgExpandMacro(), new EncryptPasswordComponent(), new PasteCopiedElementInAllLanguageMacro(),
			new ImportDataBase(), new DeleteChildren(), new DeleteChildrenAndContent(), new deleteComponentInBadArea() });

	private static MacroFactory instance = null;

	public synchronized static final MacroFactory getInstance(StaticConfig staticConfig) {
		if (instance == null) {
			instance = new MacroFactory();
			if (staticConfig.getSpecialMacros().size() > 0) {
				instance.macros.addAll(staticConfig.getSpecialMacros());
			}
		}
		return instance;
	}

	private MacroFactory() {
		macros.addAll(defaultMacros);
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

}
