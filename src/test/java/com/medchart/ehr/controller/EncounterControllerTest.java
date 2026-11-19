package com.medchart.ehr.controller;

import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.service.EncounterService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncounterControllerTest {

    @Test
    void updateCannotChangeStatus() {
        EncounterService service = mock(EncounterService.class);
        Encounter existing = new Encounter();
        existing.setId(5L);
        existing.setStatus(EncounterStatus.CHECKED_IN);
        when(service.findById(5L)).thenReturn(Optional.of(existing));
        when(service.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Encounter body = new Encounter();
        body.setStatus(EncounterStatus.COMPLETED);
        new EncounterController(service).update(5L, body);

        ArgumentCaptor<Encounter> saved = ArgumentCaptor.forClass(Encounter.class);
        verify(service).update(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(5L);
        assertThat(saved.getValue().getStatus()).isEqualTo(EncounterStatus.CHECKED_IN);
    }
}
