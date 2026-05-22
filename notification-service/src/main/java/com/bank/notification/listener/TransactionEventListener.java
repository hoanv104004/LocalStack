package com.bank.notification.listener;

import com.bank.notification.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TransactionEventListener {

    private final SqsClient sqsClient;
    private final EmailService emailService;
    private final String queueUrl;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TransactionEventListener(SqsClient sqsClient, EmailService emailService,
                                    @Value("${aws.sqs.notification-queue-url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.emailService = emailService;
        this.queueUrl = queueUrl;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void startPolling() {
        scheduler.scheduleAtFixedRate(this::pollMessages, 0, 5, TimeUnit.SECONDS);
    }

    private void pollMessages() {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(2)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);

            for (Message message : response.messages()) {
                processMessage(message);
            }
        } catch (Exception e) {
            System.out.println("Error polling SQS: " + e.getMessage());
        }
    }

    private void processMessage(Message message) {
        try {
            String body = message.body();

            JsonNode snsEnvelope = objectMapper.readTree(body);
            JsonNode messageNode = objectMapper.readTree(snsEnvelope.get("Message").asText());

            String transactionId = messageNode.get("transactionId").asText();
            String type = messageNode.get("type").asText();
            double amount = messageNode.get("amount").asDouble();
            String description = messageNode.has("description") ? messageNode.get("description").asText() : "";
            String customerEmail = messageNode.has("customerEmail") ? messageNode.get("customerEmail").asText() : "customer@bank.local";

            String subject = "Transaction " + type + " - " + transactionId;
            String bodyText = "Transaction ID: " + transactionId + "\n"
                    + "Type: " + type + "\n"
                    + "Amount: $" + String.format("%.2f", amount) + "\n"
                    + "Description: " + description;

            emailService.sendTransactionReceipt(customerEmail, subject, bodyText);

            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);

            System.out.println("Processed transaction " + transactionId + " for " + customerEmail);
        } catch (Exception e) {
            System.out.println("Error processing message: " + e.getMessage());
        }
    }
}
