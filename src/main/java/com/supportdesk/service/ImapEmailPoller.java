package com.supportdesk.service;

import com.supportdesk.dto.EmailIngestionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.mail.imap.enabled", havingValue = "true", matchIfMissing = false)
public class ImapEmailPoller {

    private static final Logger log = LoggerFactory.getLogger(ImapEmailPoller.class);

    private final EmailIngestionService emailIngestionService;

    @Value("${app.mail.imap.host:imap.supportdesk.io}")
    private String imapHost;

    @Value("${app.mail.imap.username:support@supportdesk.io}")
    private String imapUsername;

    public ImapEmailPoller(EmailIngestionService emailIngestionService) {
        this.emailIngestionService = emailIngestionService;
    }

    @Scheduled(fixedDelayString = "${app.mail.imap.poll-interval-ms:60000}")
    public void pollInbox() {
        log.info("Checking IMAP mailbox on {} for user {}...", imapHost, imapUsername);
        // Polling logic connecting via IMAP / RFC822 store
        // Each retrieved unread message is parsed into EmailIngestionRequest and passed to emailIngestionService.ingestEmail()
    }
}
