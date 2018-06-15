package org.javlo.macro;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.service.database.DataBaseService;
import org.javlo.social.SocialLocalService;
import org.javlo.social.bean.Post;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.python.icu.util.Calendar;

public class ImportMysqlDataBase extends AbstractMacro {

	@Override
	public String getName() {
		return "import-cps-mysql";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		Connection mysqlConn = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			// handle the error
		}
		int error = 0;
		int parentNotFound = 0;
		try {
			mysqlConn = DriverManager.getConnection("jdbc:mysql://192.168.0.6/cps?user=cps&password=pvdm2312&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
			DataBaseService dataBaseService = DataBaseService.getInstance(ctx.getGlobalContext());
			SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
			Statement st = mysqlConn.createStatement();
			ResultSet rs = st.executeQuery("select * from phorum_messages order by datestamp asc");
			Map<Long, Post> createdPost = new HashMap<Long, Post>();
			while (rs.next()) {
				Calendar cal = Calendar.getInstance();
				long time = rs.getLong("datestamp");
				cal.setTimeInMillis(time * 1000);
				
				Post post = new Post();
				post.setGroup("cps");
				try {
					long id = rs.getLong("message_id");
					long thread = rs.getLong("thread");
					if (thread != id && thread != 0) {
						post.setMainPost(thread);
					}
					long parent = rs.getLong("parent_id");
					if (id != parent && parent != 0) {
						post.setParent(parent);
					}
					post.setAuthor(rs.getString("author"));
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);
					post.setTitle(rs.getString("subject"));
					out.println(rs.getString("body"));
					out.close();
					
					post.setText(new String(outStream.toByteArray()));
					post.setCreationDate(cal.getTime());
					post.setAdminValided(rs.getInt("status") == 2);
					createdPost.put(post.getId(), post);
					if (post.getParent() != null && createdPost.get(post.getParent()) == null) {
						System.out.println("parent not found  : "+post.getParent());
						parentNotFound++;
					} else if (post.getMainPost() != null && createdPost.get(post.getMainPost()) == null) {
						System.out.println("main post not found  : "+post.getMainPost());
						parentNotFound++;
					} else {
						if (post.getParent() != null) {
							post.setParent(createdPost.get(post.getParent()).getId()); // convert old id to new id
						}
						if (post.getMainPost() != null) {
							post.setMainPost(createdPost.get(post.getMainPost()).getId()); // convert old id to new id
						}
						socialService.createPost(post);
						createdPost.put(id, post);
						System.out.println("v > post created : "+post.getId());
					}
				} catch (Exception e) {
					System.out.println("x > post error : "+post.getId()+" >>> "+e.getMessage());
					error++;
				}
			}
			
			
			IUserFactory userFactory = UserFactory.createUserFactory(ctx.getRequest());
			for (IUserInfo userInfo : userFactory.getUserInfoList()) {
				userFactory.deleteUser(userInfo.getLogin());
			}
			rs = st.executeQuery("select * from phorum_users");
			while (rs.next()) {
				IUserInfo userInfo = new UserInfo();
				userInfo.setLogin(rs.getString("username"));
				userInfo.setPassword(rs.getString("password"));
				userInfo.setEmail(rs.getString("email"));
				userInfo.setRoles(new HashSet<String>(Arrays.asList(new String[] {"forum"})));
				userFactory.addUserInfo(userInfo);
			}
			mysqlConn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return ex.getMessage();
		}
		return "errors="+error+"  parentNotFound="+parentNotFound;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
