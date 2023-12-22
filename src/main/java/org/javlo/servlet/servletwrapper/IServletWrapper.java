package org.javlo.servlet.servletwrapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IServletWrapper {
	
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException;

}
