package com.flight.project_flight.service;

import com.flight.project_flight.event.AlertEvent;
import org.slf4j.Logger;
import org.apache.avro.util.Utf8;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender javaMailSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @KafkaListener(topics = "event-placed")
    public void listen(AlertEvent alertEvent) {
        if (alertEvent == null || alertEvent.getEmail() == null || alertEvent.getPasswordNumber() == null) {
            log.error("Received null AlertEvent or missing required fields.");
            return;
        }

        log.info("Received event for email {} with passport number {}", alertEvent.getEmail(), alertEvent.getPasswordNumber());

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");

            // Gestion du type Utf8 (cas spécifique à Avro)
            String email = alertEvent.getEmail() instanceof Utf8
                    ? alertEvent.getEmail().toString()
                    : (String) alertEvent.getEmail();

            if (email == null || email.trim().isEmpty()) {
                log.error("Received alertEvent with no valid email.");
                return; // Ne pas envoyer l'email si l'adresse est invalide
            }
            messageHelper.setTo(email);

            messageHelper.setSubject(String.format("Your Event with passport number %s has been placed successfully", alertEvent.getPasswordNumber()));

            String body = String.format(
                    "Hi %s %s,\n\nYour event with passport number %s has been placed successfully.\n\nBest Regards,\nSpring Shop",
                    alertEvent.getFirstName() != null ? alertEvent.getFirstName() : "Customer",
                    alertEvent.getLastName() != null ? alertEvent.getLastName() : "User",
                    alertEvent.getPasswordNumber()
            );
            messageHelper.setText(body);
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Event Notification email sent to {}", alertEvent.getEmail());
        } catch (MailException e) {
            log.error("Failed to send email to {} for passport number {}: {}", alertEvent.getEmail(), alertEvent.getPasswordNumber(), e.getMessage());
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}