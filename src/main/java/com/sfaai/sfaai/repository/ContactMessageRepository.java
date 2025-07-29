package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
} 