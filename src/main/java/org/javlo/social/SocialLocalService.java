package org.javlo.social;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.GlobalContext;
import org.javlo.service.database.DataBaseService;
import org.javlo.social.bean.Post;

public class SocialLocalService {

	private static final String DATABASE_NAME = "social";

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
			st.execute("create table post (id bigint auto_increment PRIMARY KEY, author varchar(50), text varchar(1000), media varchar(255), parent int REFERENCES post(id), mainPost int REFERENCES post(id), time TIMESTAMP)");
		} catch (Exception e) {
		}
		dataBaseService.releaseConnection(conn);
	}

	public List<Post> getPost(String author) throws Exception {
		List<Post> outPost = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post where author='" + author + "' and mainPost is null order by time desc");
			while (rs.next()) {
				outPost.add(rsToPost(rs));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outPost;
	}
	
	public List<Post> getReplies(long mainPost) throws Exception {
		List<Post> outPost = new LinkedList<Post>();
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			ResultSet rs = conn.createStatement().executeQuery("select * from post where mainPost='" + mainPost + "' order by time desc");
			while (rs.next()) {
				outPost.add(rsToPost(rs));
			}
		} finally {
			dataBaseService.releaseConnection(conn);
		}
		return outPost;
	}

	protected Post rsToPost(ResultSet rs) throws SQLException {
		Post post = new Post();
		post.setId(rs.getLong("id"));
		post.setAuthor(rs.getString("author"));
		post.setMedia(rs.getString("media"));
		post.setText(rs.getString("text"));
		post.setParent(rs.getLong("parent"));
		post.setMainPost(rs.getLong("mainPost"));
		post.setCreationDate(rs.getTimestamp("time"));
		return post;
	}

	public void createPost(Post post) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		PreparedStatement ps = conn.prepareStatement("insert into post (author, text, media, parent, mainPost, time) values (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		try {
			ps.setString(1, post.getAuthor());
			ps.setString(2, post.getText());
			ps.setString(3, post.getMedia());
			if (post.getParent() != null) {
				ps.setLong(4, post.getParent());
			} else {
				ps.setNull(4, java.sql.Types.LONGVARCHAR);
			}
			if (post.getMainPost() != null) {
				ps.setLong(5, post.getMainPost());
			} else {
				ps.setNull(5, java.sql.Types.LONGVARCHAR);
			}
			ps.setTimestamp(6, new Timestamp(post.getCreationDate().getTime()));
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
	}
	
	public void deletePost(String author, long id) throws Exception {
		Connection conn = dataBaseService.getConnection(DATABASE_NAME);
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from post where id="+id+" and author='"+author+"'");
			if (rs.next()) {
				st.execute("delete from post where parent="+id);
				st.execute("delete from post where id="+id);
			}
		} finally { 
			dataBaseService.releaseConnection(conn);
		}
	}

}
