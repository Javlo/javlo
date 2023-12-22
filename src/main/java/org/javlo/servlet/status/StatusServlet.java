package org.javlo.servlet.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.service.ContentService;
import org.javlo.servlet.IVersion;
import org.javlo.utils.TimeMap;

public class StatusServlet extends HttpServlet {

	private static Map<String,String> datas = Collections.synchronizedMap(new TimeMap<>(1000 * 60 * 60));

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);;
			String data = datas.get(ctx.getGlobalContext().getContextKey());
			if (data == null) {
				synchronized (datas) {
					Runtime runtime = Runtime.getRuntime();					
					List<CheckBean> status = new LinkedList<>();
					status.add(new CheckBean("Version", "" + IVersion.VERSION, false));
					status.add(new CheckBean("Latest update", StringHelper.renderTime(new Date(System.currentTimeMillis())), false));
					long freemem = runtime.freeMemory();
					status.add(new CheckBean("Free memory", freemem + " (" + freemem / 1024 + " KB)" + " (" + freemem / 1024 / 1024 + " MB)", freemem < 1024 * 1024));
					boolean connected = NetHelper.isConnected();
					status.add(new CheckBean("Internet access", "" + connected, !connected));
					String smtpMsg = null;
					try {
						MailService.getMailTransport(new MailConfig(ctx.getGlobalContext(), ctx.getGlobalContext().getStaticConfig(), null));
					} catch (Exception e) {
						smtpMsg = e.getMessage();
					}
					status.add(new CheckBean("SMTP", StringHelper.neverEmpty(smtpMsg, "OK"), smtpMsg != null));
					ContentService content = ContentService.getInstance(ctx.getGlobalContext());
					int previewPageCount = content.getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)).getAllChildrenList().size();
					status.add(new CheckBean("#pages - preview", "" + previewPageCount, previewPageCount == 0));
					int viewPageCount = content.getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE)).getAllChildrenList().size();
					status.add(new CheckBean("#pages - view", "" + viewPageCount, viewPageCount == 0));
					
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);
					
					out.println("<table style=\"border: 1px #000000 solid; width: 100%; max-width: 1000px; margin: 15px auto; border-collapse: collapse;\">");
					out.println("<td style=\"background-color: #000000; color: #ffffff; font-size: 18px; text-align: center; padding: 10px\" colspan=\"2\">" + ctx.getGlobalContext().getContextKey() + "</td>");
					for (CheckBean checkBean : status) {
						out.println(checkBean.getTableHtml());
					}
					out.println("</table>");
					out.close();
					data = new String(outStream.toByteArray());
					datas.put(ctx.getGlobalContext().getContextKey(), data);
				}
			}
			response.getWriter().println(data);
		} catch (Exception e) {
			e.printStackTrace(response.getWriter());
		}
		response.flushBuffer();
	}

}
