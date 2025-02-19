package org.javlo.macro.interactive;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.search.DefaultSearchEngine;
import org.javlo.search.ISearchEngine;
import org.javlo.search.SearchResult;
import org.javlo.service.RequestService;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CreateRedirections implements IInteractiveMacro, IAction  {
    private static Logger logger = Logger.getLogger(CreateRedirections.class.getName());

    @Override
    public String getName() {
        return "create-redirections";
    }

    @Override
    public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
        return null;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public String getActionGroupName() {
        return "macro-create-redirections";
    }

    @Override
    public String getRenderer() {
        return "/jsp/macros/create-redirections.jsp";
    }

    @Override
    public String getInfo(ContentContext ctx) {
        return null;
    }

    @Override
    public String getModalSize() {
        return SMALL_MODAL_SIZE;
    }

    @Override
    public String getIcon() {
        return "bi bi-arrow-return-right";
    }

    protected static boolean isEditPopup() {
        return false;
    }

    @Override
    public String prepare(ContentContext ctx) {


        return null;
    }

    public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
        String msg = "";
        String count = "";
        String redirections = rs.getParameter("redirections", null);
        String outText = "";

        for (String line : redirections.split("\n")) {
            line = line.trim();

            // Ignorer les lignes contenant "="
            if (line.contains("=") || line.isEmpty()) {
                outText += line + "\n";
                continue;
            }

            // Extraire le dernier élément de l'URL (supprime l'extension si nécessaire)
            String[] segments = line.split("/");
            String lastSegment = segments[segments.length - 1];

            // Supprimer l'extension s'il y en a une
            if (lastSegment.contains(".")) {
                lastSegment = lastSegment.substring(0, lastSegment.lastIndexOf('.'));
            }

            // Séparer par "-" et "_" et trouver le dernier mot de plus de 4 lettres
            String[] words = lastSegment.split("[-_]");
            String lastValidWord = null;

            for (String word : words) {
                if (word.length() > 4) {
                    lastValidWord = word;
                }
            }

            if (lastValidWord != null) {
                System.out.println(lastValidWord);

                ISearchEngine search = new DefaultSearchEngine();
                List<SearchResult.SearchElement> elem =  search.search(ctx, null, lastValidWord, SearchResult.SORT_RELEVANCE, null);

                String redirectTo = "## > NOT FOUND : "+lastValidWord;
                if (elem.size() > 0) {
                    SearchResult.SearchElement item = elem.get(0);
                    if (item.getPriority()>0.9) {
                        redirectTo = "page:"+item.getName();
                    } else {
                        redirectTo = "## > LOW PRIORITY : "+item.getPriority();
                    }
                }

                outText += lastValidWord + "=" + redirectTo + "\n";
            }
        }

        ctx.getRequest().setAttribute("redirections", outText);

        return msg;
    }

    @Override
    public boolean isPreview() {
        return true;
    }

    @Override
    public boolean isAdd() {
        return true;
    }

    @Override
    public boolean isInterative() {
        return true;
    }

    @Override
    public boolean haveRight(ContentContext ctx, String action) {
        return ctx.getCurrentEditUser() != null;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void init(ContentContext ctx) {
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY;
    }

    @Override
    public int getType() {
        return TYPE_TOOLS;
    }
}
