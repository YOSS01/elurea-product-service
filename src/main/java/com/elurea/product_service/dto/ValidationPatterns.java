package com.elurea.product_service.dto;

/**
 * Regular expressions shared by the request DTOs.
 */
public final class ValidationPatterns {

    /** Lower-case words separated by single dashes, e.g. "summer-linen-dress". */
    public static final String SLUG = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    public static final String SLUG_MESSAGE = "must contain only lower-case letters, digits and single dashes";

    /** Letters, digits, dashes and underscores, starting with a letter or digit. */
    public static final String SKU = "^[A-Za-z0-9][A-Za-z0-9_-]*$";
    public static final String SKU_MESSAGE = "must contain only letters, digits, dashes and underscores";

    /** For optional fields in partial updates: null is allowed, blank is not. */
    public static final String NOT_BLANK = "(?s).*\\S.*";
    public static final String NOT_BLANK_MESSAGE = "must not be blank";

    private ValidationPatterns() {
    }
}
