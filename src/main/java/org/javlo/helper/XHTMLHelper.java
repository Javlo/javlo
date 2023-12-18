/*
 * Created on 07-d?c.-2003
 */
package org.javlo.helper;

import com.lowagie.text.DocumentException;
import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.context.FileContext;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.form.FormComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.ecom.Basket;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.Comparator.DoubleArrayComparator;
import org.javlo.helper.Comparator.MapEntryComparator;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.i18n.I18nAccess;
import org.javlo.i18n.RequestI18nAccess;
import org.javlo.image.ExtendedColor;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.IListItem;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.template.TemplateData;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.utils.SuffixPrefix;
import org.javlo.ztatic.StaticInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.owasp.encoder.Encode;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.ServletContext;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

/**
 * This class is a helper for construct XHTML code.
 * 
 * @author pvandermaesen
 */
public class XHTMLHelper {

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(XHTMLHelper.class.getName());

	private static final String[] TEXT_COLORS = { "#005", "#050", "#500", "#505", "#550", "#055", "#555" };

	public static final List<String> WEB_FONTS = Arrays.asList(new String[] { "Arial, sans-serif", "Courier, monospace, serif", "Myriad Pro, Myriad Pro Regular, PT Sans, sans-serif", "Times New Roman, serif", "Verdana, Geneva, sans-serif", "Open Sans, sans-serif" });

	private static String BASE64_CIRCLE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAQAAABpN6lAAAAC8npUWHRSYXcgcHJvZmlsZSB0eXBlIGV4aWYAAHja7ZdRstsqDIbfWUWXYEkIieVgMDPdQZffH0zck5z0Ifc+dcYwBgLyj9AHJAnHr589/ECi7ClENU85pQ0p5pi5oOHbmcosaYuznOkxhM9P/eEaYHQJalkvpGX/6KdL4KwKWvpVqK6B/Xkgx6XvL0JrIhkeMRptCeUlJHwO0BIo57K2lN2+LmE/zro9VuLnE0aRaOvTqyXy+jkaotcU8wjzISQbSpHlgIyHgxQ08iwTDM82waxgOC9PEJB3cboS7EIfrsa3Rk9Urha97w+vtCIvE3kJcrrqt/2B9D2VGfovM0dfLX7uNzmlwvYS/fH03rzPNWMVJSaEOq1FPZYyW7DbMcWY2gP00mZ4FBI2c0Z27OoKam2r245cKRMDV6dIjQp1OmZdqcLFyEdgQ4O5ssxOF+PMFfRI4sjU2UCyiYNindij8OULzWnzVsOczTFzI5gyQYzGvvg0h09f6H0cBaLNr1jBL+YRbLgxyI0SZiBCfQVVZ4Af+TUNrgKCOqI8jkhGYPdTYlf6cxPIBC0wVNTncSFrSwAhwtQKZ0hAANRIlHC6jNmIEEgHoALXWSLvIECq3OAkR8EpMnYeU+MVo2nKyugO6MdlBhIqSQxscNYAK0bF/rHo2ENFRaOqJjV1zVqSpJg0pWRpXIrFxGIwtWRmbtmKi0dXT27unr1kzoJLU3PKlj3nXArmLFAueLvAoJSdd9njrmFPu+2+571UbJ8aq9ZUrXrNtTRu0nB/tNSsecutHHRgKx3x0CMddviRj9Kx1bqEHrv21K17z71c1BbWb/kDarSo8SQ1DO2ihl6zhwSN60QHMwDjEAnEbSDAhubBbHOKkQe5wWzLjFOhDCd1MGs0iIFgPIi104Nd4JPoIPe/uAWLT9z4v5ILA92H5L5ze0etja+hOomdp3AEdROcPowfXtjL+LL7Voe/DXxa30K30C10C91Ct9At9A8Jdfx4GP8CfwNfrqgMVp7JkgAAASRpQ0NQSUNDIHByb2ZpbGUAAHicnZC/SsNQFMZ/rdKq2ElxEIcMrgUXM7n4B4NDobYRrE5pkmIxN4YkpfgGvok+TAdB8BF8AAVnvxsdHMzihcP343DO9917oekkoSmW98CkZe4NDkeXoyun/cYqLdp0aAVhkfWGpz615/OVhtWXrvWqn/vztKK4CKULVRpmeQmNA7E7LzPLKjZv/cGx+EHsRCaNxE/i3chElu3uwCSz8MfT3mY9Ti+Gtq/aweOMHn0cxsyYklDSlabqnOCyL/XICbinIJQmxOrNNVNyIyrk5HEk8kW6TU3edpXXV8pYHlN52YQ7jDxtHvZ/v9c+zqvNxtYiC/Kgai2pmpMJvD9CZwQbz7B2XZO18vttNTNuNfPPN34BlotQPNmMewcAAA0YaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJYTVAgQ29yZSA0LjQuMC1FeGl2MiI+CiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIKICAgIHhtbG5zOnN0RXZ0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiCiAgICB4bWxuczpkYz0iaHR0cDovL3B1cmwub3JnL2RjL2VsZW1lbnRzLzEuMS8iCiAgICB4bWxuczpHSU1QPSJodHRwOi8vd3d3LmdpbXAub3JnL3htcC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgeG1wTU06RG9jdW1lbnRJRD0iZ2ltcDpkb2NpZDpnaW1wOjc2NmY3ZTIyLTMwMTUtNGI0OS04Y2I2LThiMjI3OWZhZWE4YiIKICAgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDoyYTk4NWIzMy0xY2Y0LTRkODktOWZjYi03MzBjNzI1M2JkMWUiCiAgIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDoxMWJlYjIwZi00MGI2LTQ3YmItODI4MS1iOWM3MjgyZjQ1NDciCiAgIGRjOkZvcm1hdD0iaW1hZ2UvcG5nIgogICBHSU1QOkFQST0iMi4wIgogICBHSU1QOlBsYXRmb3JtPSJXaW5kb3dzIgogICBHSU1QOlRpbWVTdGFtcD0iMTY2MjQ0ODQwOTYwMzg4NCIKICAgR0lNUDpWZXJzaW9uPSIyLjEwLjMwIgogICB0aWZmOk9yaWVudGF0aW9uPSIxIgogICB4bXA6Q3JlYXRvclRvb2w9IkdJTVAgMi4xMCI+CiAgIDx4bXBNTTpIaXN0b3J5PgogICAgPHJkZjpTZXE+CiAgICAgPHJkZjpsaQogICAgICBzdEV2dDphY3Rpb249InNhdmVkIgogICAgICBzdEV2dDpjaGFuZ2VkPSIvIgogICAgICBzdEV2dDppbnN0YW5jZUlEPSJ4bXAuaWlkOjFkYmEwMzA5LTY5MWQtNGU1NC05MWEzLWEzNTJlMThjY2U2ZSIKICAgICAgc3RFdnQ6c29mdHdhcmVBZ2VudD0iR2ltcCAyLjEwIChXaW5kb3dzKSIKICAgICAgc3RFdnQ6d2hlbj0iMjAyMi0wOS0wNlQwOToxMzoyOSIvPgogICAgPC9yZGY6U2VxPgogICA8L3htcE1NOkhpc3Rvcnk+CiAgPC9yZGY6RGVzY3JpcHRpb24+CiA8L3JkZjpSREY+CjwveDp4bXBtZXRhPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgCjw/eHBhY2tldCBlbmQ9InciPz6iyY0yAAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAATOUAAEzlAXXO8JUAAAAHdElNRQfmCQYHDR33VWuDAAAHsklEQVR42u2dS0wWVxTHfzxE/EzFN22kUt9gBVNMK0RQuxFjk7roQk3aZbXVHbir8NHopi6q3VcraBM1rWlSn+mi3bQVa30lFQVRMBqpb3x8lE+QLoDIuXPnezHffDMXzqwmcx9z/nPvmXPOvfccGKVRGtGU5mpfY8kjnzlM5XWmMY6MgSe9dHGPDu7TShu36abPJADGUUwZJSxmHuNiKN9FM5c4xxnO0+VnADKYxwesoozXEmzhKY2c5ARX6fXb1CpiB//Q59DVxA6K/cL6BDZxhl7HmB+8evmbz8jxNvOz2MUjx1kfej3iG2Z7k/kC6ulOKvODV5gGCr0lBN+kjo/Jilquh1u00sxd2ukkRIhuYCwBAuSQz3TmM5cZZEZtKcwBgtzywpcPUMvTKN+sgyNUU8HEGABPYyIVVHOEO1FafUotgVSrUatpiSi4zlNDSQxjQ0dZlPAF5yMK1BYqXVXmBE1iDy9tX+06QRY60s8CgrTa9vOSvUxKBfsrabN5pR6Os5oxjvY2hkqO0WPTYxvvu8t8JrWEbSR0PUVJ63cR+2z6fUEwBtHpEE3mmM2XP+zQoI/8uz1oMxKOM9md//0VbfeNlLs2ApdxWvsOVyhIdtcV3Nd0/Jgt7g3AgUn4OY81b3KfimR2u5Znmk5PkZ+Sv9BMTmje5hlrk9Xheo2qG6KK9JTpIelUEbK8UzcbktHZBo38baM05ZroUs3vOOw8BGs1X/9Xcj1hhuXyq2YUrHVW9Fnn/n6yPWOJZrNfIwscE4cFGsm/M4UzXy8NvtL8ERz5KU6x/PdfEkydARLBMAtaLJMrw1eNMjVaX51nPVJBy7seG65+EtQM/jTPApCmmQi1w7P4whbRl46XKd0iDsOsTNzeb7P8+LLxOmVbfoptifkL0thraSgXP1Cu5cPtTWTarlZkaoil+IVKFQX5JZXxNhGw+Pqq8BNVKW/fHK/7tFZp4KTHhZ9VGJ5UOKiJp3qe4uh+zEz8RjMVf8FT8mKvvEdBbzN+pM0KF9/GWrFQsfxOu+ztcc5rdFqxEGO0DeoVV2c5fqVyxX26L5ZKs5XvfxA/0yFlDMyKXmW3okgW+hqAQkWZ3xWtQo6yvl+P36le2V8QZYvFJmX+F/kegCJFDmyMXPysYk2bQMcFT39FKlqsLEVXGgFApbJkH2FU7xBFWx1e4U0VjVEW13fYFczgsigYxBSSfq3LdnZNoTJUCowBoFCZ2gVDraZXtEZUusQVYwBo4pK4X6MHYLUo9AMm0Y+Ks0dD43gihsk7RgFQInh7otu0XSaK3ElwZ5dXKYsOwV+pdQpIj9+fhI0CIMwf4n6pFYASUeB3TCMJwBIVgDQWiwJnjAPgjKLzKpQtHMkvmGgcABN5MYTD5+oSz1whIm54eP0vUUpTlkvmyCkgtzm1undoyTXqo1Xc50sA5oqHVzGRripjXgAwRTy8ZyQAd8XdVAnAG+Jhu5EA3BR3uRKAqeJhp5EASK6mSQCkbhwyEoCQYvsIADLEw+dGAiC5yrSaw0M1ZxNJy1U6I5zSbYxHEykrEgDycPJ4IwGQXPVIAOQx9YCRAEiuuiQAUvfLMRKAHJ22OwhAh85QMIwkVx0SgPvi4XQjAZBcPZAASFNxvpEASK6uSQDaFVPRRIeINPnb5OOR4BLr0bnEBkdANy1CTy42DoBiYe+08J8EoI+Lovi7xgHwnri7aFWFz4kCy4wDQHJ0zlqgdGQujQ11EMjF0RKjAFgieOt85QBKH6IbnxZV1hgFgOSm8ZXtM9QcPiUKfWQUAJKbk/pCBSN9i0wzTcJTsM4YANYJPi/TbFfQ1G1y1wVf2+2LFo2IjZKLIhU+q4QlMYHi2Cpr4mbp4vg2S5u3Xb5B8PMwurvPrAMTC+M9MAGzlCMzh3wNwOH4j8yYdGiqIpFDU1BgzLG5RuX7L4i1qnpwcosvAdiS6MFJ3dFZ/60U5CtHZ58wI57q/j88fUrhYFt8DQRoVhqo9hUA1cM9Pg+VlgAKpb5hv5QuJYDCqvgbSbOIwnbfhtDYk9hCjylBVG4kHnTVGkbngA/D6KwYToO1PguktNPZQEr+D6V1dPha7GRNMLU6nwRTa3Imzqw/wulZB/895/za5ZqAit+PnICKAB9qQmr+NnJCagKs1wRVbafMA1qfLqjq+mR0pQ+ruzWlYXWr3Qur2z8RvBRYOd8SLivJgZX7xaEutHZnCkJrb9aG1r6XfAfeAppsgqtXuMZ+ueLscjG4er9q9LNteP23k957oW14/aPuhNfvH4D2CRYakrjDrIgG235r3XbdruCGbYqNE66n2FiZCikcLclKndlJVvoNkFUW36Fcir5ADSWMTaj1/jQ7FyKm2WlmVaoNswDboiZa+pef2MrymBMtLWdrTImWaoZ/tMMZ9PKo45MYdhb2cotWrnKXm3QS4jlhIIvxBMhhJrkDqbYyorYUpoEvvZFq69VM/c61ZGv7Yl/kcpdm8TUPk5xub7dX0+0N0gQ+pTEpCRfPstE/p5kWsd3BlJuX2e7HzTrpLKCKU8oe5HiuTn6hioJkGtzupN0toowlFDM/prS7IVq4xDkaueDvtLs6390M3mIOUwYSLw9q7z0DiZcfcI12bg+e5hilURqlpNP/TtnyF5dsLpIAAAAASUVORK5CYII=";

	// private static final Pattern CSS_IMPORT_PATTERN =
	// Pattern.compile("@import\\s+" +
	//
	// // optional 'url(' part (non capturing subpattern) with optional quote
	// "(?:url\\(\\s*)?" + "[\"']?" +
	//
	// // file path ending with '.?ss' in capturing subpattern 1
	// // word characters, slashes, dash, underscore, dot,
	// // colon and question mark (possible for absolute urls) are allowed
	// "([\\w\\\\/\\-_.:?]+?\\.?ss)" +
	//
	// // the rest of the line until semicolon or line break
	// "[^;$]*?(;|$)", Pattern.MULTILINE);

	public static String _textToXHTML(String text, boolean popup) {
		String res = autoLink(text);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ByteArrayInputStream inStream = new ByteArrayInputStream(res.getBytes());
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

		String prefix = "";
		String suffix = "";

		String newParagraph;
		try {
			newParagraph = in.readLine();
			while (newParagraph != null) {
				String nextParagraph = in.readLine();
				if (nextParagraph != null && nextParagraph.trim().length() == 0 && newParagraph.trim().length() > 0) {
					prefix = "</p><p>";
					suffix = "</p>";
					nextParagraph = in.readLine();
				} else {
					prefix = "";
					if (nextParagraph != null) {
						suffix = "<br />";
					} else {
						suffix = "";
					}
				}
				if (newParagraph.trim().length() > 0) {
					out.println(prefix + newParagraph + suffix);
				} else {
					if (nextParagraph != null) {
						out.println("<br />");
					}
				}
				newParagraph = nextParagraph;
			}

			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String(outStream.toByteArray()).replaceAll("  ", "&nbsp;&nbsp;");
	}

	public static boolean containsLink(String content) {
		if (content == null) {
			return false;
		} else {
			if (content.toLowerCase().contains("http://")) {
				return true;
			} else {
				return !content.equals(autoLink(content));
			}
		}
	}

	public static String autoLink(String content) {
		return autoLink(content, false, false, null);
	}

	public static String autoLinkNewWindow(String content) {
		return autoLink(content, false, true, null);
	}

	public static String autoLink(String content, boolean notfollow) {
		return autoLink(content, notfollow, false, null);
	}

	public static String autoLink(String content, GlobalContext globalContext) {
		return autoLink(content, false, false, globalContext);
	}

	private static String autoLink(String inContent, boolean notFollow, boolean newWin, GlobalContext globalContext) {
		boolean linkFound = false;
		if (inContent == null) {
			return "";
		}
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		BufferedReader reader = new BufferedReader(new StringReader(inContent));
		try {
			String line = reader.readLine();
			while (line != null) {
				String[] splitLine = StringHelper.splitStaySeparator(line, " ,()[]{}<>\n");
				boolean inLink = false;
				boolean inTag = false;
				for (String element : splitLine) {
					if (element.toLowerCase().startsWith("<")) {
						inTag = true;
					} else if (element.toLowerCase().startsWith(">")) {
						inTag = false;
					}
					if (inTag) {
						if (element.equalsIgnoreCase("a")) {
							inLink = true;
						} else if (element.toLowerCase().startsWith("/a")) {
							inLink = false;
						}
					}
					if (!inLink) {
						writer.append(createHTMLLink(element, notFollow, newWin, globalContext));
						linkFound = true;
					} else {
						writer.append(element);
					}
				}
				line = reader.readLine();
				if (line != null) {
					writer.newLine();
				}
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (linkFound) {
			return out.toString();
		} else {
			return inContent;
		}
	}

	public static String getAgnosticMessageHtml(String msg) {
		msg = Encode.forHtml(msg);
		return "<div style=\"margin: 0.5rem 0; padding: 2px 4px; background-color: rgba(255,0,0,0.1); border: rgba(255,0,0,0.8); solid 1x; border-radius: 3px;\">"+msg+"</div>";
	}

	private static String createHTMLLink(String url, boolean notFollow, boolean newWin, GlobalContext globalContext) {
		String suffix = "";
		if (url.endsWith(".")) {
			url = url.substring(0, url.length() - 1);
			suffix = ".";
		}
		String outXHTML = url;
		String target = "";
		if ((globalContext != null && globalContext.isOpenExternalLinkAsPopup(url)) || newWin) {
			target = " target=\"blank\"";
		}

		String cssClass = "auto-link";
		if (url.contains(".")) {
			cssClass = cssClass + " web file-" + StringHelper.getFileExtension(url);
		}

		String notFollowAttr = "";
		if (notFollow) {
			notFollowAttr = " rel=\"nofollow\"";
		}

		if (url.contains("@")) {
			if (PatternHelper.MAIL_PATTERN.matcher(url).matches()) {
				cssClass = "auto-link mail";
				outXHTML = "<a class=\"" + cssClass + "\" href=\"mailto:" + url.trim() + "\">" + url + "</a>";
			}
		} else if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://")) {
			url = url.replace("&nbsp;", " ").trim();
			outXHTML = "<a class=\"" + cssClass + "\" href=\"" + url + "\"" + target + "" + notFollowAttr + ">" + url + "</a>";
		} else if (url.startsWith("www.")) {
			url = url.replace("&nbsp;", " ").trim();
			outXHTML = "<a class=\"" + cssClass + "\" href=\"http://" + url + "\"" + target + "" + notFollowAttr + ">" + url + "</a>";
		}
		return outXHTML + suffix;
	}

	public static String escapeXHTML(String xhtml) {
		if (xhtml == null) {
			return "";
		}
		return StringEscapeUtils.escapeHtml4(xhtml);
	}

	public static String escapeXML(String xhtml) {
		if (xhtml == null) {
			return "";
		}
		return StringEscapeUtils.escapeXml11(xhtml);
	}

	public static String extractTitle(String xhtml) {
		int startTitle = xhtml.toLowerCase().indexOf("<title>");
		int endTitle = xhtml.toLowerCase().indexOf("</title>");
		if ((startTitle < 0) || (endTitle < 0) || (endTitle < startTitle)) {
			return null;
		} else {
			return xhtml.substring(startTitle + 7, endTitle);
		}
	}

	public static String getCheckbox(ContentContext ctx, String field, FormComponent formComponent) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");

		String addedTag = "";
		if (StringHelper.isTrue(value)) {
			addedTag = addedTag + " checked=\"checked\" ";
		}
		value = "true"; // if false there are nothing in the request

		out.print("<input class=\"in-row\" type=\"checkbox\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + "value=\"");
		out.print(value + "\"/>&nbsp;" + label);

		out.close();
		return res.toString();
	}

	public static String getCheckbox(String field, boolean value) throws ResourceNotFoundException {

		return getCheckbox(field, value, null);
	}

	public static String getCheckbox(String field, boolean value, String onchange) throws ResourceNotFoundException {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		String addedTag = "";
		if (value) {
			addedTag = addedTag + " checked=\"checked\" ";
		}
		if (!StringHelper.isEmpty(onchange)) {
			onchange = " onchange=\"" + onchange + "\"";
		} else {
			onchange = "";
		}
		out.print("<input id=\"" + field + "\" type=\"checkbox\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + " " + onchange + " />");

		out.close();
		return res.toString();
	}

	public static String getComponentPreffixSufixOneSelect(ContentContext ctx, IContentVisualComponent inComp) {
		StringBuffer outXHTML = new StringBuffer();
		if (inComp.getStyleList(ctx) != null) {
			String xhtmlID = "style_select_" + inComp.getId();
			outXHTML.append("<span class=\"select-style\"> | ");
			outXHTML.append("<label for=\"");
			outXHTML.append(xhtmlID);
			outXHTML.append("\">");
			outXHTML.append(inComp.getStyleTitle(ctx));
			outXHTML.append(" : </label>");
			String[][] listContent = new String[inComp.getStyleList(ctx).length][];
			for (int i = 0; i < listContent.length; i++) {
				listContent[i] = new String[2];
				listContent[i][0] = inComp.getStyleList(ctx)[i];
				listContent[i][1] = inComp.getStyleLabelList(ctx)[i];
			}
			outXHTML.append(getInputOneSelectInternal(xhtmlID, xhtmlID, listContent, inComp.getComponentCssClass(ctx), null, null, null, null, false));
			outXHTML.append("</span>");
		}
		return outXHTML.toString();
	}

	public static String getComponentSelectBox(ContentContext ctx, String inputName, int complexityLevel) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		List<IContentVisualComponent> comps = ComponentFactory.getGlobalContextComponent(ctx, complexityLevel);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		out.println("<select name=\"" + inputName + "\">");
		for (IContentVisualComponent comp : comps) {
			globalContext.getComponents();
			String selected = "";
			if (comp.getType().equals(editCtx.getActiveType())) {
				selected = "selected=selected ";
			}

			out.println("<option " + selected + "value=\"" + comp.getType() + "\">" + i18nAccess.getText("content." + comp.getType()) + "</option>");
		}
		out.println("</select>");
		out.close();
		return writer.toString();
	}

	/**
	 * create a drop down from a map.
	 *
	 * @param name
	 *            the name of the field.
	 * @param map
	 *            the map with keys and values.
	 * @param value
	 *            the current value of the field.
	 * @return XHTML code with a dropdown.
	 */
	public static String getDropDownFromMap(String name, Map map, String value) {
		return getDropDownFromMap(name, map, value, null, false);
	}

	public static String getDropDownFromMap(String name, Map map, String value, String emptyName, boolean sortValue) {
		return getDropDownFromMap(name, map, value, emptyName, sortValue, null);
	}

	/**
	 * create a drop down from a map.
	 *
	 * @param name
	 *            the name of the field.
	 * @param map
	 *            the map with keys and values.
	 * @param value
	 *            the current value of the field.
	 * @param emptyName
	 *            the name of the first empty element (empty as value), if null no
	 *            empty element.
	 * @return XHTML code with a dropdown.
	 */
	public static String getDropDownFromMap(String name, Map map, String value, String emptyName, boolean sortValue, String cssClass) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		if (map == null) {
			map = new HashMap();
		}

		List<Map.Entry> entriesList = new LinkedList<Map.Entry>(map.entrySet());
		if (sortValue) {
			Collections.sort(entriesList, new MapEntryComparator(true));
		}
		if (cssClass != null) {
			out.println("<select class=\"select\" id=\"" + name + "\" name=\"" + name + "\">");
		} else {
			out.println("<select class=\"select " + cssClass + "\" id=\"" + name + "\" name=\"" + name + "\">");
		}
		if (emptyName != null) {
			out.print("<option value=\"\">" + emptyName + "</option>");
		}
		for (Map.Entry entry : entriesList) {
			String key = (String) entry.getKey();
			out.print("<option");
			out.print(" value=\"");
			out.print(key);
			out.print("\"");
			if ((value != null) && (value.equals(key))) {
				out.print(" selected=\"selected\" ");
			}
			out.print(">");
			out.print(entry.getValue());
			out.println("</option>");
		}
		out.println("</select>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String getErrorMessage(ContentContext ctx, String field, GenericMessage message) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (message != null) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/error.gif");
			if (message.getType() != GenericMessage.ERROR) {
				imageURL = URLHelper.createStaticURL(ctx, "/images/valid.gif");
			}
			String msg = message.getMessage();

			out.print("<img src=\"");
			out.print(imageURL);
			out.print("\" title=\"");
			out.print(msg);
			out.print("\" alt=\"");
			out.print(msg);
			out.print("\"/>");
			out.print(msg);
		} else {
			out.print("&nbsp;");
		}

		out.close();
		return res.toString();
	}

	public static String getFileBigIcone(ContentContext ctx, String fileName) {
		String imageURL;
		String fileExt = StringHelper.getFileExtension(fileName).toLowerCase();
		String iconeName = fileExt + ".png";
		ServletContext application = ctx.getRequest().getSession().getServletContext();
		File iconeFile = new File(ResourceHelper.getRealPath(application, "/images/minetypes/64x64/" + iconeName));
		if (iconeFile.exists()) {
			imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/64x64/" + iconeName);
		} else {
			imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/64x64/unknow.png");
		}
		return imageURL;

	}

	public static String getFileIcone(ContentContext ctx, String fileName) {
		String outXHTML = "";
		String fileExt = StringHelper.getFileExtension(fileName).toLowerCase();
		if (fileExt.equals("pdf")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/pdf.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"pdf\" lang=\"en\"/>";
		} else if (fileExt.equals("zip")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/zip.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"zip\" lang=\"en\"/>";
		} else if (fileExt.equals("doc")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/doc.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"doc\" lang=\"en\"/>";
		} else if (fileExt.equals("avi") || fileExt.equals("mpg") || fileExt.equals("wmv")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/video.png");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"video\" lang=\"en\"/>";
		} else if (fileExt.equals("mp3") || fileExt.equals("wav")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/sound.png");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"sound\" lang=\"en\" />";
		} else if (fileExt.equals("ppt") || fileExt.equals("pps")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/ppt.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"MS PowerPoint\" lang=\"en\" />";
		}
		return outXHTML;
	}

	public static String getHelpAttribute(String helpMessage) {
		String helpScript = "";
		if (helpMessage != null) {
			if ((helpMessage.trim().length() > 0)) {
				helpScript = " onmouseover=\"return overlib('" + helpMessage.replaceAll("'", "\\\\'") + "');\" onmouseout=\"return nd();\"";
			}
		}
		return helpScript;
	}

	public static String getHelpLink(ContentContext ctx, String uri) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if ((uri != null) && (globalContext.isHelpLink())) {
			String url = URLHelper.mergePath(globalContext.getHelpURL(), uri);
			String imageDown = URLHelper.createStaticURL(ctx, "/images/edit/help.png");
			String imageUp = URLHelper.createStaticURL(ctx, "/images/edit/help.png");
			return "<div class=\"help-url\">" + XHTMLHelper.getImagePopupLink("help", imageDown, imageUp, url) + "</div>";
		}
		return "";
	}

	/**
	 * generate a list of navigation element. replace #id with the page id.
	 *
	 * @param startTag
	 *            insert before path sample : <option value=#id>.
	 * @param endTag
	 *            insert after path : </option>
	 * @param displayParent
	 *            false for only dispay children and not the parent.
	 * @return a string with XHTML code
	 * @throws Exception
	 */
	public static String getHTMLChildList(MenuElement elem, String selectionPath, String startTag, String startSelectedTag, String endTag, boolean displayParent) throws Exception {
		return getHTMLChildListRecursive(elem, selectionPath, startTag, startSelectedTag, endTag, 0, displayParent);
	}

	private static String getHTMLChildListRecursive(MenuElement elem, String selectionPath, String startTag, String startSelectedTag, String endTag, int depth, boolean display) throws Exception {
		StringBuffer result = new StringBuffer();

		String startTagReplaced = startTag;

		if (display) {

			String workinStartTag = startTag;
			if ((selectionPath != null) && (elem.getPath().equals(selectionPath))) {
				workinStartTag = startSelectedTag;
			}

			if (workinStartTag.indexOf("#id") >= 0) {
				startTagReplaced = workinStartTag.replaceAll("#id", elem.getId());
			}

			if (startTagReplaced.indexOf("#name") >= 0) {
				startTagReplaced = workinStartTag.replaceAll("#name", elem.getName());
			}

			result.append(startTagReplaced);
			StringBuffer currentLine = new StringBuffer();
			for (int i = 0; i < depth; i++) {
				currentLine.append("&nbsp;&nbsp;");
			}
			if (elem.getAllChildrenList().size() == 0) {
				currentLine.append(elem.getName());
			} else {
				currentLine.append(elem.getName());
			}
			result.append(currentLine.toString());
			result.append(endTag);

			depth++;

		}

		Collection<MenuElement> childs = elem.getChildMenuElements();
		for (MenuElement child : childs) {
			result.append(getHTMLChildListRecursive(child, selectionPath, startTag, startSelectedTag, endTag, depth, true));
		}
		return result.toString();
	}

	public static String getIconesCode(ContentContext ctx, String imageName, String alt) {
		return getIconesCode(ctx, imageName, alt, null);
	}

	public static String getIconesCode(ContentContext ctx, String imageName, String alt, String onclick) {
		return getIconesCode(ctx, imageName, alt, onclick, null);
	}

	public static String getIconesCode(ContentContext ctx, String imageName, String alt, String onclick, String style) {
		String url = URLHelper.createStaticURL(ctx, "/images/icones/" + imageName);
		String js = "";
		if (onclick != null) {
			js = " onclick=\"" + onclick + "\"";
		}
		String styleTag = "";
		if (style != null) {
			styleTag = " style=\"" + style + "\"";
		}

		return "<img class=\"icone\" src=\"" + url + "\" alt=\"" + alt + "\"" + styleTag + js + " />";
	}

	public static String getIconesFlag(ContentContext ctx) {
		String url = URLHelper.createStaticURL(ctx, "/images/icones/flag_lang/gif/" + ctx.getContentLanguage() + ".gif");
		return "<img class=\"icone\" src=\"" + url + "\" alt=\"" + ctx.getContentLanguage() + "\" />";
	}

	public static String getImageLabelLink(String name, String imageUp, String imageDown, String link, String label) {
		return getImageLink(name, imageUp, imageDown, link, new String[][] { { "title", label }, { "alt", label } }, false, null);
	}

	public static String getImageLink(String name, String imageUp, String imageDown, String link) {
		return getImageLink(name, imageUp, imageDown, link, new String[0][0], false, null);
	}

	public static String getImageLink(String name, String imageUp, String imageDown, String link, String js) {
		return getImageLink(name, imageUp, imageDown, link, new String[0][0], false, js);
	}

	public static String getImageLink(String name, String imageUp, String imageDown, String link, String[][] attributes) {
		return getImageLink(name, imageUp, imageDown, link, attributes, false, null);
	}

	/**
	 * @param name
	 * @param imageUp
	 * @param imageDown
	 * @param link
	 * @param attributes
	 * @param js
	 * @return
	 */
	public static String getImageLink(String name, String imageUp, String imageDown, String link, String[][] attributes, boolean popup, String js) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.print("<a href=\"");
		out.print(link);
		if (popup) {
			out.println("\" target=\"_blank\">");
		} else {
			out.println("\">");
		}
		out.print("<img src=\"");
		out.print(imageUp);
		out.print("\" onmouseover=\"this.src='");
		out.print(imageDown);
		out.print("'\" onmouseout=\"this.src='");
		out.print(imageUp);
		out.print("'\"");
		boolean altFound = false;
		for (String[] attribute : attributes) {
			if (attribute[0].trim().equalsIgnoreCase("alt")) {
				altFound = true;
			}
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		if (!altFound) {
			out.print(" alt=\"\" ");
		}
		if (js != null) {
			out.print(" onclick=\"");
			out.print(js);
			out.print("\" ");
		}
		out.print(" />");
		out.println("</a>");
		return res.toString();
	}

	public static String getImageLink(String imageUp, String imageDown, String link, String[][] attributes, String js) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.print("<div class=\"image-link\">");
		out.print("<a href=\"");
		out.print(link);
		out.println("\">");
		out.print("<img src=\"");
		out.print(imageUp);
		out.print("\" onmouseover=\"this.src='");
		out.print(imageDown);
		out.print("'\" onmouseout=\"this.src='");
		out.print(imageUp);
		out.print("'\"");
		for (String[] attribute : attributes) {
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		if (js != null) {
			out.print(" onclick=\"");
			out.print(js);
			out.print("\" ");
		}
		out.print("/>");
		out.println("</a></div>");
		return res.toString();
	}

	public static String getImagePopupLink(String name, String imageUp, String imageDown, String link) {
		return getImageLink(name, imageUp, imageDown, link, new String[0][0], true, null);
	}

	/**
	 * call .toString on all object, create String array and call the same method
	 * with array as param.
	 *
	 * @param name
	 * @param content
	 * @param value
	 * @return
	 */
	public static String getInputMultiSelect(String name, Collection content, Collection value) {

		String[] contentArray = new String[content.size()];
		Iterator contentIt = content.iterator();
		for (int i = 0; i < contentArray.length; i++) {
			contentArray[i] = contentIt.next().toString();
		}

		String[] contentValue = new String[value.size()];
		Iterator valueIt = value.iterator();
		for (int i = 0; i < contentValue.length; i++) {
			contentValue[i] = valueIt.next().toString();
		}

		return getInputMultiSelect(name, contentArray, contentValue);
	}

	public static String getInputMultiSelect(String name, Map<String, String> content, Collection value) {
		return getInputMultiSelect(name, content, value, null);
	}

	/**
	 * call .toString on all object, create String array and call the same method
	 * with array as param.
	 *
	 * @param name
	 * @param content
	 * @param value
	 * @return
	 */
	public static String getInputMultiSelect(String name, Map<String, String> content, Collection value, String cssClass) {

		String[][] contentArray = new String[content.size()][];
		Iterator entries = content.entrySet().iterator();

		for (int i = 0; i < contentArray.length; i++) {
			Map.Entry entry = (Map.Entry) entries.next();
			contentArray[i] = new String[2];
			contentArray[i][0] = (String) entry.getKey();
			contentArray[i][1] = (String) entry.getValue();
		}

		String[] contentValue = new String[0];
		if (value != null) {
			contentValue = new String[value.size()];
			Iterator valueIt = value.iterator();
			for (int i = 0; i < contentValue.length; i++) {
				contentValue[i] = valueIt.next().toString();
			}
		}

		return getInputMultiSelect(name, contentArray, contentValue, cssClass, null);
	}

	public static String getInputMultiSelect(String name, String[] content, String[] value) {
		String[][] newContent = new String[content.length][2];
		for (int i = 0; i < content.length; i++) {
			newContent[i][0] = content[i];
			newContent[i][1] = content[i];
		}
		return getInputMultiSelect(name, newContent, value, null, (String) null);
	}

	public static String getInputMultiSelect(String name, Collection<String> content, Collection<String> values, String cssClass) {
		String[][] dblContent = new String[content.size()][];
		Iterator<String> contentIt = content.iterator();
		for (int i = 0; i < content.size(); i++) {
			dblContent[i] = new String[2];
			String v = contentIt.next();
			dblContent[i][0] = v;
			dblContent[i][1] = v;
		}
		String[] contentValue = new String[0];
		if (values != null) {
			contentValue = new String[values.size()];
			Iterator valueIt = values.iterator();
			for (int i = 0; i < contentValue.length; i++) {
				contentValue[i] = valueIt.next().toString();
			}
		}
		return getInputMultiSelect(name, dblContent, contentValue, cssClass, (String) null);
	}

	public static String getInputMultiSelect(String name, String[] content, String[] values, String cssClass) {
		String[][] dblContent = new String[content.length][];
		for (int i = 0; i < content.length; i++) {
			dblContent[i] = new String[2];
			dblContent[i][0] = content[i];
			dblContent[i][1] = content[i];
		}
		return getInputMultiSelect(name, dblContent, values, cssClass, (String) null);
	}

	public static String getInputMultiSelect(String name, String[][] content, String[] values, String cssClass, String jsOnChange) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (values == null) {
			values = new String[0];
		}
		Set<String> valuesSet = new TreeSet<String>(Arrays.asList(values));

		int size = 6;
		if (content.length < 6) {
			size = content.length;
		}

		String cssClassCode = "";
		if (cssClass != null) {
			cssClassCode = " class=\"" + cssClass + "\"";
		}

		out.print("<select id=\"" + name + "\"" + cssClassCode + " size=\"" + size + "\" name=\"");
		out.print(name);
		if (jsOnChange != null) {
			out.println("\" multiple=\"multiple\" onchange=\"" + jsOnChange + "\">");
		} else {
			out.println("\" multiple=\"multiple\">");
		}
		for (String[] element : content) {
			if (valuesSet.contains(element[0])) {
				out.print("<option selected=\"selected\" value=\"");
			} else {
				out.print("<option value=\"");
			}
			out.print(element[0]);
			out.println("\">");
			out.println(element[1]);
			out.println("</option>");
		}
		out.println("</select>");
		out.close();
		return res.toString();
	}

	/**
	 * generate a one select widget in XHTML
	 *
	 * @param content
	 *            a double array with id in 0 index and label in 1 index
	 * @param value
	 * @param jsOnChange
	 * @param sorting
	 * @param jsOnClick
	 *            javascript when we click on a link (@value@ for the value of the
	 *            current line)
	 * @return
	 */
	public static String getInputMultiSelectList(ContentContext ctx, String[][] content, String value, String jsOnClick) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.println("<ul>");

		for (String[] element : content) {
			boolean selected = (value != null) && (value.equals(element[0]));
			if (selected) {
				out.print("<li class=\"selected\">");
			} else {
				out.print("<li>");
			}

			out.print("<a href=\"#\" onclick=\"" + jsOnClick.replaceAll("@value@", element[0]) + "\">");

			if (element.length > 2) {
				out.print(getIconesCode(ctx, element[2], "icone:" + element[0]));
			}
			out.print(element[1]);

			out.print("</a>");

			out.println("</li>");
		}
		out.println("</ul>");
		out.close();
		return res.toString();
	}

	public static String getInputOneSelect(String name, Collection<? extends Object> content, String value) {
		String[] contentArray = new String[content.size()];
		int i = 0;
		for (Object obj : content) {
			contentArray[i] = obj.toString();
			i++;
		}
		return getInputOneSelect(name, contentArray, value, null, true);
	}

	public static String getInputOneSelect(String name, Collection<? extends Object> content, String value, boolean sorting) {
		String[] contentArray = new String[content.size()];
		content.toArray(contentArray);
		return getInputOneSelect(name, contentArray, value, null, sorting);
	}

	public static String getInputOneSelectFirstItem(String name, Collection<? extends Object> content, String value, String firstItem, String cssClass) {
		Object[] contentArray = new Object[content.size()];
		content.toArray(contentArray);
		String[][] newContent = new String[contentArray.length][2];
		for (int i = 0; i < contentArray.length; i++) {
			newContent[i][0] = contentArray[i].toString();
			newContent[i][1] = contentArray[i].toString();
		}
		return getInputOneSelectInternal(name, name, newContent, value, firstItem, cssClass, null, null, true);
	}

	public static String getInputOneSelect(String name, Collection<? extends Object> content, String value, String cssClass) {
		Object[] contentArray = new Object[content.size()];
		content.toArray(contentArray);
		String[][] newContent = new String[contentArray.length][2];
		for (int i = 0; i < contentArray.length; i++) {
			newContent[i][0] = contentArray[i].toString();
			newContent[i][1] = contentArray[i].toString();
		}
		return getInputOneSelectInternal(name, name, newContent, value, null, cssClass, null, null, true);
	}

	public static String getInputOneSelect(String name, Collection<String> content, String value, String js, boolean sort) {
		String[] contentArray = new String[content.size()];
		content.toArray(contentArray);
		return getInputOneSelect(name, contentArray, value, js, sort);
	}

	public static String getInputOneSelect(String name, List<? extends Object> content, String value, String cssClass) {
		return getInputOneSelect(name, content, value, cssClass, null, true);
	}

	public static String getInputOneSelect(String name, List<? extends Object> content, String value, String cssClass, String js, boolean sort) {

		String[][] contentArray = new String[content.size()][];
		for (int i = 0; i < content.size(); i++) {
			contentArray[i] = new String[2];
			contentArray[i][0] = (String) content.get(i);
			contentArray[i][1] = (String) content.get(i);
		}
		return getInputOneSelectInternal(name, name, contentArray, value, null, cssClass, js, null, sort);
	}

	public static String getInputOneSelect(String name, Map<String, String> content, String value) {
		String[][] newContent = new String[content.size()][2];
		Collection<String> keys = content.keySet();
		int i = 0;
		for (String key : keys) {
			newContent[i] = new String[2];
			newContent[i][0] = key;
			newContent[i][1] = content.get(key);
			i++;
		}

		return getInputOneSelectInternal(name, name, newContent, value, null, null, null, null, true);
	}

	public static String getInputOneSelect(String name, Map<String, String> content, String value, String cssClass) {
		String[][] newContent = new String[content.size()][2];
		Collection<String> keys = content.keySet();
		int i = 0;
		for (String key : keys) {
			newContent[i] = new String[2];
			newContent[i][0] = key;
			newContent[i][1] = content.get(key);
			i++;
		}

		return getInputOneSelectInternal(name, name, newContent, value, null, cssClass, null, null, true);
	}

	public static String getInputOneSelect(String name, String[] content, String value) {
		return getInputOneSelect(name, content, value, null, true);
	}

	public static String getInputOneSelect(String name, String[] content, String value, boolean sorting) {
		return getInputOneSelect(name, content, value, null, sorting);
	}

	public static String getInputOneSelect(String name, String[] content, String value, String js, boolean sort) {
		String[][] newContent = new String[content.length][2];
		for (int i = 0; i < content.length; i++) {
			newContent[i][0] = content[i];
			newContent[i][1] = content[i];
		}
		return getInputOneSelectInternal(name, name, newContent, value, null, null, js, null, sort);
	}

	public static String getInputOneSelect(String name, String[] ids, String[] labels, String value, String js) {
		return getInputOneSelect(name, ids, labels, value, js, true);
	}

	public static String getInputOneSelect(String name, String[] ids, String[] labels, String value, String js, boolean sort) {
		String[][] newContent = new String[labels.length][2];
		for (int i = 0; i < labels.length; i++) {
			newContent[i][0] = ids[i];
			newContent[i][1] = labels[i];
		}
		return getInputOneSelectInternal(name, name, newContent, value, null, null, js, null, sort);
	}

	public static String getInputOneSelect(String name, String[] ids, String value, String cssClass, String js, boolean sort) {
		return getInputOneSelect(name, ids, ids, value, cssClass, js, sort);
	}

	public static String getInputOneSelect(String name, String[] ids, String[] labels, String value, String cssClass, String js, boolean sort) {
		String[][] newContent = new String[labels.length][2];
		for (int i = 0; i < labels.length; i++) {
			newContent[i][0] = ids[i];
			newContent[i][1] = labels[i];
		}
		return getInputOneSelectInternal(name, name, newContent, value, null, cssClass, js, null, sort);
	}

	public static String getInputOneSelect(String name, String[][] content, String value) {
		return getInputOneSelect(name, content, value, null, true);
	}

	public static String getInputOneSelect(String name, String[][] content, String value, boolean sorting) {
		return getInputOneSelect(name, content, value, null, sorting);
	}

	public static String getInputOneSelect(String name, String[][] content, String value, String jsOnChange) {
		return getInputOneSelect(name, content, value, jsOnChange, true);
	}

	/**
	 * generate a one select widget in XHTML
	 *
	 * @param name
	 *            the name of the parameter
	 * @param content
	 *            a double array with id in 0 index and label in 1 index
	 * @param value
	 * @param jsOnChange
	 * @param sorting
	 * @return
	 */
	public static String getInputOneSelect(String name, String[][] content, String value, String jsOnChange, boolean sorting) {
		return getInputOneSelectInternal(name, name, content, value, null, null, jsOnChange, null, sorting);
	}

	public static String getInputOneSelect(String name, String[][] content, String value, String jsOnChange, String popupMessage, boolean sorting) {
		return getInputOneSelectInternal(name, name, content, value, null, null, jsOnChange, popupMessage, sorting);
	}

	public static String getInputOneSelectFirstEnpty(String inputName, Collection<String> inValues, String currentValue) throws FileNotFoundException, IOException {
		return getInputOneSelectFirstEnpty(inputName, inValues, currentValue, true);
	}

	public static String getInputOneSelectFirstEnpty(String inputName, Collection<String> inValues, String currentValue, boolean sort) throws FileNotFoundException, IOException {
		String[] values = new String[inValues.size() + 1];
		values[0] = "";
		Iterator<String> iteValues = inValues.iterator();
		for (int i = 1; i < values.length; i++) {
			values[i] = iteValues.next();
		}
		return getInputOneSelect(inputName, values, currentValue, null, sort);
	}

	public static String getInputOneSelectWithClass(String name, String[][] content, String value, String cssClass) {
		return getInputOneSelectInternal(name, name, content, value, null, cssClass, null, null, true);
	}

	/**
	 * generate a one select widget in XHTML
	 *
	 * @param name
	 *            the name of the parameter
	 * @param content
	 *            a double array with id in 0 index and label in 1 index
	 * @param value
	 * @param jsOnChange
	 * @param sorting
	 * @return
	 */
	private static String getInputOneSelectInternal(String name, String id, String[][] content, String value, String firstItemLabel, String cssClass, String jsOnChange, String popupMessage, boolean sorting) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String popupScript = getHelpAttribute(popupMessage);

		if (sorting) {
			Arrays.sort(content, new DoubleArrayComparator(1));
		}

		String cssAttr = "";
		if (cssClass != null) {
			cssAttr = " class=\"" + cssClass + "\"";
		}

		if (id == null) {
			out.print("<select " + popupScript + cssAttr + " name=\"");
		} else {
			out.print("<select " + popupScript + cssAttr + " id=\"" + id + "\" name=\"");
		}
		out.print(name);
		if (jsOnChange != null) {
			out.println("\" onchange=\"" + jsOnChange + "\">");
		} else {
			out.println("\">");
		}
		if (!StringHelper.isEmpty(firstItemLabel)) {
			out.println("<option value=\"\">" + firstItemLabel + "</option>");
		}
		for (String[] element : content) {
			if ((value != null) && (value.equals(element[0]))) {
				out.print("<option selected=\"selected\" value=\"");
			} else {
				out.print("<option value=\"");
			}
			out.print(StringHelper.neverNull(element[0]));

			if (element[0] == null) {
				out.print("\" disabled=\"disabled\">");
			} else {
				out.println("\">");
			}

			out.println(element[1]);
			out.println("</option>");
		}
		out.println("</select>");
		out.close();
		return res.toString();
	}

	public static String getInputOneSelectWidthFirstElement(String name, Collection<Map.Entry<String, String>> content, String firstElement, String value, String js) {
		String[][] newContent = new String[content.size() + 1][2];
		newContent[0] = new String[2];
		newContent[0][0] = "";
		newContent[0][1] = firstElement;
		int i = 1;
		for (Map.Entry<String, String> entry : content) {
			newContent[i] = new String[2];
			newContent[i][0] = entry.getKey();
			newContent[i][1] = entry.getValue();
			i++;
		}
		return getInputOneSelectInternal(name, name, newContent, value, null, null, js, null, false);
	}

	public static String getIntegerInput(ContentContext ctx, String form, String name, int min, int max, String jsOnChange) {
		return getIntegerInput(ctx, form, name, min, max, jsOnChange, new String[0][0]);
	}

	public static String getIntegerInput(ContentContext ctx, String form, String name, int min, int max, String jsOnChange, String[][] attributes) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (jsOnChange == null) {
			jsOnChange = "";
		}

		out.print("<table style=\"padding: 0px; margin:0px; width: 55px;\"><tr><td rowspan=\"2\" style=\"padding: 0px; margin:0px; vertical-align: middle;\">");

		out.print("<input readonly=\"true\" name=\"");
		out.print(name);
		out.print("\" type=\"text\" size=\"3\" value=\"" + min + "\"");
		for (String[] attribute : attributes) {
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		out.print("/>");

		out.print("</td><td style=\"width: 55px; padding: 0px; padding-left: 1px; margin:0px;\">");

		String lessImg = URLHelper.createStaticURL(ctx, "/images/button_less.gif");
		String lessImgOn = URLHelper.createStaticURL(ctx, "/images/button_less_on.gif");

		StringBuffer js = new StringBuffer();

		js.append("document." + form + "." + name + ".value--;if ((document." + form + ".");
		js.append(name);
		js.append(".value*1)<0){document." + form + ".");
		js.append(name);
		js.append(".value=0;};");
		js.append(jsOnChange);

		String buttonLess = getImageLink("-", lessImg, lessImgOn, "javascript:;", js.toString());

		String moreImg = URLHelper.createStaticURL(ctx, "/images/button_more.gif");
		String moreImgOn = URLHelper.createStaticURL(ctx, "/images/button_more_on.gif");

		js = new StringBuffer();

		js.append("document." + form + "." + name + ".value++;if ((document." + form + ".");
		js.append(name);
		js.append(".value*1)>" + max + "){document." + form + ".");
		js.append(name);
		js.append(".value=" + max + ";};");
		js.append(jsOnChange);

		String buttonMore = getImageLink("+", moreImg, moreImgOn, "javascript:;", js.toString());

		out.print(buttonMore);
		out.print("</td></tr><tr><td style=\"padding: 0px; padding-left: 1px; padding-top: 1px; margin:0px;\">");
		out.print(buttonLess);
		out.println("</td></tr></table>");

		return res.toString();
	}

	public static String getLinkSubmit(String value) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<a href=\"javascript:this.form.submit();\">");
		out.print(value);
		out.print("</a>");

		out.close();
		return res.toString();

	}

	public static String getLinkSubmit(String formID, String value) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<a href=\"javascript:document.forms['");
		out.print(formID);
		out.print("'].submit();\">");
		out.print(value);
		out.print("</a>");

		out.close();
		return res.toString();

	}

	public static String getListedSelection(ContentContext ctx, IContentVisualComponent inComp) {
		StringBuffer outXHTML = new StringBuffer();
		if (inComp.isListable()) {
			String xhtmlID = "inlist_select_" + inComp.getId();
			outXHTML.append("<span class=\"check-style\"> | ");
			outXHTML.append("<label for=\"");
			outXHTML.append(xhtmlID);
			outXHTML.append("\">");
			try {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				outXHTML.append(i18nAccess.getText("component.inlist"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			outXHTML.append(" : </label>");
			String checked = "";
			if (inComp.isList(ctx)) {
				checked = "checked=\"checked\"";
			}
			outXHTML.append("<input type=\"checkbox\" id=\"" + xhtmlID + "\" name=\"inlist_" + inComp.getId() + "\" " + checked + "/>");
			outXHTML.append("</span>");
		}
		return outXHTML.toString();
	}

	public static String getMarkerSelect(ContentContext ctx, IContentVisualComponent inComp) throws FileNotFoundException, IOException {

		if (inComp == null || inComp.getMarkerList(ctx) == null || inComp.getMarkerList(ctx).size() < 2) {
			return "";
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		if (inComp.getMarkerList(ctx) != null) {
			out.println("<span class=\"select-style\"> | ");
			List<SuffixPrefix> sufixList = inComp.getMarkerList(ctx);
			String markerId = "marker-" + inComp.getId();
			out.print("<select id=\"" + markerId + "\">");
			out.print("<option value=\"\">" + i18nAccess.getText("component.marker.select") + "</option>");
			for (SuffixPrefix sufixPreffix : sufixList) {
				String preffix = StringUtils.replace(sufixPreffix.getPrefix(), "\"", "&quot;");
				String sufix = StringUtils.replace(sufixPreffix.getSuffix(), "\"", "&quot;");
				out.print("<option value=\"" + preffix + "|" + sufix + "\">");
				out.print(sufixPreffix.getName());
				out.println("</option>");
			}
			out.print("</select>");
			out.print("<a onclick=\"if ($('" + markerId + "').value.trim().length > 0) {insertMarker($('" + inComp.getContentName() + "'), $('" + markerId + "').value.split('|')[0], $('" + markerId + "').value.split('|')[1]);}\" href=\"#\" class=\"mark-link\">" + i18nAccess.getText("component.marker.mark") + "</a>");
			out.println("</span>");
		}
		out.close();
		return writer.toString();
	}

	public static String getRadio(String field, String radioValue, String value) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String addedTag = "";
		if (radioValue.equals(value)) {
			addedTag = addedTag + " checked=\"checked\" ";
		}

		out.print("<input id=\"" + field + radioValue + "\" type=\"radio\" name=\"");
		out.print(field);
		out.print("\" value=\"" + radioValue + "\"" + addedTag + "/>");

		out.close();
		return res.toString();
	}

	public static String getRadio(String id, String field, String value, boolean checked) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String addedTag = "";
		if (checked) {
			addedTag = addedTag + " checked=\"checked\" ";
		}

		out.print("<input id=\"" + id + "\" type=\"radio\" name=\"");
		out.print(field);
		out.print("\" value=\"" + value + "\"" + addedTag + "/>");

		out.close();
		return res.toString();
	}

	public static String getRadioInput(ContentContext ctx, FormComponent formComponent, String field, String choiceValue) {
		return getRadioInput(ctx, formComponent, field, choiceValue, null);
	}

	public static String getRadioInput(ContentContext ctx, FormComponent formComponent, String field, String choiceValue, String jsOnChange) {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<input type=\"radio\" name=\"" + field + "\" value=\"" + choiceValue + "\"");

		String fieldValue = formComponent.getValue(ctx, field, "");
		if (fieldValue.equals(choiceValue)) {
			out.print(" checked=\"checked\"");
		}

		if (jsOnChange != null) {
			out.print(" onchange=\"" + jsOnChange + "\"");
		}

		out.print(" />");

		return res.toString();
	}

	public static String getRadioInput(String field, String[] values, String value) {
		return getRadioInput(field, values, value, null);
	}

	public static String getRadioInput(String field, String[] values, String value, String jsOnChange) {
		String[][] newValue = new String[values.length][];
		for (int i = 0; i < values.length; i++) {
			newValue[i] = new String[2];
			newValue[i][0] = values[i];
			newValue[i][1] = values[i];
		}
		return getRadioInput(field, newValue, value, jsOnChange);
	}

	public static String getRadioInput(String field, String[][] values, String value, String jsOnChange) {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		for (String[] value2 : values) {

			String radioID = value2[0];
			if (radioID == null || radioID.trim().length() == 0) {
				radioID = "___bl___";
			}
			radioID = field + '_' + radioID;

			out.print("<input type=\"radio\" id=\"" + radioID + "\" name=\"" + field + "\" value=\"" + value2[0] + "\"");

			if (value2[0].equals(value)) {
				out.print(" checked=\"checked\"");
			}

			if (jsOnChange != null) {
				out.print(" onchange=\"" + jsOnChange + "\"");
			}

			out.print(" /><label class=\"radio\" for=\"" + radioID + "\">" + value2[1] + "</label>");
		}

		return res.toString();
	}

	public static String getReverlinkSelectType(ContentContext ctx, String inputName, String currentValue) throws FileNotFoundException, IOException {
		List<String> values = ReverseLinkService.LINK_TYPES;
		Map<String, String> typeSelect = new HashMap<String, String>();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		typeSelect.put(ReverseLinkService.NONE, i18nAccess.getText("global.none"));
		for (String value : values) {
			typeSelect.put(value, i18nAccess.getText("component.reverse-link." + value));
		}
		return getInputOneSelect(inputName, typeSelect, currentValue, "form-control");
	}

	public static String getRowCheckbox(ContentContext ctx, String field, String label, String value, GenericMessage message) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String addedTag = "";
		if (StringHelper.isTrue(value)) {
			addedTag = addedTag + " checked=\"checked\" ";
		}
		value = "true"; // if false there are nothing in the request

		out.print("<td class=\"label\"><input class=\"in-row\" type=\"checkbox\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + "value=\"");
		out.print(value);
		out.println("\"/>&nbsp;" + label + "</td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, message));

		out.println("</td>");

		out.close();
		return res.toString();
	}

	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent) throws FileNotFoundException, ResourceNotFoundException, IOException {

		return getRowInput(ctx, field, formComponent, "text");
	}

	/**
	 * @param request
	 * @param field
	 * @param formComponent
	 * @param inputType
	 *            if equals to "checkbox", it actually calls getRowCheckbox(...)
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent, String inputType) throws ResourceNotFoundException, FileNotFoundException, IOException {

		String result;
		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");
		GenericMessage message = formComponent.getMessage(ctx, field);

		if (inputType != null && inputType.equals("checkbox")) {
			result = getRowCheckbox(ctx, field, label, value, message);
		} else {
			result = getRowInput(ctx, field, label, value, message, inputType);
		}
		return result;
	}

	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent, String[][] content) throws ResourceNotFoundException, FileNotFoundException, IOException {

		// ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// PrintWriter out = new PrintWriter(outStream);
		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");
		GenericMessage message = formComponent.getMessage(ctx, field);
		return getRowInput(ctx, field, label, value, message, content);
	}

	public static String getRowInput(ContentContext ctx, String field, String value, GenericMessage message) throws ResourceNotFoundException {
		return getRowInput(ctx, field, field, value, message, "text");
	}

	public static String getRowInput(ContentContext ctx, String field, String value, String message) throws ResourceNotFoundException {
		return getRowInput(ctx, field, field, value, new GenericMessage(message, GenericMessage.ERROR), "text");
	}

	public static String getRowInput(ContentContext ctx, String field, String label, String value, GenericMessage message, String type) throws ResourceNotFoundException {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		String addedTag = "";
		if (type.trim().equalsIgnoreCase("checkbox")) {
			if (StringHelper.isTrue(value)) {
				addedTag = addedTag + " checked=\"checked\" ";
			}
			value = "true"; // if false there are nothing in the request
		}

		out.print("<td class=\"label\">");
		out.print(label + "&nbsp;:&nbsp;");
		out.println("</td>");
		out.print("<td class=\"in\"><input class=\"in-row\" type=\"" + type + "\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + "value=\"");
		out.print(value);
		out.println("\"/></td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, message));

		out.println("</td>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String getRowInput(ContentContext ctx, String field, String label, String value, GenericMessage message, String[][] content) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		out.print("<td class=\"label\">");
		out.print(label + " : ");
		out.println("</td>");
		out.print("<td class=\"in\">");
		out.print(getInputOneSelect(field, content, value));
		out.println("</td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, message));

		out.println("</td>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String getRowInputOneSelect(ContentContext ctx, String field, String[] content, FormComponent formComponent) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");

		String addedTag = "";
		if (StringHelper.isTrue(value)) {
			addedTag = addedTag + " selected=\"selected\" ";
		}

		String[][] contents = new String[content.length][2];
		for (int i = 0; i < content.length; i++) {
			contents[i][0] = content[i];
			contents[i][1] = content[i];
		}

		out.print("<td class=\"label\">" + label + "</td>");
		out.print("<td class=\"in\">");
		out.print(getInputOneSelect(field, contents, value, null, false));
		out.println("</td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, formComponent.getMessage(ctx, field)));

		out.print("</td>");

		out.close();
		return res.toString();
	}

	public static String getRowPassword(ContentContext ctx, String field, FormComponent formComponent) throws FileNotFoundException, ResourceNotFoundException, IOException {

		return getRowInput(ctx, field, formComponent, "password");
	}

	public static String getRowTextArea(ContentContext ctx, FormComponent formComponent, String field) throws ResourceNotFoundException {

		// TODO use getActionGroupName() instead of "form"
		String label = formComponent.getViewText(ctx, "form." + field);

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<td class=\"label\">");
		out.print(label + "&nbsp;:&nbsp;");
		out.println("</td>");

		out.print("<td class=\"in\">");
		out.print(getTextArea(ctx, formComponent, field));
		out.println("</td>");

		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, formComponent.getMessage(ctx, field)));

		out.println("</td>");

		out.flush();
		return res.toString();
	}

	public static String getSelectListFromMap(Map map, String[] currentSelection, boolean editable) {
		Set selection = null;
		if (editable) {
			selection = new TreeSet(Arrays.asList(currentSelection));
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		Iterator keys = map.keySet().iterator();
		String nextKey = (String) keys.next();
		String key = nextKey;
		out.println("<table>");
		while (keys.hasNext()) {
			key = nextKey;
			nextKey = (String) keys.next();
			out.println("<tr><td>");
			boolean title = !(nextKey.split("\\.").length <= key.split("\\.").length);
			if (!title && editable) {
				out.print("<input type=\"checkbox\" name=\"");
				out.print(key);
				if (selection.contains(key)) {
					out.print("\" checked=\"checked\"/>");
				} else {
					out.print("\"/>");
				}
			} else {
				out.print("&nbsp;");
			}
			out.print("</td><td>");
			for (int i = 0; i < key.split("\\.").length; i++) {
				out.print("&nbsp;&nbsp;");
			}
			if (title) {
				out.print("<b>");
			}
			out.print(key);
			out.print(" - ");
			out.print(map.get(key));
			if (title) {
				out.print("</b>");
			}
			out.println("</td></tr>");
		}
		key = nextKey;
		out.println("<tr><td>");
		if (editable) {
			out.print("<input type=\"checkbox\" name=\"");
			out.print(key);
			if (selection.contains(key)) {
				out.print("\" checked=\"checked\"/>");
			} else {
				out.print("\"/>");
			}
		}
		out.print("</td><td>");
		for (int i = 0; i < key.split("\\.").length; i++) {
			out.print("&nbsp;&nbsp;");
		}
		out.print(key);
		out.print(" - ");
		out.print(map.get(key));
		out.println("</td></tr>");

		out.println("</table>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String getSelectOneCountry(ContentContext ctx, String name, String country) {
		Map<String, String> countries = null;
		try {
			countries = I18nAccess.getInstance(ctx.getRequest()).getCountries(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getDropDownFromMap(name, countries, country, "", true);
	}

	public static String getStyleComponentOneSelect(ContentContext ctx, IContentVisualComponent inComp) {
		StringBuffer outXHTML = new StringBuffer();
		if (inComp.getStyleList(ctx) != null && inComp.getStyleList(ctx).length > 0) {
			String xhtmlID = "style_select_" + inComp.getId();
			outXHTML.append("<span class=\"select-style\"> | ");
			if (inComp.getStyleTitle(ctx) != null) {
				outXHTML.append("<label for=\"");
				outXHTML.append(xhtmlID);
				outXHTML.append("\">");
				outXHTML.append(inComp.getStyleTitle(ctx));
				outXHTML.append(" : </label>");
			}
			String[][] listContent = new String[inComp.getStyleList(ctx).length][];
			for (int i = 0; i < listContent.length; i++) {
				listContent[i] = new String[2];
				listContent[i][0] = inComp.getStyleList(ctx)[i];
				listContent[i][1] = inComp.getStyleLabelList(ctx)[i];
			}
			outXHTML.append(getInputOneSelectInternal(xhtmlID, xhtmlID, listContent, inComp.getComponentCssClass(ctx), null, null, null, null, false));
			outXHTML.append("</span>");
		}
		return outXHTML.toString();
	}

	public static String getTextArea(ContentContext ctx, FormComponent formComponent, String field) {
		String value = formComponent.getValue(ctx, field, "");
		return getTextArea(field, value);
	}

	public static String getTextArea(String name, String value) {
		String[][] attributes = { { "rows", "2" }, { "cols", "20" } };
		return getTextArea(name, value, attributes);
	}

	public static String getTextArea(String name, String value, String[][] attributes) {
		return getTextArea(name, value, attributes, null);
	}

	public static String getTextArea(String name, String value, String[][] attributes, String cssClass) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (!StringHelper.isEmpty(cssClass)) {
			cssClass = " class=\"" + cssClass + "\"";
		} else {
			cssClass = "";
		}

		out.print("<textarea" + cssClass + " name=\"");
		out.print(name);
		out.print("\"");
		if (attributes != null) {
			for (String[] attribute : attributes) {
				out.print(" ");
				out.print(attribute[0]);
				out.print("=\"");
				out.print(attribute[1]);
				out.print("\"");
			}
		}
		out.print(">");
		out.print(value);
		out.println("</textarea>");

		return res.toString();
	}

	public static String getTextColor(int index) {
		return TEXT_COLORS[index % TEXT_COLORS.length];
	}

	public static String getTextInput(String name, String value) {
		return getTextInput(name, value, new String[0][0], null);
	}

	public static String getTextInput(String name, String value, String cssValue) {
		return getTextInput(name, value, new String[0][0], cssValue);
	}

	public static String getTextInput(String name, String value, String[][] attributes) {
		return getTextInput(name, value, attributes, null);
	}

	private static String getTextInput(String name, String value, String[][] attributes, String cssClass) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.print("<input id=\"" + name + "\" type=\"text\" name=\"");
		out.print(name);
		out.print("\"");
		if (cssClass != null) {
			out.print(" class=\"");
			out.print(cssClass);
			out.print("\"");
		}
		if (value != null) {
			out.print(" value=\"");
			out.print(value.replace("\"", "&quot;"));
			out.print("\"");
		}
		for (String[] attribute : attributes) {
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		out.println("/>");
		return res.toString();
	}

	public static String removeTag(String html, String tag) throws BadXMLException {
		if (html == null || tag == null) {
			return html;
		}
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		for (TagDescription tag2 : tags) {
			if (tag2.getName().equalsIgnoreCase(tag)) {
				remplacement.addReplacement(tag2.getOpenStart(), tag2.getOpenEnd() + 1, "");
				remplacement.addReplacement(tag2.getCloseStart(), tag2.getCloseEnd() + 1, "");
			}
		}
		return remplacement.start(html);
	}

	public static String removeEscapeTag(String html) {
		if (html == null) {
			return null;
		} else {
			return html.replaceAll("\\&lt;(.+?)\\&gt;", "");
		}
	}

	public static String renderContentLanguage(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Set<String> languages;
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			languages = globalContext.getVisibleContentLanguages();
		} else {
			languages = globalContext.getContentLanguages();
		}
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			writer.write("<ul>");
			writer.newLine();
			for (String lg : languages) {
				ContentContext localCtx = new ContentContext(ctx);
				localCtx.setRequestContentLanguage(lg);
				localCtx.setContentLanguage(lg);
				String cssClass = "class=\"" + lg;
				if (ctx.getRequestContentLanguage().equals(lg)) {
					cssClass = cssClass + " selected\"";
				} else {
					cssClass = cssClass + "\"";
				}
				ContentService content = ContentService.getInstance(ctx.getRequest());
				try {
					String lgcode = "";
					if (!lg.equals(ctx.getLanguage())) {
						lgcode = "lang=\"" + lg + "\" ";
					}
					if (content.contentExistForContext(localCtx)) {

						Locale currentLg = ctx.getLocale();
						Locale targetLg = new Locale(lg);

						writer.write("<li " + cssClass + "><a " + lgcode + "title=\"" + targetLg.getDisplayLanguage(currentLg) + "\" href=\"" + URLHelper.createURL(localCtx) + "\"><span>" + lg + "</span></a></li>");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			writer.write("</ul>");
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderDateTime(ContentContext ctx, String name, Date date) {
		if (date == null) {
			date = new Date();
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		String dateID = "date-" + StringHelper.getRandomId();
		out.println("<div class=\"date-time\">");
		out.println("<input type=\"hidden\" id=\"" + dateID + "\" name=\"" + name + "\" />");
		out.println("<input type=\"text\" name\"date\" value=\"" + StringHelper.renderDate(date) + "\"/>");
		String[] hours = new String[24];
		for (int i = 0; i < hours.length; i++) {
			hours[i] = "" + i;
		}
		out.println(getInputOneSelect("hours", hours, "" + cal.get(Calendar.HOUR_OF_DAY), false));
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	public static boolean alreadyInserted(ContentContext ctx, String resource) {
		if (!resource.contains(".jquery.")) {
			if (resource.contains("jquery-1") || resource.contains("jquery-2") || resource.contains("jquery.min") || resource.endsWith("jquery.js")) {
				resource = "_jquery-library_";
			} else if (resource.contains("jquery-ui")) {
				resource = "_jquery-ui_";
			}
		}
		String attKey = "_ari_" + resource;
		if (ctx.getRequest().getAttribute(attKey) != null) {
			return true;
		} else {
			ctx.getRequest().setAttribute(attKey, "1");
			return false;
		}
	}

	/**
	 * return false if a tag is open but not closed.
	 *
	 * @param ctx
	 * @param resource
	 * @return true if tag is'nt closed but opened.
	 */
	public static boolean alreadyClosedIfOpen(ContentContext ctx, String resource) {
		if (!resource.contains(".jquery.")) {
			if (resource.contains("jquery-1") || resource.contains("jquery-2") || resource.contains("jquery.min") || resource.endsWith("jquery.js")) {
				resource = "_jquery-library_";
			} else if (resource.contains("jquery-ui")) {
				resource = "_jquery-ui_";
			}
		}
		String openKey = "_ari_" + resource;
		String closeKey = "_arc_" + resource;
		if (ctx.getRequest().getAttribute(openKey) != null) {
			if (ctx.getRequest().getAttribute(closeKey) != null) {
				return true;
			} else {
				ctx.getRequest().setAttribute(closeKey, "1");
				return false;
			}
		}
		return true;
	}

	public static String renderHeaderResourceInsertionWithoutalreadyTest(ContentContext ctx, String resource) {
		if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
			return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.createStaticURL(ctx, resource) + "\" />";
		} else {
			return "<script src=\"" + URLHelper.createStaticURL(ctx, resource) + "\" type=\"text/javascript\"></script>";
		}
	}

	public static String renderHeaderResourceInsertionWithoutalreadyTest(ContentContext ctx, String resource, String buildId) {
		if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
			return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.addParam(URLHelper.createStaticURL(ctx, resource), "bid", buildId) + "\" />";
		} else {
			return "<script src=\"" + URLHelper.addParam(URLHelper.createStaticURL(ctx, resource), "bid", buildId) + "\" type=\"text/javascript\"></script>";
		}
	}

	public static String renderHeaderResourceInsertion(ContentContext ctx, String resource) throws Exception {
		if (!alreadyInserted(ctx, resource)) {
			if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
				return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.createStaticURL(ctx, resource) + "\" />";
			} else {
				alreadyClosedIfOpen(ctx, resource); // close </script>
				return "<script src=\"" + URLHelper.createStaticURL(ctx, resource) + "\" type=\"text/javascript\"></script>";
			}
		} else {
			return "<!-- resource already insered : " + resource + " -->";
		}
	}

	public static String renderHeaderModuleInsertion(ContentContext ctx, String resource, String buildId) throws Exception {
		if (!alreadyInserted(ctx, resource)) {
			if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
				return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.addParam(URLHelper.createStaticURL(ctx, resource), "buid", buildId) + "\" />";
			} else {
				alreadyClosedIfOpen(ctx, resource); // close </script>
				if (StringHelper.isURL(resource)) { // cdn
					return "<script type=\"module\" src=\"" + resource + "\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>";
				} else {
					return "<script type=\"module\" src=\"" + URLHelper.addParam(URLHelper.createStaticURL(ctx, resource), "buid", buildId) + "\"></script>";
				}
			}
		} else {
			return "<!-- resource already insered : " + resource + " -->";
		}
	}

	public static String renderHeaderResourceInsertion(ContentContext ctx, String resource, String buildId) throws Exception {
		if (!alreadyInserted(ctx, resource)) {
			if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
				return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.addParam(URLHelper.createStaticURL(ctx, resource), "buid", buildId) + "\" />";
			} else {
				alreadyClosedIfOpen(ctx, resource); // close </script>
				return "<script src=\"" + URLHelper.addParam(URLHelper.createStaticURL(ctx, resource), "buid", buildId) + "\" type=\"text/javascript\"></script>";
			}
		} else {
			return "<!-- resource already insered : " + resource + " -->";
		}
	}

	public static String renderLanguage(ContentContext ctx) {
		return renderLanguage(ctx, (String) null);
	}

	public static String renderLanguage(ContentContext ctx, String cssClass) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Set<String> languages;
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			languages = globalContext.getVisibleLanguages();
		} else {
			languages = globalContext.getLanguages();
		}
		return renderLanguage(ctx, languages, cssClass);
	}

	private static String renderLanguage(ContentContext ctx, Set<String> languages, String ulCssClass) {
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			if (ulCssClass == null) {
				writer.write("<ul>");
			} else {
				writer.write("<ul class=\"" + ulCssClass + "\">");
			}
			writer.newLine();
			for (String lg : languages) {
				ContentContext localCtx = new ContentContext(ctx);
				localCtx.setLanguage(lg);
				localCtx.setContentLanguage(lg);
				localCtx.setRequestContentLanguage(lg);
				String cssClass = "class=\"" + lg;
				if (ctx.getLanguage().equals(lg)) {
					try {
						cssClass = cssClass + " " + ctx.getCurrentTemplate().getSelectedClass() + "\"";
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					cssClass = cssClass + "\"";
				}
				String lgcode = "";
				if (!lg.equals(ctx.getLanguage())) {
					lgcode = "lang=\"" + lg + "\" ";
				}
				Locale currentLg = new Locale(ctx.getRequestContentLanguage());
				Locale targetLg = new Locale(lg);
				Map<String, Object> params = null;
				try {
					if (ctx.getCurrentTemplate().isLanguageLinkKeepGetParams() && !ctx.isPostRequest()) {
						RequestService requestService = RequestService.getInstance(ctx.getRequest());
						params = requestService.getParameterMap();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				writer.write("<li " + cssClass + "><a " + lgcode + "title=\"" + targetLg.getDisplayLanguage(currentLg) + "\" href=\"" + URLHelper.createURL(localCtx, params) + "\"><span>" + lg + "</span></a></li>");
			}
			writer.write("</ul>");
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderSelectLanguage(ContentContext ctx) {
		return renderSelectLanguage(ctx, true);
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange) {
		return renderSelectLanguage(ctx, autoChange, "select_language", "select_language_submit");
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange, String selectId, String inputId) {
		return renderSelectLanguage(ctx, autoChange, selectId, inputId, true);
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange, String selectId, String inputId, boolean renderForm) {
		return renderSelectLanguage(ctx, autoChange, selectId, inputId, ctx.getLanguage(), renderForm);
	}

	public static String renderOnlySelectLangue(ContentContext ctx, String selectId, String inputName, String currentLg, boolean autoChange) {
		if (inputName == null) {
			inputName = "lg";
		}
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			if (autoChange) {
				writer.write("<select id=\"" + selectId + "\" onchange=\"document.forms['select_language_form'].submit();\" name=\"" + inputName + "\">");
			} else {
				writer.write("<select id=\"" + selectId + "\" name=\"" + inputName + "\">");
			}
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			Set<String> languages;
			if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				languages = globalContext.getVisibleLanguages();
			} else {
				languages = globalContext.getLanguages();
			}
			for (String lg : languages) {
				ContentContext localCtx = new ContentContext(ctx);
				localCtx.setLanguage(lg);
				String cssClass = "class=\"" + lg;
				if (currentLg != null && currentLg.equals(lg)) {
					cssClass = cssClass + " selected\"";
				} else {
					cssClass = cssClass + "\"";
				}
				Locale locale = new Locale(lg);
				String selected = "";
				if (currentLg != null && currentLg.equals(lg)) {
					selected = " selected=\"selected\"";
				}
				writer.write("<option lang=\"" + lg + "\" " + cssClass + " value=\"" + lg + "\"" + selected + ">" + lg + " - " + locale.getDisplayLanguage(locale) + "</option>");
			}
			writer.write("</select>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange, String selectId, String inputId, String currentLg, boolean renderForm) {
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			if (renderForm) {
				writer.write("<form id=\"select_language_form\" action=\"" + URLHelper.createURL(ctx) + "\">");
			}
			writer.write("<fieldset><legend>" + i18nAccess.getViewText("global.change-language") + "</legend>");
			writer.newLine();
			writer.write("<input type=\"hidden\" name=\"webaction\" value=\"view.language\" />");
			writer.newLine();
			writer.write(renderOnlySelectLangue(ctx, selectId, null, currentLg, autoChange));
			if (autoChange) {
				writer.write("<input id=\"" + inputId + "\" class=\"submit\" type=\"submit\" value=\"" + i18nAccess.getViewText("global.ok") + "\" />");
				writer.write("<script type=\"text/javascript\">document.getElementById('select_language_submit').style.visibility = 'hidden'; document.getElementById('select_language_submit').style.width = 0;</script>");
			} else {
				writer.newLine();
				writer.write("<input id=\"" + inputId + "\" type=\"submit\" value=\"" + i18nAccess.getContentViewText("global.ok") + "\" />");
			}
			writer.write("</fieldset>");
			if (renderForm) {
				writer.write("</form>");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderSpecialLink(ContentContext ctx, String currentLg, String multimediaFileURL, StaticInfo staticInfo) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"resource-special-links\"><a class=\"hd\" href=\"" + URLHelper.createResourceURL(ctx, multimediaFileURL) + "\" title=\"" + StringHelper.removeTag(staticInfo.getFullDescription(ctx)) + "\">");
		out.println("HD");
		out.println("</a>");
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	public static String replaceJSTLData(ContentContext ctx, String xhtml) throws Exception {

		if (xhtml == null || !xhtml.contains("${")) {
			return xhtml;
		}
		Collection<String> params = StringHelper.extractItem(xhtml, "${param.", "}");
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		for (String param : params) {
			String decodedParam = requestService.getParameter(param, "");
			try {
				decodedParam = URLDecoder.decode(decodedParam, ContentContext.CHARACTER_ENCODING);
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
			if (!StringHelper.isHTMLText(decodedParam)) {
				decodedParam = XHTMLHelper.textToXHTML(decodedParam, ctx.getGlobalContext());
			}
			xhtml = xhtml.replace("${param." + param + "}", decodedParam);
		}

		InfoBean infoBean = InfoBean.getCurrentInfoBean(ctx.getRequest());
		if (infoBean == null) {
			return xhtml;
		}
		// infoBean = InfoBean.createInfoBean(ctx);
		// Map<String, String> properties = BeanHelper.cachedDescribe(infoBean);
		// Map<String, String> properties = BeanUtils.describe(infoBean);
		params = StringHelper.extractItem(xhtml, "${" + InfoBean.REQUEST_KEY + ".", "}");
		for (String param : params) {
			String val = (String) BeanHelper.getProperty(infoBean, param);
			if (val != null) {
				xhtml = xhtml.replace("${" + InfoBean.REQUEST_KEY + "." + param + "}", val);
			}
		}

		/** i18n **/
		Collection<String> i18nKeys = StringHelper.extractItem(xhtml, "${vi18n['", "']}");
		if (i18nKeys.size() > 0) {
			RequestI18nAccess requestI18n = new RequestI18nAccess(ctx, I18nAccess.getInstance(ctx), true);
			for (String key : i18nKeys) {
				String val = requestI18n.get(key);
				xhtml = xhtml.replace("${vi18n['" + key + "']}", val);
			}
		}
		i18nKeys = StringHelper.extractItem(xhtml, "${vi18n[\"", "\"]}");
		if (i18nKeys.size() > 0) {
			RequestI18nAccess requestI18n = new RequestI18nAccess(ctx, I18nAccess.getInstance(ctx), true);
			for (String key : i18nKeys) {
				String val = requestI18n.get(key);
				xhtml = xhtml.replace("${vi18n[\"" + key + "\"]}", val);
			}
		}

		/*
		 * for (String key : properties.keySet()) { String jstlStr = "${" +
		 * InfoBean.REQUEST_KEY + '.' + key + '}'; if (properties.get(key) != null &&
		 * xhtml.contains(jstlStr)) { xhtml = xhtml.replace(jstlStr,
		 * properties.get(key).toString()); } }
		 */
		if (!xhtml.contains("${")) {
			return xhtml;
		}
		// properties = BeanHelper.cachedDescribe(infoBean.getPage());
		Map<String, String> properties = BeanUtils.describe(infoBean.getPage());
		for (String key : properties.keySet()) {
			String jstlStr = "${" + InfoBean.REQUEST_KEY + ".page." + key + '}';
			if (properties.get(key) != null && xhtml.contains(jstlStr)) {
				xhtml = xhtml.replace(jstlStr, properties.get(key).toString());
			}
		}
		if (!xhtml.contains("${")) {
			return xhtml;
		}
		if (Basket.isInstance(ctx)) {
			// properties = BeanHelper.cachedDescribe(Basket.getInstance(ctx));
			properties = BeanUtils.describe(Basket.getInstance(ctx));
			for (String key : properties.keySet()) {
				String jstlStr = "${" + Basket.KEY + '.' + key + '}';
				if (properties.get(key) != null && xhtml.contains(jstlStr)) {
					xhtml = xhtml.replace(jstlStr, properties.get(key).toString());
				}
			}
		}
		return xhtml;
	}

	public static String replaceJSTLUserInfo(String xhtml, IUserInfo userInfo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, UnsupportedEncodingException {
		// Map<String, String> properties = BeanHelper.cachedDescribe(userInfo);
		Map<String, String> properties = BeanUtils.describe(userInfo);
		for (String key : properties.keySet()) {
			String jstlStr = "${user." + key + '}';
			if (properties.get(key) != null) {
				xhtml = xhtml.replace(jstlStr, properties.get(key).toString());
			}
		}
		return xhtml;
	}

	/**
	 * replace text out test tag.
	 *
	 * @param html
	 *            a html
	 * @param token
	 *            the token to be replace
	 * @param newToken
	 *            the new token
	 * @return
	 */
	public static String replaceOutTag(String html, char token, char newToken) {
		boolean inTag = false;
		char[] content = html.toCharArray();
		for (int i = 0; i < html.length(); i++) {
			if (inTag) {
				if (content[i] == '>') {
					inTag = false;
				}
			} else {
				if (content[i] == token) {
					content[i] = newToken;
				}
				if (content[i] == '<') {
					inTag = true;
				}
			}
		}
		return new String(content);
	}

	/**
	 * replace text out test tag.
	 *
	 * @param html
	 *            a html
	 * @param token
	 *            the token to be replace
	 * @param newToken
	 *            the new token
	 * @return
	 */
	public static String replaceOutTag(String html, String token, String newToken) {
		boolean inTag = false;
		char[] content = html.toCharArray();
		int pos = html.indexOf(token);
		int tokenSize = token.length();
		StringRemplacementHelper replace = new StringRemplacementHelper();
		if (pos >= 0) {
			for (int i = 0; i < html.length(); i++) {
				if (i == pos) {
					if (!inTag) {
						replace.addReplacement(pos, pos + tokenSize, newToken);
					}
					pos = html.substring(pos + 1).indexOf(token) + pos + 1;
				}
				if (inTag) {
					if (content[i] == '>') {
						inTag = false;
					}
				} else {
					if (content[i] == '<') {
						inTag = true;
					}
				}
			}
		}
		return replace.start(html);
	}

	public static final String stringToAttribute(String str) {
		if (str == null) {
			return null;
		}
		return escapeXHTML(str.replace("\"", "&quot;"));
	}

	public static String textToXHTML(String text) {
		return textToXHTML(text, false, null, (GlobalContext) null);
	}

	public static String textToXHTMLWidthParagraph(String text) {
		return textToXHTML(text, false, false, null, (GlobalContext) null, true);
	}

	public static void main(String[] args) throws MalformedURLException, Exception {
		String testHtml = "<div class=\"col-lg-3 col-md-12 footer-contact text-center text-md-start\">\n" +
				"\t\t\t<h4>Contact Us</h4>\n" +
				"<p><strong>HOTREC AISBL</strong><br>\n" +
				"              <span class=\"less\">7955861942-03<br>\n" +
				"                BE 0451258945</span><br>\n" +
				"\t\t\t\t\t36-38 Rue Dautzenberg<br> B-1050 Brussels<br> Belgium <br> <br> <strong>Phone:</strong>\n" +
				"  \t\t\t\t\t<a class=\"white-link\" href=\"tel:003225136323\">+32 2 513 63 23</a><br> <strong>Email:</strong>\n" +
				"  <a class=\"white-link\" href=\"mailto:info@hotrec.eu\">info@hotrec.eu</a></p>\n" +
				"\t</div>";

		testHtml = minimizeHtml(replaceLinks(null, testHtml));

		System.out.println(testHtml);
	}

	public static String textToXHTMLNewWin(String text) {
		return textToXHTML(text, false, true, null, (GlobalContext) null, false);
	}

	public static String textToXHTML(String text, boolean notFollow) {
		return textToXHTML(text, notFollow, false, null, (GlobalContext) null, false);
	}

	public static String textToXHTML(String text, GlobalContext globalContext) {
		return textToXHTML(text, false, false, null, globalContext, false);
	}

	public static String textToXHTMLWidthParagraph(String text, GlobalContext globalContext) {
		return textToXHTML(text, false, false, null, globalContext, true);
	}

	public static String textToXHTML(String text, boolean notFollow, GlobalContext globalContext) {
		return textToXHTML(text, notFollow, false, null, globalContext, false);
	}

	public static String textToXHTML(String text, boolean notFollow, String cssClass, GlobalContext globalContext) {
		return textToXHTML(text, notFollow, false, cssClass, globalContext, false);
	}

	// cssClass and popup not used
	private static String textToXHTML(String text, boolean notFollow, boolean newWin, String cssClass, GlobalContext globalContext, boolean paragraph) {

		String separation = "<br />";
		if (paragraph) {
			separation = "<p>";
		}

		String res = autoLink(text, notFollow, newWin, globalContext);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ByteArrayInputStream inStream = new ByteArrayInputStream(res.getBytes());
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

		try {
			String p = in.readLine();
			if (p != null) {
				if (paragraph) {
					out.print("<p>");
				}
				out.print(p);
			}

			for (p = in.readLine(); p != null; p = in.readLine()) {
				out.print(separation);
				out.print(p);
				if (paragraph) {
					separation = "</p><p>";
				}
			}

			if (paragraph) {
				out.print("</p>");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}

		String content = new String(outStream.toByteArray());
		return content.replaceAll("  ", "&nbsp;&nbsp;");
	}

	public static String textToXHTMLP(String text) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		BufferedReader reader = new BufferedReader(new CharArrayReader(text.toCharArray()));
		String line;
		try {
			line = reader.readLine();
			while (line != null) {
				out.println("<p>" + line + "</p>");
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String textToXHTMLDIV(String text) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		BufferedReader reader = new BufferedReader(new CharArrayReader(text.toCharArray()));
		String line;
		try {
			line = reader.readLine();
			int i = 0;
			while (line != null) {
				i++;
				out.println("<div class=\"line-" + i + "\">" + line + "</div>");
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String renderLine(String label, String inputName, String value) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + inputName + "\">" + label + "</label>");
		out.println("<input class=\"form-control\" type=\"text\" id=\"" + inputName + "\" name=\"" + inputName + "\" value=\"" + value + "\" />");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String renderLine(String label, String value) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label>" + label + "</label>");
		out.println("<div class=\"value\">" + value + "</div>");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String renderLine(String label, String inputName, boolean checked) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + inputName + "\">" + label + "</label>");
		String chechedHTML = "";
		if (checked) {
			chechedHTML = " checked=\"checked\"";
		}
		out.println("<input class=\"form-control\" type=\"checkbox\" id=\"" + inputName + "\" name=\"" + inputName + "\"" + chechedHTML + " />");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String extractBody(String content) {
		String lowerContent = content.toLowerCase();
		int startBody = lowerContent.indexOf("<body");
		if (startBody >= 0) {
			startBody = lowerContent.indexOf(">", startBody + 3);
			int endBody = lowerContent.indexOf("</body");
			if (startBody >= 0 && endBody >= startBody) {
				return content.substring(startBody + 1, endBody);
			}
		}
		return content;

	}

	public static List<String> extractUnsedLinks(String html, boolean absolute) {

		List<String> outLinks = new LinkedList<>();

		Document doc = Jsoup.parse(html, "UTF-8");

		Elements hrefs = doc.select("link[href]");
		Elements srcs = doc.select("[src]");

		for (int i = 0; i < hrefs.size(); i++) {
			Element href = hrefs.get(i);
			String lnk = href.attr("href");
			if (!absolute || StringHelper.isURL(lnk)) {
				outLinks.add(lnk);
			}
		}

		for (int i = 0; i < srcs.size(); i++) {
			Element src = srcs.get(i);
			String lnk = src.attr("src");
			if (!absolute || StringHelper.isURL(lnk)) {
				outLinks.add(lnk);
			}
		}

		return outLinks;

	}

	/**
	 * transform a value to a span with key as class and value inside.
	 *
	 * @param list
	 * @param key
	 * @return the key if list null, and empty string if key not found in the list.
	 */
	public static String renderListItem(List<IListItem> list, String key) {
		if (list == null) {
			return key;
		}
		for (IListItem item : list) {
			if (item.getKey().equals(key)) {
				return "<span class=\"" + item.getKey() + "\">" + item.getValue() + "</span>";
			}
		}
		return "";
	}

	/**
	 * transform a value to a span with key as class and value inside.
	 *
	 * @param list
	 * @param key
	 * @return the key if list null, and empty string if key not found in the list.
	 */
	public static String renderMultiListItem(List<IListItem> list, Collection<String> keys) {
		if (list == null) {
			return StringHelper.collectionToString(keys, ";");
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<ul>");
		for (IListItem item : list) {
			if (keys.contains(item.getKey())) {
				out.println("<li class=\"" + item.getKey() + "\">" + item.getValue() + "</li>");
			}
		}
		out.println("</ul>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String renderUserData(ContentContext ctx, User user) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<span class=\"user-data\">");
		String avatar = URLHelper.createAvatarUrl(ctx, user.getUserInfo());
		if (avatar != null) {
			out.println("<img class=\"avatar\" src=\"" + avatar + "\">");
		}
		out.println("<span class=\"fullname\">");
		out.println("<span class=\"lastname\">");
		out.println(user.getUserInfo().getLastName());
		out.println("</span>");
		out.println("<span class=\"firstname\">");
		out.println(user.getUserInfo().getFirstName());
		out.println("</span>");
		out.println("</span>");
		out.println("<span class=\"email\">");
		out.println(user.getUserInfo().getEmail());
		out.println("</span>");
		out.println("</span>");
		out.close();
		return writer.toString();
	}

	private XHTMLHelper() {
	}

	public static String safeHTML(String html) {
		return Jsoup.clean(html, Safelist.relaxed());
	}

	// public static void expandCSSImports(File css) throws IOException {
	// String expandedCSS;
	// try {
	// expandedCSS = expandCSSIncludesToString(css);
	// } catch (IOException ex) {
	// logger.log(Level.WARNING, "Expand CSS imports failed for '" + css + "'.",
	// ex);
	// return; // Don't write on error, let the original as it is.
	// }
	// ResourceHelper.writeStringToFile(css, expandedCSS);
	// }

	// public static String expandCSSIncludesToString(File css) throws IOException {
	// String content = ResourceHelper.loadStringFromFile(css);
	// Matcher m = CSS_IMPORT_PATTERN.matcher(content);
	// StringBuffer sb = new StringBuffer();
	// while (m.find()) {
	// String fileName = m.group(1);
	// if (!fileName.contains("/") && !fileName.contains("\\")) {
	// File importedFile = new File(css.getParentFile(), fileName);
	// if (importedFile.exists()) {
	// m.appendReplacement(sb, "");
	// sb.append("/* START " + fileName + " */\r\n");
	// sb.append(expandCSSIncludesToString(importedFile));
	// sb.append("/* END " + fileName + " */");
	// continue;
	// }
	// }
	// m.appendReplacement(sb, "$0");
	// }
	// m.appendTail(sb);
	// return sb.toString();
	// }

	/**
	 * replace link in xhtml with createURL call.
	 *
	 * @param ctx
	 * @param content
	 * @return
	 * @throws Exception
	 * @link Deprecated
	 */
	public static String replaceLinkSearchAllTag(ContentContext ctx, String content) throws Exception {
		String outContent = content;
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(outContent, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();

		boolean script = false;
		int closeScriptPosition = -1;

		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("script")) {
				script = true;
				closeScriptPosition = tag.getCloseEnd();
			} else if (script) {
				if (tag.getCloseStart() > closeScriptPosition && !tag.getName().equalsIgnoreCase("script")) {
					script = false;
				}
			}

			if (!script) {
				if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("area")) {
					String hrefValue = tag.getAttributes().get("href");
					if (hrefValue != null) {
						hrefValue = hrefValue.trim();
						if (!hrefValue.startsWith("#") && !hrefValue.startsWith("${")) {
							if (hrefValue.startsWith("page:")) {
								String pageName = hrefValue.substring("page:".length());
								String params = "";

								if (pageName.contains("|")) {
									params = pageName.substring(pageName.indexOf("|") + 1);
									pageName = hrefValue.substring(0, pageName.indexOf("|"));
								} else if (pageName.contains("#")) {
									params = pageName.substring(pageName.indexOf("#") );
									pageName = hrefValue.substring(0, pageName.indexOf("#"));
								}

								if (ctx == null) {
									tag.getAttributes().put("href", "[TEST]-page:" + pageName);
								} else {
									ContentContext pageContext = ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
									pageContext.setFormat("html");
									tag.getAttributes().put("href", URLHelper.createURLFromPageName(pageContext, pageName) + params);
								}
							} else if (hrefValue.toLowerCase().startsWith("rss")) {
								String channel = "";
								if (hrefValue.contains(":")) {
									channel = hrefValue.split(":")[1];
								}
								if (ctx == null) {
									tag.getAttributes().put("href", "[TEST]-rss");
								} else {
									hrefValue = URLHelper.createRSSURL(ctx, channel);
									tag.getAttributes().put("href", hrefValue);
								}
							} else if (!StringHelper.isURL(hrefValue) && (!StringHelper.isMailURL(hrefValue)) && !hrefValue.contains("${") && !ResourceHelper.isResourceURL(ctx, hrefValue) && !ResourceHelper.isTransformURL(ctx, hrefValue)) {
								String url = URLHelper.removeParam(hrefValue);
								String params = URLHelper.getParamsAsString(hrefValue);
								if (ctx == null) {
									tag.getAttributes().put("href", "[TEST]-url:" + url);
								} else {
									url = URLHelper.createURLCheckLg(ctx, url);
									tag.getAttributes().put("href", URLHelper.addParams(url, params));
								}
							}
							remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
						}
					}
				} else if (tag.getName().equalsIgnoreCase("img")) {
					String src = tag.getAttribute("src", null);
					if (src != null) {
						if (!StringHelper.isURL(src) && !src.startsWith("${")) { // relative path
							String urlPrefix = URLHelper.mergePath("/", ctx.getRequest().getContextPath(), ctx.getPathPrefix(), "/");
							if (src.startsWith(urlPrefix)) {
								InfoBean info = InfoBean.getCurrentInfoBean(ctx);
								src = URLHelper.mergePath(info.getHostURLPrefix(), src);
							} else {
								src = URLHelper.createResourceURL(ctx, src);
							}
							tag.getAttributes().put("src", src);
							remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
						}
					}
				}
			}
		}

		outContent = remplacement.start(outContent);
		return outContent;
	}

	/**
	 * replace link in xhtml with createURL call.
	 *
	 * @param ctx
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static String replaceLinks(ContentContext ctx, String html) throws Exception {
		Document doc = Jsoup.parse(html, "", Parser.xmlParser());
		doc.getElementsByAttribute("href").forEach(item -> {
			try {
				String hrefValue = item.attr("href");
				if (hrefValue != null && !hrefValue.startsWith("\\")) {
					hrefValue = hrefValue.trim();
					if (!hrefValue.startsWith("#") && !hrefValue.startsWith("${")) {
						if (hrefValue.startsWith("page:")) {
							String pageName = hrefValue.substring("page:".length());
							String params = "";
							if (pageName.contains("|")) {
								params = pageName.substring(pageName.indexOf("|") + 1);
								pageName = hrefValue.substring(0, pageName.indexOf("|"));
							}

							if (ctx == null) {
								item.attr("href", "[TEST]-page:" + pageName);
							} else {
								ContentContext pageContext = ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
								pageContext.setFormat("html");
								item.attr("href", URLHelper.createURLFromPageName(pageContext, pageName) + params);
							}
						} else if (hrefValue.startsWith("file:")) {
							String fileUrl = hrefValue.substring("file:".length());
							String params = "";
							if (ctx == null) {
								item.attr("href", "[TEST]-file:" + fileUrl);
							} else {
								ContentContext pageContext = ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
								pageContext.setFormat("html");
								item.attr("href", URLHelper.createMediaURL(ctx, fileUrl));
							}
						} else if (hrefValue.toLowerCase().startsWith("rss")) {
							String channel = "";
							if (hrefValue.contains(":")) {
								channel = hrefValue.split(":")[1];
							}
							if (ctx == null) {
								item.attr("href", "[TEST]-rss");
							} else {
								hrefValue = URLHelper.createRSSURL(ctx, channel);
								item.attr("href", hrefValue);
							}
						} else if (!StringHelper.isURL(hrefValue) && (!StringHelper.isMailURL(hrefValue)) && !hrefValue.contains("${") && !ResourceHelper.isResourceURL(ctx, hrefValue) && !ResourceHelper.isTransformURL(ctx, hrefValue)) {
							String url = URLHelper.removeParam(hrefValue);
							String params = URLHelper.getParamsAsString(hrefValue);
							if (ctx == null) {
								item.attr("href", "[TEST]-url:" + url);
							} else {
								url = URLHelper.createURLCheckLg(ctx, url);
								item.attr("href", URLHelper.addParams(url, params));
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		doc.getElementsByAttribute("src").forEach(item -> {
			try {
				String src = item.attr("src");
				if (src != null) {
					if (!StringHelper.isURL(src) && !src.startsWith("${")) { // relative path
						if (ctx == null) {
							item.attr("src", "[TEST]-src:" + src);
						} else {
							String urlPrefix = URLHelper.mergePath("/", ctx.getRequest().getContextPath(), ctx.getPathPrefix(), "/");
							if (src.startsWith(urlPrefix)) {
								InfoBean info = InfoBean.getCurrentInfoBean(ctx);
								src = URLHelper.mergePath(info.getHostURLPrefix(), src);
							} else {
								src = URLHelper.createResourceURL(ctx, src);
							}
							item.attr("src", src);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		doc.outputSettings().escapeMode(EscapeMode.xhtml);
		return doc.html();
	}

	/**
	 * replace absolute url by local url (page:#page_name#) if page found.
	 *
	 * @param ctx
	 * @param html
	 * @return
	 * @throws Exception
	 */
	public static String replaceAbsoluteLinks(ContentContext ctx, String html) throws Exception {
		Document doc = Jsoup.parse(html, "", Parser.xmlParser());
		doc.getElementsByAttribute("href").forEach(item -> {
			try {
				String hrefValue = item.attr("href");
				if (hrefValue != null) {
					hrefValue = hrefValue.trim();
					if (StringHelper.isURL(hrefValue)) {
							if (ctx != null) {
								MenuElement page = NavigationHelper.getPageFromAbsoluteUrl(ctx, hrefValue);
								if (page != null) {
									item.attr("href", "page:"+page.getName());
								}
							}
						}
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		doc.outputSettings().escapeMode(EscapeMode.xhtml);
		return doc.html();
	}

	public static void compressCSS(File targetFile) throws IOException {
		String newContent;
		FileInputStream in = null;
		StringWriter out = null;
		InputStreamReader reader = null;
		try {
			in = new FileInputStream(targetFile);
			reader = new InputStreamReader(in, ContentContext.CHARACTER_ENCODING);
			out = new StringWriter();
			CSSFastMin.minimize(reader, out);
			newContent = out.toString();
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Compress CSS failed for '" + targetFile + "'.", ex);
			return; // Don't write on error, let the original as it is.
		} finally {
			ResourceHelper.closeResource(in);
			ResourceHelper.closeResource(out);
			ResourceHelper.closeResource(reader);
		}
		ResourceHelper.writeStringToFile(targetFile, newContent, ContentContext.CHARACTER_ENCODING);
	}

	public static String removeComment(String text) {
		return text.replaceAll("/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/", "");
	}

	public static void removeComment(File targetFile) throws IOException {
		String newContent;
		newContent = ResourceHelper.loadStringFromFile(targetFile);
		newContent = removeComment(newContent);
		ResourceHelper.writeStringToFile(targetFile, newContent, ContentContext.CHARACTER_ENCODING);
	}

	public static String cleanHTML(String html) {
		if (html == null) {
			return null;
		}
		Document doc = Jsoup.parse(html, "", Parser.xmlParser());

		// clean plugin injection
		Elements items = doc.select("#ConnectiveDocSignExtentionInstalled");
		if (items != null && items.size() > 0) {
			items.remove();
		}

		// Entities.EscapeMode.xhtml.getMap().put('\u00A0', "#160");
		doc.outputSettings().escapeMode(EscapeMode.xhtml);
		return doc.outerHtml();
	}

	public static void compressJS(final File targetFile) throws IOException {
		// String newContent;
		// FileInputStream in = null;
		// StringWriter out = null;
		// try {
		// in = new FileInputStream(targetFile);
		// InputStreamReader reader = new InputStreamReader(in,
		// ContentContext.CHARACTER_ENCODING);
		// out = new StringWriter();
		// ErrorReporter reporter = new ErrorReporter() {
		//
		// @Override
		// public void warning(String message, String sourceName, int line,
		// String lineSource, int lineOffset) {
		// logger.warning("JS compressor warning: " + message + " (" +
		// targetFile + " L:" + line + " C:" + lineOffset + ")");
		// }
		//
		// @Override
		// public EvaluatorException runtimeError(String message, String
		// sourceName, int line, String lineSource, int lineOffset) {
		// logger.warning("JS compressor runtimeError: " + message + " (" +
		// targetFile + " L:" + line + " C:" + lineOffset + ")");
		// return new EvaluatorException(message, sourceName, line, lineSource,
		// lineOffset);
		// }
		//
		// @Override
		// public void error(String message, String sourceName, int line, String
		// lineSource, int lineOffset) {
		// logger.warning("JS compressor error: " + message + " (" + targetFile
		// + " L:" + line + " C:" + lineOffset + ")");
		// }
		// };
		// new JavaScriptCompressor(reader, reporter).compress(out, 0, false,
		// false, false, false);
		// newContent = out.toString();
		// } catch (Exception ex) {
		// logger.log(Level.WARNING, "Compress JS failed for '" + targetFile +
		// "'.", ex);
		// return; // Don't write on error, let the original as it is.
		// } finally {
		// ResourceHelper.closeResource(in);
		// ResourceHelper.closeResource(out);
		// }
		// ResourceHelper.writeStringToFile(targetFile, newContent,
		// ContentContext.CHARACTER_ENCODING);
		XHTMLHelper.removeComment(targetFile);
	}

	private static int listDepth(TagDescription[] tags, TagDescription tag) {
		int depth = 1;
		for (String parent : XMLManipulationHelper.getAllParentName(tags, tag)) {
			if (parent.equalsIgnoreCase("ul") || parent.equalsIgnoreCase("ol")) {
				depth++;
			}
		}
		return depth;
	}

	public static String prepareToMailing(String xhtml) throws BadXMLException {
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(xhtml, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		int[] liNumber = new int[100];
		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("ul") || tag.getName().equalsIgnoreCase("ol")) {
				int ind = listDepth(tags, tag);
				if (tag.getName().equalsIgnoreCase("ol")) {
					liNumber[ind] = 1;
					String startVal = tag.getAttribute("start", null);
					if (startVal != null) {
						if (StringHelper.isDigit(startVal)) {
							liNumber[ind] = Integer.parseInt(startVal);
						}
					}
				} else {
					liNumber[ind] = 0;
				}
				List<String> parentsNames = XMLManipulationHelper.getAllParentName(tags, tag);
				String prefix = "";
				String suffix = "";
				if ((parentsNames.contains("ul") || parentsNames.contains("ol")) && !parentsNames.contains("li")) {
					prefix = "<tr class=\"table-li\"><td colspan=\"2\" valign=\"top\">";
					suffix = "</td></tr>";
				} else if (!(parentsNames.contains("ul") || parentsNames.contains("ol"))) {
					prefix = "<div class=\"table-list-wrapper\">";
					suffix = "</div>";
				}
				remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, prefix + "<table class=\"table-" + tag.getName() + "-depth-" + ind + " table-" + tag.getName() + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tbody>");
				remplacement.addReplacement(tag.getCloseStart(), tag.getCloseEnd() + 1, "</tbody></table>" + suffix);
			} else if (tag.getName().equalsIgnoreCase("li")) {
				String bullet = "&bull;";
				int ind = listDepth(tags, tag) - 1;
				if (liNumber[ind] > 0) {
					bullet = "" + liNumber[ind] + ".";
					liNumber[ind]++;
				}
				remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, "<tr class=\"table-li\"><td class=\"bullet\" valign=\"top\" style=\"padding-right:3px; width: 14px;\">" + bullet + "</td><td class=\"list-text\" valign=\"top\">");
				remplacement.addReplacement(tag.getCloseStart(), tag.getCloseEnd() + 1, "</td></tr>");
			}
		}
		return remplacement.start(xhtml);
	}

	public static String renderStaticInfo(ContentContext ctx, StaticInfo info) throws FileNotFoundException, IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		ContentContext lgCtx = info.getContextWithContent(ctx);
		boolean isPreview = false;

		if (!StringHelper.isEmpty(info.getTitle(lgCtx))) {
			out.println("<span class=\"title " + (isPreview ? "not-first-item" : "first-item") + "\">" + info.getTitle(lgCtx) + "</span>");
			isPreview = true;
		}
		if (!StringHelper.isEmpty(info.getManualDate(lgCtx))) {
			out.println("<span class=\"date " + (isPreview ? "not-first-item" : "first-item") + "\">" + StringHelper.renderShortDate(lgCtx, info.getManualDate(lgCtx)) + "</span>");
			isPreview = true;
		}
		if (!StringHelper.isEmpty(info.getDescription(lgCtx))) {
			out.println("<span class=\"description " + (isPreview ? "not-first-item" : "first-item") + "\">" + info.getDescription(lgCtx) + "</span>");
			isPreview = true;
		}
		if (!StringHelper.isEmpty(info.getLocation(lgCtx))) {
			out.println("<span class=\"location " + (isPreview ? "not-first-item" : "first-item") + "\">" + info.getLocation(lgCtx) + "</span>");
			isPreview = true;
		}
		if (!StringHelper.isEmpty(info.getCopyright(lgCtx))) {
			out.println("<span class=\"copyright " + (isPreview ? "not-first-item" : "first-item") + "\">" + info.getCopyright(lgCtx) + "</span>");
			isPreview = true;
		}
		out.close();
		String html = new String(outStream.toByteArray());
		if (html.trim().length() > 0) {
			html = "<div class=\"resource-data\">" + html + "</div>";
		}
		return html;
	}

	public static String renderColorChooser(String name, String cssValue, List<ExtendedColor> colors, String value) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"color-chooser " + StringHelper.neverNull(cssValue) + "\">");
		out.println("<input type=\"hidden\" id=\"" + name + "\" name=\"" + name + "\" value=\"" + StringHelper.neverNull(value) + "\" />");
		out.println("<button id=\"select-color-" + name + "\" class=\"select-color\" readonly=\"readonly\" style=\"background-color: " + value + "\" title=\"" + value + "\">&nbsp</button>");
		String js = "jQuery('#" + name + "').val(jQuery(this).val()); jQuery('#select-color-" + name + "').attr('style', 'background-color:'+jQuery(this).val()); return false;";
		for (Color c : colors) {
			String hex = "#" + Integer.toHexString(c.getRGB()).substring(2);
			out.println("<button type=\"button\" class=\"color-choice\" value=\"" + hex + "\" title=\"" + hex + "\" style=\"background-color: " + hex + "\" onclick=\"" + js + "\">&nbsp</button>");
		}
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;<button type=\"button\" class=\"color-choice\" value=\"\" title=\"reset\" onclick=\"" + js + "\">&nbsp</button>");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String collectionToList(Collection<? extends Object> col) {
		if (col == null || col.size() == 0) {
			return "";
		} else {
			String outHTML = "<ul>";
			for (Iterator iterator = col.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				outHTML = outHTML + "<li>" + object + "</li>";
			}
			return outHTML + "</ul>";
		}
	}

	public static String createHTMLTable(Map data) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		if (data != null && data.size() > 0) {
			out.println("<table style='width: 100%; border-collapse: collapse;' border='1'>");
			for (Object key : data.keySet()) {
				out.println("<tr><td style='font-weight: bold; padding: 10px; font-family:Helvetica, sans-serif; vertical-align: top; font-size: 12px;'>" + key + "</td><td style='padding: 10px; font-family:Helvetica, sans-serif;  vertical-align: top; font-size: 12px;'>" + data.get(key) + "</td></tr>");
			}
			out.println("</table>");
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String createAdminMail(String title, String content, Map data, String link, String linkLabel, String footer) {
		boolean center = true;
		if (content != null) {
			center = content.length() < 120;
		}
		return createAdminMail(title, content, data, link, linkLabel, footer, center);
	}

	public static String createAdminMail(String title, String content, Map data, String link, String linkLabel, String footer, boolean center) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String align = "center";
		if (!center) {
			align = "left";
		}
		out.println("<html xmlns='http://www.w3.org/1999/xhtml'><body paddingwidth='0' paddingheight='0' bgcolor='#ffffff'  style='padding: 0; background-repeat: repeat; width: 100% !important; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; -webkit-font-smoothing: antialiased;' offset='0' toppadding='0' leftpadding='0'>  <table width='100%' border='0' cellspacing='0' cellpadding='0' class='tableContent bgBody' align='" + align + "'  style='font-family:Helvetica, sans-serif; font-size: 14px;'><tr><td align='" + align + "'><table width='600' border='0' cellspacing='0' cellpadding='0' align='" + align + "'><tr><td align='" + align + "'><table width='678' border='0' cellspacing='0' cellpadding='0' align='" + align + "'><tr><td align='" + align + "'>");
		if (!StringHelper.isEmpty(title)) {
			out.println("<p style='font-family:Helvetica; font-size: 18px; color: #D31996;'>" + title + "</p>");
		}
		if (!StringHelper.isEmpty(content)) {
			out.println("<p style='font-family:Helvetica; font-size: 14px; text-align: " + align + ";'>" + content + "</p>");
		}
		out.println(createHTMLTable(data));
		if (!StringHelper.isEmpty(footer)) {
			out.println("<table><tr><td>&nbsp;</td></tr><tr><td class=\"footer\"><div style='display: block; margin: 10px 20px;  color: #dddddd; font-family:Helvetica, sans-serif;'>" + footer + "</div></td></tr></table>");
		}
		if (!StringHelper.isEmpty(link)) {
			out.println(getTableButton(link, linkLabel));
		}
		out.println("</td></tr></table></td></tr></table></td></tr></table></body></html>");
		return new String(outStream.toByteArray());
	}

	private static String getTableButton(String link, String linkLabel) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<table><tr><td>&nbsp;</td></tr><tr><td align=\"center\" bgcolor=\"#FFFFFF\">");
		out.println("<table width=\"280\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		out.println("<tr>");
		out.println("<td height=\"40\" valign=\"middle\" align=\"center\" bgcolor=\"#D31996\" style=\"font-family:Helvetica, sans-serif; font-size:13px; color:#FFFFFF; mso-line-height-rule:exactly; line-height:16px; font-weight:bold; border:1px solid #D31996; border-radius:2px; -moz-border-radius:2px; -ms-border-radius:2px; -o-border-radius:2px; -webkit-border-radius:2px;\">");
		out.println("<a href=\"" + link + "\" target=\"_blank\" style=\"color:#FFFFFF; text-decoration:underline; display:table-cell; text-align:center; height:40px; width:600px; vertical-align:middle;\">" + StringHelper.neverNull(linkLabel, link) + "</a>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</td></tr></table>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String getTableAlert(String linkLabel) {
		if (StringHelper.isEmpty(linkLabel)) {
			return "";
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<table width=\"100%\"><tr><td>&nbsp;</td></tr><tr><td align=\"center\" bgcolor=\"#FFFFFF\">");
		out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		out.println("<tr>");
		out.println("<td width=\"1\">&nbsp;</td>");
		out.println("<td height=\"40\" valign=\"middle\" align=\"center\" bgcolor=\"#fafafa\" style=\"font-family:Helvetica, sans-serif; font-size:13px; color:#565656; mso-line-height-rule:exactly; line-height:16px; font-weight:bold; border:1px solid #dedede; border-radius:2px; -moz-border-radius:2px; -ms-border-radius:2px; -o-border-radius:2px; -webkit-border-radius:2px;\">");
		out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		out.println("<tr>");
		out.println("<td width=\"1\">&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");
		out.println("</tr><tr>");
		out.println("<td width=\"1\">&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"font-family:Helvetica, sans-serif; font-size:13px; color:#565656; text-align: center;\">" + linkLabel + "</td><td width=\"1\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		out.println("</tr><tr>");
		out.println("<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");
		out.println("</tr></table>");
		out.println("</td>");
		out.println("<td width=\"1\">&nbsp;</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</td></tr></table>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String createUserMail(ContentContext ctx, String logo, String title, String content, String link, String linkLabel, String footer) throws IOException, Exception {
		String xhtml = ctx.getCurrentTemplate().getUserMailHtml(ctx.getGlobalContext());
		TemplateData templateData = ctx.getCurrentTemplate().getTemplateData();
		if (xhtml == null) {
			return createUserMail(templateData, title, content, null, link, linkLabel, footer);
		} else {
			xhtml = xhtml.replace("${site}", ctx.getCurrentPage().getGlobalTitle(ctx));
			if (title != null) {
				xhtml = xhtml.replace("${title}", title);
			}
			if (content != null) {
				xhtml = xhtml.replace("${text}", content);
			}
			if (link != null) {
				xhtml = xhtml.replace("${action.url}", link);
			}
			if (linkLabel != null) {
				xhtml = xhtml.replace("${action.text}", linkLabel);
			}
			xhtml = xhtml.replace("${root}", URLHelper.createURL(ctx.getContextForAbsoluteURL(), "/"));
			String logoUrl = URLHelper.getLogoUrl(ctx.getContextForAbsoluteURL(), "mail-logo");
			if (!StringHelper.isEmpty(logoUrl)) {
				xhtml = xhtml.replace("${logo}", logoUrl);
			} else {
				xhtml = xhtml.replace("${logo}", BASE64_CIRCLE);
			}
			if (logo == null) {
				logo = ctx.getGlobalContext().getTemplateData().getLogo();
				if (logo != null) {
					logo = URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), logo);
					xhtml = xhtml.replace("${action.logo}", URLHelper.createTransformURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE).getContextForAbsoluteURL(), logo, "mail-logo"));
				} else {
					xhtml = xhtml.replace("${action.logo}", URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/images/font/mail_logo.png"));
				}
			} else {
				if (!StringHelper.isURL(logo)) {
					xhtml = xhtml.replace("${action.logo}", URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), logo));
				} else {
					xhtml = xhtml.replace("${action.logo}", logo);
				}
			}
			xhtml = xhtml.replace("${footer}", footer);
			xhtml = xhtml.replace("${email}", ctx.getGlobalContext().getAdministratorEmail());
			if (templateData.getTitle() != null) {
				xhtml = xhtml.replace("#555555", templateData.getTitle().toString());
			}
			if (templateData.getText() != null) {
				xhtml = xhtml.replace("#333333", templateData.getText().toString());
			}
			if (templateData.getSpecial() != null) {
				xhtml = xhtml.replace("#cccccc", templateData.getSpecial().toString());
			}
			return xhtml;
		}

	}

	public static String minimizeHtml(String html)  {
		if (html == null) {
			return null;
		}
		String out = "";
		BufferedReader reader = new BufferedReader(new StringReader(html));
		try {
			String line = reader.readLine();
			while (line != null) {
				while (line.contains("  ")) {
					line = line.replace("  "," ");
				}
				while (line.contains("\t\t")) {
					line = line.replace("\t\t","\t");
				}
				out += line;
				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out;
	}

	public static String createUserMail(TemplateData templateData, String title, String content, Map data, String link, String linkLabel, String footer) {

		String backgroundColor = "#ffffff";
		String titleColor = "#000000";
		if (templateData != null) {
			if (templateData.getTitle() != null) {
				titleColor = templateData.getTitle().toString();
			}
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<html xmlns='http://www.w3.org/1999/xhtml'><body paddingwidth='0' paddingheight='0' bgcolor='#ffffff'  style='padding: 0; background-repeat: repeat; width: 100% !important; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; -webkit-font-smoothing: antialiased;' offset='0' toppadding='0' leftpadding='0'>  <table width='100%' border='0' cellspacing='0' cellpadding='0' class='tableContent bgBody' align='center'  style='font-family:Helvetica, sans-serif; font-size: 14px;'><tr><td align='center'><table width='600' border='0' cellspacing='0' cellpadding='0' align='center'><tr><td align='center'><table width='678' border='0' cellspacing='0' cellpadding='0' align='center'><tr><td align='center' style='background-color:" + backgroundColor + "'><br />");
		if (!StringHelper.isEmpty(title)) {
			out.println("<h1 style='font-family:Helvetica, sans-serif; font-size: 24px; color: " + titleColor + "'>" + title + "</h1>");
		}
		if (!StringHelper.isEmpty(content)) {
			out.println("<p style='font-family:Helvetica, sans-serif; font-size: 14px;'>" + content + "</p>");
		}
		if (data != null && data.size() > 0) {
			out.println("<table style='border-collapse: collapse;' border='1'>");
			for (Object key : data.keySet()) {
				out.println("<tr><td style='font-weight: bold; padding: 10px; font-family:Helvetica, sans-serif; vertical-align: top; font-size: 12px;'>" + key + " : </td><td style='padding: 10px; font-family:Helvetica, sans-serif;  vertical-align: top; font-size: 12px;'>" + data.get(key) + "</td></tr>");
			}
			out.println("</table>");
		}
		if (!StringHelper.isEmpty(footer)) {
			out.println("<table><tr><td>&nbsp;</td></tr><tr><td class=\"footer\"><div style='display: block; margin: 10px 20px;  color: #dddddd; font-family:Helvetica, sans-serif;'>" + footer + "</div></td></tr></table>");
		}
		if (!StringHelper.isEmpty(link)) {
			out.println(getTableButton(link, StringHelper.neverNull(linkLabel, link)));
		}
		out.println("</td></tr></table></td></tr></table></td></tr></table></body></html>");
		return new String(outStream.toByteArray());
	}

	public static String compileScss(String sass) throws IOException {

		File tempFile = File.createTempFile("css-", ".scss");
		ResourceHelper.writeStringToFile(tempFile, sass);

		Compiler compiler = new Compiler();
		Options options = new Options();
		try {
			FileContext context = new FileContext(tempFile.toURI(), null, options);
			Output output = compiler.compile(context);
			return output.getCss();

		} catch (CompilationException e) {
			throw new IOException(e);
		} finally {
			tempFile.delete();
		}
	}

	public static final void convert(final File xhtmlFile, final File pdfFile) throws IOException, DocumentException {
		final String xhtmlUrl = xhtmlFile.toURI().toURL().toString();
		final OutputStream reportPdfStream = new FileOutputStream(pdfFile);
		final ITextRenderer renderer = new ITextRenderer();
		renderer.setDocument(xhtmlUrl);
		renderer.layout();
		renderer.createPDF(reportPdfStream);
		reportPdfStream.close();
	}

	public static final String escapeNotAsciiChar(String html) {
		return StringEscapeUtils.unescapeXml(StringEscapeUtils.escapeHtml4(html));
	}

	// public static void main(String[] args) throws Exception {
	// File file = new File("c:/trans/mail.html");
	// String html = createUserMail(null, null, "title", "ceci est le contenu",
	// null, "http://www.javlo.org", "action", "ceci est le footer");
	// ResourceHelper.writeStringToFile(file, html);
	// }

	public static String compress(String newContent) {
		newContent = newContent.replaceAll("[\n\r]", " ");
		newContent = newContent.replaceAll("\t", " ");
		while (newContent.contains("  ")) {
			newContent = newContent.replace("  ", " ");
		}
		return newContent;
	}



}
