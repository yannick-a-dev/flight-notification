package com.project_flight.service;

import com.project_flight.event.AlertEvent;
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
    public void sendEventNotification(AlertEvent alertEvent){
        if (alertEvent == null || alertEvent.getEmail() == null || alertEvent.getPassportNumber() == null) {
            log.error("Received null AlertEvent or missing required fields.");
            return;
        }

        log.info("Received event for email {} with passport number {}", alertEvent.getEmail(), alertEvent.getPassportNumber());

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(alertEvent.getEmail());
            messageHelper.setSubject(String.format("Your Event with passport number %s has been placed successfully", alertEvent.getPassportNumber()));
            messageHelper.setText(String.format(
                    "Hi,%n%n" +
                            "Your event with passport number %s is now placed successfully.%n%n" +
                            "Best Regards,%n" +
                            "Spring Shop",
                    alertEvent.getPassportNumber()));
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Event Notification email sent to {}", alertEvent.getEmail());
        } catch (MailException e) {
            log.error("Failed to send email to {} for passport number {}: {}", alertEvent.getEmail(), alertEvent.getPassportNumber(), e.getMessage());
            // You may consider implementing retries or alerting an admin here
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}
