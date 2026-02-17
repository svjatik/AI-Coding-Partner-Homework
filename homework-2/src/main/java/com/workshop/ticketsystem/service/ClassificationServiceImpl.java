package com.workshop.ticketsystem.service;

import com.workshop.ticketsystem.dto.ClassificationResult;
import com.workshop.ticketsystem.entity.ClassificationLog;
import com.workshop.ticketsystem.entity.Ticket;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.exception.TicketNotFoundException;
import com.workshop.ticketsystem.repository.ClassificationLogRepository;
import com.workshop.ticketsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClassificationServiceImpl implements ClassificationService {

    private final TicketRepository ticketRepository;
    private final ClassificationLogRepository classificationLogRepository;

    // Category keywords mapping
    private static final Map<TicketCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
            TicketCategory.ACCOUNT_ACCESS, Arrays.asList("login", "password", "2fa", "sign in", "authentication", "access denied", "locked out", "reset password", "forgot password", "cannot login"),
            TicketCategory.TECHNICAL_ISSUE, Arrays.asList("error", "bug", "crash", "broken", "not working", "failure", "exception", "timeout", "slow", "performance"),
            TicketCategory.BILLING_QUESTION, Arrays.asList("billing", "invoice", "payment", "charge", "refund", "subscription", "pricing", "credit card", "cost", "fee"),
            TicketCategory.FEATURE_REQUEST, Arrays.asList("feature", "request", "enhancement", "improvement", "add", "new feature", "would like", "suggest", "could you add", "need"),
            TicketCategory.BUG_REPORT, Arrays.asList("bug", "issue", "defect", "incorrect", "wrong", "broken functionality", "not behaving", "unexpected", "error message", "fails"),
            TicketCategory.OTHER, Arrays.asList("other", "general", "question", "help", "support", "inquiry")
    );

    // Priority keywords mapping
    private static final Map<TicketPriority, List<String>> PRIORITY_KEYWORDS = Map.of(
            TicketPriority.URGENT, Arrays.asList("urgent", "critical", "production down", "can't access", "immediately", "asap", "emergency", "outage", "cannot work", "blocking"),
            TicketPriority.HIGH, Arrays.asList("important", "high priority", "serious", "major", "significant impact", "affecting many", "soon", "quickly"),
            TicketPriority.MEDIUM, Arrays.asList("moderate", "normal", "standard", "regular", "when possible", "sometime"),
            TicketPriority.LOW, Arrays.asList("low", "minor", "small", "cosmetic", "nice to have", "eventually", "whenever", "not urgent")
    );

    @Override
    @Transactional
    public ClassificationResult classify(Ticket ticket) {
        String content = (ticket.getSubject() + " " + ticket.getDescription()).toLowerCase();

        // Classify category
        Map.Entry<TicketCategory, CategoryMatchResult> categoryResult = classifyCategory(content);
        TicketCategory category = categoryResult.getKey();
        CategoryMatchResult categoryMatch = categoryResult.getValue();

        // Classify priority
        Map.Entry<TicketPriority, PriorityMatchResult> priorityResult = classifyPriority(content);
        TicketPriority priority = priorityResult.getKey();
        PriorityMatchResult priorityMatch = priorityResult.getValue();

        // Calculate overall confidence score
        double confidenceScore = (categoryMatch.getScore() + priorityMatch.getScore()) / 2.0;

        // Generate reasoning
        String reasoning = "Category: %s (%.0f%% confidence based on keywords: %s). Priority: %s (%.0f%% confidence based on keywords: %s).".formatted(
                category,
                categoryMatch.getScore() * 100,
                String.join(", ", categoryMatch.getKeywords()),
                priority,
                priorityMatch.getScore() * 100,
                String.join(", ", priorityMatch.getKeywords())
        );

        // Combine all found keywords
        List<String> allKeywords = new ArrayList<>();
        allKeywords.addAll(categoryMatch.getKeywords());
        allKeywords.addAll(priorityMatch.getKeywords());

        // Save classification log
        ClassificationLog log = new ClassificationLog();
        log.setTicketId(ticket.getId());
        log.setSuggestedCategory(category);
        log.setSuggestedPriority(priority);
        log.setConfidenceScore(confidenceScore);
        log.setReasoning(reasoning);
        log.setKeywordsFound(allKeywords);
        classificationLogRepository.save(log);

        // Create result
        ClassificationResult result = new ClassificationResult();
        result.setCategory(category);
        result.setPriority(priority);
        result.setConfidenceScore(confidenceScore);
        result.setReasoning(reasoning);
        result.setKeywordsFound(allKeywords);

        return result;
    }

    @Override
    @Transactional
    public ClassificationResult classifyById(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        return classify(ticket);
    }

    private Map.Entry<TicketCategory, CategoryMatchResult> classifyCategory(String content) {
        TicketCategory bestCategory = TicketCategory.OTHER;
        int maxMatches = 0;
        List<String> matchedKeywords = new ArrayList<>();

        for (Map.Entry<TicketCategory, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            List<String> foundKeywords = new ArrayList<>();
            int matches = 0;

            for (String keyword : entry.getValue()) {
                if (content.contains(keyword.toLowerCase())) {
                    matches++;
                    foundKeywords.add(keyword);
                }
            }

            if (matches > maxMatches) {
                maxMatches = matches;
                bestCategory = entry.getKey();
                matchedKeywords = foundKeywords;
            }
        }

        double score = maxMatches > 0 ? Math.min(1.0, maxMatches / 3.0) : 0.3;
        return Map.entry(bestCategory, new CategoryMatchResult(score, matchedKeywords));
    }

    private Map.Entry<TicketPriority, PriorityMatchResult> classifyPriority(String content) {
        TicketPriority bestPriority = TicketPriority.MEDIUM;
        int maxMatches = 0;
        List<String> matchedKeywords = new ArrayList<>();

        for (Map.Entry<TicketPriority, List<String>> entry : PRIORITY_KEYWORDS.entrySet()) {
            List<String> foundKeywords = new ArrayList<>();
            int matches = 0;

            for (String keyword : entry.getValue()) {
                if (content.contains(keyword.toLowerCase())) {
                    matches++;
                    foundKeywords.add(keyword);
                }
            }

            if (matches > maxMatches) {
                maxMatches = matches;
                bestPriority = entry.getKey();
                matchedKeywords = foundKeywords;
            }
        }

        double score = maxMatches > 0 ? Math.min(1.0, maxMatches / 2.0) : 0.3;
        return Map.entry(bestPriority, new PriorityMatchResult(score, matchedKeywords));
    }

    // Inner classes for match results
    private static class CategoryMatchResult {
        private final double score;
        private final List<String> keywords;

        public CategoryMatchResult(double score, List<String> keywords) {
            this.score = score;
            this.keywords = keywords;
        }

        public double getScore() {
            return score;
        }

        public List<String> getKeywords() {
            return keywords;
        }
    }

    private static class PriorityMatchResult {
        private final double score;
        private final List<String> keywords;

        public PriorityMatchResult(double score, List<String> keywords) {
            this.score = score;
            this.keywords = keywords;
        }

        public double getScore() {
            return score;
        }

        public List<String> getKeywords() {
            return keywords;
        }
    }
}
