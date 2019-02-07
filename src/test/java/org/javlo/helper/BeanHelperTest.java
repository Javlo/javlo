package org.javlo.helper;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class BeanHelperTest extends TestCase {

	public BeanHelperTest() {
		// TODO Auto-generated constructor stub
	}

	public BeanHelperTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	private static class Bean {
		private String firstname;
		private String lastname;
		private String title;
		private int age;
		private long dist;
		private double distance = 0;
		private boolean optin = false;

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		public String getLastname() {
			return lastname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public long getDist() {
			return dist;
		}

		public void setDist(long dist) {
			this.dist = dist;
		}

		public boolean isOptin() {
			return optin;
		}

		public void setOptin(Boolean optin) {
			this.optin = optin;
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}
	}

	public void testSetProperty() throws Exception {
		Bean bean = new Bean();
		BeanHelper.setProperty(bean, "firstname", "Patrick");
		BeanHelper.setProperty(bean, "lastname", "MyName");
		assertEquals(bean.getFirstname(), "Patrick");
		assertEquals(bean.getLastname(), "MyName");
		bean.setLastname("noname");
		BeanHelper.setProperty(bean, "lastname", null);
		assertEquals(bean.getLastname(), null);
		BeanHelper.setProperty(bean, "optin", true);
		assertTrue(bean.isOptin());
	}
	
	public void testCopy() throws Exception {
		Map<String,Object> test = new HashMap<>();
		test.put("firstname", "Barbara");

		Bean b = new Bean();
		BeanHelper.copy(test, b, false);
		assertEquals(b.getFirstname(), "Barbara");
		assertFalse(b.isOptin());
		test.put("optin", "true");
		BeanHelper.copy(test, b,false);
		assertTrue(b.isOptin());
		test.remove("optin");
		BeanHelper.copy(test, b, true);
		assertFalse(b.isOptin());
		
		test = new HashMap<>();
		b = new Bean();
		test.put("firstname-69", "Catherine");
		test.put("age-69", "23");
		test.put("dist-69", "9999");
		test.put("distance-69", "2.5");
		test.put("optin-69", (Boolean)true);
		BeanHelper.copy(test, b, null, "-69");
		assertEquals(b.getFirstname(), "Catherine");
		assertEquals(b.getAge(), 23);
		assertEquals(b.getDist(), 9999);
		assertEquals(b.getDistance(), 2.5, 0.0001);
		assertTrue(b.isOptin());
		
		BeanHelper.copy(test, b, null, "-69");
		
		test = new HashMap<>();
		test.put("*firstname-69", "Patrick");
		BeanHelper.copy(test, b, "*", "-69");
		assertEquals(b.getFirstname(), "Patrick");

		b.setFirstname("test");
		test.put("*firstname-69", "Patrick");
		BeanHelper.copy(test, b, "+", "-69");
		assertEquals(b.getFirstname(), "test");
		
		
		Bean a = new Bean();
		test.put("firstname-69", "Isabelle");
		BeanHelper.copy(test, a, null, "-69");
	}
	
	public void testReplaceValueInText() {
		String text = "Hi {{firstname}}, how are you ?";
		Bean b = new Bean();
		b.setFirstname("Patrick");
		text = BeanHelper.replaceValueInText(text, b, "{{", "}}");
		assertEquals(text, "Hi Patrick, how are you ?");
	}
}
