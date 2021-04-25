package com.bharattracking.bharatracking.utilities;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by swadhin on 9/2/18.
 */

public class GMail {

    final String emailPort = "465";// gmail's smtp port
    final String emailHost = "smtp.gmail.com";

    String fromEmail;
    String fromPassword;
    List<String> toEmailList;
    String emailSubject;
    String emailBody;

    Properties emailProperties;
    Properties props;
    Session mailSession;
    MimeMessage emailMessage;

    public GMail() {

    }

    public GMail(String fromEmail, String fromPassword,
                 List<String> toEmailList, String emailSubject, String emailBody) {
        this.fromEmail = fromEmail;
        this.fromPassword = fromPassword;
        this.toEmailList = toEmailList;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;

        //emailProperties = System.getProperties();
        //emailProperties.put("mail.smtp.port", emailPort);
        //emailProperties.put("mail.smtp.auth", smtpAuth);
        //emailProperties.put("mail.smtp.starttls.enable", starttls);

        props = new Properties();
        props.put("mail.smtp.user", fromEmail);
        props.put("mail.smtp.host", emailHost);
        props.put("mail.smtp.port", emailPort);
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", emailPort);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        //SMTPAuthenticator auth = new SMTPAuthenticator();
        Session session = Session.getInstance(props, null);
        session.setDebug(true);
        Log.i("GMail", "Mail server properties set.");
    }

    public MimeMessage createEmailMessage() throws MessagingException, UnsupportedEncodingException {

        mailSession = Session.getDefaultInstance(props, null);
        emailMessage = new MimeMessage(mailSession);

        emailMessage.setFrom(new InternetAddress(fromEmail));
        for (String toEmail : toEmailList) {
            Log.i("GMail","toEmail: "+toEmail);
            emailMessage.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(toEmail));
        }

        emailMessage.setSubject(emailSubject);
        //emailMessage.setContent(emailBody, "text/html");// for a html email
        emailMessage.setText(emailBody);// for a text email
        emailMessage.setSentDate(new Date());
        Log.i("GMail", "Email Message created.");
        return emailMessage;
    }

    public boolean sendEmail() throws MessagingException {
        Transport transport = mailSession.getTransport("smtps");
        transport.connect(emailHost,Integer.valueOf(emailPort),fromEmail,fromPassword);
        Log.i("GMail","allrecipients: "+emailMessage.getAllRecipients());
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
        Log.i("GMail", "Email sent successfully.");
        return true;
    }

}
