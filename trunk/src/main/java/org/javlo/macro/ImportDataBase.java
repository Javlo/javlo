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

	// static final String SQL_ALL_TECHNICAL = "select distinct technical.*, technical_has_type.type_id type, country.name country, trans_en.description desc_en, trans_fr.description desc_fr from technical,technical_has_country, technical_has_type, country, technical_translation trans_en, technical_translation trans_fr where technical.environment_id='LIVE' and technical.id = technical_has_country.technical_id and country.id = technical_has_country.country_id and trans_fr.language='fr' and trans_fr.technical_id=technical.id and trans_en.language='en' and trans_en.technical_id=technical.id and technical_has_type.technical_id=technical.id;";

	static final String SQL_ALL_TECHNICAL_FR = "select distinct technical.*, technical_translation.*, technical_has_type.type_id type, technical_type.name, country.name country from technical, technical_translation, technical_type, technical_has_type, technical_has_country, country where technical_translation.language='fr' and technical_translation.technical_id=technical.id and technical_type.id = technical_has_type.type_id and technical_has_type.technical_id = technical.id and technical.environment_id = 'live' and technical.id = technical_has_country.technical_id and country.id = technical_has_country.country_id";
	static final String SQL_ALL_TECHNICAL_EN = SQL_ALL_TECHNICAL_FR.replace("'fr'", "'en'");
	static final Map<String, String> technicalTypes = new HashMap<String, String>();

	static final String SQL_JURIDIQUE_FR = "select distinct juridique.*, juridique_translation.*, juridique_has_type.type_id type, juridique_type.name, country.name country from juridique, juridique_translation, juridique_type, juridique_has_type, juridique_has_country, country where juridique_translation.language='fr' and juridique_translation.juridique_id=juridique.id and juridique_type.id = juridique_has_type.type_id and juridique_has_type.juridique_id = juridique.id and juridique.environment_id = 'live' and juridique.id = juridique_has_country.juridique_id and country.id = juridique_has_country.country_id";
	static final String SQL_JURIDIQUE_EN = SQL_JURIDIQUE_FR.replace("'fr'", "'en'");
	static final Map<String, String> juriTypes = new HashMap<String, String>();

	static final String SQL_BANK_FR = "select distinct bank.*, bank_translation.*, bank_has_type.type_id type, type.name, country.name country from bank, bank_translation, type, bank_has_type, bank_has_country, country where bank_translation.language='fr' and bank_translation.bank_id=bank.id and type.id = bank_has_type.type_id and bank_has_type.bank_id = bank.id and bank.environment_id = 'live' and bank.id = bank_has_country.bank_id and country.id = bank_has_country.country_id";
	static final String SQL_BANK_EN = SQL_BANK_FR.replace("'fr'", "'en'");
	static final Map<String, String> bankTypes = new HashMap<String, String>();

	static final Map<String, String> countries = new HashMap<String, String>();



	@Override
	public String getName() {
		return "import-database";
	}

	protected void importItem(ContentContext ctx, MenuElement currentPage, Map<String, DynamicComponent> compCache, ResultSet rs, Map<String,String> types, String componentType, String lg) throws Exception {
		String name = rs.getString("name");

		ContentService content = ContentService.createContent(ctx.getRequest());

		if (compCache.get(name) != null) {
			String type = "";
			if (compCache.get(name).getField(ctx, "type") != null) {
				type = compCache.get(name).getField(ctx, "type").getValue();
			}
			if (type == null) {
				type = "";
			}
			String dbType = rs.getString("type");
			if (types.get(dbType) == null) {
				System.out.println("**** WARNING : type not found : " + dbType);
			} else {
				if (!type.contains(types.get(dbType))) {
					compCache.get(name).getField(ctx, "type").setValue(type + ';' + types.get(dbType));
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
			String parentId = MacroHelper.addContent(ctx.getRequestContentLanguage(), currentPage, "0", componentType, "");
			DynamicComponent comp = (DynamicComponent) content.getComponent(ctx, parentId);

			comp.getField(ctx, "title").setValue(name);
			comp.getField(ctx, "web").setValue(rs.getString("website"));
			comp.getField(ctx, "phone").setValue(rs.getString("phone"));
			comp.getField(ctx, "email").setValue(rs.getString("email"));
			comp.getField(ctx, "address").setValue(rs.getString("address").replace("<p>&nbsp;</p>", ""));

			String ctr = rs.getString("country");
			String javloCtr = countries.get(ctr);
			System.out.println("**** CREATE "+componentType+" : " + name + " (ctr=" + ctr + " - javloCtr=" + javloCtr + ") ****");
			if (javloCtr == null) {
				System.out.println("*** WARNING : bad country : " + ctr);
			} else {
				comp.getField(ctx, "countries").setValue(javloCtr);
			}

			String type = rs.getString("type");
			String javloType = types.get(type);
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

		countries.put("Algérie", "algeria");
		countries.put("Bénin", "benin");
		countries.put("Burundi", "burundi");
		countries.put("République Démocratique du Congo", "drc");
		countries.put("Maroc", "marocco");
		countries.put("Mozambique", "mozambique");
		countries.put("Mali", "mali");
		countries.put("Niger", "niger");
		countries.put("Rwanda", "rwanda");
		countries.put("Sénégal", "senegal");
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
		
		juriTypes.put("1", "lawyers");
		juriTypes.put("2", "notaries");
		
		bankTypes.put("1","investmentloan"); 
		bankTypes.put("2","leasing"); 
		bankTypes.put("3","discountcredit"); 
		bankTypes.put("4","guarantees"); 
		bankTypes.put("5","cashcredit"); 
		bankTypes.put("6","straightloan"); 
		bankTypes.put("8","factoring"); 
		bankTypes.put("9","venturecapital");
		bankTypes.put("10","insuranceimportexport");
		bankTypes.put("11","quasicapital");
		bankTypes.put("12","smallloans");

		/** import general **/
		Connection conn = getConnection();

		// IMPORT GENERAL
		ResultSet rs = readDataBase(conn, SQL_ALL_GENERAL);

		ContentService content = ContentService.createContent(ctx.getRequest());

		MenuElement dataPage = MacroHelper.addPageIfNotExist(ctx, content.getNavigation(ctx), "data", false, false);

		// MenuElement currentPage = content.getNavigation(ctx).searchChildFromName("general");
		MenuElement currentPage = MacroHelper.addPageIfNotExist(ctx, dataPage, "general", true, false);

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

		currentPage = currentPage = MacroHelper.addPageIfNotExist(ctx, dataPage, "technical", true, false);

		Map<String, DynamicComponent> compCache = new HashMap<String, DynamicComponent>();

		rs = readDataBase(conn, SQL_ALL_TECHNICAL_FR);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs, technicalTypes, "technical", "fr");
		}
		rs.close();

		rs = readDataBase(conn, SQL_ALL_TECHNICAL_EN);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs, technicalTypes, "technical", "en");
		}
		rs.close();

		// IMPORT JURIDIQUE

		currentPage = currentPage = MacroHelper.addPageIfNotExist(ctx, dataPage, "juridique", true, false);

		compCache = new HashMap<String, DynamicComponent>();

		rs = readDataBase(conn, SQL_JURIDIQUE_FR);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs, juriTypes, "juridique", "fr");
		}
		rs.close();

		rs = readDataBase(conn, SQL_JURIDIQUE_EN);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs,  juriTypes, "juridique", "en");
		}
		rs.close();

		// IMPORT BANK

		currentPage = currentPage = MacroHelper.addPageIfNotExist(ctx, dataPage, "bank", true, false);

		compCache = new HashMap<String, DynamicComponent>();

		rs = readDataBase(conn, SQL_BANK_FR);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs, bankTypes, "bank", "fr");
		}
		rs.close();

		rs = readDataBase(conn, SQL_BANK_EN);
		while (rs.next()) {
			importItem(ctx, currentPage, compCache, rs, bankTypes, "bank", "en");
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
