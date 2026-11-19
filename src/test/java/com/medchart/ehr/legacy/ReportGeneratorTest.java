package com.medchart.ehr.legacy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportGeneratorTest {

    @Test
    void dailyReportMasksSsn() {
        Object[] row = {1L, "MRN001", "123-45-6789", "Jane", "Doe", "1980-01-01",
            "555-0100", "555-0101", "jane@example.test", "1 Main St", "Town", "CA", "90000", "Acme", "M123"};
        Query query = mock(Query.class);
        when(query.getResultList()).thenReturn(Collections.singletonList(row));
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        ReportGenerator generator = new ReportGenerator();
        ReflectionTestUtils.setField(generator, "entityManager", entityManager);

        String csv = new String(generator.generateDailyReport(), StandardCharsets.UTF_8);

        assertThat(csv).contains("***-**-6789").doesNotContain("123-45-6789");
    }
}
