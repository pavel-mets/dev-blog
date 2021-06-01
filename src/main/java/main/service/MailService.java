package main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class MailService {

        @Autowired
        private JavaMailSender emailSender;

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