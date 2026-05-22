package com.bank.statement.controller;

import com.bank.statement.domain.Statement;
import com.bank.statement.service.StatementGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/statements")
public class StatementController {

    private final StatementGenerationService statementGenerationService;

    public StatementController(StatementGenerationService statementGenerationService) {
        this.statementGenerationService = statementGenerationService;
    }

    @PostMapping
    public ResponseEntity<Statement> generateStatement(@RequestBody Map<String, String> request) {
        String accountId = request.get("accountId");
        String content = request.get("content");
        if (accountId == null || content == null || accountId.isBlank() || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "accountId and content are required");
        }
        Statement statement = statementGenerationService.generateStatement(accountId, content);
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/{statementId}")
    public ResponseEntity<Statement> getStatement(@PathVariable String statementId) {
        Statement statement = statementGenerationService.getStatement(statementId);
        if (statement == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/{statementId}/download")
    public ResponseEntity<byte[]> downloadStatement(@PathVariable String statementId) {
        Statement statement = statementGenerationService.getStatement(statementId);
        if (statement == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] content = statementGenerationService.downloadStatement(statement.getS3Key());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", statementId + ".txt");
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
