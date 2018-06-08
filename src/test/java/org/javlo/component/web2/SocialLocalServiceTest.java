package org.javlo.component.web2;

import java.util.Collection;

import org.javlo.social.SocialLocalService;
import org.javlo.social.bean.Post;

import junit.framework.TestCase;

public class SocialLocalServiceTest extends TestCase {
	
	private static final String AUTHOR_1 = "test_case_1";
	private static final String AUTHOR_2 = "test_case_2";
	private static final String AUTHOR_3 = "test_case_3";
	private static final int POST_SIZE = 5;

	
	public void testCreatePost() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		for (int i=1; i<=POST_SIZE; i++) {
			Post post = new Post();
			post.setAuthor(AUTHOR_1);
			post.setText("hello word - "+i);
			socialService.createPost(post);
		}
		Collection<Post> posts = socialService.getPost();
		assertTrue(posts.size()>0);
		Post post = posts.iterator().next();
		assertEquals(post.getAuthor(), AUTHOR_1);
 		assertNotNull(post.getCreationDate());
 		for (Post delPost : posts) {
 			socialService.deletePost(delPost.getAuthor(), delPost.getId());
		}
	}
	
	public void testCreatePostAuthors() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		for (int i=1; i<=POST_SIZE; i++) {
			Post post = new Post();
			post.setAuthor(AUTHOR_1);
			post.setText("hello word - "+i);
			socialService.createPost(post);
		}
		Collection<Post> posts = socialService.getPost(AUTHOR_1);
		assertTrue(posts.size()>0);
		Post post = posts.iterator().next();
		assertEquals(post.getAuthor(), AUTHOR_1);
 		assertNotNull(post.getCreationDate());
 		for (Post delPost : posts) {
 			socialService.deletePost(delPost.getAuthor(), delPost.getId());
		}
	}
	
	public void testCreateReplies() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		Post mainPost = new Post();
		mainPost.setAuthor(AUTHOR_3);
		mainPost.setText("hello word to be reply");
		socialService.createPost(mainPost);
		Post lastPost = null;
		for (int i=1; i<=POST_SIZE; i++) {
			Post post = new Post();
			lastPost = post;
			post.setAuthor(AUTHOR_3);
			post.setText("hello word - "+i);
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
		
		Collection<Post> posts = socialService.getReplies(mainPost.getId());
		assertEquals(posts.size(), POST_SIZE+1);
		Post post = posts.iterator().next();
		assertEquals(post.getAuthor(), AUTHOR_3);
 		assertNotNull(post.getCreationDate());
		socialService.deletePost(mainPost.getAuthor(), mainPost.getId());
 		assertEquals(socialService.getReplies(mainPost.getId()), null);
	}
	
	public void testDeletePost() throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(null);
		for (int i=1; i<=POST_SIZE; i++) {
			Post post = new Post();
			post.setAuthor(AUTHOR_1);
			post.setText("hello word - "+i);
			socialService.createPost(post);
			Post reply = new Post();
			reply.setAuthor(AUTHOR_2);
			reply.setText("re: hello word - "+i);
			reply.setParent(post.getId());
			socialService.createPost(reply);
		}
		Collection<Post> posts = socialService.getPost(AUTHOR_1);
		assertTrue(posts.size()>0);
		int saveSize1 = socialService.getPost(AUTHOR_1).size();
		int saveSize2 = socialService.getPost(AUTHOR_2).size();
		assertTrue(saveSize1>0);
		assertTrue(saveSize2>0);
		socialService.deletePost(AUTHOR_1, posts.iterator().next().getId());
		// check if post is removed
		assertEquals(saveSize1-1, socialService.getPost(AUTHOR_1).size());
		// check if the replies of the post is removed
		assertEquals(saveSize2-1, socialService.getPost(AUTHOR_2).size());
	}
}
