package org.javlo.image;

import java.awt.Color;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.helper.ResourceHelper;
import org.javlo.utils.ConfigurationProperties;

public class ThumbnailsConfig {
    
    /**
     * create a static logger.
     */
    protected static Logger logger = Logger.getLogger(ThumbnailsConfig.class.getName());
    
    ConfigurationProperties properties = new ConfigurationProperties();
    
    private static final String FILE = "/WEB-INF/config/thumbnails-config.properties";
    private static final String KEY = ThumbnailsConfig.class.getName();
    
    private ThumbnailsConfig (ServletContext servletContext) {
        
        InputStream in = servletContext.getResourceAsStream(FILE);
        if (in==null) {
            logger.warning("config file for thunbnails not found : "+FILE);
        } else {
            try {
                properties.load(new InputStreamReader(in));
                servletContext.setAttribute(KEY, this);
            } catch (Exception e) {
                logger.warning("config file for thumbnails can not be loaded (msg: "+e.getMessage()+")");
            } finally {
            	ResourceHelper.closeResource(in);
            }
        }
    }
    
    public static ThumbnailsConfig getInstance(ServletContext servletContext) {
        ThumbnailsConfig outCfg = (ThumbnailsConfig)servletContext.getAttribute(KEY);
        if (outCfg == null) {
            outCfg = new ThumbnailsConfig(servletContext);
        }
        return outCfg;
    }
    
    /**
     * return the number of little image
     * @return
     */
    public int getThumbnailsCount() {
        return properties.getInt( "thumb.number", 9 );
    }
    
    public String getThumbnailsFilter() {
        return properties.getString( "filter.thumbnails", "thumbnails" );
    }
    
    public String get3DFilter() {
        return properties.getString( "filter.3D", "thumbnails-3D" );
    }
    
    public String getPreviewFilter() {
        return properties.getString( "filter.preview", "preview" );
    }
    
    public String getViewFilter() {
        return properties.getString( "filter.view", null );
    }
    
    public String getViewOnFilter() {
        return properties.getString( "filter.onview", null );
    }
    
    public String getViewOffFilter() {
        return properties.getString( "filter.offview", null );
    }
    
    public String getViewSlide() {
        return properties.getString( "filter.slide", "slide" );
    }
   
    public Color getBGColor() {
    	String bgColor = properties.getString("thumb.background-color", "ffffff");
    	Color res;
		try {
			res = Color.decode(bgColor);
		} catch (NumberFormatException e) {
			logger.warning("bad color found in thumbconfig file : "+bgColor);
			res = Color.WHITE;
		}
    	return res;
    }
    
    public boolean isRoundCorner() {
    	return properties.getBoolean("thumb.round-corner", true);
    }
    
    public boolean isPreview() {
    	return properties.getBoolean("thumb.preview", true);
    }
    
    public boolean isRandom() {
    	return properties.getBoolean("thumb.random", false);
    }
}
