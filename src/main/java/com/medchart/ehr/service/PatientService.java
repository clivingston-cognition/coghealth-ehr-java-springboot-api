package com.medchart.ehr.service;

import com.medchart.ehr.audit.AuditAccess;
import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.mapper.PatientMapper;
import com.medchart.ehr.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
    private static final int GENERATED_MRN_DIGITS = 16;
    private static final SecureRandom MRN_RANDOM = new SecureRandom();

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient record")
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));
        return patientMapper.toDto(patient);
    }

    @AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient by MRN")
    public PatientDTO getPatientByMrn(String mrn) {
        Patient patient = patientRepository.findByMrn(mrn)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with MRN: " + mrn));
        return patientMapper.toDto(patient);
    }

    @AuditAccess(action = AuditAction.SEARCH, resourceType = "Patient", description = "Search patients")
    public Page<PatientDTO> searchPatients(String searchTerm, Pageable pageable) {
        return patientRepository.searchPatients(escapeLikeWildcards(searchTerm), pageable)
                .map(patientMapper::toDto);
    }

    @Transactional
    @AuditAccess(action = AuditAction.CREATE, resourceType = "Patient", description = "Create patient record")
    public PatientDTO createPatient(PatientDTO patientDTO) {
        boolean admin = callerIsAdmin();
        if (admin && patientDTO.getMrn() != null) {
            Optional<Patient> existing = patientRepository.findByMrn(patientDTO.getMrn());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Patient with MRN " + patientDTO.getMrn() + " already exists");
            }
        }

        Patient patient = patientMapper.toEntity(patientDTO);
        if (!admin) {
            patient.setMrn(null);
            patient.setActive(true);
            patient.setDeceased(false);
            patient.setDeceasedDate(null);
        }
        if (patient.getMrn() == null) {
            patient.setMrn(generateMrn());
        }

        Patient saved = patientRepository.save(patient);
        log.info("Created patient with MRN: {}", saved.getMrn());
        return patientMapper.toDto(saved);
    }

    @Transactional
    @AuditAccess(action = AuditAction.UPDATE, resourceType = "Patient", description = "Update patient record")
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));

        if (callerIsAdmin()) {
            patientMapper.updateEntityFromDto(patientDTO, existing);
        } else {
            patientMapper.updateDemographicsFromDto(patientDTO, existing);
        }
        Patient saved = patientRepository.save(existing);
        log.info("Updated patient with MRN: {}", saved.getMrn());
        return patientMapper.toDto(saved);
    }

    /**
     * @deprecated SSN-based patient lookup is deprecated for HIPAA compliance.
     * Use getPatientByMrn() or searchPatients() instead.
     */
    @Deprecated
    public Optional<PatientDTO> findBySsn(String ssn) {
        throw new UnsupportedOperationException(
            "SSN-based patient lookup is disabled for HIPAA compliance. " +
            "Use MRN or name-based search instead.");
    }

    static String escapeLikeWildcards(String term) {
        char esc = PatientRepository.LIKE_ESCAPE_CHAR;
        StringBuilder escaped = new StringBuilder(term.length());
        for (char c : term.toCharArray()) {
            if (c == esc || c == '%' || c == '_') {
                escaped.append(esc);
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    private boolean callerIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
    }

    static String generateMrn() {
        StringBuilder mrn = new StringBuilder("MRN");
        for (int i = 0; i < GENERATED_MRN_DIGITS; i++) {
            mrn.append(MRN_RANDOM.nextInt(10));
        }
        return mrn.toString();
    }
}
