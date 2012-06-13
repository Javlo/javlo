package org.javlo.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.image.ImageConfig;
import org.javlo.image.ImageEngine;
import org.javlo.image.ImageHelper;
import org.javlo.mailing.MailingContext;
import org.javlo.rendering.Device;
import org.javlo.template.Template;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;

import com.jhlabs.image.CrystallizeFilter;
import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.EmbossFilter;
import com.jhlabs.image.GrayscaleFilter;

/**
 * @author pvandermaesen This Class get all button params from the html file and create a new TextButton with this params.
 * 
 * 
 */
public class ImageTransformServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// private static final Object LOCK = new Object();

	/**
	 * create a static logger.
	 */
	public static Logger logger = Logger.getLogger(ImageTransformServlet.class.getName());

	static int servletRun = 0;

	static final String DB_IMAGE_NAME = "_dir_cache.jpg";

	static final String MINETYPES_DIR = "/images/minetypes";

	static final String[] IMAGES_EXT = { "jpg", "jpeg", "png", "gif" };

	static final Set<String> IMAGES_EXT_SET = new TreeSet<String>(Arrays.asList(IMAGES_EXT));

	public static final String VIEW_PICTURE_ACTION = "view picture";

	private static final Object LOCK = new Object();

	private final String cacheDir = "_dc";

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		try {
			processRequest(httpServletRequest, httpServletResponse);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		try {
			processRequest(httpServletRequest, httpServletResponse);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
	}

	public long getLastModified(String name, String filter, String area, Device device, Template template) throws FileNotFoundException {
		String deviceCode = "no-device";
		if (device != null) {
			deviceCode = device.getCode();
		}
		FileCache fc = FileCache.getInstance(getServletContext());
		long lm = fc.getLastModified(ImageHelper.createSpecialDirectory(filter, area, deviceCode, template), name);
		return lm;
	}

	private void imageTransform(ContentContext ctx, ImageConfig config, StaticInfo staticInfo, String filter, String area, Template template, String realFile, File imageFile, String imageName) throws IOException {

		String fileSize = StringHelper.renderSize(imageFile.length());

		if (template != null) {
			logger.info("image transform, file:" + imageFile + " (size:" + fileSize + ") filter:" + filter + " area: " + area + " template:" + template.getName());
		} else {
			logger.info("image transform, file:" + imageFile + " (size:" + fileSize + ") filter:" + filter + " area: " + area + " template:" + Template.EDIT_TEMPLATE_CODE);
		}

		if (imageFile.isDirectory()) {
			logger.severe("file : " + imageFile + " is a directory.");
			return;
		}

		//
		logger.info("image not found in cache (generate it) : " + imageFile);
		// org.javlo.helper.Logger.stepCount("transform", "start - LOCK");
		/** only one servlet */

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation");

		int width = config.getWidth(ctx.getDevice(), filter, area);
		int height = config.getHeight(ctx.getDevice(), filter, area);

		BufferedImage layer = null;
		if (config.getLayer(ctx.getDevice(), filter, area) != null) {
			String layerName = ctx.getRequest().getSession().getServletContext().getRealPath(config.getLayer(ctx.getDevice(), filter, area));
			File layerFile = new File(layerName);
			if (layerFile.exists()) {
				layer = ImageIO.read(layerFile);
			} else {
				logger.warning("layer not found : " + layerName);
			}
		}

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 1");
		
		BufferedImage img = ImageIO.read(imageFile);

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2 (src image size : "+img.getWidth()+","+img.getHeight()+")");

		if (config.isBackGroudColor(ctx.getDevice(), filter, area) && img.getColorModel().hasAlpha()) {
			img = ImageEngine.applyBgColor(img, config.getBGColor(ctx.getDevice(), filter, area));
		}
		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.1");
		if (config.isGrayscale(ctx.getDevice(), filter, area)) {
			img = (new GrayscaleFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.2");
		if (config.isCrystallize(ctx.getDevice(), filter, area)) {
			img = (new CrystallizeFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.3");
		if (config.isEdge(ctx.getDevice(), filter, area)) {
			img = (new EdgeFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.4");
		if (config.isEmboss(ctx.getDevice(), filter, area)) {
			img = (new EmbossFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.5");
		img = ImageEngine.RBGAdjust(img, config.getAdjustColor(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.6");
		img = ImageEngine.replaceAlpha(img, config.getReplaceAlpha(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.7");

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 3");

		if (img.getType() == BufferedImage.TYPE_CUSTOM) {
			BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					newImg.setRGB(x, y, img.getRGB(x, y));
				}
			}
			img = newImg;
		}

		// resize and border
		if (layer == null) {
			if ((height > 0) && (width > 0)) {
				if (config.isFraming(ctx.getDevice(), filter, area)) {
					// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 3.1");
					if ((float) img.getWidth() / (float) width > (float) img.getHeight() / (float) height) {
						img = ImageEngine.resizeWidth(img, width);
					} else {
						img = ImageEngine.resizeHeight(img, height, config.getBGColor(ctx.getDevice(), filter, area));
					}
				} else {
					int mt = config.getMarginTop(ctx.getDevice(), filter, area);
					int ml = config.getMarginLeft(ctx.getDevice(), filter, area);
					int mr = config.getMarginRigth(ctx.getDevice(), filter, area);
					int mb = config.getMarginBottom(ctx.getDevice(), filter, area);
					// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 3.2");
					System.out.println("***** ImageTransformServlet.imageTransform : staticInfo.getFocusZoneY(ctx) = "+staticInfo.getFocusZoneY(ctx)); //TODO: remove debug trace
					img = ImageEngine.resize(img, width, height, config.isCropResize(ctx.getDevice(), filter, area), config.isAddBorder(ctx.getDevice(), filter, area), mt, ml, mr, mb, config.getBGColor(ctx.getDevice(), filter, area), staticInfo.getFocusZoneX(ctx), staticInfo.getFocusZoneY(ctx), config.isFocusZone(ctx.getDevice(), filter, area));
				}
			} else {
				int mt = config.getMarginTop(ctx.getDevice(), filter, area);
				int ml = config.getMarginLeft(ctx.getDevice(), filter, area);
				int mr = config.getMarginRigth(ctx.getDevice(), filter, area);
				int mb = config.getMarginBottom(ctx.getDevice(), filter, area);

				if (config.isCropResize(ctx.getDevice(), filter, area)) {
					// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 3.3");
					img = ImageEngine.resize(img, width, height, true, false, mt, ml, mr, mb, null, staticInfo.getFocusZoneX(ctx), staticInfo.getFocusZoneY(ctx), config.isFocusZone(ctx.getDevice(), filter, area));
				} else {
					if (width > 0) {
						// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 3.4");

						img = ImageEngine.resizeWidth(img, width, mt, ml, mr, mb, config.getBGColor(ctx.getDevice(), filter, area));
						// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 3.4.1");
					}
					if (height > 0) {
						// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 3.5");
						img = ImageEngine.resizeHeight(img, height, config.getBGColor(ctx.getDevice(), filter, area));
					}
				}
			}
		}

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 4");

		if (config.isRoundCorner(ctx.getDevice(), filter, area)) {
			img = ImageEngine.borderCorner(img, config.getBGColor(ctx.getDevice(), filter, area));
		}
		if (layer != null) {
			int mt = config.getMarginTop(ctx.getDevice(), filter, area);
			int ml = config.getMarginLeft(ctx.getDevice(), filter, area);
			int mr = config.getMarginRigth(ctx.getDevice(), filter, area);
			int mb = config.getMarginBottom(ctx.getDevice(), filter, area);
			img = ImageEngine.applyFilter(img, layer, config.isCropResize(ctx.getDevice(), filter, area), config.isAddBorder(ctx.getDevice(), filter, area), mt, ml, mr, mb, staticInfo.getFocusZoneX(ctx), staticInfo.getFocusZoneY(ctx), config.isFocusZone(ctx.getDevice(), filter, area), config.getBGColor(ctx.getDevice(), filter, area));
		}

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 5");

		if (config.isWeb2(ctx.getDevice(), filter, area)) {
			if (config.isBackGroudColor(ctx.getDevice(), filter, area)) {
				img = ImageEngine.web2(img, config.getBGColor(ctx.getDevice(), filter, area), config.getWeb2Height(ctx.getDevice(), filter, area), config.getWeb2Separation(ctx.getDevice(), filter, area));
			} else {
				img = ImageEngine.web2(img, null, config.getWeb2Height(ctx.getDevice(), filter, area), config.getWeb2Separation(ctx.getDevice(), filter, area));
			}
		}

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 6");

		/** max width and max height **/
		if (config.getMaxWidth(ctx.getDevice(), filter, area) > 0) {
			if (img.getWidth() > config.getMaxWidth(ctx.getDevice(), filter, area)) {
				img = ImageEngine.resizeWidth(img, config.getMaxWidth(ctx.getDevice(), filter, area));
			}
		}
		if (config.getMaxHeight(ctx.getDevice(), filter, area) > 0) {
			if (img.getHeight() > config.getMaxHeight(ctx.getDevice(), filter, area)) {
				img = ImageEngine.resizeHeight(img, config.getMaxHeight(ctx.getDevice(), filter, area), config.getBGColor(ctx.getDevice(), filter, area));
			}
		}

		// org.javlo.helper.Logger.stepCount("transform", "start - transformation - 7");

		if (img == null) {
			logger.severe("image : " + realFile + " could not be resized.");
		} else {
			/* create cache image */
			FileCache fc = FileCache.getInstance(getServletContext());
			String deviceCode = "no-device";
			if (ctx.getDevice() != null) {
				deviceCode = ctx.getDevice().getCode();
			}
			OutputStream outImage = fc.saveFile(ImageHelper.createSpecialDirectory(filter, area, deviceCode, template), imageName);

			try {
				String fileExtension = config.getFileExtension(ctx.getDevice(), filter, area);
				if (fileExtension == null) {
					fileExtension = (img.getColorModel().hasAlpha() ? "png" : "jpg");
				}
				logger.info("write image : " + fileExtension + " width: " + img.getWidth() + " height: " + img.getHeight());

				if (!"png".equals(fileExtension) && !"gif".equals(fileExtension)) {
					img = ImageEngine.removeAlpha(img);
				}
				if ("jpg".equals(fileExtension)) {
					ImageEngine.compressJpegFile(img, outImage, 0.9);
				} else {
					// fileExtension never null for now, see ImageConfig
					ImageIO.write(img, fileExtension, outImage);
				}
			} finally {
				outImage.close();
			}

		}

	}

	private InputStream loadFileFromDisk(String name, String filter, String area, Device device, Template template, long lastModificationDate) throws FileNotFoundException {
		String deviceCode = "no-device";
		if (device != null) {
			deviceCode = device.getCode();
		}
		FileCache fc = FileCache.getInstance(getServletContext());
		InputStream stream = fc.getFileInputStream(ImageHelper.createSpecialDirectory(filter, area, deviceCode, template), name, lastModificationDate);
		return stream;
	}

	public BufferedImage loadImageFromDisk(String name, int width, int filter) {
		File file = new File(cacheDir + "/" + ImageHelper.createSpecialDirectory(width, filter) + "/" + ImageHelper.pathToKey(name));
		BufferedImage image = null;
		if (file.exists()) {
			try {
				image = ImageIO.read(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;
	}

	/**
	 * get the text and the picture and build a button
	 * 
	 * @throws Exception
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

		// org.javlo.helper.Logger.startCount("transform");
		// org.javlo.helper.Logger.stepCount("transform", "start");

		servletRun++;

		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		ContentContext ctx = ContentContext.getContentContext(request, response);

		boolean mailing = false;

		OutputStream out = null;

		/* TRACKING */
		GlobalContext globalContext = GlobalContext.getInstance(request);
		IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
		User user = fact.getCurrentUser(request.getSession());
		String userName = null;
		if (user != null) {
			userName = user.getLogin();
		}
		try {
			Tracker tracker = Tracker.getTracker(globalContext, request.getSession());
			Track track = new Track(userName, VIEW_PICTURE_ACTION, request.getRequestURI(), System.currentTimeMillis(), request.getHeader("referer"), request.getHeader("User-Agent"));
			track.setIP(request.getRemoteAddr());
			track.setSessionId(request.getSession().getId());
			tracker.addTrack(track);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		/* END TRACKING */

		// org.javlo.helper.Logger.stepCount("transform", "end tracking");

		String pathInfo = request.getPathInfo().substring(1);
		pathInfo = pathInfo.replace('\\', '/'); // for windows server

		String dataFolder = globalContext.getDataFolder();

		String imageName = pathInfo;
		imageName = imageName.replace('\\', '/');

		logger.finest("apply fitler on image : " + imageName);		
		
		try {
			String filter = "default";
			String area = null;
			Template template = null;
			int slachIndex = pathInfo.indexOf('/');
			if (slachIndex > 0) {
				try {
					filter = pathInfo.substring(0, slachIndex);
					pathInfo = pathInfo.substring(slachIndex + 1);
					slachIndex = pathInfo.indexOf('/');
					String templateId = pathInfo.substring(0, slachIndex);
					if (templateId.startsWith(MailingContext.MAILING_TEMPLATE_PREFIX)) {
						mailing = true;
						templateId = templateId.substring(MailingContext.MAILING_TEMPLATE_PREFIX.length());
					}
					try {
						if (!Template.EDIT_TEMPLATE_CODE.equals(templateId)) {
							template = Template.getApplicationInstance(request.getSession().getServletContext(), ctx, templateId, mailing);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					/** AREA **/
					if (!filter.startsWith("template")) {
						pathInfo = pathInfo.substring(slachIndex + 1);
						slachIndex = pathInfo.indexOf('/');
						area = pathInfo.substring(0, slachIndex);
					}

					// pathInfo = pathInfo.substring(slachIndex + 1);

					imageName = pathInfo.substring(slachIndex);
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				}
			}

			// org.javlo.helper.Logger.stepCount("transform", "template");
			
			StaticInfo staticInfo = null;
			if (imageName.substring(1).startsWith(staticConfig.getShareDataFolderKey())) {
				imageName = imageName.substring(staticConfig.getShareDataFolderKey().length() + 2);
				dataFolder = staticConfig.getShareDataFolder();
				if (imageName != null) {
					staticInfo = StaticInfo.getShareInstance(ctx, imageName.replaceFirst("/static", ""));
				}
			} else {
				if (imageName != null) {
					staticInfo = StaticInfo.getInstance(ctx, imageName.replaceFirst("/static", ""));
				}
			}

			if (staticInfo != null) {
				if (globalContext.getImageViewFilter().contains(filter) && !StringHelper.isTrue(request.getParameter("no-access"))) {
					staticInfo.addAccess(ctx);
				}
			}

			ImageConfig config = ImageConfig.getInstance(globalContext, request.getSession(), template);

			String fileExtension = config.getFileExtension(ctx.getDevice(), filter, area);
			if (fileExtension == null) {
				int index = imageName.lastIndexOf('.');
				if (index > -1) {
					fileExtension = imageName.substring(index + 1);
				}
			}
			response.setContentType(ImageHelper.getImageExtensionToManType(fileExtension));
			out = response.getOutputStream();

			// org.javlo.helper.Logger.stepCount("transform", "start - check cache");

			/** * CHECK CACHE ** */
			if (imageName != null) {

				boolean returnImageDescription = false;

				if (StringHelper.getFileExtension(imageName).equalsIgnoreCase("html")) {
					returnImageDescription = true;
					imageName = imageName.substring(0, imageName.length() - ".html".length());
				}

				/** * LOCALISE IMAGE ** */
				// TODO: this version of template detection need better
				// method
				String baseFolder = URLHelper.mergePath(dataFolder, staticConfig.getStaticFolder()); //TODO: with javlo 1.4 it seem we do'nt need static folder ????
				if (filter.startsWith("template")) {
					baseFolder = getServletContext().getRealPath("");
					//filter = "template";
				}				
				
				String realFile = URLHelper.mergePath(baseFolder,imageName);
				
				File imageFile = new File(realFile);

				if (returnImageDescription) {
					StaticInfo info = StaticInfo.getInstance(ctx, imageFile);
					response.setContentType("text/html");
					response.setCharacterEncoding(ContentContext.CHARACTER_ENCODING);
					ctx.setRequestContentLanguage(filter);
					String html = "<html lang=\"" + ctx.getRequestContentLanguage() + "\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + ContentContext.CHARACTER_ENCODING + "\" /><title>" + info.getTitle(ctx) + "</title></head><body>" + info.getDescription(ctx) + "</body></html>";
					ResourceHelper.writeStringToStream(html, response.getOutputStream());
					return;
				}

				/* last modified management */
				long lastModified = getLastModified(imageName, filter, area, ctx.getDevice(), template);
				response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, lastModified);
				long lastModifiedInBrowser = request.getDateHeader(NetHelper.HEADER_IF_MODIFIED_SINCE);
				if (lastModified <= lastModifiedInBrowser) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}

				InputStream fileStream = loadFileFromDisk(imageName, filter, area, ctx.getDevice(), template, imageFile.lastModified());
				if ((fileStream != null)) {
					try {
						ResourceHelper.writeStreamToStream(fileStream, out);
					} finally {
						ResourceHelper.closeResource(fileStream);
					}
					// org.javlo.helper.Logger.stepCount("transform", "cache readed");
				} else {

					/*** TRANSFORM IMAGE ***/

					synchronized (LOCK) { // TODO: test if this synchronisation is ok
						fileStream = loadFileFromDisk(imageName, filter, area, ctx.getDevice(), template, imageFile.lastModified());
						if ((fileStream == null)) {
							imageTransform(ctx, ImageConfig.getNewInstance(globalContext, request.getSession(), template), staticInfo, filter, area, template, realFile, imageFile, imageName);
							fileStream = loadFileFromDisk(imageName, filter, area, ctx.getDevice(), template, imageFile.lastModified());
						}
					}

					/*********************/

					if ((fileStream != null)) {
						try {
							ResourceHelper.writeStreamToStream(fileStream, out);
						} finally {
							ResourceHelper.closeResource(fileStream);
						}
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warning("problem with : " + imageName + " (" + e.getMessage() + ')');
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				logger.warning(e.getMessage());
				// e.printStackTrace();
			}
		}
		servletRun--;

	}

}
