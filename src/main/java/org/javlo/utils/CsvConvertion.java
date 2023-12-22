package org.javlo.utils;

import org.javlo.helper.StringHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CsvConvertion {

    public static void csvToSwitch(File source, File target) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(target))) {
            List<Map<String, String>> data = CSVFactory.loadContentAsMap(source);
            List<String> postCodeDone = new LinkedList<>();
            for (Map<String, String> line : data) {
                //out.write(line.get("Code postal")+" = "+line.get("Dureté de l'eau (en f°)"));

                String postCode = StringHelper.neverEmpty(line.get("Code postal"), "").trim();
                if (!StringHelper.isDigit(postCode)) {
                    System.out.println(("ERROR BAD POST CODE : ")+postCode);
                }
                if (!postCodeDone.contains(postCode) && StringHelper.isDigit(postCode)) {
                    String dur = line.get("Dureté de l'eau (en f°)");
                    if (!StringHelper.isEmpty(dur)) {
                        out.write("case " + postCode + " :%>" + dur.replace(",", ".") + "<%break;");
                        out.newLine();
                    }
                    postCodeDone.add(postCode);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void csvToMap(File source, File target) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(target))) {
            List<Map<String, String>> data = CSVFactory.loadContentAsMap(source);
            List<String> postCodeDone = new LinkedList<>();
            for (Map<String, String> line : data) {
                //out.write(line.get("Code postal")+" = "+line.get("Dureté de l'eau (en f°)"));

                String postCode = StringHelper.neverEmpty(line.get("Code postal"), "").trim();
                postCode = StringHelper.trim(postCode, '0');
                if (!StringHelper.isDigit(postCode)) {
                    System.out.println(("ERROR BAD POST CODE : ")+postCode);
                }
                if (!postCodeDone.contains(postCode) && StringHelper.isDigit(postCode)) {
                    String dur = line.get("Dureté de l'eau (en f°)");
                    if (!StringHelper.isEmpty(dur)) {
                        //out.write("case " + postCode + " :%>" + dur.replace(",", ".") + "<%break;");
                        out.write("data.put((long)"+postCode+", "+Math.round(Double.parseDouble(dur.replace(",", ".")))+");");
                        out.newLine();
                    }
                    postCodeDone.add(postCode);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void csvToProperties(File source, File target) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(target))) {
            List<Map<String, String>> data = CSVFactory.loadContentAsMap(source);
            List<String> postCodeDone = new LinkedList<>();
            for (Map<String, String> line : data) {
                //out.write(line.get("Code postal")+" = "+line.get("Dureté de l'eau (en f°)"));

                String postCode = StringHelper.neverEmpty(line.get("Code postal"), "").trim();
                postCode = StringHelper.trim(postCode, '0');
                if (!StringHelper.isDigit(postCode)) {
                    System.out.println(("ERROR BAD POST CODE : ")+postCode);
                }
                if (!postCodeDone.contains(postCode) && StringHelper.isDigit(postCode)) {
                    String dur = line.get("Dureté de l'eau (en f°)");
                    if (!StringHelper.isEmpty(dur)) {
                        //out.write("case " + postCode + " :%>" + dur.replace(",", ".") + "<%break;");
                        out.write(postCode+" = "+Math.round(Double.parseDouble(dur.replace(",", "."))));
                        out.newLine();
                    }
                    postCodeDone.add(postCode);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
       /* {
            File source = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\belgique_durete_de_l_eau_clean.csv");
            File target = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\belgique_durete_de_l_eau_clean.txt");
            csvToSwitch(source, target);
        }*/

        {
            File source = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\espagne_durete_de_l_eau.csv");
            File target = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\dur_eau-es.properties");
            csvToProperties(source, target);
        }

        {
            File source = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\belgique_durete_de_l_eau_clean.csv");
            File target = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\dur_eau-be.properties");
            csvToProperties(source, target);
        }

        {
            File source = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\france_durete_de_l_eau.csv");
            File target = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\dur_eau-fr.properties");
            csvToProperties(source, target);
        }

        {
            File source = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\france_durete_de_l_eau.csv");
            File target = new File("G:\\Drive partagés\\Clients\\Solucalc\\2023\\Donnée de dureté de l'eau\\dur_eau-fr.properties");
            csvToProperties(source, target);
        }
    }
}
