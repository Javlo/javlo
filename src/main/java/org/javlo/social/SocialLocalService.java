package org.javlo.social;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.database.DataBaseService;
import org.javlo.social.bean.Post;

public class SocialLocalService {
	
	private static Logger logger = Logger.getLogger(SocialLocalService.class.getName());

	public static final String DATABASE_NAME = "social";

	private DataBaseService dataBaseService = null;

	private static final String KEY = SocialLocalService.class.getName();

	public static SocialLocalService getInstance(GlobalContext globalContext) throws Exception {
		SocialLocalService outService = null;
		if (globalContext != null) {
			outService = (SocialLocalService) globalContext.getAttribute(KEY);
		}
		if (outService == null) {
			outService = new SocialLocalService();
			outService.dataBaseService = DataBaseService.getInstance(globalContext);
			outService.createDataBase();
			if (globalContext != null) {
				globalContext.setAttribute(KEY, outService);
			}
		}
		return outService;
	}

	private void createDataBase() throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		Statement st = conn.createStatement();
		try {
			st.execute("create table post (id bigint auto_increment PRIMARY KEY, groupName varchar(50), author varchar(50), title varchar(500), text varchar(MAX), adminValid BOOLEAN DEFAULT TRUE, adminCheck BOOLEAN DEFAULT FALSE, adminMessage varchar(500), media varchar(255), parent int REFERENCES post(id), mainPost int REFERENCES post(id), time TIMESTAMP)");
		} catch (Exception e) {
		}
		dataBaseService.releaseConnection(conn);
	}

	public List<Post> getPostByAuthor(String group, String author) throws Exception {
		List<Post> outPost = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post where groupName='"+group+"' and author='" + author + "' and mainPost is null order by time asc");
			while (rs.next()) {
				outPost.add(rsToPost(rs, author, false, false));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outPost;
	}
	
	private static String getSQLFilter(SocialFilter socialFilter, String username) {
		if (socialFilter == null) {
			return "";
		}
		String filterSQL = "";
		String sep = "";
		if (socialFilter.isNotValided()) {
			filterSQL = filterSQL + sep + " (adminCheck=false or id in (select parent from POST where adminCheck=false))";
			sep = " and ";
		}
		if (socialFilter.isOnlyMine()) {
			filterSQL = filterSQL + sep + "author='"+username+"'";
			sep = " and ";
		}
		if (!StringHelper.isEmpty(socialFilter.getQuery())) {
			filterSQL = filterSQL + sep + "(UPPER(text) like UPPER('%"+socialFilter.getQuery()+"%') or UPPER(author) like UPPER('%"+socialFilter.getQuery()+"%'))";
			sep = " and ";
		}
		if (!StringHelper.isEmpty(filterSQL)) {
			filterSQL = " and "+filterSQL;
		}
		System.out.println(">>>>>>>>> SocialLocalService.getSQLFilter : filterSQL = "+filterSQL); //TODO: remove debug trace
		return filterSQL;
	}
	
	public long getPostListSize(SocialFilter socialFilter, String username, String group,boolean admin, boolean needCheck) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			
			String notAdminQuery = "and (adminValid=1 or author='"+username+"')";
			if (needCheck) {
				notAdminQuery = "and ((adminCheck=1 and adminValid=1) or author='"+username+"')";
			}
			if (admin) {
				notAdminQuery = "";
			}
			
			String sql = "select count(id) from post where groupName='"+group+"' "+notAdminQuery+" and mainPost is null"+getSQLFilter(socialFilter, username);
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return -1;
	}
	
	public long getUnvalidedPostListSize(String group) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			String sql = "select count(id) from post where groupName='"+group+"' and (adminCheck=0)";
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return -1;
	}
	
	public long getPostListSize(String group) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			String sql = "select count(id) from post where groupName='"+group+"'";
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return -1;
	}
	
	
	
	public List<Post> getPost(String group) throws Exception {
		List<Post> outPost = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post where groupName='"+group+"' and mainPost is null order by time desc");
			while (rs.next()) {
				outPost.add(rsToPost(rs, null, true, false));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outPost;
	}
	
	public List<Post> getPost(SocialFilter socialFilter, boolean admin, boolean needCheck, String username, String group, int size, int index) throws Exception {
		List<Post> outPost = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			String notAdminQuery = "and (adminValid=1 or author='"+username+"')";
			if (needCheck) {
				notAdminQuery = "and ((adminCheck=1 and adminValid=1) or author='"+username+"')";
			}
			if (admin) {
				notAdminQuery = "";
			}
			String sql = "select * from post where groupName='"+group+"' "+notAdminQuery+" and mainPost is null"+getSQLFilter(socialFilter, username)+" order by time desc limit "+size+" offset "+index;
			ResultSet rs = conn.createStatement().executeQuery(sql);
			while (rs.next()) {
				outPost.add(rsToPost(rs, username, admin, needCheck));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outPost;
	}
	
	public Post getPost(Long id, String author, boolean admin, boolean needCheck) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post where id=" + id);
			if (rs.next()) {
				return rsToPost(rs, author, admin, needCheck);
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return null;
	}
	
	private int countReplies(long mainPost, String username, boolean admin, boolean needCheck) throws Exception {		
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			String notAdminQuery = "and (adminValid=1 or author='"+username+"')";
			if (needCheck) {
				notAdminQuery = "and (adminCheck=1 and adminValid=1 or author='"+username+"')";
			}
			if (admin) {
				notAdminQuery = "";
			}
			ResultSet rs = conn.createStatement().executeQuery("select count(id) from post where mainPost='" + mainPost+"' "+notAdminQuery);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return -1;
	}
	
	private boolean uncheckedReplies(long mainPost) throws Exception {		
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select id from post where mainPost='" + mainPost+"' and adminCheck=0");
			if (rs.next()) {
				return true;
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return false;
	}
	
	public List<Post> getReplies(SocialFilter socialFilter, String username, boolean admin, boolean needCheck, long mainPost) throws Exception {
		List<Post> workList = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			String notAdminQuery = "and (adminValid=1 or author='"+username+"')";
			if (needCheck) {
				notAdminQuery = "and (adminCheck=1 and adminValid=1 or author='"+username+"')";
			}
			if (admin) {
				notAdminQuery = "";
			}
			String filter = "";
			if (socialFilter.isNotValided()) {
				filter = " and admincheck=false";
			}
			ResultSet rs = conn.createStatement().executeQuery("select * from post where mainPost='" + mainPost + "' "+notAdminQuery+filter+" order by time asc");
			while (rs.next()) {
				workList.add(rsToPost(rs, username, admin, needCheck));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		Map<Long,Post> masterPost = new HashMap<Long, Post>();
		for (Post p : workList) {
			masterPost.put(p.getId(), p);
		}
		Post mainPostBean = getPost(mainPost, username, admin, needCheck);
		if (mainPostBean == null) {
			return null;
		} else {
			masterPost.put(mainPostBean.getId(), mainPostBean);
			for (Post p : workList) {
				Post parent = masterPost.get(p.getParent());
				if (parent != null) {
					p.setParentPost(parent);
				} else {
					logger.warning("parent not found : "+p.getParent());
				}
			}
			return workList;
		}
	}

	protected Post rsToPost(ResultSet rs, String authors, boolean admin, boolean needCheck) throws Exception {
		Post post = new Post();
		post.setId(rs.getLong("id"));
		post.setAuthor(rs.getString("author"));
		post.setMedia(rs.getString("media"));
		post.setTitle(rs.getString("title"));
		post.setText(rs.getString("text"));
		post.setParent(rs.getLong("parent"));
		post.setMainPost(rs.getLong("mainPost"));
		post.setCreationDate(rs.getTimestamp("time"));
		post.setValid(rs.getBoolean("adminValid"));
		post.setAdminMessage(rs.getString("adminMessage"));
		post.setAdminValided(rs.getBoolean("adminCheck"));
		post.setGroup(rs.getString("groupName"));
		post.setCountReplies(countReplies(post.getId(), authors, admin, needCheck));
		if (admin) {
			post.setUncheckedChild(uncheckedReplies(post.getId()));
		}
		return post;
	}
	
	public void updatePost(Post post) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		PreparedStatement ps = conn.prepareStatement("update post set groupName=?, author=?, title=?, text=?, media=?, parent=?, mainPost=?, time=?, adminMessage=?, adminValid=?, adminCheck=? where id="+post.getId());
		try {
			ps.setString(1, post.getGroup());
			ps.setString(2, post.getAuthor());
			ps.setString(3, post.getTitle());
			ps.setString(4, post.getText());
			ps.setString(5, post.getMedia());
			if (post.getParent() != null && post.getParent() > 0) {
				ps.setLong(6, post.getParent());
			} else {
				ps.setNull(6, java.sql.Types.LONGVARCHAR);
			}
			if (post.getMainPost() != null && post.getMainPost() > 0) {
				ps.setLong(7, post.getMainPost());
			} else {
				ps.setNull(7, java.sql.Types.LONGVARCHAR);
			}
			ps.setTimestamp(8, new Timestamp(post.getCreationDate().getTime()));
			ps.setString(9, post.getAdminMessage());
			ps.setBoolean(10, post.isValid());
			ps.setBoolean(11, post.isAdminValided());
			ps.executeUpdate();
		} finally {
			dataBaseService.releaseConnection(conn);
		}
	}

	public Post createPost(Post post) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		PreparedStatement ps = conn.prepareStatement("insert into post (groupName, author, title, text, media, parent, mainPost, time, adminMessage, adminValid, adminCheck) values (?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		try {
			ps.setString(1, post.getGroup());
			ps.setString(2, post.getAuthor());
			ps.setString(3, post.getTitle());
			ps.setString(4, post.getText());
			ps.setString(5, post.getMedia());
			if (post.getParent() != null) {
				ps.setLong(6, post.getParent());
			} else {
				ps.setNull(6, java.sql.Types.LONGVARCHAR);
			}
			if (post.getMainPost() != null) {
				ps.setLong(7, post.getMainPost());
			} else {
				ps.setNull(7, java.sql.Types.LONGVARCHAR);
			}
			ps.setTimestamp(8, new Timestamp(post.getCreationDate().getTime()));
			ps.setString(9, post.getAdminMessage());
			ps.setBoolean(10, post.isValid());
			ps.setBoolean(11, post.isAdminValided());
			ps.executeUpdate();
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) {
				post.setId(generatedKeys.getLong(1));
			} else {
				throw new SQLException("Creating post failed, no ID obtained.");
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return post;
	}
	
	public Post createPost(Post post, Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("insert into post (groupName, author, title, text, media, parent, mainPost, time, adminMessage, adminValid, adminCheck) values (?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, post.getGroup());
			ps.setString(2, post.getAuthor());
			ps.setString(3, post.getTitle());
			ps.setString(4, post.getText());
			ps.setString(5, post.getMedia());
			if (post.getParent() != null) {
				ps.setLong(6, post.getParent());
			} else {
				ps.setNull(6, java.sql.Types.LONGVARCHAR);
			}
			if (post.getMainPost() != null) {
				ps.setLong(7, post.getMainPost());
			} else {
				ps.setNull(7, java.sql.Types.LONGVARCHAR);
			}
			ps.setTimestamp(8, new Timestamp(post.getCreationDate().getTime()));
			ps.setString(9, post.getAdminMessage());
			ps.setBoolean(10, post.isValid());
			ps.setBoolean(11, post.isAdminValided());
			ps.executeUpdate();
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) {
				post.setId(generatedKeys.getLong(1));
			} else {
				throw new SQLException("Creating post failed, no ID obtained.");
			}
		return post;
	}
	
	private static void deleteAllPostsChildren(Connection conn, long id) throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("select * from post where parent="+id);
		while (rs.next()) {
			deleteAllPostsChildren(conn, rs.getLong("id"));			
		}
		st.execute("delete from post where id="+id);
	}
	
	public void deletePost(boolean admin, String author, long id) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			Statement st = conn.createStatement();
			ResultSet rs;
			if (!admin) {
				rs = st.executeQuery("select * from post where id="+id+" and author='"+author+"'");
			} else  {
				rs = st.executeQuery("select * from post where id="+id);
			}
			if (rs.next()) {
				deleteAllPostsChildren(conn, id);
				st.execute("delete from post where parent="+id);
				st.execute("delete from post where id="+id);
			}
		} finally { 
			dataBaseService.releaseConnection(conn);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(">>>>>>>>> SocialLocalService.main : password = "+StringHelper.md5Hex("coucou")); //TODO: remove debug trace
	}

}
