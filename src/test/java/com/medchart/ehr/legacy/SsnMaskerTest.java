package com.medchart.ehr.legacy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SsnMaskerTest {

    @Test
    void keepsOnlyLastFourDigits() {
        assertThat(SsnMasker.mask("123-45-6789")).isEqualTo("***-**-6789");
        assertThat(SsnMasker.mask("123456789")).isEqualTo("***-**-6789");
    }

    @Test
    void masksShortOrMissingValues() {
        assertThat(SsnMasker.mask("12")).isEqualTo("***-**-****");
        assertThat(SsnMasker.mask(null)).isEmpty();
    }
}
