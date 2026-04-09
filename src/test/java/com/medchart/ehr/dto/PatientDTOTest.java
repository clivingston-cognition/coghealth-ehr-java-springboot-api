package com.medchart.ehr.dto;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientDTOTest {

    private final Validator validator;

    PatientDTOTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void getFullName_withMiddleName() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .middleName("Andrew")
                .lastName("Doe")
                .build();

        assertEquals("John Andrew Doe", dto.getFullName());
    }

    @Test
    void getFullName_withoutMiddleName() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        assertEquals("John Doe", dto.getFullName());
    }

    @Test
    void getFullName_withBlankMiddleName() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .middleName("   ")
                .lastName("Doe")
                .build();

        assertEquals("John Doe", dto.getFullName());
    }

    @Test
    void getAge_withKnownDateOfBirth() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();

        Integer age = dto.getAge();

        assertNotNull(age);
        assertTrue(age >= 26);
    }

    @Test
    void getAge_withNullDateOfBirth() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        assertNull(dto.getAge());
    }

    @Test
    void validation_failsWhenFirstNameBlank() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .build();

        Set<ConstraintViolation<PatientDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    void validation_failsWhenLastNameBlank() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .lastName("")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .build();

        Set<ConstraintViolation<PatientDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    void validation_failsWhenDateOfBirthInFuture() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<PatientDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
    }

    @Test
    void validation_passesWithValidData() {
        PatientDTO dto = PatientDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .build();

        Set<ConstraintViolation<PatientDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
