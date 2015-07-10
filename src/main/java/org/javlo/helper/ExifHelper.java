package org.javlo.helper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.javlo.ztatic.StaticInfo.Position;

public class ExifHelper {

	public ExifHelper() {
		// TODO Auto-generated constructor stub
	}

	public static Position readPosition(File file) throws ImageReadException, IOException {
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
		return null;
	}
}
