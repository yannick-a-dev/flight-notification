package com.flight.project_flight.service;

import com.flight.project_flight.event.AlertEvent;
import org.slf4j.Logger;
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

        String email = alertEvent.getEmail().toString();
        String firstName = alertEvent.getFirstName() != null ? alertEvent.getFirstName().toString() : "Customer";
        String lastName = alertEvent.getLastName() != null ? alertEvent.getLastName().toString() : "User";
        String passportNumber = alertEvent.getPasswordNumber().toString();

        // ✅ AJOUT DES CHAMPS MANQUANTS
        String severity = alertEvent.getSeverity() != null ? alertEvent.getSeverity().toString() : "UNKNOWN";
        String flightNumber = alertEvent.getFlightNumber() != null ? alertEvent.getFlightNumber().toString() : "N/A";
        String alertDate = alertEvent.getAlertDate() != null ? alertEvent.getAlertDate().toString() : "N/A";
        String message = alertEvent.getMessage() != null ? alertEvent.getMessage().toString() : "No details provided";

        log.info("Received event for email {} with passport number {}", email, passportNumber);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(email);

            messageHelper.setSubject(
                    String.format("🚨 Alert [%s] for flight %s", severity, flightNumber)
            );

            messageHelper.setText(String.format(
                    "Hello %s %s,\n\n" +
                            "An alert has been triggered for your flight.\n\n" +
                            "📌 Flight Number : %s\n" +
                            "⚠️ Severity      : %s\n" +
                            "🕒 Date          : %s\n" +
                            "📝 Message       : %s\n\n" +
                            "Reference (Passport): %s\n\n" +
                            "Please take the necessary actions.\n\n" +
                            "Best regards,\nFlight Notification System",
                    firstName,
                    lastName,
                    flightNumber,
                    severity,
                    alertDate,
                    message,
                    passportNumber
            ));
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Event Notification email sent to {}", email);
        } catch (MailException e) {
            log.error("Failed to send email to {} for passport number {}: {}", email, passportNumber, e.getMessage());
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}