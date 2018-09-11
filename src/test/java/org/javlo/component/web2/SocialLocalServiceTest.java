package org.javlo.component.web2;

import java.util.Collection;

import org.javlo.social.SocialFilter;
import org.javlo.social.SocialLocalService;
import org.javlo.social.bean.Post;

import junit.framework.TestCase;

public class SocialLocalServiceTest extends TestCase {

	private static final String AUTHOR_1 = "test_case_1";
	private static final String AUTHOR_2 = "test_case_2";
	private static final String AUTHOR_3 = "test_case_3";
	private static final int POST_SIZE = 5;

	private static final String GROUP1 = "group1";
	private static final String GROUP2 = "group2";
	private static final String GROUP3 = "group3";

	public void testCreatePost() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		for (int i = 1; i <= POST_SIZE; i++) {
			Post post = new Post();
			post.setAuthor(AUTHOR_1);
			post.setTitle("title - " + i);
			post.setText("hello word - " + i);
			post.setGroup(GROUP1);
			socialService.createPost(post);
		}
		SocialFilter filter = new SocialFilter();
		Collection<Post> posts = socialService.getPost(filter, false, false, AUTHOR_1, GROUP1, 30, 0);
		assertTrue(posts.size() > 0);

		for (int i = 1; i <= POST_SIZE - 1; i++) {
			Post post = new Post();
			post.setAuthor(AUTHOR_2);
			post.setTitle("title - " + i);
			post.setText("hello word - " + i);
			post.setGroup(GROUP2);
			socialService.createPost(post);
		}
		Collection<Post> posts2 = socialService.getPost(filter, false, false, AUTHOR_2, GROUP2, 30, 0);
		assertTrue(posts2.size() == POST_SIZE - 1);

		Post post = posts.iterator().next();
		assertEquals(post.getTitle(), "title - " + POST_SIZE);
		assertEquals(post.getText(), "hello word - " + POST_SIZE);
		assertEquals(post.getAuthor(), AUTHOR_1);
		assertNotNull(post.getCreationDate());

		/** test filter ***/
		filter.setQuery("word - 2");
		Collection<Post> postsFilterd = socialService.getPost(filter, false, false, AUTHOR_2, GROUP1, 30, 0);
		assertTrue(postsFilterd.size() == 1);
		filter.setOnlyMine(true);
		postsFilterd = socialService.getPost(filter, false, false, AUTHOR_2, GROUP1, 30, 0);
		assertTrue(postsFilterd.size() == 0);

		for (Post delPost : posts) {
			socialService.deletePost(false, delPost.getAuthor(), delPost.getId());
		}
		for (Post delPost : posts2) {
			socialService.deletePost(false,delPost.getAuthor(), delPost.getId());
		}
	}

	public void testCreatePostAuthors() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		for (int i = 1; i <= POST_SIZE; i++) {
			Post post = new Post();
			post.setAuthor(AUTHOR_1);
			post.setGroup(GROUP1);
			post.setText("hello word - " + i);
			socialService.createPost(post);
		}
		Collection<Post> posts = socialService.getPostByAuthor(GROUP1, AUTHOR_1);
		assertTrue(posts.size() > 0);
		Post post = posts.iterator().next();
		assertEquals(post.getAuthor(), AUTHOR_1);
		assertNotNull(post.getCreationDate());
		for (Post delPost : posts) {
			socialService.deletePost(false,delPost.getAuthor(), delPost.getId());
		}
	}

	public void testCreateReplies() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		Post mainPost = new Post();
		mainPost.setAuthor(AUTHOR_3);
		mainPost.setText("hello word to be reply");
		socialService.createPost(mainPost);
		Post lastPost = null;
		for (int i = 1; i <= POST_SIZE; i++) {
			Post post = new Post();
			lastPost = post;
			post.setAuthor(AUTHOR_3);
			post.setText("hello word - " + i);
			post.setParent(mainPost.getId());
			post.setMainPost(mainPost.getId());
			socialService.createPost(post);
		}
		Post replyReply = new Post();
		replyReply.setAuthor(AUTHOR_3);
		replyReply.setText("hello word - reply reply");
		replyReply.setParent(lastPost.getId());
		replyReply.setMainPost(mainPost.getId());
		socialService.createPost(replyReply);

		Collection<Post> posts = socialService.getReplies(null, AUTHOR_3, false, false, mainPost.getId());
		assertEquals(posts.size(), POST_SIZE + 1);
		Post post = posts.iterator().next();
		assertEquals(post.getAuthor(), AUTHOR_3);
		assertNotNull(post.getCreationDate());
		socialService.deletePost(false, mainPost.getAuthor(), mainPost.getId());
		assertEquals(socialService.getReplies(null, AUTHOR_3, false, false, mainPost.getId()), null);
	}

	public void testValidation() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		Post mainPost = new Post();
		final String GROUP_VALIDATION = "group_validation";
		mainPost.setGroup(GROUP_VALIDATION);
		mainPost.setAuthor(AUTHOR_2);
		mainPost.setText("hello word to be reply");
		mainPost.setAdminValided(false);
		mainPost = socialService.createPost(mainPost);
		Collection<Post> posts = socialService.getPost(null, false, true, AUTHOR_3, GROUP_VALIDATION, 10, 0);		
		assertEquals(posts.size(), 0);
		assertEquals(socialService.getPostListSize(null, AUTHOR_3,  GROUP_VALIDATION, false, true), 0);
		mainPost.setAdminValided(true);
		mainPost.setValid(false);
		socialService.updatePost(mainPost);
		posts = socialService.getPost(null, false, true, AUTHOR_3, GROUP_VALIDATION, 10, 0);
		assertEquals(posts.size(), 0);
		assertEquals(socialService.getPostListSize(null, AUTHOR_3,  GROUP_VALIDATION, false, true), 0);
		mainPost.setValid(true);
		socialService.updatePost(mainPost);
		posts = socialService.getPost(null, false, true, AUTHOR_3, GROUP_VALIDATION, 10, 0);
		assertEquals(posts.size(), 1);
		assertEquals(socialService.getPostListSize(null, AUTHOR_3,  GROUP_VALIDATION, false, true), 1);
		
		
		Post reply = new Post();
		reply.setParent(mainPost.getId());
		reply.setMainPost(mainPost.getId());
		reply.setGroup(GROUP_VALIDATION);
		reply.setAuthor(AUTHOR_2);
		reply.setText("re:hello word to be reply");
		reply.setAdminValided(false);
		reply = socialService.createPost(reply);
		posts = socialService.getPost(null, false, true, AUTHOR_3, GROUP_VALIDATION, 10, 0);
		assertEquals(posts.iterator().next().getCountReplies(), 0);
		reply.setValid(true);
		reply.setAdminValided(true);
		socialService.updatePost(reply);
		posts = socialService.getPost(null, false, true, AUTHOR_3, GROUP_VALIDATION, 10, 0);
		assertEquals(posts.iterator().next().getCountReplies(), 1);
		
		socialService.deletePost(false, mainPost.getAuthor(), mainPost.getId());
	}

	public void testAdmin() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);

		Post post1 = new Post();
		post1.setAuthor(AUTHOR_1);
		post1.setGroup(GROUP3);
		post1.setText("hello word - 1");
		socialService.createPost(post1);

		Collection<Post> posts = socialService.getPost(GROUP3);
		assertTrue(posts.size() > 0);

		Post post = posts.iterator().next();
		post.setValid(true);
		post.setAdminValided(true);
		socialService.updatePost(post);

		posts = socialService.getPost(GROUP3);
		assertTrue(posts.size() > 0);
		post = posts.iterator().next();
		assertTrue(post.isValid());
		assertTrue(post.isAdminValided());
		post.setValid(false);
		post.setAdminMessage("admin msg");
		socialService.updatePost(post);

		posts = socialService.getPost(GROUP3);
		assertTrue(posts.size() > 0);
		post = posts.iterator().next();
		assertFalse(post.isValid());
		assertTrue(post.isAdminValided());
		assertEquals(post.getAdminMessage(), "admin msg");
	}

	public void testDeletePost() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		for (int i = 1; i <= POST_SIZE; i++) {
			Post post = new Post();
			post.setAuthor(AUTHOR_1);
			post.setGroup(GROUP1);
			post.setText("hello word - " + i);
			socialService.createPost(post);
			Post reply = new Post();
			reply.setAuthor(AUTHOR_1);
			reply.setGroup(GROUP2);
			reply.setText("re: hello word - " + i);
			reply.setParent(post.getId());
			socialService.createPost(reply);
		}
		Collection<Post> posts = socialService.getPost(GROUP1);
		assertTrue(posts.size() > 0);
		int saveSize1 = socialService.getPost(GROUP1).size();
		int saveSize2 = socialService.getPost(GROUP2).size();
		assertTrue(saveSize1 > 0);
		assertTrue(saveSize2 > 0);
		socialService.deletePost(false, AUTHOR_1, posts.iterator().next().getId());
		// check if post is removed
		assertEquals(saveSize1 - 1, socialService.getPost(GROUP1).size());
	}
}
