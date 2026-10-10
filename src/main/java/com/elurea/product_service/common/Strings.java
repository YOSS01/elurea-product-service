package com.elurea.product_service.common;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalisation helpers applied to user-supplied text before it is persisted.
 */
public final class Strings {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-+|-+$)");

    private Strings() {
    }

    /** Strips surrounding whitespace; returns null for null or blank input. */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** "Robe d'été Élégante!" becomes "robe-d-ete-elegante". */
    public static String slugify(String value) {
        String ascii = DIACRITICS.matcher(Normalizer.normalize(value, Normalizer.Form.NFD)).replaceAll("");
        String dashed = NON_ALPHANUMERIC.matcher(ascii.toLowerCase(Locale.ROOT)).replaceAll("-");
        return EDGE_DASHES.matcher(dashed).replaceAll("");
    }
}
