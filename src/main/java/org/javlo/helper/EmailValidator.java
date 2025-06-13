package org.javlo.helper;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class EmailValidator {

    private static final Pattern NAMED_EMAIL_PATTERN = Pattern.compile(
            "^\\s*([\\p{L}0-9\\s'\"\\-\\.]+)?\\s*<\\s*([^>\\s]+)\\s*>\\s*$"
    );

    public static boolean isValidEmail(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        input = input.trim();

        Matcher namedMatcher = NAMED_EMAIL_PATTERN.matcher(input);
        String email = input;

        if (namedMatcher.matches()) {
            email = namedMatcher.group(2);
        }

        // Check regex stricte
        if (!email.matches("^(?!\\.)[a-zA-Z0-9_.+-]+(?<!\\.)@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+$")) {
            return false;
        }

        // Reject double dots in local part
        String localPart = email.substring(0, email.indexOf('@'));
        return !localPart.contains("..");
    }

    public static void main(String[] args) {
        System.out.println(isValidEmail("info@belgium.be")); // true
        System.out.println(isValidEmail("info@ph.belgium.be")); // true
        System.out.println(isValidEmail("Jean Dupont <info@ph.belgium.be>")); // true
        System.out.println(isValidEmail("info@belgium.be_unvalid")); // false
        System.out.println(isValidEmail("invalid.@email.com")); // false
    }
}
