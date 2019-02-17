package org.javlo.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;
import org.javlo.service.document.DataDocumentService;

public class DataDocFilter implements javax.servlet.Filter {


	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest inRequest, ServletResponse response, FilterChain next) throws IOException, ServletException {		
		HttpServletRequest request = (HttpServletRequest)inRequest;
		String path = request.getPathInfo();
		String[] pathSplit = StringHelper.split(request.getPathInfo(), "/");
		String category = pathSplit[1];
		String idStr = pathSplit[2];
		System.out.println(">>>>>>>>> DataDocFilter.process : cat = "+category); //TODO: remove debug trace
		System.out.println(">>>>>>>>> DataDocFilter.process : idStr = "+idStr); //TODO: remove debug trace
		RequestService rs = RequestService.getInstance(request);
		DataDocumentService docService = DataDocumentService.getInstance(GlobalContext.getInstance(request));
		try {
			for(Map.Entry<Object, Object> e : docService.getDocumentData(category, Long.parseLong(idStr)).entrySet()) {
				rs.putParameter(""+e.getKey(), ""+e.getValue());
			}
			String newPath = path.substring(category.length()+idStr.length()+2);
			System.out.println(">>>>>>>>> DataDocFilter.process : newPath = "+newPath); //TODO: remove debug trace
			
			request.getRequestDispatcher(newPath).forward(request, response);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
		next.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}	

}
