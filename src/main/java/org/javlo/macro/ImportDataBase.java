package org.javlo.macro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.fields.Field;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class ImportDataBase extends AbstractMacro {

	static final String SQL_ALL_GENERAL = "select distinct general.*, country.name country, trans_en.description desc_en, trans_fr.description desc_fr " + "from general,general_has_country, country, general_translation trans_en, general_translation trans_fr " + "where general.environment_id=\"LIVE\" and general.id = general_has_country.general_id and country.id = general_has_country.country_id and " + "trans_fr.language=\"fr\" and trans_fr.general_id=general.id and trans_en.language=\"en\" and trans_en.general_id=general.id;";

	static final String SQL_ALL_TECHNICAL = "select distinct technical.*, technical_has_type.type_id type, country.name country, trans_en.description desc_en, trans_fr.description desc_fr from technical,technical_has_country, technical_has_type, country, technical_translation trans_en, technical_translation trans_fr where technical.environment_id='LIVE' and technical.id = technical_has_country.technical_id and country.id = technical_has_country.country_id and trans_fr.language='fr' and trans_fr.technical_id=technical.id and trans_en.language='en' and trans_en.technical_id=technical.id and technical_has_type.technical_id=technical.id;";

	static final String SQL_ALL_TECHNICAL_FR = "select distinct technical.*, technical_translation.*, technical_has_type.type_id type, technical_type.name, country.name country from technical, technical_translation, technical_type, technical_has_type, technical_has_country, country where technical_translation.language='fr' and technical_translation.technical_id=technical.id and technical_type.id = technical_has_type.type_id and technical_has_type.technical_id = technical.id and technical.environment_id = 'live' and technical.id = technical_has_country.technical_id and country.id = technical_has_country.country_id";

	static final String SQL_ALL_TECHNICAL_EN = SQL_ALL_TECHNICAL_FR.replace("'fr'", "'en'");

	static final Map<String, String> countries = new HashMap<String, String>();

	static final Map<String, String> technicalTypes = new HashMap<String, String>();

	@Override
	public String getName() {
		return "import-database";
	}

	protected void importItem(ContentContext ctx, MenuElement currentPage, Map<String, DynamicComponent> compCache, ResultSet rs, String lg) throws Exception {
		String name = rs.getString("name");

		ContentService content = ContentService.createContent(ctx.getRequest());

		if (compCache.get(name) != null) {
			String type = "";
			if (compCache.get(name).getField(ctx, "type") != null) {
				type = compCache.get(name).getField(ctx, "type").getValue();
			}
			if (type == null) {
				type ="";
			}			
			String dbType = rs.getString("type");
			if (technicalTypes.get(dbType) == null) {
				System.out.println("**** WARNING : type not found : "+dbType);
			} else {
				if (!type.contains(technicalTypes.get(dbType))) {
					compCache.get(name).getField(ctx, "type").setValue(type + ';' + technicalTypes.get(dbType));
				}
			}

			Field descField = compCache.get(name).getField(ctx, "description");
			descField.setCurrentLocale(new Locale(lg));
			descField.setValue(rs.getString("description"));
			
			Field titleField = compCache.get(name).getField(ctx, "title");
			titleField.setCurrentLocale(new Locale(lg));
			titleField.setValue(rs.getString("title"));

			compCache.get(name).storeProperties();
			compCache.get(name).setModify();
		} else {
			String parentId = MacroHelper.addContent(ctx.getRequestContentLanguage(), currentPage, "0", "technical", "");
			DynamicComponent comp = (DynamicComponent) content.getComponent(ctx, parentId);
			
			comp.getField(ctx, "title").setValue(name);
			comp.getField(ctx, "web").setValue(rs.getString("website"));
			comp.getField(ctx, "phone").setValue(rs.getString("phone"));
			comp.getField(ctx, "email").setValue(rs.getString("email"));
			comp.getField(ctx, "address").setValue(rs.getString("address").replace("<p>&nbsp;</p>", ""));

			String ctr = rs.getString("country");			
			String javloCtr = countries.get(ctr);
			System.out.println("**** CREATE TECHNICAL : "+name+" (ctr="+ctr+" - javloCtr="+javloCtr+") ****");
			if (javloCtr == null) {
				System.out.println("*** WARNING : bad country : " + ctr);
			} else {
				comp.getField(ctx, "countries").setValue(javloCtr);
			}

			String type = rs.getString("type");
			String javloType = technicalTypes.get(type);
			if (javloType == null) {
				System.out.println("*** WARNING : bad type : " + type);
			} else {
				comp.getField(ctx, "type").setValue(javloType);
			}

			Field descField = comp.getField(ctx, "description");
			descField.setCurrentLocale(new Locale(lg));
			descField.setValue(rs.getString("description"));

			comp.storeProperties();
			comp.setModify();

			compCache.put(name, comp);
		}
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		
		countries.put("Alg�rie", "algeria");
		countries.put("B�nin", "benin");
		countries.put("Burundi", "burundi");
		countries.put("R�publique D�mocratique du Congo", "drc");
		countries.put("Maroc", "marocco");
		countries.put("Mozambique", "mozambique");
		countries.put("Mali", "mali");
		countries.put("Niger", "niger");
		countries.put("Rwanda", "rwanda");
		countries.put("S�n�gal", "senegal");
		countries.put("Afrique du Sud", "south-africa");
		countries.put("Tanzanie", "tanzania");
		countries.put("Ouganda", "uganda");
		
		technicalTypes.put("1", "agroali");
		technicalTypes.put("2", "assurance");
		technicalTypes.put("3", "automob");
		technicalTypes.put("4", "bank");
		technicalTypes.put("5", "construction");
		technicalTypes.put("6", "commerce");
		technicalTypes.put("7", "communication");
		technicalTypes.put("8", "education");
		technicalTypes.put("9", "energy");
		technicalTypes.put("10", "industry");
		technicalTypes.put("11", "telecom");
		technicalTypes.put("12", "medecin");
		technicalTypes.put("13", "security");
		technicalTypes.put("14", "textile");
		technicalTypes.put("15", "trans");
		technicalTypes.put("16", "tourism");
		technicalTypes.put("17", "agri");

		/** import general **/
		Connection conn = getConnection();

		// IMPORT GENERAL
		ResultSet rs = readDataBase(conn, SQL_ALL_GENERAL);

		ContentService content = ContentService.createContent(ctx.getRequest());
		
		MenuElement dataPage = MacroHelper.addPageIfNotExist(ctx, content.getNavigation(ctx), "data", false);

		//MenuElement currentPage = content.getNavigation(ctx).searchChildFromName("general");
		MenuElement currentPage = MacroHelper.addPageIfNotExist(ctx, dataPage, "general", true);

		String parentId = "0";
		while (rs.next()) {

			parentId = MacroHelper.addContent(ctx.getRequestContentLanguage(), currentPage, "0", "general", "");
			DynamicComponent comp = (DynamicComponent) content.getComponent(ctx, parentId);

			String name = rs.getString("name");

			System.out.println("*** INSERT GENERAL : " + name);

			comp.getField(ctx, "title").setValue(name);
			comp.getField(ctx, "web").setValue(rs.getString("website"));
			comp.getField(ctx, "phone").setValue(rs.getString("phone"));
			comp.getField(ctx, "email").setValue(rs.getString("email"));
			comp.getField(ctx, "countries").setValue(countries.get(rs.getString("country")));

			Field descField = comp.getField(ctx, "description");
			descField.setCurrentLocale(Locale.FRENCH);
			descField.setValue(rs.getString("desc_fr"));
			descField.setCurrentLocale(Locale.ENGLISH);
			descField.setValue(rs.getString("desc_en"));

			comp.storeProperties();
			comp.setModify();

		}
		rs.close();

		// IMPORT TECHNICAL

		currentPage = currentPage = MacroHelper.addPageIfNotExist(ctx, dataPage, "technical", true);

		Map<String, DynamicComponent> compCache = new HashMap<String, DynamicComponent>();

		rs = readDataBase(conn, SQL_ALL_TECHNICAL_FR);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs, "fr");
		}
		rs.close();

		rs = readDataBase(conn, SQL_ALL_TECHNICAL_EN);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs, "en");
		}
		rs.close();

		conn.close();

		return null;
	}

	Connection getConnection() throws Exception {
		// This will load the MySQL driver, each DB has its own driver
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		return DriverManager.getConnection("jdbc:mysql://localhost/abeo2?user=root");
	}

	ResultSet readDataBase(Connection connect, String sql) throws Exception {
		Statement statement = null;
		try {
			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// Result set get the result of the SQL query
			return statement.executeQuery(sql);

		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String[] args) {
		ImportDataBase iData = new ImportDataBase();
		Connection conn = null;
		try {
			conn = iData.getConnection();
			ResultSet rs = iData.readDataBase(conn, SQL_ALL_GENERAL);
			while (rs.next()) {
				System.out.println("*** name : " + rs.getString("name"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
