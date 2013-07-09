package org.javlo.test.javlo;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;

public class TestPersistenceService extends PersistenceService {
	
	private static String data = " ";
	
	@Override
	protected MenuElement load(ContentContext ctx, int renderMode, Map<String, String> contentAttributeMap, Date timeTravelDate, boolean correctXML) throws Exception {
		Reader in = new StringReader(data);
		LoadingBean bean = load(ctx,in,contentAttributeMap, renderMode);
		in.close();
		return bean.getRoot();
	}
	
	public static void main(String[] args) {
		System.out.println(data);
		Reader in = new StringReader(data);
		try {
			NodeXML node = XMLFactory.getFirstNode(in);
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

}
