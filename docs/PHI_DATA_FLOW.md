# PHI Data Flow Map

## Overview

This document provides a hierarchical view of Protected Health Information (PHI) flow through the CogHealth EHR system. Use this map to understand how patient data enters, moves through, and exits the system.

**HIPAA Requirement**: All PHI access must be logged. See `PatientAccessLogger.java` for the audit logging pattern.

---

## Hierarchical PHI Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ENTRY POINTS                                    │
│  (Where PHI enters the system)                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  REST APIs                          HL7 Interfaces        File Imports      │
│  ├── PatientController.java         ├── (planned)         ├── (planned)    │
│  ├── EncounterController.java       └── ADT feeds         └── CSV imports  │
│  ├── ProviderController.java                                                │
│  └── LegacyExportController.java ⚠️ NO AUDIT LOGGING                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PROCESSING                                      │
│  (Services that transform or access PHI)                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  WITH Audit Logging ✓               WITHOUT Audit Logging ⚠️                │
│  ├── PatientService.java            ├── LegacyPatientLookup.java           │
│  ├── EncounterService.java          ├── EncounterExportService.java        │
│  ├── ProviderService.java           ├── ReportGenerator.java               │
│  ├── AppointmentService.java        └── InsuranceCache.java                │
│  └── ProviderNotificationService                                            │
│                                                                             │
│  PATTERNS TO FOLLOW:                                                        │
│  • @AuditAccess annotation on all PHI methods                              │
│  • PatientAccessLogger for manual logging                                   │
│  • Never log SSN or full identifiers                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              STORAGE                                         │
│  (Where PHI is persisted)                                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Database Entities                  Caches                 Temp Files       │
│  ├── Patient.java                   ├── InsuranceCache ⚠️  ├── ReportGen ⚠️│
│  │   └── SSN, DOB, Address          │   └── Caches SSN     │   └── No TTL  │
│  ├── Encounter.java                 │   └── No TTL ⚠️      │   └── No      │
│  ├── InsuranceCoverage.java         │                      │       cleanup │
│  ├── MedicationOrder.java           │                      │               │
│  └── ClinicalNote.java              │                      │               │
│                                                                             │
│  HIPAA ISSUES FOUND:                                                        │
│  ⚠️ InsuranceCache stores SSN without expiration                           │
│  ⚠️ ReportGenerator writes PHI to temp files without cleanup               │
│  ⚠️ No encryption at rest configured                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              EXIT POINTS                                     │
│  (Where PHI leaves the system)                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  API Responses                      Reports                Integrations     │
│  ├── PatientDTO.java                ├── EncounterExport ⚠️ ├── InsuranceGW │
│  │   └── Excludes SSN ✓             │   └── Includes SSN   ├── FHIR APIs   │
│  ├── EncounterDTO.java              ├── PatientRoster ⚠️   └── (planned)   │
│  └── ProviderDTO.java               │   └── Includes SSN                   │
│                                     └── DailyReport ⚠️                      │
│                                                                             │
│  HIPAA ISSUES FOUND:                                                        │
│  ⚠️ EncounterExportService includes SSN in CSV exports                     │
│  ⚠️ ReportGenerator includes SSN in patient roster                         │
│  ⚠️ No audit logging on bulk exports                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## File Reference (Click to Navigate)

### Entry Points
| File | Location | Audit Status |
|------|----------|--------------|
| PatientController | `controller/PatientController.java:24` | ✓ Via Service |
| EncounterController | `controller/EncounterController.java:20` | ✓ Via Service |
| ProviderController | `controller/ProviderController.java:18` | ✓ Via Service |
| LegacyExportController | `controller/LegacyExportController.java:15` | ⚠️ NO AUDIT |

### Processing Services
| File | Location | Audit Status | Issues |
|------|----------|--------------|--------|
| PatientService | `service/PatientService.java:23` | ✓ @AuditAccess | Line 85: Logs SSN |
| EncounterService | `service/EncounterService.java:18` | ✓ @AuditAccess | None |
| AppointmentService | `service/AppointmentService.java:25` | ✓ @AuditAccess | None |
| LegacyPatientLookup | `legacy/LegacyPatientLookup.java:16` | ⚠️ NONE | Direct JDBC, no audit |
| EncounterExportService | `legacy/EncounterExportService.java:19` | ⚠️ NONE | Bulk export, no audit |
| ReportGenerator | `legacy/ReportGenerator.java:18` | ⚠️ NONE | Temp files with PHI |
| InsuranceCache | `legacy/InsuranceCache.java:12` | ⚠️ NONE | Caches SSN, no TTL |

### Storage
| File | Location | PHI Fields | Encryption |
|------|----------|------------|------------|
| Patient | `domain/patient/Patient.java:20` | SSN, DOB, Address | ⚠️ None |
| InsuranceCoverage | `domain/insurance/InsuranceCoverage.java:15` | Member ID | ⚠️ None |
| ClinicalNote | `domain/clinical/ClinicalNote.java:18` | Note content | ⚠️ None |

### Exit Points
| File | Location | PHI Exposed | Issues |
|------|----------|-------------|--------|
| PatientDTO | `dto/PatientDTO.java:10` | Name, DOB, MRN | ✓ SSN excluded |
| EncounterExportService | `legacy/EncounterExportService.java:24` | SSN in CSV | ⚠️ HIPAA violation |
| ReportGenerator | `legacy/ReportGenerator.java:25` | SSN in reports | ⚠️ HIPAA violation |
| FhirPatientMapper | `mapper/FhirPatientMapper.java:30` | All demographics | ✓ Standard format |

---

## HIPAA Compliance Checklist

### Critical Issues (Must Fix)

- [ ] **PatientService.java:85** - Logs full SSN in error messages
- [ ] **EncounterExportService.java** - No audit logging on batch exports
- [ ] **LegacyPatientLookup.java** - Direct JDBC bypasses audit layer
- [ ] **InsuranceCache.java** - Caches SSN with no TTL/expiration
- [ ] **ReportGenerator.java** - Writes PHI to temp files without cleanup
- [ ] **15+ endpoints** - Missing access logging (see audit gap analysis)

### Patterns to Follow

1. **Audit Logging Pattern** - See `PatientAccessLogger.java`
2. **Insurance Eligibility Pattern** - See `AppointmentService.java`
3. **Async Notification Pattern** - See `ProviderNotificationService.java`
4. **FHIR Mapping Pattern** - See `FhirPatientMapper.java`
5. **External API Pattern** - See `InsuranceGateway.java`

---

## Remediation Guide

### Adding Audit Logging to Legacy Services

```java
// BEFORE (no audit)
public Patient findPatientByMrn(String mrn) {
    Query query = entityManager.createNativeQuery(...);
    return (Patient) query.getSingleResult();
}

// AFTER (with audit)
@Autowired
private PatientAccessLogger accessLogger;

public Patient findPatientByMrn(String mrn, Long userId, String userRole) {
    Patient patient = // ... query logic
    
    accessLogger.logAccess(
        userId, userRole,
        patient.getId(), patient.getMrn(),
        "READ", "Patient",
        "Legacy MRN lookup",
        RequestContextHolder.getIpAddress(),
        RequestContextHolder.getSessionId()
    );
    
    return patient;
}
```

### Removing SSN from Logs

```java
// BEFORE (logs SSN - HIPAA violation)
log.error("Error finding patient by SSN: " + ssn, e);

// AFTER (masks SSN)
log.error("Error finding patient by SSN: XXX-XX-{}", ssn.substring(ssn.length() - 4), e);
```

### Adding Cache TTL

```java
// BEFORE (no expiration)
eligibilityCache.put(cacheKey, cached);

// AFTER (24-hour TTL)
eligibilityCache.put(cacheKey, cached, 24, TimeUnit.HOURS);
```
