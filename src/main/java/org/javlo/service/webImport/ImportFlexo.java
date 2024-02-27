package org.javlo.service.webImport;

import org.javlo.component.image.ImageBean;
import org.javlo.context.ContentContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ImportFlexo extends AbstractSiteMapXMLWebImport {

    @Override
    public String getName() {
        return "flexo";
    }

    @Override
    public SimpleContentBean importContent(ContentContext ctx, String url) throws IOException {
        // Charger le document HTML depuis l'URL
        Document doc = Jsoup.connect(url).get();

        // Extraire le titre, la date et le contenu
        String title = doc.select("article header h1").text();

        // La date est extraite à deux endroits différents, à vous de choisir la méthode appropriée
        String dateString = doc.select("section#news-description").first().ownText();
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH));

        String content = doc.select("section#news-description").html();

        // Les images et fichiers ne sont pas présents dans l'exemple HTML fourni.
        // Vous devrez adapter ces lignes selon la structure de votre HTML et la façon dont vos ressources statiques sont gérées.
        Element imageElement = doc.select("#news-photos img").first();
        ImageBean imageBean = null;
        if (imageElement != null) {
            String imageUrl = imageElement.absUrl("src"); // Obtenir l'URL absolue de l'image
            String description = imageElement.attr("alt"); // Utiliser l'attribut alt comme description
            imageBean = new ImageBean(imageUrl, null
                    , description, ""); // Aucun lien spécifique pour l'image dans cet exemple
        }

        // Créer et retourner l'instance SimpleContentBean avec les données extraites
        return new SimpleContentBean(title, date, content, imageBean);
    }
}
