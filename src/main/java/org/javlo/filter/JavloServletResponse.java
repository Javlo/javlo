package org.javlo.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

	public class JavloServletResponse extends HttpServletResponseWrapper {

	    private int httpStatus;
	    private ByteArrayOutputStream internalStream = null; 
	    private JavloServletOurputStream out = null;

	    public JavloServletResponse(HttpServletResponse response) {
	        super(response);
	    }

	    @Override
	    public void sendError(int sc) throws IOException {
	        httpStatus = sc;
	        super.sendError(sc);
	    }

	    @Override
	    public void sendError(int sc, String msg) throws IOException {
	        httpStatus = sc;
	        super.sendError(sc, msg);
	    }


	    @Override
	    public void setStatus(int sc) {
	        httpStatus = sc;
	        super.setStatus(sc);
	    }

	    public boolean isError()  {
	    	return httpStatus >= 400 && httpStatus < 417;
	    }
	    
	    @Override
	    public ServletOutputStream getOutputStream() throws IOException {
	    	if (!isError()) {
	    		return super.getOutputStream();
	    	} else {
	    		if (internalStream == null) {
	    			internalStream = new ByteArrayOutputStream();
	    			out = new JavloServletOurputStream(internalStream, this);
	    			out.disableFlush();
	    		}
	    		return out;
	    	}
	    }
	    
	    @Override
	    public boolean isCommitted() {	    
	    	return super.isCommitted();
	    }
	    

	}