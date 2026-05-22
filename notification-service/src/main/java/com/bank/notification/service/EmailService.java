package com.bank.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    private final SesClient sesClient;
    private final String fromEmail;

    public EmailService(SesClient sesClient, @Value("${aws.ses.from-email:no-reply@bank.local}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    public void sendTransactionReceipt(String toEmail, String subject, String body) {
        SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).build())
                        .body(Body.builder()
                                .text(Content.builder().data(body).build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }
}
