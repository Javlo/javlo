package org.javlo.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.javlo.helper.ResourceHelper;
import org.zwobble.mammoth.images.Image;
import org.zwobble.mammoth.images.ImageConverter;

import coza.opencollab.epub.creator.model.EpubBook;

public class EpubDocxImageConverter implements ImageConverter.ImgElement {
	
	private EpubBook book = null;
	private int imageNumber = 1;
	
	public EpubDocxImageConverter(EpubBook book) {
		this.book = book;
	}
	
	@Override
	public Map<String, String> convert(Image image) throws IOException {
		String imageName = "image-"+imageNumber+'.'+ResourceHelper.getMineTypeToFileExtension(image.getContentType());
		book.addContent(image.getInputStream(), image.getContentType(), imageName, false, false);
		imageNumber++;
		Map<String, String> attributes = new HashMap<>();
        attributes.put("src", "../"+imageName);
        return attributes;
	}
}
