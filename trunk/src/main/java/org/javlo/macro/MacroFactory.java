package org.javlo.macro;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.config.StaticConfig;

public class MacroFactory {

	private final List<IMacro> macros = new LinkedList<IMacro>();

	private static final List<IMacro> defaultMacros = Arrays.asList(new IMacro[] { new ImportGalleryMacro(), new CreateDefaultPageStructure(), new CreatePressReleaseTodayMacro(), new CreatePressReleaseHereMacro(), new CreateExternalNewsMacro(), new CreateWeekHereMacro(), new CreateMonthHereMacro(), new CreatePressReleaseTodayHereMacro(), new CreateMonthStructureMacro(), new CreateBaseStructureMacro(), new CreateChildrenStructureMacro(), new DeletePageFromSpecificUser(), new MergeDynamicComponent(), new ReduceSubtitleLevelMacro(), new IncreaseSubtitleLevelMacro(), new CleanResourceNameMacro(), new CopyLanguageStructureMacro(), new CopyLanguageStructureHereMacro(), new ImportDefaultLanguageMacro(), new DeleteSmartExternalLinkMacro(), new ImageAfterDescriptionMacro(), new ImageAfterDescriptionChildrenMacro(), new NoClickableImageMacro(), new CurrentLgExpandMacro(), new EncryptPasswordComponent(), new PasteCopiedElementInAllLanguageMacro(), new ImportDataBase(), new DeleteChildren(),
			new DeleteChildrenAndContent(), new deleteComponentInBadArea() });

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
