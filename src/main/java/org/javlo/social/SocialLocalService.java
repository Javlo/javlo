package org.javlo.social;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
				
				SocialStat st = outService.getSocialStat(2018);
				logger.info("social stat 2018 : "+st);
				
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
		try {
			st.execute("alter table post add contributors varchar(500)");
		} catch (Exception e) {
		}
		try {
			st.execute("alter table post add latestContributor varchar(50)");
		} catch (Exception e) {
		}
		try {
			st.execute("alter table post modify adminMessage varchar(6500)");
		} catch (Exception e) {
		}
		try {
			st.execute("alter table post add updateTime TIMESTAMP");
		} catch (Exception e) {
		}
		int updated = 0;
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post");
			while (rs.next()) {
				Date updateTime;
				String author;
				long id = rs.getLong("id");
				ResultSet replies = conn.createStatement().executeQuery("select time, author from post where mainPost='" + id + "' and adminCheck=true order by time desc");
				if (replies.next()) {
					updateTime = replies.getTimestamp("time");
					author = replies.getString("author");
				} else {
					updateTime = rs.getTimestamp("time");
					author = rs.getString("author");
				}
				PreparedStatement psParent = conn.prepareStatement("update post set updateTime=?, latestcontributor=? where id=" + id);
				psParent.setTimestamp(1, new java.sql.Timestamp(updateTime.getTime()));
				psParent.setString(2, author);
				psParent.execute();
				updated++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("updated post by init process : " + updated);

		dataBaseService.releaseConnection(conn);
	}

	public List<Post> getPostByAuthor(String group, String author) throws Exception {
		List<Post> outPost = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post where groupName='" + group + "' and author='" + author + "' and mainPost is null order by time asc");
			while (rs.next()) {
				outPost.add(rsToPost(conn, rs, author, false, false));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outPost;
	}
	
	public SocialStat getSocialStat(LocalDate date) throws Exception {
		SocialStat outStat = new SocialStat();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			PreparedStatement ps = conn.prepareStatement("select count(distinct author) from post where adminvalid=true AND time > ?");
			ps.setDate(1, java.sql.Date.valueOf(date));
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				outStat.setTotalAuthors(rs.getInt(1));
			}
			ps.close();
			ps = conn.prepareStatement("select count(*) from post where adminvalid=true AND time > ?");
			ps.setDate(1, java.sql.Date.valueOf(date));
			rs = ps.executeQuery();
			if (rs.next()) {
				outStat.setTotalPost(rs.getInt(1));
			}
			ps.close();
			ps = conn.prepareStatement("select count(*) from post where adminvalid=true AND parent is null AND time > ?");
			ps.setDate(1, java.sql.Date.valueOf(date));
			rs = ps.executeQuery();
			if (rs.next()) {
				outStat.setTotalMessage(rs.getInt(1));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outStat;
	}
	
	public SocialStat getSocialStat(int year) throws Exception {
		SocialStat outStat = new SocialStat();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		
		Calendar start = Calendar.getInstance();		
		start.set(Calendar.YEAR, year);
		start.set(Calendar.DAY_OF_MONTH, 1);
		start.set(Calendar.MONTH, 0);
		start.set(Calendar.HOUR, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		
		Calendar end = Calendar.getInstance();
		end.set(Calendar.YEAR, year);
		end.set(Calendar.DAY_OF_MONTH, 31);
		end.set(Calendar.MONTH, 11);
		end.set(Calendar.HOUR, 0);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		
		try {
			PreparedStatement ps = conn.prepareStatement("select count(distinct author) from post where adminvalid=true AND time > ? AND time < ?");
			ps.setDate(1, new java.sql.Date(start.getTime().getTime()));
			ps.setDate(2, new java.sql.Date(end.getTime().getTime()));
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				outStat.setTotalAuthors(rs.getInt(1));
			}
			ps.close();
			ps = conn.prepareStatement("select count(*) from post where adminvalid=true AND time > ? and time < ?");
			ps.setDate(1, new java.sql.Date(start.getTime().getTime()));
			ps.setDate(2, new java.sql.Date(end.getTime().getTime()));			
			rs = ps.executeQuery();
			if (rs.next()) {
				outStat.setTotalPost(rs.getInt(1));
			}
			ps.close();
			ps = conn.prepareStatement("select count(*) from post where adminvalid=true AND parent is null AND time > ? and time < ?");
			ps.setDate(1, new java.sql.Date(start.getTime().getTime()));
			ps.setDate(2, new java.sql.Date(end.getTime().getTime()));			
			rs = ps.executeQuery();
			if (rs.next()) {
				outStat.setTotalMessage(rs.getInt(1));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outStat;
	}

	private static String getSQLFilter(SocialFilter socialFilter, String username) {
		if (socialFilter == null) {
			return "";
		}
		String filterSQL = "";
		String sep = "";
		if (socialFilter.isNotValided()) {
			filterSQL = filterSQL + sep + " (adminCheck=false or id in (select mainpost from POST where adminCheck=false))";
			sep = " and ";
		}
		if (socialFilter.isOnlyMine()) {
			filterSQL = filterSQL + sep + "author='" + username + "'";
			sep = " and ";
		}
		if (!StringHelper.isEmpty(socialFilter.getQuery())) {
			String qr = socialFilter.getQuery().replace("'", "''");
			filterSQL = filterSQL + sep + "UPPER(text) like UPPER('%" + qr + "%') or mainpost.id in (select mainpost from post where mainpost=mainpost.id and UPPER(text) like UPPER('%" + qr + "%'))";
			filterSQL = filterSQL + " or mainpost.id in (select mainpost from post where mainpost=mainpost.id and UPPER(adminmessage) like UPPER('%" + qr + "%'))";
			sep = " and ";
		}
		if (!StringHelper.isEmpty(socialFilter.getAuthor())) {
			String qr = socialFilter.getAuthor().replace("'", "''");
			filterSQL = filterSQL + sep + "UPPER(author) like UPPER('%" + qr + "%') or mainpost.id in (select mainpost from post where mainpost=mainpost.id and UPPER(author)=UPPER('" + qr + "'))";
			sep = " and ";
		}
		if (!StringHelper.isEmpty(socialFilter.getTitle())) {
			String qr = socialFilter.getTitle().replace("'", "''");
			filterSQL = filterSQL + sep + "UPPER(title) like UPPER('%" + qr + "%')";
			sep = " and ";
		}
		if (!StringHelper.isEmpty(filterSQL)) {
			filterSQL = " and " + filterSQL;
		}
		return filterSQL;
	}
	
	public long __getPostListSize(SocialFilter socialFilter, String username, String group, boolean admin, boolean needCheck) throws Exception {
		//public List<Post> getPost(SocialFilter socialFilter, boolean admin, boolean needCheck, String username, String group, int size, int index) throws Exception {
		return getPost(socialFilter, admin, needCheck, username, group,-1,-1).size();
	}

	public long getPostListSize(SocialFilter socialFilter, String username, String group, boolean admin, boolean needCheck) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		PreparedStatement st=null;
		try {
			String notAdminQuery = "and (adminValid=1 or author=?)";
			if (needCheck) {
				notAdminQuery = "and ((adminCheck=1 and adminValid=1) or author=?)";
			}
			if (admin) {
				notAdminQuery = "";
			}

			String sql = "select count(id) from post mainpost where groupName='" + group + "' " + notAdminQuery + " and mainPost is null" + getSQLFilter(socialFilter, username);
			if (socialFilter != null && socialFilter.isNoResponse() && admin) {
				sql = sql + " AND mainpost.adminValid=1 AND (select count(*) from post childPost where childPost.parent=mainpost.id)=0";
			}
			//System.out.println("sql = "+sql);
			st = conn.prepareStatement(sql);
			if (!StringHelper.isEmpty(notAdminQuery)) {
				st.setString(1, username);
			}
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			dataBaseService.releaseConnection(st, conn);
		}
		return -1;
	}

	public long getUnvalidedPostListSize(String group) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			String sql = "select count(id) from post where groupName='" + group + "' and (adminCheck=0)";
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
			String sql = "select count(id) from post where groupName='" + group + "'";			
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
			ResultSet rs = conn.createStatement().executeQuery("select * from post where groupName='" + group + "' and mainPost is null order by updateTime desc");
			while (rs.next()) {
				outPost.add(rsToPost(conn, rs, null, true, false));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outPost;
	}

	public List<Post> getPost(SocialFilter socialFilter, boolean admin, boolean needCheck, String username, String group, int size, int index) throws Exception {
		List<Post> outPost = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		PreparedStatement st=null;
		try {
			String notAdminQuery = "and (adminValid=1 or author=?)";
			if (needCheck) {
				notAdminQuery = "and ((adminCheck=1 and adminValid=1) or author=?)";
			}
			if (admin) {
				notAdminQuery = "";
			}
			String sql = "select * from post mainpost where groupName='" + group + "' " + notAdminQuery + " and mainPost is null" + getSQLFilter(socialFilter, username);
			if (socialFilter != null && socialFilter.isNoResponse() && admin) {
				sql = sql + " AND mainpost.adminValid=1 AND (select count(*) from post childPost where childPost.parent=mainpost.id)=0";
			}
			sql = sql + " order by updateTime desc";
			if (size > 0) {
				sql = sql + " limit " + size + " offset " + index;
			}
			st = conn.prepareStatement(sql);
			if (!StringHelper.isEmpty(notAdminQuery)) {
				st.setString(1, username);			
			}
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				Post post = rsToPost(conn, rs, username, admin, needCheck);
				outPost.add(post);
			}
		} finally {
			dataBaseService.releaseConnection(st, conn);
		}
		return outPost;
	}

	public Post getPost(Long id, String author, boolean admin, boolean needCheck) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post where id=" + id);
			if (rs.next()) {
				return rsToPost(conn, rs, author, admin, needCheck);
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return null;
	}

	private int countReplies(long mainPost, String username, boolean admin, boolean needCheck) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			String notAdminQuery = "and (adminValid=1 or author='" + username + "')";
			if (needCheck) {
				notAdminQuery = "and (adminCheck=1 and adminValid=1 or author='" + username + "')";
			}
			if (admin) {
				notAdminQuery = "";
			}
			ResultSet rs = conn.createStatement().executeQuery("select count(id) from post where mainPost='" + mainPost + "' " + notAdminQuery);
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
			ResultSet rs = conn.createStatement().executeQuery("select id from post where mainPost='" + mainPost + "' and adminCheck=0");
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
		PreparedStatement st=null;
		try {
			String notAdminQuery = "and (adminValid=1 or author='?')";
			if (needCheck) {
				notAdminQuery = "and (adminCheck=1 and adminValid=1 or author='?')";
			}
			if (admin) {
				notAdminQuery = "";
			}
			st = conn.prepareStatement("select * from post where mainPost='" + mainPost + "' " + notAdminQuery + " order by time asc");
			if (!StringHelper.isEmpty(notAdminQuery)) {
				st.setString(1,  username);
			}
			if (needCheck) {
				st.setString(2,  username);
			}
			ResultSet rs = st.executeQuery();
			
			while (rs.next()) {
				workList.add(rsToPost(conn, rs, username, admin, needCheck));
			}
		} finally {
			dataBaseService.releaseConnection(st, conn);
		}
		Map<Long, Post> masterPost = new HashMap<Long, Post>();
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
					p.setParentPost(mainPostBean);
				}
			}
			return workList;
		}
	}

	protected Post rsToPost(Connection conn, ResultSet rs, String authors, boolean admin, boolean needCheck) throws Exception {
		Post post = new Post();
		post.setId(rs.getLong("id"));
		post.setAuthor(rs.getString("author"));
		post.setMedia(rs.getString("media"));
		post.setTitle(rs.getString("title"));
		post.setText(rs.getString("text"));
		post.setParent(rs.getLong("parent"));
		post.setMainPost(rs.getLong("mainPost"));
		post.setCreationDate(rs.getTimestamp("time"));
		post.setLatestUpdate(rs.getTimestamp("updateTime"));
		post.setValid(rs.getBoolean("adminValid"));
		post.setAdminMessage(rs.getString("adminMessage"));
		post.setAdminValided(rs.getBoolean("adminCheck"));
		post.setGroup(rs.getString("groupName"));
		post.setCountReplies(countReplies(post.getId(), authors, admin, needCheck));
		post.setLatestContributor(rs.getString("latestContributor"));

		String contrib = rs.getString("contributors");
		if (StringHelper.isEmpty(contrib)) {
			ResultSet replies = conn.createStatement().executeQuery("select distinct author from post where mainPost='" + post.getId() + "'");
			Set<String> contribSet = new HashSet<String>();
			contribSet.add(post.getAuthor());
			while (replies.next()) {
				String author = replies.getString("author");
				if (!contribSet.contains(author)) {
					contribSet.add(author);
				}
			}
			post.setContributors(contribSet);
		}
		if (admin) {
			post.setUncheckedChild(uncheckedReplies(post.getId()));
		}
		return post;
	}

	public void updatePost(Post post) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		PreparedStatement ps = conn.prepareStatement("update post set groupName=?, author=?, title=?, text=?, media=?, parent=?, mainPost=?, time=?, adminMessage=?, adminValid=?, adminCheck=?, updateTime=? where id=" + post.getId());
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
			if (post.getMainPost() != null && post.getMainPost() > 0 && post.isValid() && post.isAdminValided()) {
				ps.setLong(7, post.getMainPost());
				PreparedStatement psParent = conn.prepareStatement("update post set updateTime=?, latestContributor=? where id=" + post.getMainPost());
				psParent.setTimestamp(1, new java.sql.Timestamp(new Date().getTime()));
				psParent.setString(2, post.getAuthor());
				psParent.execute();
			} else {
				ps.setNull(7, java.sql.Types.LONGVARCHAR);
			}
			ps.setTimestamp(8, new Timestamp(post.getCreationDate().getTime()));
			ps.setString(9, post.getAdminMessage());
			ps.setBoolean(10, post.isValid());
			ps.setBoolean(11, post.isAdminValided());
			ps.setTimestamp(12, new Timestamp(post.getLatestUpdate().getTime()));
			ps.executeUpdate();
		} finally {
			dataBaseService.releaseConnection(conn);
		}
	}

	public Post createPost(Post post) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		PreparedStatement ps = conn.prepareStatement("insert into post (groupName, author, title, text, media, parent, mainPost, time, adminMessage, adminValid, adminCheck, updateTime) values (?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
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
			ps.setTimestamp(12, new Timestamp(post.getCreationDate().getTime()));
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
		PreparedStatement ps = conn.prepareStatement("insert into post (groupName, author, title, text, media, parent, mainPost, time, adminMessage, adminValid, adminCheck, updateTime) values (?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
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
		ps.setTimestamp(12, new Timestamp(post.getLatestUpdate().getTime()));
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
		ResultSet rs = st.executeQuery("select * from post where parent=" + id);
		while (rs.next()) {
			deleteAllPostsChildren(conn, rs.getLong("id"));
		}
		st.execute("delete from post where id=" + id);
	}

	public void deletePost(boolean admin, String author, long id) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		Statement st = null;
		try {
			
			ResultSet rs;
			if (!admin) {
				st = conn.prepareStatement("select * from post where id=" + id + " and author='?'");
				((PreparedStatement)st).setString(1, author);
				rs = ((PreparedStatement)st).executeQuery();
			} else {
				st = conn.createStatement();
				rs = st.executeQuery("select * from post where id=" + id);
			}
			if (rs.next()) {
				deleteAllPostsChildren(conn, id);
				st.execute("delete from post where parent=" + id);
				st.execute("delete from post where id=" + id);
			}
		} finally {
			dataBaseService.releaseConnection(st, conn);
		}
	}

	public static void main(String[] args) {
		System.out.println(">>>>>>>>> SocialLocalService.main : password = " + StringHelper.md5Hex("coucou")); // TODO: remove debug trace
	}

}
