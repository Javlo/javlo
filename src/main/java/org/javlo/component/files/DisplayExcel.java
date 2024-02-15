package org.javlo.component.files;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.Cell;
import org.javlo.utils.NeverEmptyMap;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DisplayExcel extends AbstractPropertiesComponent  {

    public static class CellResult {

        public static final int NUMBER = 1;
        public static final int TEXT = 2;

        private String label;
        private int type;
        private double average = 0;
        private Map<String, Integer> count = new NeverEmptyMap<>(String.class, Integer.class);

        private Boolean largeCountLabel = null;

        Boolean largeCount = null;

        public boolean isEmpty() {
            if (StringHelper.isEmpty(label)) {
                return true;
            }
            if (average == 0 && count.size() == 0) {
                return true;
            }
            return false;
        }

        public int getCountMax() {
            return count.values().stream().mapToInt(Integer::intValue).sum();
        }

        public String getLabel() {
            return label;
        }

        public boolean isLargeLabel() {
            return label.length() > 30;
        }

        public int getType() {
            return type;
        }

        public double getAverage() {
            return average;
        }

        public Map<String, Integer> getCount() {
            return count;
        }

        public boolean isLargeLabelCount() {
            if (largeCountLabel == null) {
                for (Map.Entry<String, Integer> e : count.entrySet()) {
                    if (e.getKey().length() > 30) {
                        largeCountLabel = true;
                        return true;
                    }
                }
                largeCountLabel = false;
            }
            return largeCountLabel;
        }

        public Map<String, Integer> getSortedCount() {
            Map<String, Integer> sortedMap = count.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, // en cas de valeurs égales, on garde la première rencontrée
                            LinkedHashMap::new // on conserve l'ordre du tri dans une LinkedHashMap
                    ));
            return sortedMap;
        }

        @Override
        public String toString() {
            return "CellResult{" +
                    "label='" + label + '\'' +
                    ", type=" + type +
                    ", average=" + average +
                    ", count=" + count +
                    '}';
        }
    }

    public static final String TYPE = "display-excel";

    private static final List<String> FIELDS = Arrays.asList(new String[] {"file", "fields", "averageMax", "cutSeparator", "filter"});

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<String> getFields(ContentContext ctx) throws Exception {
        return FIELDS;
    }

    private String cleanLabel(ContentContext ctx, String label) {
        String sep = getFieldValue(ctx, "cutSeparator");
        if (!StringHelper.isEmpty(sep)) {
            if (label.contains(sep)) {
                return label.substring(0, label.indexOf(sep));
            }
        }
        return label;
    }

    @Override
    public void prepareView(ContentContext ctx) throws Exception {
        super.prepareView(ctx);
        File arrayFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), getFieldValue("file")));
        MessageRepository msgRepo = MessageRepository.getInstance(ctx);

        if (!arrayFile.exists()) {
            logger.warning("file not found : "+arrayFile);
            msgRepo.setGlobalMessage(new GenericMessage("error file not found : "+arrayFile, GenericMessage.ERROR));
        } else {
            msgRepo.setGlobalMessage(new GenericMessage("file found : "+arrayFile, GenericMessage.INFO));
        }

        List<String> filters = StringHelper.stringToCollection(getFieldValue("filter"), ",");

        String[][] data = null;
        List<CellResult> averageList = new LinkedList<>();
        if (arrayFile.getName().endsWith(".csv")) {
            Cell[][] cData = new Cell[0][];
            try {
                cData = CSVFactory.loadContentAsCell(arrayFile);
                for (int c=0; c<cData[0].length; c++) {
                    CellResult result = new CellResult();
                    if (!StringHelper.isEmpty(cData[0][c].getValue()) && !cData[0][c].getValue().startsWith("_")) {
                        result.label = cleanLabel(ctx,cData[0][c].getValue());
                        int type = CellResult.NUMBER;
                        double average=0;
                        int countLine = 0;
                        for (int i=1; i<cData.length; i++) {

                            boolean escapeLine = false;
                            for (int j=0; j<cData[i].length; j++) {
                                if (ctx.getRequest().getParameter("c"+j) != null && !ctx.getRequest().getParameter("c"+j).equals(cData[i][j].getValue())) {
                                    escapeLine = true;
                                    break;
                                }
                            }
                            if (!escapeLine) {
                                countLine++;
                                if (!cData[i][c].getValue().isEmpty()) {
                                    result.count.put(cleanLabel(ctx, cData[i][c].getValue()), result.count.get(cleanLabel(ctx, cData[i][c].getValue())) + 1);
                                }
                                if (!StringHelper.isDigit(cData[i][c].getValue())) {
                                    type = CellResult.TEXT;
                                } else {
                                    average += Integer.parseInt(cData[i][c].getValue());
                                }
                            }
                        }
                        result.type = type;
                        if (type == CellResult.NUMBER) {
                            result.count.clear();
                            result.average = average / countLine;
                        }
                    }
                    if (!result.isEmpty()) {
                        averageList.add(result);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ctx.getRequest().setAttribute("averageList", averageList);
    }

    public static void main(String[] args) {
        File arrayFile = new File("C:\\Users\\user\\data\\javlo\\data-ctx\\data-sexy\\static\\dynamic-form-result\\survey\\dieteren_test.csv");

        List<String> filters = StringHelper.stringToCollection("What entity do you represent in the project ?", ",");
        Map<String,String> values = new HashMap<>();
        values.put("c13", "DMC");
        String[][] data = null;
        List<CellResult> averageList = new LinkedList<>();
        if (arrayFile.getName().endsWith(".csv")) {
            Cell[][] cData = new Cell[0][];
            try {
                cData = CSVFactory.loadContentAsCell(arrayFile);
                for (int c=0; c<cData[0].length; c++) {
                    CellResult result = new CellResult();
                    averageList.add(result);
                    if (!StringHelper.isEmpty(cData[0][c].getValue()) && !cData[0][c].getValue().startsWith("_")) {
                        //System.out.println("Title : "+cData[0][c].getValue());
                        int type = CellResult.NUMBER;
                        double average=0;
                        for (int i=1; i<cData.length; i++) {
                            boolean escapeLine = false;
                            for (int j=0; j<cData[i].length; j++) {
                                if (values.get("c"+j) != null && !values.get("c"+j).equals(cData[i][j].getValue())) {
                                    escapeLine = true;
                                    break;
                                }
                            }
                            if (!escapeLine) {
                                result.count.put(cData[i][c].getValue(), result.count.get(cData[i][c].getValue())+1);
                                if (!StringHelper.isDigit(cData[i][c].getValue())) {
                                    type = CellResult.TEXT;
                                } else {
                                    average += Integer.parseInt(cData[i][c].getValue());
                                }
                            } else {
                                System.out.println("escape : "+cData[i][c].getValue());
                            }
                        }
                        result.type = type;
                        if (type == CellResult.NUMBER) {
                            result.count.clear();
                            average = average / (cData.length - 1);
                            //System.out.println("average : "+average);
                        } else {
                            //System.out.println("text average");
                            //System.out.println(result.count.size());
                        }
                    }
                }
                System.out.println("averageList = "+averageList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
