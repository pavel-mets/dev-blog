package main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailService {

        private final JavaMailSender emailSender;

        public void sendMessage(String to, String subject, String text) throws MessagingException {
            //сообщение в формате html
            MimeMessage htmlMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(htmlMessage);
            helper.setFrom("noreply@blogdev.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text,true);
            emailSender.send(htmlMessage);
        }
    }