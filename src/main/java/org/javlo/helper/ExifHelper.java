package org.javlo.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.util.IoUtils;
import org.apache.commons.io.FileUtils;
import org.javlo.ztatic.StaticInfo.Position;

public class ExifHelper {
	
	private static Logger logger = Logger.getLogger(ExifHelper.class.getName());

	public ExifHelper() {
		// TODO Auto-generated constructor stub
	}

	public static Position readPosition(File file) throws ImageReadException, IOException {
		if (!file.exists()) {
			logger.warning("file not found : "+file);
			return null;
		}
		final ImageMetadata metadata = Imaging.getMetadata(file);
		if (metadata instanceof JpegImageMetadata) {
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			if (null != exifMetadata) {
				final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
				if (null != gpsInfo) {
					final double longitude = gpsInfo.getLongitudeAsDegreesEast();
					final double latitude = gpsInfo.getLatitudeAsDegreesNorth();
					return new Position(longitude, latitude);
				}
			}
		}
		return null;
	}

	public static Date readDate(File file) throws ImageReadException, IOException {
		if (!file.exists()) {
			logger.warning("file not found : "+file);
			return null;
		}
		if (StringHelper.isJpeg(file.getName())) {
			final ImageMetadata metadata = Imaging.getMetadata(file);
			if (metadata instanceof JpegImageMetadata) {
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
				if (field != null) {
					try {
						SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
						return format.parse(field.getValue().toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public static ImageMetadata readMetadata(final File file) throws ImageReadException, IOException {
		return Imaging.getMetadata(file);
	}

	public static void setExifGPSTag(final File jpegImageFile, final File dst) throws IOException, ImageReadException, ImageWriteException {
		if (!jpegImageFile.exists()) {
			logger.warning("file not found : "+jpegImageFile);
			return;
		}
		OutputStream os = null;
		boolean canThrow = false;
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

				outputSet.setGPSInDegrees(longitude, latitude);
			}

			os = new FileOutputStream(dst);
			os = new BufferedOutputStream(os);

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
			canThrow = true;
		} finally {
			IoUtils.closeQuietly(canThrow, os);
		}
	}

	public static void changeExifMetadata(final File jpegImageFile, final File dst) throws IOException, ImageReadException, ImageWriteException {
		if (!jpegImageFile.exists()) {
			logger.warning("file not found : "+jpegImageFile);
			return;
		}
		OutputStream os = null;
		boolean canThrow = false;
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
				outputSet.setGPSInDegrees(longitude, latitude);
			}
			os = new FileOutputStream(dst);
			os = new BufferedOutputStream(os);
			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
			canThrow = true;
		} finally {
			IoUtils.closeQuietly(canThrow, os);
		}
	}

	public static void writeMetadata(ImageMetadata metadata, final File file) throws ImageWriteException, ImageReadException, IOException {
		if (!file.exists()) {
			logger.warning("file not found : "+file);
			return;
		}
		OutputStream os = null;
		boolean canThrow = false;
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
			os = new FileOutputStream(file);
			os = new BufferedOutputStream(os);
			new ExifRewriter().updateExifMetadataLossless(data, os, outputSet);
			canThrow = false;
		} finally {
			IoUtils.closeQuietly(canThrow, os);
		}
	}
}
