package org.javlo.utils.test;

import org.javlo.helper.FakeContentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ChargeTest {

    private static class ThreadCharge extends Thread {
        private String url;
        private int numberOfRequest = 1;

        public boolean running = true;


        public ThreadCharge(String url, int numberOfRequest) {
            this.url = url;
            this.numberOfRequest = numberOfRequest;
        }

        @Override
        public void run() {
            try {
                fillForm(url, numberOfRequest);
                running = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void fillForm(String url, int numberOfSSubmit) throws Exception {
        int error = 0;
        for (int j=0; j<numberOfSSubmit; j++) {

            Thread.sleep(Math.round(Math.round(+20*Math.random())));

            Document doc = Jsoup.connect(url).get();
            Elements fields = doc.select("input[name], textarea[name], select[name]");
            Map<String, String> params = new HashMap<>();
            int i = 1;
            for (org.jsoup.nodes.Element field : fields) {
                String fieldName = field.attr("name");
                if (fieldName.toLowerCase().contains("name")) {
                    params.put(fieldName, FakeContentHelper.getName());
                } else if (fieldName.toLowerCase().contains("email")) {
                    String email = StringHelper.stringToFileName(FakeContentHelper.getName()).trim();
                    email += "@"+StringHelper.stringToFileName(FakeContentHelper.getCompany())+".com";
                    params.put(fieldName, email);
                } else {
                    String value = fieldName+"_"+i;
                    if (field.tagName().equalsIgnoreCase("select")) {
                        Elements elms = field.select("option");
                        int size = elms.size()-1;
                        if (size<0) {
                            size=0;
                        }
                        Element option = elms.get((int)Math.round(size*Math.random()));
                        value = option.attr("value");
                        if (value == null) {
                            value = option.val();
                        }
                    }
                    if (field.tagName().equalsIgnoreCase("input")) {
                        value = field.attr("value");
                    }
                    params.put(fieldName, value);
                }
                i++;
            }

            Elements forms = doc.select("form");
            if (forms.size()>0) {
                if (!StringHelper.isEmpty(forms.attr("action"))) {
                    URL urlObj = new URL(url);
                    String protocol = urlObj.getProtocol();
                    String host = urlObj.getHost();
                    url = forms.attr("action");
                    if (!url.contains("//")) {
                        url = protocol+"://"+host+url;
                    }
                }
            }

            Connection connection = Jsoup.connect(url).method(Connection.Method.POST);

            // Ajoutez tous les paramètres de la carte à la requête
            for (Map.Entry<String, String> entry : params.entrySet()) {
                connection.data(entry.getKey(), entry.getValue());
            }

            // Exécutez la requête POST et récupérez la réponse
            Connection.Response response = connection.execute();
            if (response.statusCode() != 200) {
                error++;
            }

            ResourceHelper.writeStringToFile(new File("c:/trans/out.html"), response.body());
        }
        System.out.println("send form : #"+numberOfSSubmit+" url="+url+" #error="+error);
    }

    private static boolean isThreadRunning(Collection<ThreadCharge> threadList) {
        for (ThreadCharge t : threadList) {
            if (t.running) {
                return true;
            }
        }
        return false;
    }

    public static void runTest(String url, int numberOfRequest, int numberOfThread) throws Exception {
        long startTime = System.currentTimeMillis();
        Collection<ThreadCharge> threadList = new LinkedList<>();

        System.out.println("---");
        System.out.println("test url start numberOfRequest="+numberOfRequest+" / numberOfThread="+numberOfThread);

        for (int i = 0; i < numberOfThread; i++) {
            ThreadCharge t = new ThreadCharge(url, numberOfRequest);
            t.start();
            threadList.add(t);
        }

        while(isThreadRunning(threadList)) {
            Thread.sleep(1);
        }

        long sec = System.currentTimeMillis()-startTime;

        System.out.println("---");
        System.out.println("test url done numberOfRequest="+numberOfRequest+" / numberOfThread="+numberOfThread+" time="+StringHelper.renderTimeInSecond(sec));
    }

    public static void main(String[] args) throws Exception {
        runTest("https://s1.humind.eu/fr/d%C3%A9signons-des-ambassadeurs-au-sein-de-titeca.html", 20, 20);
    }
}
