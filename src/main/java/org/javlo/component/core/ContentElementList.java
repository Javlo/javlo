/*
 * Created on 20 ao?t 2003
 */
package org.javlo.component.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.javlo.component.container.RepeatContainer;
import org.javlo.component.title.MenuTitle;
import org.javlo.component.title.PageTitle;
import org.javlo.component.title.SubTitle;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author pvanderm
 */
public class ContentElementList implements IContentComponentsList {
	
	private class ContentElementListIterator implements Iterator<IContentVisualComponent> {
		
		private ContentContext ctx;
		private ContentElementList contentElementList;
		
		public ContentElementListIterator(ContentContext ctx, ContentElementList contentElementList) {
			super();
			this.ctx = new ContentContext(ctx);
			this.contentElementList = contentElementList;
		}

		@Override
		public boolean hasNext() {
			return contentElementList.hasNext(ctx);
		}

		@Override
		public IContentVisualComponent next() {
			return contentElementList.next(ctx);
		}

		@Override
		public void remove() {
			throw new NotImplementedException();
		}
		
	}
	
	private class ContentElementListIterable implements Iterable<IContentVisualComponent> {
		
		private ContentContext ctx;
		private ContentElementList contentElementList;

		public ContentElementListIterable(ContentContext ctx, ContentElementList contentElementList) {
			super();
			this.ctx = ctx;
			this.contentElementList = contentElementList;
		}

		@Override
		public Iterator iterator() { 
			return new ContentElementListIterator(ctx,contentElementList);
		}
		
	}

	private final Set<String> addedElementId = new HashSet<String>();

	private final Set<String> repeatAddedElementId = new HashSet<String>();

	private LinkedList<IContentVisualComponent> contentElements = new LinkedList<IContentVisualComponent>();

	private final LinkedList<IContentVisualComponent> repeatContentElements = new LinkedList<IContentVisualComponent>();

	private int pos = 0;

	// private String area;

	private boolean allArea = false; // browse only the content of the current area.

	private MenuElement page;

	private String language;

	public ContentElementList() {
	};

	public ContentElementList(ComponentBean[] beans, ContentContext ctx, MenuElement inPage, boolean allArea) throws Exception {

		language = ctx.getRequestContentLanguage();

		this.allArea = allArea;
		page = inPage;

		ContentService content = ContentService.getInstance(ctx.getRequest());

		IContentVisualComponent previousComponent = null;
		for (ComponentBean bean : beans) {
			if (bean != null && bean.getLanguage() != null && bean.getLanguage().equals(language)) {
				IContentVisualComponent comp = content.getCachedComponent(ctx, bean.getId());
				//IContentVisualComponent comp = content.getComponent(ctx, bean.getId());
				if (comp == null) {
					comp = ComponentFactory.createComponent(ctx, bean, inPage, previousComponent, null);
					content.setCachedComponent(ctx,comp);
				}
				previousComponent = comp;
				contentElements.add(comp);
			}
		}
	};

	public ContentElementList(ContentContext ctx, MenuElement inPage, boolean allArea) throws Exception {
		this(new ComponentBean[0], ctx, inPage, allArea);
	}

	public ContentElementList(ContentElementList list) {
		contentElements.addAll(list.contentElements);
		pos = list.pos;
		language = list.language;
		allArea = list.allArea;
	}

	protected void addElement(IContentVisualComponent elem) {
		/* need this test for repeat element */
		if (!addedElementId.contains(elem.getId())) {
			contentElements.add(elem);
			addedElementId.add(elem.getId());
		}
	}

	protected void addElementAsFirst(IContentVisualComponent elem) {
		/* need this test for repeat element */
		if (!addedElementId.contains(elem.getId())) {
			contentElements.addFirst(elem);
			addedElementId.add(elem.getId());
		}
	}

	public void addRepeatElement(IContentVisualComponent elem) {
		/* need this test for repeat element */
		if (!repeatAddedElementId.contains(elem.getId())) {
			repeatContentElements.add(elem);
			repeatAddedElementId.add(elem.getId());
		}
	}

	/**
	 * Return an iterable instance of this {@link ContentElementList} calling {@link #hasNext(ContentContext)} and {@link #next(ContentContext)} with the <code>ctx</code> parameter. <br/>
	 * WARNING: {@link #initialize()} is called when {@link Iterable#iterator()} is called.
	 * 
	 * @param ctx
	 * @return
	 */
	public Iterable<IContentVisualComponent> asIterable(final ContentContext ctx) {
		return new Iterable<IContentVisualComponent>() {
			@Override
			public Iterator<IContentVisualComponent> iterator() {
				initialize(ctx);
				return new Iterator<IContentVisualComponent>() {
					@Override
					public boolean hasNext() {
						return ContentElementList.this.hasNext(ctx);
					}

					@Override
					public IContentVisualComponent next() {
						return ContentElementList.this.next(ctx);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Cannot modify ContentElementList");
					}
				};
			}
		};
	}

	IContentVisualComponent currentElem(ContentContext ctx) {
		if (getElement(pos - 1) != null) {
			if (!isVisible(ctx, getElement(pos - 1))) {
				return next(ctx);
			}
		}
		return getElement(pos - 1);
	}

	IContentVisualComponent firstElement(ContentContext ctx) {
		Iterator<IContentVisualComponent> compIt = contentElements.iterator();
		while (compIt.hasNext()) {
			IContentVisualComponent comp = compIt.next();
			if (isVisible(ctx, comp)) {
				return comp;
			}
		}
		return null;
	}

	IContentVisualComponent getElement(int elemPos) {
		if ((elemPos >= contentElements.size()) || (elemPos < 0)) {
			return null;
		} else {
			return contentElements.get(elemPos);
		}
	}

	public String getLabel(ContentContext ctx) {
		String res = "";
		Iterator elems = contentElements.iterator();
		
		String firstSubtitle = null;
		
		while (elems.hasNext()) {
			IContentVisualComponent comp = (IContentVisualComponent) elems.next();
			if (comp.isLabel(ctx) && !comp.isRepeat()) {
				res = comp.getTextTitle(ctx);
				if (res == null) {
					res = "";
				}
			}
			if (comp instanceof MenuTitle && !comp.isRepeat()) {
				return comp.getTextLabel();
			}
			if (firstSubtitle == null && comp instanceof SubTitle) {
				firstSubtitle = comp.getTextLabel();
			}
		}
		if (res.length() == 0) { // if no element not repeat search with repeat element
			elems = contentElements.iterator();
			while (elems.hasNext()) {
				IContentVisualComponent comp = (IContentVisualComponent) elems.next();
				if (comp.isLabel(ctx)) {
					res = comp.getTextLabel();
					if (res == null) {
						res = "";
					}
				}
				if (comp instanceof MenuTitle) {
					return comp.getTextLabel();
				}
			}
		}
		if (res.length() == 0 && firstSubtitle != null) {			
			return firstSubtitle;
		}
		return res;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	public MenuElement getPage() {
		return page;
	}

	public String getPageTitle(ContentContext ctx) {

		String res = null;
		Iterator elems = contentElements.iterator();
		while (elems.hasNext()) {
			IContentVisualComponent comp = (IContentVisualComponent) elems.next();
			if (comp instanceof PageTitle) {
				res = comp.getTextTitle(ctx);
				if (res == null) {
					res = "";
				}
				return res;
			}
		}

		return res;
	}

	@Override
	public String getPrefixXHTMLCode(ContentContext ctx) {

		StringBuffer prefix = new StringBuffer();

		if (contentElements.size() != 0) {

			if (ctx.getRenderMode() != ContentContext.EDIT_MODE) {

				if (isPrevious(ctx)) {
					if (!previousElem(ctx).getListGroup().equals(currentElem(ctx).getListGroup())) {
						prefix.append(currentElem(ctx).getFirstPrefix(ctx));
					}
				} else {
					prefix.append(currentElem(ctx).getFirstPrefix(ctx));
				}
			}
		}

		return prefix.toString();
	}
	
	public String getSubTitle(ContentContext ctx) {
		String res = "";
		Iterator elems = contentElements.iterator();		
		while (elems.hasNext()) {
			IContentVisualComponent comp = (IContentVisualComponent) elems.next();			
			if (comp instanceof SubTitle) {
				res = comp.getValue(ctx);
				if (res == null) {
					res = "";
				} else {
					return res;
				}
			}
		}
		return "";
	}

	@Override
	public String getSufixXHTMLCode(ContentContext ctx) {

		StringBuffer sufix = new StringBuffer();
		if (ctx.getRenderMode() != ContentContext.EDIT_MODE) {
			if (contentElements.size() != 0) {
				if (isNext(ctx)) {
					if (!nextElem(ctx).getListGroup().equals(currentElem(ctx).getListGroup())) {					
						sufix.append(currentElem(ctx).getLastSufix(ctx));
					}
				} else {
					sufix.append(currentElem(ctx).getLastSufix(ctx));
				}
			}
		}
		return sufix.toString();
	}

	public String getTitle(ContentContext ctx) {
		return getTitle(ctx,true);
	}

	public String getLocalTitle(ContentContext ctx) {
		return getTitle(ctx,false);
	}

	private String getTitle(ContentContext ctx, boolean repeat) {

		String res = "";
		Iterator elems = contentElements.iterator();
		while (elems.hasNext() && res.length() == 0) {
			IContentVisualComponent comp = (IContentVisualComponent) elems.next();
			if (comp.isLabel(ctx) && !comp.isRepeat()) {
				res = comp.getTextTitle(ctx);
				if (res == null) {
					res = "";
				}
			}
		}
		if (repeat) {
			if (res.length() == 0) { // if no element not repeat search with repeat element
				elems = contentElements.iterator();
				while (elems.hasNext() && res.length() == 0) {
					IContentVisualComponent comp = (IContentVisualComponent) elems.next();
					if (comp.isLabel(ctx)) {
						res = comp.getTextTitle(ctx);
						if (res == null) {
							res = "";
						}
					}
				}
			}
		}
		if (res.length() == 0) {
			elems = contentElements.iterator();
			while (elems.hasNext()) {
				IContentVisualComponent comp = (IContentVisualComponent) elems.next();
				if (comp.getType().equals(PageTitle.TYPE)) {
					if (repeat || !comp.isRepeat()) {
						res = comp.getTextTitle(ctx);
						if (res == null) {
							res = "";
						}
						return res;
					}
				}

			}
		}
		return res;
	}

	public String getXHTMLTitle(ContentContext ctx) throws Exception {
		String res = "";
		Iterator elems = contentElements.iterator();
		while (elems.hasNext()) {
			IContentVisualComponent comp = (IContentVisualComponent) elems.next();
			if (comp.isLabel(ctx)) {
				res = comp.getXHTMLCode(ctx);
			}

		}
		return res;
	}

	@Override
	public boolean hasNext(ContentContext ctx) {
		return isNext(ctx);
	}

	/**
	 * return to the start of the list
	 */
	@Override
	public void initialize(ContentContext ctx) {

		if (repeatContentElements.size() > 0) {

			LinkedList<IContentVisualComponent> newContentElements = new LinkedList<IContentVisualComponent>();

			boolean repeatComponentFound = false;
			for (IContentVisualComponent comp : contentElements) {
				if (comp instanceof RepeatContainer) {
					if (!((RepeatContainer) comp).isBlockRepeat(ctx)) {
						newContentElements.addAll(repeatContentElements);
					}
					repeatComponentFound = true;
				} else {
					newContentElements.add(comp);
				}
			}

			if (repeatComponentFound) {
				contentElements = newContentElements;
			} else {
				LinkedList<IContentVisualComponent> comps = new LinkedList<IContentVisualComponent>();
				for (IContentVisualComponent comp : repeatContentElements) {
					if (comp.isFirstRepeated()) {
						comps.addFirst(comp);
					} else {
						addElement(comp);
					}
				}
				for (IContentVisualComponent compElem : comps) {
					addElementAsFirst(compElem);
				}
			}
		}

		pos = 0;
	}

	boolean isNext(ContentContext ctx) {
		boolean isNext = nextElem(ctx) != null;
		return isNext;
	}

	boolean isPrevious(ContentContext ctx) {
		return previousElem(ctx) != null;
	}

	private boolean isVisible(ContentContext ctx, IContentVisualComponent comp) {
		boolean outVisibility;
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE || allArea) {
			outVisibility = true;
		} else {
			outVisibility = comp.isVisible(ctx);
		}
		if (!(allArea || ctx.getArea() == null)) {
			outVisibility = outVisibility && comp.getArea().equals(ctx.getArea());
		}		
		return outVisibility;

	}

	@Override
	public IContentVisualComponent next(ContentContext ctx) {

		IContentVisualComponent comp = getElement(pos);
		pos = pos + 1;
		while ((comp != null) && (!isVisible(ctx, comp))) {
			comp = getElement(pos);
			pos = pos + 1;
		}
		return comp;
	}

	IContentVisualComponent nextElem(ContentContext ctx) {
		IContentVisualComponent comp = getElement(pos);

		int i = 1;
		while ((comp != null) && (!isVisible(ctx, comp))) {
			comp = getElement(pos + i);
			i++;
		}
		return comp;
	}

	IContentVisualComponent previousElem(ContentContext ctx) {
		IContentVisualComponent comp = getElement(pos - 2);

		int i = 3;
		while ((comp != null) && (!isVisible(ctx, comp))) {
			comp = getElement(pos - i);
			i++;
		}
		return comp;
	}

	public int realSize() {
		return contentElements.size();
	}

	@Override
	public void setAllArea(boolean inAllArea) {
		allArea = inAllArea;
	}

	public void setPage(MenuElement page) {
		this.page = page;
	}

	@Override
	public int size(ContentContext ctx) {

		int outSize = 0;

		Iterator<IContentVisualComponent> compIt = contentElements.iterator();
		while (compIt.hasNext()) {
			IContentVisualComponent comp = compIt.next();
			if (isVisible(ctx, comp)) {
				outSize++;
			}
		}

		return outSize;
	}
	
	public Iterable<IContentVisualComponent> getIterable(ContentContext ctx) {
		return new ContentElementListIterable(ctx,this);
	}

}
