package org.javlo.test.spring;

import org.springframework.web.client.RestTemplate;

public class LanguageTest {
	
	public static class LangBean {
		String code;
		String title;
		String more;
	}
	
	private static final String URL = "http://www.ecomback.europarl.europa.eu/ecomback/ws/EMeetingRESTService/languages";	
	
	public static void main(String[] args) {
		RestTemplate restTemplate = new RestTemplate();
		LangBean[] response = restTemplate.getForObject(URL, LangBean[].class);
		for (LangBean langBean : response) {
			System.out.println(langBean.code+" - "+langBean.title);
		}		
	}

}
