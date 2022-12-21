package org.javlo.component.image;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.owasp.encoder.Encode;

public class LottieAnimationComponent extends AbstractPropertiesComponent {

	private static final String IMPORT_LIB_HMTL = "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/bodymovin/5.10.0/lottie.min.js\" integrity=\"sha512-17otjw7eTNU9MtpB7mFfXEwB6yDjA2qjDFkKSvsywI1PRAgpyOo+cp/wPSE/1kK4fFjA9OKsZVO1tr93MGNCEw==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>";

	private static final String TYPE = "lottie-animation";

	private static final String LOOP = "loop#checkbox";

	private static final String AUTOPLAY = "autoplay#checkbox";

	private static final String ANIMATION = "animation";

	static final List<String> FIELDS = Arrays.asList(new String[] { ANIMATION, LOOP, AUTOPLAY });

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String id = "lottieAnim" + getId();
		String style = "";
		if (ctx.isPreview()) {
			style = " style=\"min-height: 5px; min-width: 5px\"";
		}
		out.println("<div id=\"" + id + "\"" + style + "></div>");

		if (ctx.getRequest().getAttribute(id) == null) {
			ctx.getRequest().setAttribute(id, "true");
			out.println(IMPORT_LIB_HMTL);
		}
		out.println("<script>");
		out.println("var jsonData = JSON.parse('" + Encode.forJavaScript(StringHelper.removeCR(StringHelper.removeDuplicateToken(getFieldValue(ANIMATION), " "))) + "');");
		out.println("var animation = lottie.loadAnimation({");
		out.println("  container: document.getElementById('" + id + "'),");
		out.println("  animationData: jsonData,");
		out.println("  renderer: 'svg',");
		out.println("  loop: " + StringHelper.isTrue(getFieldValue(LOOP)) + ",");
		out.println("  autoplay: " + StringHelper.isTrue(getFieldValue(AUTOPLAY)) + ",");
		out.println("});");
		out.println("</script>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public String getIcon() {
		return "bi bi-file-earmark-play";
	}

	public static void main(String[] args) {
		System.out.println(Encode.forJavaScript("{\"coucou\", 'coucou'}"));
	}

}
