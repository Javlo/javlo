package org.javlo.servlet;

import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.document.DataDocument;
import org.javlo.service.document.DataDocumentService;

public class DataDocServlet extends HttpServlet {
	
	public static void main(String[] args) {
		String idStr = "15-dsfqkjfdl";
		String token = null;
		int posMinus = idStr.indexOf('-');
		if (posMinus>0) {
			token = idStr.substring(posMinus+1);
			idStr = idStr.substring(0, posMinus);
		}
		System.out.println(">>>>>>>>> DataDocServlet.main : idStr = "+idStr); //TODO: remove debug trace
		System.out.println(">>>>>>>>> DataDocServlet.main : token = "+token); //TODO: remove debug trace
	}

	private static final long serialVersionUID = -2086338711912267925L;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(DataDocServlet.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String path = request.getPathInfo();
		String[] pathSplit = StringHelper.split(request.getPathInfo(), "/");
		String category = pathSplit[1];
		String idStr = pathSplit[2];
		int idStrSize = idStr.length();
		String token = null;
		int posMinus = idStr.indexOf('-');
		if (posMinus>0) {
			token = idStr.substring(posMinus+1);
			idStr = idStr.substring(0, posMinus);
		}
		DataDocumentService docService = DataDocumentService.getInstance(GlobalContext.getInstance(request));
		try {
			String newPath = path.substring(category.length()+idStrSize+2);
			ContentContext ctx = ContentContext.getContentContext(request, response);
			newPath = newPath.substring(3);
			String contentType = getServletContext().getMimeType(newPath);
			if (StringHelper.isEmpty(contentType)) {
				contentType = "text/html; charset=" + ContentContext.CHARACTER_ENCODING;
			}
			DataDocument doc = docService.getDocumentData(category, Long.parseLong(idStr), token);
			if (doc == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			} else {
				response.setContentType(contentType);
				String url = URLHelper.createURL(ctx.getContextForAbsoluteURL(), newPath, doc.getData());
				url = URLHelper.addParam(url, "documentId", ""+doc.getId());				
				url = URLHelper.addParam(url, "date", ""+StringHelper.renderDate(doc.getDate()));
				NetHelper.writeURLToStream(new URL(url), response.getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
}
