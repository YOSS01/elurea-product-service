package com.elurea.product_service.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringsTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Linen Summer Dress        | linen-summer-dress",
            "Robe d'été Élégante!      | robe-d-ete-elegante",
            "  --Hello   World--  !!   | hello-world",
            "T-Shirt 100% Coton        | t-shirt-100-coton",
            "!!!                       | ''",
    })
    void slugify(String input, String expected) {
        assertThat(Strings.slugify(input)).isEqualTo(expected);
    }
}
