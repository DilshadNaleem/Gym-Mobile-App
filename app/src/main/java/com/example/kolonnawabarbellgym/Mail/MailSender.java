package com.example.kolonnawabarbellgym.Mail;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender extends Thread {
    private String email, subject, messageBody;

    public MailSender(String email, String subject, String messageBody) {
        this.email = email;
        this.messageBody = messageBody;
        this.subject = subject;
    }

    public void run() {
        try {
            final String senderEmail = "hypermarket403@gmail.com";
            final String senderPassword = "akmn uzov llex andl";
            final String displayName = "Kolonnawa Barbell Gym";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail, displayName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(subject);
            message.setText(messageBody);

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}