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
import java.util.*;

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

    public String readAllAsHTML(String sheetName, String cssClass, boolean underscoreFiltered) throws IOException {
        // Read the full data range of the sheet
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return "<div class=\"" + cssClass + "\"><table></table></div>"; // return empty table
        }

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<div class=\"").append(cssClass).append("\"><table>");

        // Identify columns to remove if underscoreFiltered is true
        Set<Integer> removedColumns = new HashSet<>();
        if (underscoreFiltered && !values.isEmpty()) {
            List<Object> headerRow = values.get(0);
            for (int i = 0; i < headerRow.size(); i++) {
                Object header = headerRow.get(i);
                if (header != null && header.toString().startsWith("_")) {
                    removedColumns.add(i);
                }
            }
        }

        // Build HTML table
        for (List<Object> row : values) {
            htmlBuilder.append("<tr>");
            for (int i = 0; i < row.size(); i++) {
                if (removedColumns.contains(i)) continue; // skip filtered column
                Object cell = row.get(i);
                htmlBuilder.append("<td>")
                        .append(cell != null ? cell.toString() : "")
                        .append("</td>");
            }
            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</table></div>");

        return htmlBuilder.toString();
    }


    public String readAllAsCSV(String sheetName, boolean secured) throws IOException {
        // Read the full data range of the sheet
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            return ""; // return empty string if no data
        }

        StringBuilder csvBuilder = new StringBuilder();

        // Identify columns to remove if secured is true
        Set<Integer> removedColumns = new HashSet<>();
        if (secured && !values.isEmpty()) {
            List<Object> headerRow = values.get(0);
            for (int i = 0; i < headerRow.size(); i++) {
                Object header = headerRow.get(i);
                if (header != null && header.toString().startsWith("_")) {
                    removedColumns.add(i);
                }
            }
        }

        // Build CSV content
        for (List<Object> row : values) {
            boolean firstCell = true;
            for (int i = 0; i < row.size(); i++) {
                if (removedColumns.contains(i)) continue; // skip filtered column

                if (!firstCell) {
                    csvBuilder.append(",");
                } else {
                    firstCell = false;
                }

                String cell = row.get(i) != null ? row.get(i).toString() : "";

                // Escape commas, quotes, and newlines
                if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                    cell = cell.replace("\"", "\"\""); // escape double quotes
                    cell = "\"" + cell + "\"";
                }

                csvBuilder.append(cell);
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
        
        String csvData = googleSheetService.readAllAsCSV(sheetName, true);
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
