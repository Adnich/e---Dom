package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "e.dom.zenica@gmail.com";  // tvoj Gmail
    private static final String PASSWORD = "tvda hvcw vewe moqq";          // 16-znak app password

    public static void sendResetCode(String toEmail, String code) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
                    }
                });


        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(
                    FROM_EMAIL,
                    "noreply@edom.ba" // ovo je "fake domain" prikaz
            ));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject("Reset lozinke - E-Dom");
            message.setText(
                    "Poštovani,\n\n" +
                            "Vaš kod za reset lozinke je:\n\n" +
                            code +
                            "\n\nAko niste tražili reset, ignorišite ovu poruku."
            );

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
