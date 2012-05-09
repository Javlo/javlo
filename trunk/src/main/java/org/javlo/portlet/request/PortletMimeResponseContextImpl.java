package org.javlo.portlet.request;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.portlet.CacheControl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletMimeResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.apache.pluto.container.PortletWindow;

public abstract class PortletMimeResponseContextImpl extends PortletResponseContextImpl implements PortletMimeResponseContext {

    public PortletMimeResponseContextImpl(
    		PortletContainer portletContainer, PortletWindow portletWindow,
    		HttpServletRequest req, HttpServletResponse resp, PortletURLProvider.TYPE type) {

    	super(portletContainer, portletWindow, req, resp, type);
	}

	private CacheControl cacheControl = new CacheControl() {
    	
        private String eTag;
        private int expirationTime;
        private boolean publicScope;
        private boolean cachedContent;
        
        public boolean useCachedContent() {
            return cachedContent;
        }

        public String getETag() {
            return this.eTag;
        }

        public int getExpirationTime() {
            return expirationTime;
        }

        public boolean isPublicScope() {
            return publicScope;
        }

        public void setETag(String eTag) {
            this.eTag = eTag;
        }

        public void setExpirationTime(int expirationTime) {
            this.expirationTime = expirationTime;
        }

        public void setPublicScope(boolean publicScope) {
            this.publicScope = publicScope;
        }

        public void setUseCachedContent(boolean cachedContent) {
            this.cachedContent = cachedContent;
        }
    };
    
	@Override
	public CacheControl getCacheControl() {
		return cacheControl;
	}

    @Override
	public void flushBuffer() throws IOException {
    	getServletResponse().flushBuffer();
    }

	@Override
	public int getBufferSize() {
		return getServletResponse().getBufferSize();
	}

	@Override
	public String getCharacterEncoding() {
		return getServletResponse().getCharacterEncoding();
	}

	@Override
	public void setContentType(String contentType) {
		getServletResponse().setContentType(contentType);
	}

	@Override
	public String getContentType() {
		return getServletResponse().getContentType();
	}
	
	@Override
	public Locale getLocale() {
		return getServletResponse().getLocale();
	}

	@Override
	public OutputStream getOutputStream() throws IOException, IllegalStateException {
		return getServletResponse().getOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException, IllegalStateException {
		return getServletResponse().getWriter();
	}

	@Override
	public PortletURLProvider getPortletURLProvider(TYPE type) {
		return super.getPortletURLProvider(type);
	}

	@Override
	public boolean isCommitted() {
		return getServletResponse().isCommitted();
	}

	@Override
	public void reset() {
		getServletResponse().reset();
	}

	@Override
	public void resetBuffer() {
		getServletResponse().resetBuffer();
	}

	@Override
	public void setBufferSize(int size) {
		getServletResponse().setBufferSize(size);
	}
}
