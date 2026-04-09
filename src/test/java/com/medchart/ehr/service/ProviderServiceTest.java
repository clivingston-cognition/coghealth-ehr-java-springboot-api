package com.medchart.ehr.service;

import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.domain.provider.ProviderType;
import com.medchart.ehr.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderService providerService;

    private Provider sampleProvider;

    @BeforeEach
    void setUp() {
        sampleProvider = Provider.builder()
                .id(1L)
                .npi("1234567890")
                .firstName("Jane")
                .lastName("Smith")
                .providerType(ProviderType.PHYSICIAN)
                .specialty("Internal Medicine")
                .department("Medicine")
                .active(true)
                .build();
    }

    @Test
    void findAll_returnsAllProviders() {
        when(providerRepository.findAll()).thenReturn(List.of(sampleProvider));

        List<Provider> result = providerService.findAll();

        assertEquals(1, result.size());
        verify(providerRepository).findAll();
    }

    @Test
    void findActive_returnsActiveProviders() {
        when(providerRepository.findByActiveTrue()).thenReturn(List.of(sampleProvider));

        List<Provider> result = providerService.findActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void findById_returnsProviderWhenFound() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));

        Optional<Provider> result = providerService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("1234567890", result.get().getNpi());
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Provider> result = providerService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByNpi_returnsProviderWhenFound() {
        when(providerRepository.findByNpi("1234567890"))
                .thenReturn(Optional.of(sampleProvider));

        Optional<Provider> result = providerService.findByNpi("1234567890");

        assertTrue(result.isPresent());
    }

    @Test
    void findByNpi_returnsEmptyWhenNotFound() {
        when(providerRepository.findByNpi("0000000000")).thenReturn(Optional.empty());

        Optional<Provider> result = providerService.findByNpi("0000000000");

        assertFalse(result.isPresent());
    }

    @Test
    void findByDepartment_delegatesToRepository() {
        when(providerRepository.findByDepartment("Medicine"))
                .thenReturn(List.of(sampleProvider));

        List<Provider> result = providerService.findByDepartment("Medicine");

        assertEquals(1, result.size());
    }

    @Test
    void findBySpecialty_delegatesToRepository() {
        when(providerRepository.findBySpecialty("Internal Medicine"))
                .thenReturn(List.of(sampleProvider));

        List<Provider> result = providerService.findBySpecialty("Internal Medicine");

        assertEquals(1, result.size());
    }

    @Test
    void getAllDepartments_delegatesToRepository() {
        when(providerRepository.findAllDepartments())
                .thenReturn(List.of("Medicine", "Surgery"));

        List<String> result = providerService.getAllDepartments();

        assertEquals(2, result.size());
    }

    @Test
    void getAllSpecialties_delegatesToRepository() {
        when(providerRepository.findAllSpecialties())
                .thenReturn(List.of("Internal Medicine", "Cardiology"));

        List<String> result = providerService.getAllSpecialties();

        assertEquals(2, result.size());
    }

    @Test
    void save_delegatesToRepository() {
        when(providerRepository.save(sampleProvider)).thenReturn(sampleProvider);

        Provider result = providerService.save(sampleProvider);

        assertEquals("1234567890", result.getNpi());
        verify(providerRepository).save(sampleProvider);
    }

    @Test
    void deactivate_setsActiveFalseAndSaves() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));

        providerService.deactivate(1L);

        assertFalse(sampleProvider.getActive());
        verify(providerRepository).save(sampleProvider);
    }

    @Test
    void deactivate_doesNothingWhenNotFound() {
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());

        providerService.deactivate(999L);

        verify(providerRepository, never()).save(any());
    }

    @Test
    void search_delegatesToRepository() {
        when(providerRepository.findByLastNameContainingIgnoreCase("Smith"))
                .thenReturn(List.of(sampleProvider));

        List<Provider> result = providerService.search("Smith");

        assertEquals(1, result.size());
        assertEquals("Smith", result.get(0).getLastName());
    }
}
