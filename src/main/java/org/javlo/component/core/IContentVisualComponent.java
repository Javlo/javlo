/*
 * Created on 19-sept.-2003
 *
 */
package org.javlo.component.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.config.ComponentConfig;
import org.javlo.context.ContentContext;
import org.javlo.data.rest.IRestItem;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 * 
 */
public interface IContentVisualComponent extends Comparable<IContentVisualComponent>, IRestItem {

	public static final String ID_SEPARATOR = "__";

	static public String VIEW_DEFINITION_REQUEST_ID = "view_definition_dc";

	public static final String COMP_ID_REQUEST_PARAM = "comp-id";

	public static final String EDIT_ACTION_CSS_CLASS = "pc_edit_action";

	static public int SEARCH_LEVEL_NONE = 0;
	static public int SEARCH_LEVEL_LOW = 1;
	static public int SEARCH_LEVEL_MIDDLE = 2;
	static public int SEARCH_LEVEL_HIGH = 3;

	static public int COMPLEXITY_EASY = 1;
	static public int COMPLEXITY_STANDARD = 2;
	static public int COMPLEXITY_ADMIN = 3;

	/* default values for colors */
	static public String DEFAULT_COLOR = "74AA27";
	static public String TEXT_COLOR = "3F3F3F";
	static public String LINK_COLOR = "F79A0A";
	static public String META_COLOR = "E9320F";
	static public String GRAPHIC_COLOR = "86C12D";
	static public String ECOM_COLOR = "DDDDDD";
	static public String DYN_COMP_COLOR = "99CCF9";
	static public String WEB2_COLOR = "5A4C93";
	static public String CONTAINER_COLOR = "D57C60";
	
	public static final String COLORED_WRAPPER_CLASS = "colored-wrapper";
	
	public static int HIGH_LABEL_LEVEL = 1000;
	public static int MIDDLE_LABEL_LEVEL = 100;
	public static int LOW_LABEL_LEVEL = 10;

	/**
	 * get the configuration of the component. from project or from template.
	 * 
	 * @param ctx
	 * @return
	 */
	public ComponentConfig getConfig(ContentContext ctx);

	/**
	 * action call when update the content page.
	 * @return the error message, null if no error
	 * 
	 * @throws Exception
	 */
	public String performEdit(ContentContext ctx) throws Exception; 

	/**
	 * the code for view the element in XHTML environment.
	 * 
	 * @return
	 */
	public String getXHTMLCode(ContentContext ctx) throws Exception;
	
	/**
	 * display empty xhtml code if empty
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public boolean isDispayEmptyXHTMLCode(ContentContext ctx) throws Exception;
	
	/**
	 * return the code if component contain's no data.
	 */
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception;

	/**
	 * xhtml code for config the component
	 * 
	 * @return
	 */
	public String getXHTMLConfig(ContentContext ctx) throws Exception;

	/**
	 * return true if there the component is configurable.
	 * 
	 * @param ctx
	 * @return
	 */
	public boolean isConfig(ContentContext ctx);

	/**
	 * action to manage the config, call when global content form is saved.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public String performConfig(ContentContext ctx) throws Exception;

	/**
	 * get the value of the content without any rendering.
	 * 
	 * @pararm ctx current contentcontext
	 * @return content in a text format.
	 */
	public String getValue(ContentContext ctx);

	/**
	 * set the content without rendering
	 * 
	 * @param inContent
	 */
	public void setValue(String inContent);

	/**
	 * the language of the element.
	 * 
	 * @return
	 */
	// public String getLanguage();

	/**
	 * return the type of the element.
	 * 
	 * @return the type of the element.
	 */
	public String getType();

	/**
	 * return the label of the element.
	 * 
	 * @param lg
	 *            language of the label
	 * @return the label of the element.
	 */
	public String getComponentLabel(ContentContext ctx, String lg);

	/**
	 * get the level of the title (1,2,3...). return 0 if the component is not a
	 * title
	 * 
	 * @return the level of the title, 0 if the component is not a title.
	 */
	public int getTitleLevel(ContentContext ctx);

	/**
	 * check if this element is visible in the format.
	 * 
	 * @return true if elment visible, false else
	 */
	public boolean isVisible(ContentContext ctx);

	/**
	 * return a unique id for the coponent
	 */
	public String getId();

	/**
	 * init the component, do that before all using.
	 * 
	 * @param newContent
	 *            the content of the component
	 * @param newId
	 *            the id of the component
	 * @param newCtx
	 *            the context of the content manager.
	 */
	public IContentVisualComponent newInstance(ComponentBean bean, ContentContext newCtx) throws Exception;

	/**
	 * @return true if the composant was modified.
	 */
	public boolean isModify();

	/**
	 * return true if the component must be unique on a page
	 * 
	 * @return
	 */
	public boolean isUnique();

	/**
	 * check if the component content pertinent content. Some component as title
	 * is never pertinent.
	 * 
	 * @return true if component don't content pertinent content
	 */
	public boolean isEmpty(ContentContext ctx);

	/**
	 * return true if component is considered as realContent and the page that
	 * contain the component is also considered as real content.
	 * 
	 * @param ctx
	 *            current context
	 * @return
	 */
	public boolean isRealContent(ContentContext ctx);

	/**
	 * return true if value is never modified.
	 * 
	 * @return true if value contain default value.
	 */
	public boolean isDefaultValue(ContentContext ctx);

	/**
	 * call this method when the component is stored.
	 */
	public void stored();

	/**
	 * get a internationalized text, in the languague of the view.
	 * 
	 * @param key
	 *            the key of the text
	 * @return the text in the current language
	 * @throws ResourceNotFoundException
	 */
	public String getViewText(ContentContext ctx, String key) throws ResourceNotFoundException;

	/**
	 * get a internationalized text, in the languague of the edition.
	 * 
	 * @param key
	 *            the key of the text
	 * @return the text in the current language
	 */
	public String getEditText(ContentContext ctx, String key);

	/**
	 * return a error message in current view language
	 * 
	 * @param key
	 *            the of the error message
	 * @return a error message in current view language
	 * @throws ResourceNotFoundException
	 */
	public String getErrorMessage(String fieldName) throws ResourceNotFoundException;

	/**
	 * get the internal data
	 * 
	 * @return
	 */
	public ComponentBean getComponentBean();

	/**
	 * return a string representation of the component
	 * 
	 * @author pvandermaesen
	 */
	public String getTextLabel();

	/**
	 * return a string representation of the component with complete title
	 * 
	 * @return
	 */
	public String getTextTitle(ContentContext ctx);

	public boolean isRepeat();

	public void setRepeat(boolean newRepeat);

	/**
	 * define the level of label.
	 * If there area most than 1 level on page, the label of the page is the bigger level.
	 * 
	 * @param ctx
	 * @return 0=default, no the components is'nt label.
	 */
	public int getLabelLevel(ContentContext ctx);

	/**
	 * return the java script code called when global form is submited.
	 * 
	 * @return java script code.
	 */
	public String getJSOnSubmit();

	/**
	 * return a text represent the content of the component. sample all the
	 * content :-)
	 * 
	 * @return a text represent the content of the component for the search
	 *         module
	 */
	public String getTextForSearch(ContentContext ctx);

	/**
	 * return the level of the component is a search.
	 * 
	 * @return 1-LOW 2-MIDDLE 3-HIGH
	 */
	public int getSearchLevel();

	/* STYLE */

	/**
	 * return the title of the style choice.
	 */
	public String getStyleTitle(ContentContext ctx);

	/**
	 * get the list of style possible for this component.
	 * 
	 * @return a list of string represent a style.
	 */
	public String[] getStyleList(ContentContext ctx);

	/**
	 * return a list of label define the style.
	 * 
	 * @return a list of label.
	 */
	public String[] getStyleLabelList(ContentContext ctx);

	/**
	 * return the title of the style choice.
	 */
	public List<SuffixPrefix> getMarkerList(ContentContext ctx);

	/**
	 * get the current style of the component.
	 * 
	 * @return the current style.
	 */
	public String getStyle(ContentContext ctx);

	/**
	 * get the label of the current style
	 * 
	 * @param ctx
	 * @return
	 */
	public String getStyleLabel(ContentContext ctx);

	/**
	 * set the current style of the component.
	 * 
	 * @param inStyle
	 *            new style
	 */
	public void setStyle(ContentContext ctx, String inStyle);

	/**
	 * set the current renderer for the component view.
	 * 
	 * @param a
	 *            key to a renderer (defined in config file of the component)
	 */
	public void setRenderer(ContentContext ctx, String renderer);

	/**
	 * get the current renderer for the component view.
	 * 
	 * @param a
	 *            key to a renderer (defined in config file of the component)
	 */

	public String getRenderer(ContentContext ctx);

	/**
	 * return the name of the renderer.
	 * 
	 * @param ctx
	 * @return
	 */
	public String getCurrentRenderer(ContentContext ctx);

	/**
	 * code for prefix the view XHTML code.
	 * 
	 * @return XHTML code.
	 */
	public String getPrefixViewXHTMLCode(ContentContext ctx);

	/**
	 * get the input name of the renderer.
	 * 
	 * @return
	 */
	public String getInputNameRenderer();

	/**
	 * code for sufix the view XHTML code.
	 * 
	 * @return XHTML code.
	 */
	public String getSuffixViewXHTMLCode(ContentContext ctx);

	/**
	 * return the name if the content field (textarea...)
	 * 
	 * @return
	 */
	public String getContentName();

	/**
	 * check if the component can be inserd inline.
	 * 
	 * @return true if component is a inline component. (text, link, image)...
	 */
	public boolean isInline();

	/**
	 * get the next component on the page. if null this is the last component.
	 * 
	 * @return a component.
	 */
	public IContentVisualComponent next();

	/**
	 * get the next component on the page. if null this is the last component.
	 * 
	 * @return a component.
	 */
	public IContentVisualComponent previous();

	/**
	 * change the previous component
	 * 
	 * @param nextComponent
	 */
	public void setNextComponent(IContentVisualComponent nextComponent);

	/**
	 * change the previous component
	 * 
	 * @param nextComponent
	 */
	public void setPreviousComponent(IContentVisualComponent nextComponent);

	/**
	 * get the prefix must be insered before a list of the current component
	 * (warning: one element is a list) #return XHTML code of prefix
	 */
	public String getFirstPrefix(ContentContext ctx);

	/**
	 * get the sufix before must be insered after a list of the current
	 * component.
	 * 
	 * @return XHTML code of sufix.
	 */
	public String getLastSufix(ContentContext ctx);

	/**
	 * get the exadecimal color for representation of the component.
	 * 
	 * @return a exadecimal color (sample : a5b499)
	 */
	public String getHexColor();

	/**
	 * get a message for the component.
	 * 
	 * @return a generic message (error, info, warning, help), null if no
	 *         message.
	 */
	public GenericMessage getMessage();

	/**
	 * you can insert a text in this component
	 * 
	 * @return true if a text is insertable
	 */
	public boolean isInsertable();

	/**
	 * you display the composant as a list (if you want)
	 * 
	 * @return true if a text is listable
	 */
	public boolean isListable();

	/**
	 * insert a text in the component
	 * 
	 * @param text
	 *            the text to be insered
	 */
	public void insert(String text);

	/**
	 * true if the component is in a list
	 * 
	 * @return true if component is in a list
	 */
	public boolean isList(ContentContext ctx);

	/**
	 * put the component is a list
	 * 
	 * @param inList
	 *            true for put in a list, false for remove from a list
	 */
	public void setList(boolean inList);

	/**
	 * return true if the modifition of the component need a refresh of the edit
	 * page (ajax).
	 */
	public boolean isNeedRefresh();

	public boolean isVisible();

	public boolean isRepeatable();

	public void setValid(boolean inVisible);

	/**
	 * get the area where the component must be rendered
	 * 
	 * @return a area name
	 */
	public String getArea();

	/**
	 * a url to descripe the usage of the component
	 * 
	 * @param lang
	 *            the langage of the edit interface
	 * @return url to a help page or null if no help page defined
	 */
	public String getHelpURL(ContentContext ctx);
	
	public boolean isHelpURL(ContentContext ctx);

	/**
	 * if you set true the page is refresh after update
	 * 
	 * @param needRefresh
	 */
	public void setNeedRefresh(boolean needRefresh);

	/**
	 * reset the view data.
	 * 
	 * @throws IOException
	 */
	public void resetViewData(ContentContext ctx) throws IOException;

	/**
	 * replace a content in visual element
	 * 
	 * @param source
	 *            the source element (sample. : test)
	 * @param target
	 *            the target element (sample. : -- test --)
	 */
	public void replaceInContent(String source, String target);

	/**
	 * add a list of remplacement key, value
	 * 
	 * @param replacement
	 */
	public void replaceAllInContent(Map<String, String> replacement);

	/**
	 * clear remplacement values
	 */
	public void clearReplacement();

	/**
	 * return the complexity level of the component.
	 * 
	 * @param ctx
	 *            TODO
	 * 
	 * @return see contant
	 */
	public int getComplexityLevel(ContentContext ctx);

	/**
	 * true if component marked as hidden (in components.txt the class name
	 * start with '.')
	 * 
	 * @return
	 */
	public boolean isHidden(ContentContext ctx);

	/**
	 * get the page contains the component
	 * 
	 * @return a page of the navigation
	 */
	public MenuElement getPage();

	/**
	 * this method is called when component is deleted.
	 */
	public void delete(ContentContext ctx);

	/**
	 * return the next component in the page sequence.
	 * 
	 * @return
	 */
	public IContentVisualComponent getNextComponent();

	/**
	 * return the previous component in the page sequence.
	 * 
	 * @return
	 */
	public IContentVisualComponent getPreviousComponent();

	/**
	 * return true if component is in the first repeat sequence of the page.
	 * 
	 * @return
	 */
	public boolean isFirstRepeated();

	/**
	 * return the external resources needed from component as css, javascript...
	 * 
	 * @return a list of URI to external resources
	 */
	public Collection<String> getExternalResources(ContentContext ctx);

	/**
	 * return the part of header needed for this component
	 * 
	 * @return
	 */
	public String getHeaderContent(ContentContext ctx);

	/**
	 * return the part of header needed for this component
	 * 
	 * @return
	 */
	public boolean isContentCachable(ContentContext ctx);

	/**
	 * load special data file.
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	public void loadViewData(ContentContext ctx) throws IOException;

	/**
	 * get i18n keys for edition
	 * 
	 * @param ctx
	 * @return a list of i18n keys.
	 */
	public List<String> getI18nEditableKeys(ContentContext ctx);

	/**
	 * get a unic key of the component. User special for DynamicComponent.
	 */
	public String getKey();

	/**
	 * count the word in the content.
	 * 
	 * @return
	 */
	public int getWordCount(ContentContext ctx);

	/**
	 * return true is this component is a title of a group of component.
	 * 
	 * @return
	 */
	public boolean isMetaTitle();

	/**
	 * return the name of the class (use for JSTL)
	 * 
	 * @return
	 */
	public String getClassName();

	/**
	 * return the version of the component : A.B.C A : major version, no
	 * compatibility with the older data B : new function but compatibilty with
	 * older data C : bug correction
	 * 
	 * @return ? if version undefined.
	 */
	public String getVersion();

	/**
	 * a description of the component;
	 * 
	 * @return
	 */
	public String getDescription(Locale local);

	/**
	 * authors of the component
	 * 
	 * @return
	 */
	public String getAuthors();

	/**
	 * init the content with a default value. sample : "lorem ipsum" for text
	 * component.
	 * 
	 * @param ctx
	 * @return true if content is create.
	 * @throws Exception
	 */
	public boolean initContent(ContentContext ctx) throws Exception;

	public Date getCreationDate();

	public Date getModificationDate();

	void setPage(MenuElement inPage);

	public String getSpecialTagTitle(ContentContext ctx) throws Exception;

	public String getSpecialTagXHTML(ContentContext ctx) throws Exception;
	
	/**
	 * get a simple layout for the component.
	 * @return
	 */
	public ComponentLayout getLayout();
	
	/**
	 * check of component is considered as the same than an other.
	 * @param ctx
	 * @param comp
	 * @return
	 */
	public boolean equals(ContentContext ctx, IContentVisualComponent comp);
	
	/**
	 * return the description of the page if this component is or contains description.
	 * @return
	 */
	public String getPageDescription(ContentContext ctx);
	
	/**
	 * return the name of group component.
	 * @return
	 */
	public String getListGroup();
	
	/**
	 * warning message on content tab
	 * @param ctx
	 * @return
	 */
	public GenericMessage getContentMessage(ContentContext ctx);
	
	/**
	 * warning message on text tab
	 * @param ctx
	 * @return
	 */
	public GenericMessage getTextMessage(ContentContext ctx);
	
	/**
	 * warning message on config tab
	 * @param ctx
	 * @return
	 */
	public GenericMessage getConfigMessage(ContentContext ctx);
	
	/**
	 * mark component as new in the current request
	 * @param ctx
	 * @return
	 */
	public void markAsNew(ContentContext ctx);
	
	/**
	 * check if this component has maked has new in the current request
	 * @param ctx
	 * @return
	 */
	public boolean isNew(ContentContext ctx);
	
	/**
	 * return true if the component is directly edited when it is insered.
	 * @param ctx
	 * @return
	 */
	public boolean isEditOnCreate(ContentContext ctx);
	
	public boolean isNolink();
	
	
	/**
	 * if this component is duplicated, by default it is mirrored.
	 * @return
	 */
	public boolean isMirroredByDefault(ContentContext ctx);
	

}
