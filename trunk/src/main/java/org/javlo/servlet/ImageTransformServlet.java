package org.javlo.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IImageFilter;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.image.ImageConfig;
import org.javlo.image.ImageEngine;
import org.javlo.image.ImageHelper;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;

import com.jhlabs.image.ContrastFilter;
import com.jhlabs.image.CrystallizeFilter;
import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.EmbossFilter;
import com.jhlabs.image.GlowFilter;
import com.jhlabs.image.GrayscaleFilter;

/**
 * transform a image. url :
 * /transform/[filter]/[template]/[area]/[local*]|[]/uri_to_image. *local =
 * access to a file in the webapp, if not local the file come from data folder.
 * 
 * @author pvandermaesen.
 * 
 * 
 */
public class ImageTransformServlet extends HttpServlet {

	public static long COUNT_ACCESS = 0;

	public static long COUNT_304 = 0;

	private static final Object LOCK_LARGE_TRANSFORM = new Object();

	private static final String TEMP_IMAGE_FILTER = "__TEMP_IMAGE_FILTER";

	public static final String COMPONENT_ID_URL_DIR_PREFIX = "/comp-";
	
	public static final String HASH_PREFIX = "/hash-";

	public static final class ImageTransforming {
		private final File file;
		private long startTime;
		private String context;
		private long fileSize;

		public ImageTransforming(ContentContext ctx, File image) {
			startTime = System.currentTimeMillis();
			file = image;
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			context = globalContext.getContextKey();
			fileSize = image.length();
		}

		public String getName() {
			return file.getName();
		}

		public String getPath() {
			return file.getAbsolutePath();
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		public String getStartTimeText() {
			return StringHelper.renderTime(new Date(startTime));
		}

		public String getTransformTimeText() {
			return StringHelper.renderTimeInSecond(System.currentTimeMillis() - startTime);
		}

		public String getContext() {
			return context;
		}

		public void setContext(String context) {
			this.context = context;
		}

		public long getFileSize() {
			return fileSize;
		}

		public void setFileSize(long fileSize) {
			this.fileSize = fileSize;
		}

		public String getFileSizeText() {
			return StringHelper.renderSize(fileSize);
		}
	}

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

	private static final Map<String, Object> imageTransforming = new ConcurrentHashMap<String, Object>();

	@Override
	public void init() throws ServletException {
		super.init();
		getServletContext().setAttribute("imagesTransforming", imageTransforming);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest,
	 *      HttpServletResponse)
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
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest,
	 *      HttpServletResponse)
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

	public long getLastModified(ContentContext ctx, String name, String filter, String area, Device device, Template template, IImageFilter comp) throws FileNotFoundException {
		String deviceCode = "no-device";
		if (device != null) {
			deviceCode = device.getCode();
		}
		FileCache fc = FileCache.getInstance(getServletContext());
		long lm = fc.getLastModified(ImageHelper.createSpecialDirectory(ctx, ctx.getGlobalContext().getContextKey(), filter, area, deviceCode, template, comp), name);
		return lm;
	}

	private void folderTransform(ContentContext ctx, ImageConfig config, StaticInfo staticInfo, String filter, String area, Template template, IImageFilter comp, File folderFile, String imageName, String inFileExtention) throws Exception {
		logger.info("image (folder) not found in cache (generate it) : " + folderFile);

		int w = config.getFolderWidth(ctx.getDevice(), filter, area);
		int h = config.getFolderHeight(ctx.getDevice(), filter, area);

		if (w <= 0 || h <= 0) {
			logger.warning("no file defined for render folder image.");
			return;
		}

		ArrayList<StaticInfo> children = new ArrayList<StaticInfo>();
		for (File file : ResourceHelper.getAllFilesList(folderFile)) {
			if (StringHelper.isImage(file.getName())) {
				StaticInfo info = StaticInfo.getInstance(ctx, file);
				if (info.isShared(ctx)) {
					children.add(info);
				}
			}
		}
		int thumbWidth = config.getFolderThumbWidth(ctx.getDevice(), filter, area);
		int thumbHeight = config.getFolderThumbHeight(ctx.getDevice(), filter, area);		

		BufferedImage img = new BufferedImage(thumbWidth * w, thumbHeight * h, BufferedImage.TYPE_4BYTE_ABGR);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {				
				int i = (x + y * w) % children.size();
				if (config.isFolderThumbShuffle(ctx.getDevice(), filter, area) || i == 0) {
					Collections.shuffle(children);
				}				
				StaticInfo info = children.get(i);
				BufferedImage image = ImageIO.read(info.getFile());
				
				int mt = config.getMarginTop(ctx.getDevice(), filter, area);
				int ml = config.getMarginLeft(ctx.getDevice(), filter, area);
				int mr = config.getMarginRigth(ctx.getDevice(), filter, area);
				int mb = config.getMarginBottom(ctx.getDevice(), filter, area);
				
				image = ImageEngine.resize(image, thumbWidth, thumbHeight, config.isCropResize(ctx.getDevice(), filter, area), config.isAddBorder(ctx.getDevice(), filter, area), mt, ml, mr, mb, config.getBGColor(ctx.getDevice(), filter, area), info.getFocusZoneX(ctx), info.getFocusZoneY(ctx), true, config.isHighQuality(ctx.getDevice(),filter, area));
				ImageEngine.insertImage(image, img, x * thumbWidth, y * thumbHeight);
			}
		}

		if (config.isBackGroudColor(ctx.getDevice(), filter, area) && img.getColorModel().hasAlpha()) {
			img = ImageEngine.applyBgColor(img, config.getBGColor(ctx.getDevice(), filter, area));
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.1");
		if (config.isGrayscale(ctx.getDevice(), filter, area)) {
			img = (new GrayscaleFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.2");
		if (config.isCrystallize(ctx.getDevice(), filter, area)) {
			img = (new CrystallizeFilter()).filter(img, null);
		}
		if (config.isGlow(ctx.getDevice(), filter, area)) {
			img = (new GlowFilter()).filter(img, null);
		}
		float contrast = config.getConstrast(ctx.getDevice(), filter, area);
		float brightness = config.getBrightness(ctx.getDevice(), filter, area);
		if (contrast != 1 || brightness != 1) {
			ContrastFilter imageFilter = new ContrastFilter();			
			imageFilter.setContrast(contrast);
			imageFilter.setBrightness(brightness);
			img = imageFilter.filter(img, null);
		}
		int sepia = config.getSepiaIntensity(ctx.getDevice(), filter, area);
		if (sepia > 0) {
			ImageEngine.applySepiaFilter(img, sepia);
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.3");
		if (config.isEdge(ctx.getDevice(), filter, area)) {
			img = (new EdgeFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.4");
		if (config.isEmboss(ctx.getDevice(), filter, area)) {
			img = (new EmbossFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.5");
		img = ImageEngine.RBGAdjust(img, config.getAdjustColor(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.6");
		img = ImageEngine.replaceAlpha(img, config.getReplaceAlpha(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.7");
		img = ImageEngine.createAlpha(img, config.getAlpha(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 3");
		
		if (img == null) {
			logger.severe("image : " + folderFile + " could not be resized.");
		} else {
			/* create cache ima8ge */
			FileCache fc = FileCache.getInstance(getServletContext());
			String deviceCode = "no-device";
			if (ctx.getDevice() != null) {
				deviceCode = ctx.getDevice().getCode();
			}
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String dir = ImageHelper.createSpecialDirectory(ctx, globalContext.getContextKey(), filter, area, deviceCode, template, comp);

			String fileExtension = config.getFileExtension(ctx.getDevice(), filter, area);
			if (fileExtension == null) {
				fileExtension = "jpg";
			}
			OutputStream outImage = fc.saveFile(dir, imageName);

			try {
				logger.info("write image (folder) : " + fileExtension + " width: " + img.getWidth() + " height: " + img.getHeight());

				if (comp != null && StringHelper.trimAndNullify(comp.getImageFilterKey(ctx)) != null) {
					img = ((IImageFilter) comp).filterImage(ctx, img);
				}
				if (!"png".equals(fileExtension) && !"gif".equals(fileExtension)) {
					img = ImageEngine.removeAlpha(img);
				}
				ImageIO.write(img, fileExtension, outImage);

			} finally {
				outImage.close();
			}

		}
	}

	private void imageTransform(ContentContext ctx, ImageConfig config, StaticInfo staticInfo, String filter, String area, Template template, IImageFilter comp, File imageFile, String imageName, String inFileExtention) throws IOException {

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

		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation");

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

		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 1");

		BufferedImage img = ImageIO.read(imageFile);
		if (img == null) {
			logger.warning("could'nt read : " + imageFile);
			ctx.getResponse().setStatus(404);
			return;
		}
		IIOMetadata metadata = null;

		try {
			metadata = ResourceHelper.getImageMetadata(imageFile);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}

		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2 (src image size : "+img.getWidth()+","+img.getHeight()+")");

		int focusX = staticInfo.getFocusZoneX(ctx);
		int focusY = staticInfo.getFocusZoneY(ctx);

		if (config.getZoom(ctx.getDevice(), filter, area) > 1) {
			double zoom = config.getZoom(ctx.getDevice(), filter, area);
			img = ImageEngine.zoom(img, zoom, focusX, focusY);
			focusX = (int) Math.round(focusX / zoom);
			focusY = (int) Math.round(focusY / zoom);
		}

		if (config.isBackGroudColor(ctx.getDevice(), filter, area) && img.getColorModel().hasAlpha()) {
			img = ImageEngine.applyBgColor(img, config.getBGColor(ctx.getDevice(), filter, area));
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.1");
		
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.2");
		if (config.isCrystallize(ctx.getDevice(), filter, area)) {
			img = (new CrystallizeFilter()).filter(img, null);
		}
		if (config.isGlow(ctx.getDevice(), filter, area)) {
			img = (new GlowFilter()).filter(img, null);
		}
		float contrast = config.getConstrast(ctx.getDevice(), filter, area);
		float brightness = config.getBrightness(ctx.getDevice(), filter, area);		
		if (contrast != 1 || brightness != 1) {
			ContrastFilter imageFilter = new ContrastFilter();			
			imageFilter.setContrast(contrast);
			
			imageFilter.setBrightness(brightness);
			img = imageFilter.filter(img,null);
		}
		if (config.isGrayscale(ctx.getDevice(), filter, area)) {
			//img = (new GrayscaleFilter()).filter(img, null);
			img = ImageEngine.grayscale(img);
		}
		int sepia = config.getSepiaIntensity(ctx.getDevice(), filter, area);
		if (sepia > 0) {
			ImageEngine.applySepiaFilter(img, sepia);
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.3");
		if (config.isEdge(ctx.getDevice(), filter, area)) {
			img = (new EdgeFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.4");
		if (config.isEmboss(ctx.getDevice(), filter, area)) {
			img = (new EmbossFilter()).filter(img, null);
		}
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.5");
		img = ImageEngine.RBGAdjust(img, config.getAdjustColor(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.6");
		img = ImageEngine.replaceAlpha(img, config.getReplaceAlpha(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 2.7");
		img = ImageEngine.createAlpha(img, config.getAlpha(ctx.getDevice(), filter, area));
		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 3");

		if (img.getType() == BufferedImage.TYPE_CUSTOM) {
			BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					newImg.setRGB(x, y, img.getRGB(x, y));
				}
			}
			img = newImg;
		}
		
		boolean hq = config.isHighQuality(ctx.getDevice(),filter, area);

		// resize and border
		if (layer == null) {
			if ((height > 0) && (width > 0)) {
				if (config.isFraming(ctx.getDevice(), filter, area)) {
					// org.javlo.helper.Logger.stepCount("transform",
					// "start - transformation - 3.1");
					if ((float) img.getWidth() / (float) width > (float) img.getHeight() / (float) height) {
						img = ImageEngine.resizeWidth(img, width, hq);
					} else {
						img = ImageEngine.resizeHeight(img, height, config.getBGColor(ctx.getDevice(), filter, area), hq);
					}
				} else {
					int mt = config.getMarginTop(ctx.getDevice(), filter, area);
					int ml = config.getMarginLeft(ctx.getDevice(), filter, area);
					int mr = config.getMarginRigth(ctx.getDevice(), filter, area);
					int mb = config.getMarginBottom(ctx.getDevice(), filter, area);
					img = ImageEngine.resize(img, width, height, config.isCropResize(ctx.getDevice(), filter, area), config.isAddBorder(ctx.getDevice(), filter, area), mt, ml, mr, mb, config.getBGColor(ctx.getDevice(), filter, area), focusX, focusY, config.isFocusZone(ctx.getDevice(), filter, area), hq);
				}
			} else {
				int mt = config.getMarginTop(ctx.getDevice(), filter, area);
				int ml = config.getMarginLeft(ctx.getDevice(), filter, area);
				int mr = config.getMarginRigth(ctx.getDevice(), filter, area);
				int mb = config.getMarginBottom(ctx.getDevice(), filter, area);

				if (config.isCropResize(ctx.getDevice(), filter, area)) {
					// org.javlo.helper.Logger.stepCount("transform",
					// "start - transformation - 3.3");
					img = ImageEngine.resize(img, width, height, true, false, mt, ml, mr, mb, null, focusX, focusY, config.isFocusZone(ctx.getDevice(), filter, area), hq);
				} else {
					if (width > 0) {
						// org.javlo.helper.Logger.stepCount("transform",
						// "start - transformation - 3.4");

						img = ImageEngine.resizeWidth(img, width, mt, ml, mr, mb, config.getBGColor(ctx.getDevice(), filter, area), hq);
						// org.javlo.helper.Logger.stepCount("transform",
						// "start - transformation - 3.4.1");
					}
					if (height > 0) {
						// org.javlo.helper.Logger.stepCount("transform",
						// "start - transformation - 3.5");
						img = ImageEngine.resizeHeight(img, height, config.getBGColor(ctx.getDevice(), filter, area), hq);
					}
				}
			}
		}

		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 4");

		if (config.isRoundCorner(ctx.getDevice(), filter, area)) {
			img = ImageEngine.borderCorner(img, config.getBGColor(ctx.getDevice(), filter, area));
		}
		if (layer != null) {
			int mt = config.getMarginTop(ctx.getDevice(), filter, area);
			int ml = config.getMarginLeft(ctx.getDevice(), filter, area);
			int mr = config.getMarginRigth(ctx.getDevice(), filter, area);
			int mb = config.getMarginBottom(ctx.getDevice(), filter, area);
			img = ImageEngine.applyFilter(img, layer, config.isCropResize(ctx.getDevice(), filter, area), config.isAddBorder(ctx.getDevice(), filter, area), mt, ml, mr, mb, focusX, focusY, config.isFocusZone(ctx.getDevice(), filter, area), config.getBGColor(ctx.getDevice(), filter, area), hq);
		}

		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 5");

		if (config.isWeb2(ctx.getDevice(), filter, area)) {
			if (config.isBackGroudColor(ctx.getDevice(), filter, area)) {
				img = ImageEngine.web2(img, config.getBGColor(ctx.getDevice(), filter, area), config.getWeb2Height(ctx.getDevice(), filter, area), config.getWeb2Separation(ctx.getDevice(), filter, area));
			} else {
				img = ImageEngine.web2(img, null, config.getWeb2Height(ctx.getDevice(), filter, area), config.getWeb2Separation(ctx.getDevice(), filter, area));
			}
		}

		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 6");

		/** max width and max height **/
		if (config.getMaxWidth(ctx.getDevice(), filter, area) > 0) {
			if (img.getWidth() > config.getMaxWidth(ctx.getDevice(), filter, area)) {
				if (!config.isCropResize(ctx.getDevice(), filter, area)) {
					img = ImageEngine.resizeWidth(img, config.getMaxWidth(ctx.getDevice(), filter, area), hq);
				} else {
					img = ImageEngine.resize(img, config.getMaxWidth(ctx.getDevice(), filter, area), img.getHeight(), true, false, 0, 0, 0, 0, null, focusX, focusY, config.isFocusZone(ctx.getDevice(), filter, area), hq);
				}
			}
		}

		if (config.getMaxHeight(ctx.getDevice(), filter, area) > 0) {
			if (img.getHeight() > config.getMaxHeight(ctx.getDevice(), filter, area)) {
				if (!config.isCropResize(ctx.getDevice(), filter, area)) {
					img = ImageEngine.resizeHeight(img, config.getMaxHeight(ctx.getDevice(), filter, area), config.getBGColor(ctx.getDevice(), filter, area), hq);
				} else {
					img = ImageEngine.resize(img, img.getWidth(), config.getMaxHeight(ctx.getDevice(), filter, area), true, false, 0, 0, 0, 0, null, focusX, focusY, config.isFocusZone(ctx.getDevice(), filter, area), hq);
				}
			}
		}

		/** align on grid **/
		int newWidth = ImageConfig.alignToGrid(img.getWidth(), config.getGridWidth(ctx.getDevice(), filter, area));
		int newHeight = ImageConfig.alignToGrid(img.getHeight(), config.getGridHeight(ctx.getDevice(), filter, area));
		if (newWidth != img.getWidth() || newHeight != img.getHeight()) {
			img = ImageEngine.resize(img, newWidth, newHeight, true, false, 0, 0, 0, 0, null, focusX, focusY, config.isFocusZone(ctx.getDevice(), filter, area), hq);
		}

		/** dashed after resize **/
		if (config.getDashed(ctx.getDevice(), filter, area) > 1) {
			img = ImageEngine.dashed(img, config.getDashed(ctx.getDevice(), filter, area));
		}

		// org.javlo.helper.Logger.stepCount("transform",
		// "start - transformation - 7");

		if (img == null) {
			logger.severe("image : " + imageFile + " could not be resized.");
		} else {
			/* create cache image */
			FileCache fc = FileCache.getInstance(getServletContext());
			String deviceCode = "no-device";
			if (ctx.getDevice() != null) {
				deviceCode = ctx.getDevice().getCode();
			}
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String dir = ImageHelper.createSpecialDirectory(ctx, globalContext.getContextKey(), filter, area, deviceCode, template, comp);
			OutputStream outImage = fc.saveFile(dir, imageName);

			try {
				String fileExtension = config.getFileExtension(ctx.getDevice(), filter, area);
				if (fileExtension == null) {
					if (inFileExtention != null) {
						fileExtension = inFileExtention;
					} else {
						fileExtension = StringHelper.getFileExtension(imageFile.getName());
					}
				}
				logger.info("write image : " + fileExtension + " width: " + img.getWidth() + " height: " + img.getHeight());

				if (comp != null && StringHelper.trimAndNullify(comp.getImageFilterKey(ctx)) != null) {
					img = ((IImageFilter) comp).filterImage(ctx, img);
				}
				if (!"png".equals(fileExtension) && !"gif".equals(fileExtension)) {
					img = ImageEngine.removeAlpha(img);
				}
				ImageIO.write(img, fileExtension, outImage);
				if (metadata != null) {
					ResourceHelper.writeImageMetadata(metadata, fc.getFileName(dir, dir).getCanonicalFile());
				}
			} finally {
				outImage.close();
			}

		}

	}

	private File loadFileFromDisk(ContentContext ctx, String name, String filter, String area, Device device, Template template, IImageFilter comp, long lastModificationDate) throws IOException {
		String deviceCode = "no-device";
		if (device != null) {
			deviceCode = device.getCode();
		}
		FileCache fc = FileCache.getInstance(getServletContext());
		return fc.getFile(ImageHelper.createSpecialDirectory(ctx, ctx.getGlobalContext().getContextKey(), filter, area, deviceCode, template, comp), name, lastModificationDate);		
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

		COUNT_ACCESS++;

		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		ContentContext ctx = ContentContext.getFreeContentContext(request, response);
		ctx.setRenderMode(ContentContext.PREVIEW_MODE); // user for staticInfo
														// storage
		RequestHelper.traceMailingFeedBack(ctx);

		OutputStream out = null;

		/* TRACKING */
		GlobalContext globalContext = GlobalContext.getInstance(request);

		Thread.currentThread().setName("ImageTransformServlet-" + globalContext.getContextKey());

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

		String imageKey = null;

		try {
			String filter = "default";
			String area = null;
			Template template = null;
			IImageFilter comp = null;
			int slachIndex = pathInfo.indexOf('/');
			if (slachIndex > 0) {
				try {
					filter = pathInfo.substring(0, slachIndex);
					pathInfo = pathInfo.substring(slachIndex + 1);
					slachIndex = pathInfo.indexOf('/');
					String templateId = pathInfo.substring(0, slachIndex);

					/** AREA **/
					if (!filter.startsWith("template")) {
						pathInfo = pathInfo.substring(slachIndex + 1);
						slachIndex = pathInfo.indexOf('/');
						area = pathInfo.substring(0, slachIndex);
						pathInfo = pathInfo.substring(slachIndex);
					}

					if (pathInfo.startsWith(COMPONENT_ID_URL_DIR_PREFIX)) {
						slachIndex = pathInfo.indexOf('/', 1);
						String compId = pathInfo.substring(COMPONENT_ID_URL_DIR_PREFIX.length(), slachIndex);
						pathInfo = pathInfo.substring(slachIndex);
						IContentVisualComponent c = null;
						try {
							c = ContentService.getInstance(globalContext).getComponent(ctx, compId);
						} catch (Exception ex) {
							logger.log(Level.SEVERE, "Exception when retreiving component to transform image.", ex);
						}
						if (c instanceof IImageFilter) {
							comp = (IImageFilter) c;
						}
					}
					
					if (pathInfo.startsWith(HASH_PREFIX)) { // only use for browser cache
						slachIndex = pathInfo.indexOf('/', 1);
						pathInfo = pathInfo.substring(slachIndex);						
					}

					try {
						if (!Template.EDIT_TEMPLATE_CODE.equals(templateId)) {
							template = TemplateFactory.getTemplates(getServletContext()).get(templateId);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					imageName = pathInfo;
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				}
			}

			boolean localFile = false;
			StaticInfo staticInfo = null;

			if (imageName.substring(1).startsWith(staticConfig.getShareDataFolderKey())) {
				imageName = imageName.substring(staticConfig.getShareDataFolderKey().length() + 2);
				dataFolder = staticConfig.getShareDataFolder();
				if (imageName != null) {
					staticInfo = StaticInfo.getShareInstance(ctx, imageName.replaceFirst("/static", ""));
				}
			} else if (imageName.startsWith("/static")) {
				staticInfo = StaticInfo.getInstance(ctx, imageName.replaceFirst("/static", ""));
			} else if (imageName.startsWith("/local")) {
				localFile = true;
				imageName = imageName.replaceFirst("/local", "");
				staticInfo = StaticInfo.getInstance(ctx, new File(getServletContext().getRealPath(imageName)));
			} else {
				staticInfo = StaticInfo.getInstance(ctx, imageName);
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

			// org.javlo.helper.Logger.stepCount("transform",
			// "start - check cache");

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
				// String baseFolder = URLHelper.mergePath(dataFolder,
				// staticConfig.getStaticFolder()); //TODO: with javlo 1.4 it
				// seem we do'nt need static folder ????
				String baseFolder = dataFolder; // TODO: with javlo 1.4 it seem
												// we do'nt need static folder
												// ????
				if (filter.startsWith("template") || localFile) {
					baseFolder = getServletContext().getRealPath("");
				}

				File imageFile = new File(URLHelper.mergePath(baseFolder, imageName));
				String baseExtension = StringHelper.getFileExtension(imageFile.getName());

				if (!imageFile.exists() || imageFile.isDirectory()) {
					File dirFile = new File(StringHelper.getFileNameWithoutExtension(imageFile.getAbsolutePath()));
					if (!dirFile.exists()) {
						logger.warning("file not found : " + imageFile);
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					} else {
						imageFile = dirFile;
					}
				}

				if (returnImageDescription) {
					StaticInfo info = StaticInfo.getInstance(ctx, imageFile);
					response.setContentType("text/html");
					response.setCharacterEncoding(ContentContext.CHARACTER_ENCODING);
					ctx.setRequestContentLanguage(filter);
					String html = "<html lang=\"" + ctx.getRequestContentLanguage() + "\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + ContentContext.CHARACTER_ENCODING + "\" /><title>" + info.getTitle(ctx) + "</title></head><body>" + info.getDescription(ctx) + "</body></html>";
					ResourceHelper.writeStringToStream(html, response.getOutputStream(), ContentContext.CHARACTER_ENCODING);
					return;
				}

				/* last modified management */
				long lastModified = getLastModified(ctx, imageName, filter, area, ctx.getDevice(), template, comp);
				response.setHeader("Cache-Control", "public,max-age=600,s-max-age=60,must-revalidate");
				response.setHeader("Accept-Ranges", "bytes");
				response.setHeader("Transfer-Encoding", null);
				Calendar cal = Calendar.getInstance();
				cal.roll(Calendar.MINUTE, 10);
				response.setDateHeader("Expires", cal.getTimeInMillis());
				if (lastModified > 0) {
					response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, lastModified);
				}
				long lastModifiedInBrowser = request.getDateHeader(NetHelper.HEADER_IF_MODIFIED_SINCE);
				if (lastModified > 0 && lastModified / 1000 <= lastModifiedInBrowser / 1000) {
					COUNT_304++;
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
				/*String eTag = "" + lastModified;
				response.setHeader(NetHelper.HEADER_ETAG, eTag);
				String lastETag = request.getHeader(NetHelper.HEADER_IF_MODIFIED_SINCE_ETAG);
				if (lastETag != null && lastETag.equals(eTag)) {
					COUNT_304++;
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}*/

				/*
				String deviceCode = "no-device";
				if (ctx.getDevice() != null) {
					deviceCode = ctx.getDevice().getCode();
				}

				/*FileCache fc = FileCache.getInstance(getServletContext());
				String key = ImageHelper.createSpecialDirectory(ctx, globalContext.getContextKey(), filter, area, deviceCode, template, comp);
				if (fc.getFileName(key, imageName + '.' + baseExtension).exists()) {
					InputStream in = null;
					try {
						in = new FileInputStream(fc.getFileName(key, imageName));
						ResourceHelper.writeStreamToStream(in, response.getOutputStream());
					} finally {
						ResourceHelper.closeResource(in);
					}
					return;

				}*/

				InputStream fileStream = null;
				File file = loadFileFromDisk(ctx, imageName, filter, area, ctx.getDevice(), template, comp, imageFile.lastModified());				
				if ((file != null)) {
					if (file.length() > 0) {
						response.setContentLength((int)file.length());
					}
					fileStream = new FileInputStream(file);
					try {						
						ResourceHelper.writeStreamToStream(fileStream, out);
					} finally {
						ResourceHelper.closeResource(fileStream);
					}
					// org.javlo.helper.Logger.stepCount("transform",
					// "cache readed");
				} else {

					/*** TRANSFORM IMAGE ***/

					int maxWidth = staticConfig.getImageMaxWidth();
					if (maxWidth > 0) {
						synchronized (LOCK_LARGE_TRANSFORM) {
							if (!staticInfo.isResized(ctx) && !imageFile.isDirectory()) {
								logger.info("source image to large resize to " + maxWidth + " : " + imageFile);
								BufferedImage image = ImageIO.read(imageFile);
								if (image != null) {
									if (image.getWidth() > maxWidth) {
										image = ImageEngine.resizeWidth(image, maxWidth,true);
										ImageIO.write(image, StringHelper.getFileExtension(imageFile.getName().toLowerCase()), imageFile);
									}
								} else {
									logger.warning("Could'nt read image : " + imageFile);
								}
								staticInfo.setResized(ctx, true);
							}
						}
					}

					String templateId = "no-template";
					if (template != null) {
						templateId = template.getId();
					}
					imageKey = imageFile.getAbsolutePath() + '_' + filter + '_' + area + '_' + ctx.getDevice() + '_' + templateId;
					if (comp != null) {
						String compFilterKey = StringHelper.trimAndNullify(comp.getImageFilterKey(ctx));
						if (compFilterKey != null) {
							imageKey = imageKey + "_" + compFilterKey;
						}
					}

					long size = imageFile.length();
					boolean foundInSet = false;

					// synchronized (imageTransforming) {
					// if (imageTransforming.get(imageKey) != null) {
					// foundInSet = true;
					// } else {
					// imageTransforming.put(imageKey, new
					// ImageTransforming(ctx, imageFile));
					// }
					// }

					if (imageTransforming.get(imageKey) != null) {
						foundInSet = true;
					} else {
						int i = 0;
						while (imageTransforming.size() > staticConfig.getTransformingSize() && i < 10) {
							i++;
							logger.warning("too much images in transformation. Waiting [" + i + "]...");
							Thread.sleep(10000);
						}
						if (imageTransforming.size() > staticConfig.getTransformingSize()) {
							logger.severe("too much images in transformation eject image transform : " + imageKey);
							response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
							return;
						}
						imageTransforming.put(imageKey, new ImageTransforming(ctx, imageFile));
					}

					if (!foundInSet) {
						file = loadFileFromDisk(ctx, imageName, filter, area, ctx.getDevice(), template, comp, imageFile.lastModified());
						if ((file == null)) {
							long currentTime = System.currentTimeMillis();
							synchronized (imageTransforming.get(imageKey)) {
								if (imageFile.isFile()) {
									imageTransform(ctx, ImageConfig.getNewInstance(globalContext, request.getSession(), template), staticInfo, filter, area, template, comp, imageFile, imageName, baseExtension);
								} else {
									folderTransform(ctx, ImageConfig.getNewInstance(globalContext, request.getSession(), template), staticInfo, filter, area, template, comp, imageFile, imageName, baseExtension);
								}
							}
							logger.info("transform image (" + StringHelper.renderSize(size) + ") : '" + imageName + "' in site '" + globalContext.getContextKey() + "' page : " + ctx.getRequestContentLanguage() + ctx.getPath() + " time : " + StringHelper.renderTimeInSecond(System.currentTimeMillis() - currentTime) + " sec.  #transformation:" + imageTransforming.size());
							file = loadFileFromDisk(ctx, imageName, filter, area, ctx.getDevice(), template, comp, imageFile.lastModified());							
							if (file != null) {
								fileStream = new FileInputStream(file);
							}
						}
						imageTransforming.remove(imageKey);
						imageKey = null;
					} else {
						synchronized (imageTransforming.get(imageKey)) {
							file = loadFileFromDisk(ctx, imageName, filter, area, ctx.getDevice(), template, comp, imageFile.lastModified());
							fileStream= new FileInputStream(file);
							if (fileStream == null) {
								logger.severe("problem on loading from cache : " + imageFile);
							}
						}
					}

					/*********************/

					if (fileStream != null) {
						if (file.length() > 0) {
							response.setContentLength((int)file.length());
						}
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
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if (imageKey != null) {
				imageTransforming.remove(imageKey);
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				logger.warning(e.getMessage());
				e.printStackTrace();
			}
		}
		servletRun--;

	}

	public static void main(String[] args) {
		File image1 = new File("c:/trans/test.jpg");
		File image2 = new File("c:/trans/test_out.jpg");
		long time = System.currentTimeMillis();
		try {
			BufferedImage image = ImageIO.read(image1);
			System.out.println("1. read time  : " + StringHelper.renderTimeInSecond(System.currentTimeMillis() - time));
			time = System.currentTimeMillis();
			image = ImageEngine.resize(image, 1920, 1200, true);
			System.out.println("2. resize     : " + StringHelper.renderTimeInSecond(System.currentTimeMillis() - time));
			time = System.currentTimeMillis();
			ImageIO.write(image, "jpg", image2);
			System.out.println("2. write time : " + StringHelper.renderTimeInSecond(System.currentTimeMillis() - time));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
