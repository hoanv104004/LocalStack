package com.bank.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public class StatementGeneratorHandler implements RequestHandler<SNSEvent, String> {

    private static final String BUCKET_NAME = "bank-statements";
    private static final String ENDPOINT = "http://localhost:4566";
    private static final String REGION = "ap-southeast-1";

    private static final S3Client s3 = S3Client.builder()
            .endpointOverride(URI.create(ENDPOINT))
            .region(Region.of(REGION))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("test", "test")))
            .forcePathStyle(true)
            .build();

    private static final Gson gson = new Gson();

    @Override
    public String handleRequest(SNSEvent event, Context context) {
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            String message = record.getSNS().getMessage();
            JsonObject transaction = gson.fromJson(message, JsonObject.class);

            String transactionId = transaction.get("transactionId").getAsString();
            String type = transaction.get("type").getAsString();
            String amount = transaction.get("amount").getAsString();

            String statementContent = String.format(
                    "Bank Statement\nTransaction: %s\nType: %s\nAmount: %s\nDate: %s\n",
                    transactionId, type, amount, Instant.now().toString());

            String key = "statements/lambda/" + UUID.randomUUID() + ".txt";
            s3.putObject(r -> r.bucket(BUCKET_NAME).key(key),
                    RequestBody.fromString(statementContent));

            context.getLogger().log("Statement uploaded to S3: " + key);
        }
        return "OK";
    }
}
