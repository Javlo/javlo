package org.javlo.service.google.translation;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleSheetService {

    private final Sheets sheetsService;
    private final String spreadsheetId;

    public GoogleSheetService(String credentialsPath, String spreadsheetId) throws IOException, GeneralSecurityException {
        this.spreadsheetId = spreadsheetId;

        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        )
                .setApplicationName("Java Google Sheets API")
                .build();
    }

    /**
     *  @param range
     * "Sheet1"	Toute la plage utilisée de la feuille "Sheet1"
     * "Feuille 1!A1"	Une seule cellule
     * "Feuille 1!A1:B2"	Une plage de 4 cellules (2 lignes × 2 colonnes)
     * "Sheet1!A:A"	Toute la colonne A (non vide)
     * "Sheet1!2:2"	Toute la ligne 2 (non vide)
     *
     * @return
     * @throws IOException
     */
    public List<List<Object>> readRange(String fullRange) throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, fullRange)
                .execute();
        return response.getValues();
    }

    public List<List<Object>> readAll(String sheetName) throws IOException {
        // Utilise la plage complète du classeur spécifié
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();

        return response.getValues();
    }

    public String readAllAsHTML(String sheetName, String cssClass) throws IOException {
        // Lit toute la plage remplie de cette feuille
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return "<div class=\"" + cssClass + "\"><table></table></div>"; // retourne un tableau vide
        }

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<div class=\"").append(cssClass).append("\"><table>");

        for (List<Object> row : values) {
            htmlBuilder.append("<tr>");
            for (Object cell : row) {
                htmlBuilder.append("<td>");
                htmlBuilder.append(cell.toString());
                htmlBuilder.append("</td>");
            }
            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</table></div>");

        return htmlBuilder.toString();
    }

    public String readAllAsCSV(String sheetName) throws IOException {
        // Lit toute la plage remplie de cette feuille
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return ""; // ou throw exception
        }

        StringBuilder csvBuilder = new StringBuilder();

        for (List<Object> row : values) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i).toString();

                // Protège les cellules contenant des virgules, guillemets ou retours à la ligne
                if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                    cell = cell.replace("\"", "\"\""); // échappe les "
                    cell = "\"" + cell + "\"";
                }

                csvBuilder.append(cell);

                if (i < row.size() - 1) {
                    csvBuilder.append(",");
                }
            }
            csvBuilder.append("\n");
        }

        return csvBuilder.toString();
    }

    public void writeRange(String range, List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange().setValues(values);
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    /**
     * Liste toutes les feuilles disponibles dans le spreadsheet
     */
    public List<String> getSheetNames() throws IOException {
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        List<String> sheetNames = new ArrayList<>();
        for (Sheet sheet : spreadsheet.getSheets()) {
            sheetNames.add(sheet.getProperties().getTitle());
        }
        return sheetNames;
    }

    public static void main(String[] args) {
    try {
        // Appel de la méthode readAllAsCSV
        GoogleSheetService googleSheetService = new GoogleSheetService("C:/Users/pvand/data/javlo/data-ctx/data-sexy/_private/credentials/standline.json", "1ol-dqJBMFAxN7cfZVlhTLBZS8dRTcyorb2ayPE0R1MY");
        
        // Lister les feuilles disponibles
        List<String> sheetNames = googleSheetService.getSheetNames();
        System.out.println("Feuilles disponibles : " + sheetNames);
        
        // Utiliser la première feuille disponible ou "test" si elle existe
        String sheetName = sheetNames.contains("test") ? "test" : sheetNames.get(0);
        System.out.println("Utilisation de la feuille : " + sheetName);
        
        String csvData = googleSheetService.readAllAsCSV(sheetName);
        System.out.println("CSV Data: \n" + csvData);

        // Appel de la méthode writeRange avec la bonne feuille
        List<List<Object>> values = Arrays.asList(
                Arrays.asList("A1", "B1", "C1"),
                Arrays.asList("A2", "B2", "Patrick")
        );
        googleSheetService.writeRange(sheetName + "!A1:C2", values);
        System.out.println("Data written to the sheet.");

    } catch (IOException | GeneralSecurityException e) {
        e.printStackTrace();
    }

    }
}
