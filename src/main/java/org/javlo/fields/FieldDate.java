package org.javlo.fields;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.core.IDate;
import org.javlo.context.ContentContext;
import org.javlo.fields.Field.FieldBean;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldDate extends Field implements IDate {
	
	protected static final String MONTH_SEARCH_TYPE = "month";
	
	public class FieldDateBean extends FieldBean {

		public FieldDateBean(ContentContext ctx) {
			super(ctx);		
		}
		
		public String getShortDate() throws FileNotFoundException, IOException {
			return StringHelper.renderShortDate(ctx, FieldDate.this.getDate(ctx));
		}
		
		public String getMediumDate() throws FileNotFoundException, IOException {
			return StringHelper.renderMediumDate(ctx, FieldDate.this.getDate(ctx));
		}
		
		public String getFullDate() throws FileNotFoundException, IOException {
			return StringHelper.renderFullDate(ctx, FieldDate.this.getDate(ctx));
		}	
		
		public String getFormatedDate() throws FileNotFoundException, IOException {
			String format = getMetaData("format");
			if (format == null) {
				return getShortDate();
			} else {
				SimpleDateFormat dateFormat = new SimpleDateFormat(format, new Locale(ctx.getRequestContentLanguage()));
				return dateFormat.format(FieldDate.this.getDate(ctx));				
			}
		}
		
		public boolean isPast() {
			if (getDate() == null) {
				return false;
			}
			Calendar now = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();
			cal.setTime(getDate());
			return cal.before(now);
		}
	}

	@Override
	public boolean validate() {
		if (getValue() == null || getValue().trim().length() == 0) {
			return true;
		}
		try {
			StringHelper.parseDate(getValue());
		} catch (ParseException e) {
			setMessage(e.getMessage());
			setMessageType(Field.MESSAGE_ERROR);
			return false;
		}
		return super.validate();
	}
	
	@Override
	protected FieldBean newFieldBean(ContentContext ctx) {	
		return new FieldDateBean(ctx);
	}

	@Override
	public Date getDate(ContentContext ctx) {
		return getDate();
	}
	
	@Override
	public boolean isValidDate(ContentContext ctx) { 
		return getValue() != null && getValue().trim().length() > 0;
	}
	
	protected Date getDate() {
		try {
			if (getValue() != null && getValue().trim().length() > 0) {
				return StringHelper.parseDate(getValue());
			}
		} catch (ParseException e) {
		}
		return null;
	}
	
	@Override
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {		
		if (getSearchType().equals(DEFAULT_SEARCH_TYPE)) {
			return super.getSearchEditXHTMLCode(ctx);
		} else if (getSearchType().equals(MONTH_SEARCH_TYPE)) {
			Map<String,String> values = new LinkedHashMap<String, String>();
			values.put("", "");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("MMMMM");
			for (int i=0; i<=11; i++) {
				cal.set(Calendar.MONTH, i);
				values.put(""+i, format.format(cal.getTime()));
			}
			return renderSelect(ctx, getSearchLabel(new Locale(ctx.getContextRequestLanguage())), "", values, false, "field-"+getName());
		} else {
			return"bad search type : "+getSearchType();
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String displayStr = StringHelper.neverNull(getValue());
		if (displayStr.trim().length() == 0 || !isViewDisplayed()) {
			return "";
		}

		String format = getMetaData("format");
		if (format == null) {
			out.println(XHTMLHelper.textToXHTML(displayStr));
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format, new Locale(ctx.getRequestContentLanguage()));
			try {
				out.println(dateFormat.format(StringHelper.parseDate(displayStr)));
			} catch (ParseException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "date";
	}
	
	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		setValue(StringHelper.renderDate(new Date()));
		return true;
	}
	
	@Override
	public String getSpecialClass() {
		return " datepicker";
	}
	
	@Override
	public boolean search(ContentContext ctx, String query) {
		if (!getSearchType().equals(MONTH_SEARCH_TYPE)) {
			return super.search(ctx, query);
		} else {
			if (getDate(ctx) == null) {
				return false;
			} else {
				Calendar cal = Calendar.getInstance();
				cal.setTime(getDate(ctx));
				if ((""+cal.get(Calendar.MONTH)).equals(query)) {
					return true;
				} else {
					return false;
				}
			}
		}
	}
	
	@Override
	public int compareTo(Field o) {
		if (getDate() == null || o == null) {
			return 0;
		}
		if (!(o instanceof FieldDate)) {
			return super.compareTo(o);
		} else {
			if (((FieldDate)o).getDate() == null)  {
				return 0;
			} else {
				return getDate().compareTo(((FieldDate)o).getDate());
			}
		}		
	}
	
	public FieldBean getBean(ContentContext ctx) {
		return new FieldDateBean(ctx);
	}

}
