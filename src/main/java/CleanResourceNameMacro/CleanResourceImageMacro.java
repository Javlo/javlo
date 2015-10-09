package CleanResourceNameMacro;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.AbstractMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.resource.Resource;
import org.javlo.ztatic.IStaticContainer;

import com.Ostermiller.util.StringHelper;

public class CleanResourceImageMacro extends AbstractMacro {
	
	private static Logger logger = Logger.getLogger(CleanResourceImageMacro.class.getName());

	@Override
	public String getName() {
		return "clean-resource-image";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String backupFolder = "images-backup";
		File imageFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder()));
		File imageBackupFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), backupFolder));
		if (!imageFolder.exists()) {
			return "image folder not found.";
		} else {
			if (imageBackupFolder.exists()) {
				return "delete or rename backup image folder (" + backupFolder + ") before run this macro.";
			} else {
				ResourceHelper.copyDir(imageFolder, imageBackupFolder, false, null);
				Set<String> imagesList = new HashSet<String>();
				ContentContext ctxNoLang = new ContentContext(ctx);
				for (String lg : globalContext.getContentLanguages()) {
					ctxNoLang.setAllLanguage(lg);
					for (IContentVisualComponent comp : ComponentFactory.getAllComponentsFromContext(ctxNoLang)) {
						if (comp instanceof IStaticContainer) {
							Collection<Resource> resources = ((IStaticContainer) comp).getAllResources(ctx);
							if (resources != null) {
								for (Resource resource : resources) {
									imagesList.add(URLHelper.cleanPath(resource.getUri(), true));
								}
							}
						}
					}
				}
				int deletedFile = 0;
				int conservedFile = 0;
				for (File file : ResourceHelper.getAllFiles(imageFolder, null)) {
					String url = StringHelper.replace(file.getAbsolutePath(),globalContext.getDataFolder(),"");
					url = URLHelper.cleanPath(url, true);
					if (imagesList.contains(url)) {
						conservedFile++;
					} else {
						deletedFile++;
						file.delete();
					}
				}
				logger.info("deleted file : "+deletedFile+" conserved file : "+conservedFile);
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.clearGlobalMessage();
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				if (deletedFile == 0) {
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("macro.clean-resource-image.no-deleted", "No images deleted, you ressource was clean."), GenericMessage.INFO));
				} else {
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("macro.create-content-children.create.image-deleted", "Images deleted : "+deletedFile), GenericMessage.INFO));	
				}
				
			}
		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

}
