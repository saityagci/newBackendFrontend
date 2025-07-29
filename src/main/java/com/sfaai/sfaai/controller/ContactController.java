package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.ContactFormDTO;
import com.sfaai.sfaai.dto.ContactMessageResponseDTO;
import com.sfaai.sfaai.entity.ContactMessage;
import com.sfaai.sfaai.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // Adjust origins as needed
public class ContactController {
    private final ContactMessageRepository contactMessageRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContactForm(@Valid @RequestBody ContactFormDTO form) {
        try {
            ContactMessage message = new ContactMessage();
            message.setName(form.getName());
            message.setEmail(form.getEmail());
            message.setRole(form.getRole());
            message.setCompany(form.getCompany());
            message.setWebsite(form.getWebsite());
            message.setService(form.getService());
            message.setMessage(form.getMessage());
            message.setSubmittedAt(LocalDateTime.now());
            contactMessageRepository.save(message);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Thank you for contacting us!");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Failed to save contact form submission", e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "An error occurred while submitting the form.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // Admin endpoints for managing contact forms
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessageResponseDTO>> getAllContactForms() {
        try {
            log.info("Admin requesting all contact forms");
            List<ContactMessage> messages = contactMessageRepository.findAll();
            List<ContactMessageResponseDTO> response = messages.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            log.info("Retrieved {} contact forms", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving contact forms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ContactMessageResponseDTO>> getContactFormsPaginated(Pageable pageable) {
        try {
            log.info("Admin requesting paginated contact forms");
            Page<ContactMessage> messages = contactMessageRepository.findAll(pageable);
            Page<ContactMessageResponseDTO> response = messages.map(this::convertToDTO);
            log.info("Retrieved {} contact forms (page {})", response.getContent().size(), pageable.getPageNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving paginated contact forms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageResponseDTO> getContactFormById(@PathVariable Long id) {
        try {
            log.info("Admin requesting contact form with ID: {}", id);
            ContactMessage message = contactMessageRepository.findById(id)
                    .orElse(null);
            if (message == null) {
                log.warn("Contact form with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }
            ContactMessageResponseDTO response = convertToDTO(message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving contact form with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteContactForm(@PathVariable Long id) {
        try {
            log.info("Admin deleting contact form with ID: {}", id);
            if (!contactMessageRepository.existsById(id)) {
                log.warn("Contact form with ID {} not found for deletion", id);
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "Contact form not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }
            contactMessageRepository.deleteById(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Contact form deleted successfully");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Error deleting contact form with ID: {}", id, e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "An error occurred while deleting the contact form");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/admin/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getContactFormCount() {
        try {
            log.info("Admin requesting contact form count");
            long count = contactMessageRepository.count();
            Map<String, Object> response = new HashMap<>();
            response.put("total", count);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving contact form count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper method to convert entity to DTO
    private ContactMessageResponseDTO convertToDTO(ContactMessage message) {
        return ContactMessageResponseDTO.builder()
                .id(message.getId())
                .name(message.getName())
                .email(message.getEmail())
                .role(message.getRole())
                .company(message.getCompany())
                .website(message.getWebsite())
                .service(message.getService())
                .message(message.getMessage())
                .submittedAt(message.getSubmittedAt())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", "Validation failed");
        resp.put("errors", errors);
        return ResponseEntity.badRequest().body(resp);
    }
} 