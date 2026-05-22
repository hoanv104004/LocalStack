package com.bank.transaction.service;

import com.bank.transaction.domain.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class TransactionEventPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public TransactionEventPublisher(SnsClient snsClient, ObjectMapper objectMapper,
                                     @Value("${aws.sns.transaction-topic-arn:arn:aws:sns:ap-southeast-1:000000000000:transaction-events}") String topicArn) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    public void publish(Transaction transaction) {
        try {
            String message = objectMapper.writeValueAsString(transaction);
            snsClient.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish transaction event", e);
        }
    }
}
