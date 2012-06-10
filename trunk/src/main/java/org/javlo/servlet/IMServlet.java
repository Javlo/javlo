package org.javlo.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.IMService;
import org.javlo.service.IMService.IMItem;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class IMServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	private void fillMessages(IMService service, String username, Long currentId, JSONObject o) throws JSONException {
		List<IMItem> list = new LinkedList<IMItem>();
		o.put("newCurrentId", service.fillMessageList(username, currentId, list));
		List<JSONObject> jsonList = new LinkedList<JSONObject>();
		for (IMItem item : list) {
			JSONObject json = new JSONObject();
			json.put("from", item.getFrom());
			json.put("message", item.getMessage());
			jsonList.add(json);
		}
		o.put("messages", jsonList);
	}

	private void fillUsers(IMService service, GlobalContext globalContext, String username, JSONObject out) throws JSONException {
		List<Principal> list = globalContext.getAllPrincipals();
		Map<String, JSONObject> listOut = new LinkedHashMap<String, JSONObject>();
		for (Principal user : list) {
			if (!user.getName().equals(username)) {
				JSONObject json = new JSONObject();
				json.put("username", user.getName());
				json.put("color", service.getUserColor(user.getName()));
				listOut.put(user.getName(), json);
			}
		}
		out.put("users", listOut);
		out.put("currentUser", username);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			IUserFactory userFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());
			if (userFactory.getCurrentUser(request.getSession()) == null) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			} else {
				String username = userFactory.getCurrentUser(request.getSession()).getName();

				IMService service = IMService.getInstance(globalContext, request.getSession());

				JSONObject out = new JSONObject();
				String message = request.getParameter("message");
				if (message != null) {
					message = XHTMLHelper.autoLink(XHTMLHelper.escapeXHTML(message));
					service.appendMessage(username, message);
				}
				Long currentId = StringHelper.safeParseLong(request.getParameter("currentId"), null);
				fillMessages(service, username, currentId, out);
				fillUsers(service, globalContext, username, out);

				response.getWriter().print(out.toString());
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
