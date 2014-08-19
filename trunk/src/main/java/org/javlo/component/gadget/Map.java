/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.gadget;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class Map extends AbstractVisualComponent {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(Map.class.getName());

	public static final String TYPE = "map";

	private static boolean isJSImported(ContentContext ctx) {
		final String KEY = "_map_js_imported";
		if (ctx.getRequest().getAttribute(KEY) == null) {
			ctx.getRequest().setAttribute(KEY, KEY);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		String mapID = "maps_" + getId();
		out.println("<div id=\"" + mapID + "\" class=\"element map map-large\"></div>");
		//if (!isJSImported(ctx)) {
			//out.println("<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp&amp;sensor=false\"></script>");
		//}
		out.println("<script type=\"text/javascript\">");
		out.println("$(document).ready(function() {");
		out.println("var featureOpts = [");
		out.println("{");
		out.println("featureType: \"road.highway\",");
		out.println("elementType: \"all\",");
		out.println("stylers: [");
		out.println("{ saturation: -100 }");
		out.println("]");
		out.println("},");
		out.println("{");
		out.println("featureType: \"road.arterial\",");
		out.println("elementType: \"all\",");
		out.println("stylers: [");
		out.println("{ saturation: -100 }");
		out.println("]");
		out.println("}");
		out.println("];");

		out.println("var MY_MAPTYPE_ID = 'custom_style';");
		out.println("var mapOptions = {");
		out.println("draggable: false,");
		out.println("zoom: 17,");
		out.println("center: new google.maps.LatLng("+getValue()+"),");
		out.println("scrollwheel: false,");
		out.println("mapTypeControlOptions: {");
		out.println("mapTypeIds: [google.maps.MapTypeId.ROADMAP, MY_MAPTYPE_ID]");
		out.println("},");
		out.println("mapTypeId: MY_MAPTYPE_ID");
		out.println("};");
		out.println("map = new google.maps.Map(document.getElementById('" + mapID + "'),");
		out.println("mapOptions);");

		out.println("var styledMapOptions = {");
		out.println("name: 'Our offices'");
		out.println("};");

		out.println("var customMapType = new google.maps.StyledMapType(featureOpts, styledMapOptions);");

		out.println("map.mapTypes.set(MY_MAPTYPE_ID, customMapType);");

		out.println("var myLatLng = new google.maps.LatLng("+getValue()+");");
		out.println("var marker = new google.maps.Marker({");
		out.println("position: myLatLng,");
		out.println("map: map,");
		out.println("title: 'Hello World!',");
		out.println("icon: \"img/samples/pin.png\"");
		out.println("});");

		out.println("google.maps.event.addDomListener(window, 'load', initialize);");
		out.println(" });");
		out.println("</script>");
		
		out.close();
		return new String(outStream.toByteArray());
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

}
