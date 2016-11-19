package org.javlo.helper;

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
	}

}
