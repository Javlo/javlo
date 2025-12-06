package org.javlo.helper;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileUtils;
import org.javlo.image.ImageSize;
import org.javlo.io.TransactionFile;
import org.javlo.ztatic.StaticInfo.Position;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ExifHelper {

	private static Logger logger = Logger.getLogger(ExifHelper.class.getName());

	public ExifHelper() {
		// TODO Auto-generated constructor stub
	}

	public static Position readPosition(File file) throws ImagingException, IOException {
		if (!file.exists()) {
			logger.warning("file not found : " + file);
			return null;
		}
		if (StringHelper.isJpeg(file.getName())) {
			final ImageMetadata metadata = Imaging.getMetadata(file);
			if (metadata instanceof JpegImageMetadata) {
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
				if (null != exifMetadata) {
					final TiffImageMetadata.GpsInfo gpsInfo = exifMetadata.getGpsInfo();
					if (null != gpsInfo) {
						final double longitude = gpsInfo.getLongitudeAsDegreesEast();
						final double latitude = gpsInfo.getLatitudeAsDegreesNorth();
						return new Position(longitude, latitude);
					}
				}
			}
		}
		return null;
	}

	public static Date readDate(File file) throws ImagingException, IOException {
		if (!file.exists()) {
			logger.warning("file not found : " + file);
			return null;
		}

		if (StringHelper.isJpeg(file.getName())) {
			final ImageMetadata metadata = Imaging.getMetadata(file);
			if (metadata instanceof JpegImageMetadata) {
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				final TiffField field = jpegMetadata.findExifValueWithExactMatch(
						ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);

				if (field != null) {
					String dateStr = field.getValue().toString().trim();

					// *** English comment: Try multiple possible formats ***
					String[] patterns = {
							"yyyy:MM:dd HH:mm:ss",     // Traditional EXIF
							"yyyy-MM-dd'T'HH:mm:ss"    // ISO 8601 format from your trace
					};

					for (String pattern : patterns) {
						try {
							SimpleDateFormat sdf = new SimpleDateFormat(pattern);
							return sdf.parse(dateStr);
						} catch (ParseException ignore) {
							// *** English comment: Continue testing other patterns ***
						}
					}
				}
			}
		}

		return null;
	}


	public static ImageMetadata readMetadata(final File file) throws ImagingException, IOException {
		return Imaging.getMetadata(file);
	}

	public static void setExifGPSTag(final File jpegImageFile, final File dst) throws IOException, ImagingException {
		if (!jpegImageFile.exists()) {
			logger.warning("file not found : " + jpegImageFile);
			return;
		}
		OutputStream os = null;
		try {
			TiffOutputSet outputSet = null;

			// note that metadata might be null if no metadata is found.
			final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				// note that exif might be null if no Exif metadata is found.
				final TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}
			if (null == outputSet) {
				outputSet = new TiffOutputSet();
			}

			{

				final double longitude = -74.0; // 74 degrees W (in Degrees
												// East)
				final double latitude = 40 + 43 / 60.0; // 40 degrees N (in
														// Degrees

				outputSet.setGpsInDegrees(longitude, latitude);
			}

			os = new FileOutputStream(dst);
			os = new BufferedOutputStream(os);

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
		} finally {
			ResourceHelper.closeResource(os);
			// IoUtils.closeQuietly(arg0, arg1);(arg0, arg1);(canThrow, os);
		}
	}

	public static void changeExifMetadata(final File jpegImageFile, final File dst) throws IOException, ImagingException {
		if (!jpegImageFile.exists()) {
			logger.warning("file not found : " + jpegImageFile);
			return;
		}
		OutputStream os = null;
		try {
			TiffOutputSet outputSet = null;
			final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				final TiffImageMetadata exif = jpegMetadata.getExif();
				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}
			if (null == outputSet) {
				outputSet = new TiffOutputSet();
			}
			{
				final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
				exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE, new RationalNumber(3, 10));
			}
			{
				final double longitude = -74.0;
				final double latitude = 40 + 43 / 60.0;
				outputSet.setGpsInDegrees(longitude, latitude);
			}
			os = new FileOutputStream(dst);
			os = new BufferedOutputStream(os);
			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
		} finally {
			ResourceHelper.closeResource(os);
		}
	}

	public static ImageSize getExifSize(InputStream in) throws IOException {
		Metadata metadata;
		try {
			metadata = ImageMetadataReader.readMetadata(in);
		} catch (ImageProcessingException e) {
			throw new IOException(e);
		}
		Integer width = null;
		Integer height = null;
		if (metadata != null) {
			for (Directory directory : metadata.getDirectories()) {
				for (Tag tag : directory.getTags()) {
					if (!tag.getTagName().toLowerCase().contains("exif")) {
						if (tag.getTagName().toLowerCase().contains("width")) {
							width = StringHelper.extractNumber(tag.getDescription());
						} else if (tag.getTagName().toLowerCase().contains("height")) {
							height = StringHelper.extractNumber(tag.getDescription());
						}
					}
				}
			}
		}
		if (width != null && height != null) {
			return new ImageSize(width, height);
		}
		return null;
	}

	public static ImageSize _getExifSize(InputStream in) {
		try {
			final ImageMetadata metadata = Imaging.getMetadata(in, "test.jpg");
			if (metadata instanceof JpegImageMetadata) {
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
				if (null != exifMetadata) {
					ImageSize imageSize = new ImageSize(0, 0);
					boolean width = false;
					boolean height = false;
					for (TiffField tag : jpegMetadata.getExif().getAllFields()) {
						System.out.println(">>>>>>>>> ExifHelper.getExifSize : tag.getTagName() = " + tag.getTagName() + " = " + tag.getValue()); // TODO: remove debug trace

						if (tag.getTagName().equalsIgnoreCase("ExifImageWidth")) {
							if (tag.getValue() instanceof Integer) {
								imageSize.setWidth((Integer) tag.getValue());
							} else {
								imageSize.setWidth((Short) tag.getValue());
							}
							width = true;
						} else if (tag.getTagName().equalsIgnoreCase("ExifImageLength")) {
							if (tag.getValue() instanceof Integer) {
								imageSize.setHeight((Integer) tag.getValue());
							} else {
								imageSize.setHeight((Short) tag.getValue());
							}
							height = true;
						}
						if (width && height) {
							return imageSize;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static void writeMetadata(ImageMetadata metadata, final File file) throws ImagingException, IOException {
		if (metadata == null) {
			return;
		}
		if (!file.exists()) {
			logger.warning("file not found : " + file);
			return;
		}
		TransactionFile tFile = null;
		if (!StringHelper.isJpeg(file.getName())) {
			return;
		} else {
			OutputStream os = null;
			byte[] data = FileUtils.readFileToByteArray(file);
			try {
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				TiffOutputSet outputSet = null;
				if (null != jpegMetadata) {
					final TiffImageMetadata exif = jpegMetadata.getExif();
					if (null != exif) {
						outputSet = exif.getOutputSet();
					}
				}
				if (null == outputSet) {
					outputSet = new TiffOutputSet();
				}
				tFile = new TransactionFile(file, true);
				os = new BufferedOutputStream(tFile.getOutputStream());
				new ExifRewriter().updateExifMetadataLossless(data, os, outputSet);
				tFile.commit();
			} catch (Exception e) {
				e.printStackTrace();
				if (tFile != null) {
					tFile.rollback();
				}
			} finally {
				ResourceHelper.closeResource(os);
			}
		}
	}

	public static void main(String[] args) throws ImagingException, IOException {
		File test = new File("c:/trans/gps.jpg");
		System.out.println("date : " + readDate(test));
		System.out.println("date : " + readPosition(test));
	}
}
