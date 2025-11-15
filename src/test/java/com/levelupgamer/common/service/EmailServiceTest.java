package com.levelupgamer.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

    @Mock
    private SesClient sesClient;

    @Captor
    private ArgumentCaptor<SendEmailRequest> sendEmailRequestCaptor;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(sesClient, "test@example.com");
    }

    @Test
    void sendEmail_shouldCallSesClientWithCorrectParameters() {
        // Given
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        emailService.sendEmail(to, subject, body);

        // Then
        verify(sesClient).sendEmail(sendEmailRequestCaptor.capture());
        SendEmailRequest capturedRequest = sendEmailRequestCaptor.getValue();

        assertEquals("test@example.com", capturedRequest.source());
        assertEquals(to, capturedRequest.destination().toAddresses().get(0));
        assertEquals(subject, capturedRequest.message().subject().data());
        assertEquals(body, capturedRequest.message().body().text().data());
    }
}