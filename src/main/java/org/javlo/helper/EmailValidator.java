package org.javlo.helper;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class EmailValidator {

    // Regex simplifiée pour un email valide
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$";

    // Regex pour le format avec nom : Nom Prénom <email@domaine.com>
    private static final Pattern NAMED_EMAIL_PATTERN = Pattern.compile(
            "^\\s*([\\p{L}0-9\\s'\"\\-\\.]+)?\\s*<\\s*([^>\\s]+)\\s*>\\s*$"
    );

    public static boolean isValidEmail(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        input = input.trim();

        // Cas 1 : Format avec nom (ex: "Jean Dupont <jean@exemple.com>")
        Matcher namedMatcher = NAMED_EMAIL_PATTERN.matcher(input);
        if (namedMatcher.matches()) {
            String email = namedMatcher.group(2);
            return email.matches(EMAIL_REGEX);
        }

        // Cas 2 : Email simple sans nom
        return input.matches(EMAIL_REGEX);
    }

    public static void main(String[] args) {
        System.out.println(isValidEmail("jean.dupont@example.com")); // true
        System.out.println(isValidEmail("Jean Dupont <jean.dupont@example.com>")); // true
        System.out.println(isValidEmail("Jean Dupont <jean.dupont@example.com.>")); // false
        System.out.println(isValidEmail("jean.dupont@example")); // false
        System.out.println(isValidEmail("p@noctis.be_obsolete")); // false
    }
}

