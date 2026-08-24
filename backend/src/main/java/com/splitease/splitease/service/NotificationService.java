package com.splitease.splitease.service;

import com.splitease.splitease.model.Expense;
import com.splitease.splitease.model.ExpenseSplit;
import com.splitease.splitease.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    /**
     * Notify every split-participant that a new expense was added and how much they owe.
     * Runs on the "notificationExecutor" thread pool -> caller (ExpenseService) does not wait.
     */
    @Async("notificationExecutor")
    public void notifyExpenseAdded(Expense expense, List<ExpenseSplit> splits) {
        for (ExpenseSplit split : splits) {
            User user = split.getUser();
            try {
                String subject = "New expense in \"" + expense.getGroup().getName() + "\": "
                        + expense.getDescription();

                String body = String.format(
                        "Hi %s,%n%n" +
                                "%s added a new expense in the group \"%s\".%n%n" +
                                "Description : %s%n" +
                                "Total Amount : ₹%.2f%n" +
                                "Your Share   : ₹%.2f%n%n" +
                                "Open Splitease to see full details.%n%n" +
                                "- Splitease",
                        user.getName(),
                        expense.getPaidBy().getName(),
                        expense.getGroup().getName(),
                        expense.getDescription(),
                        expense.getAmount(),
                        split.getAmountOwed()
                );

                sendEmail(user.getEmail(), subject, body);
            } catch (Exception e) {
                // Ek user ka email fail ho to baaki users ko bhejna nahi rukna chahiye
                log.error("Failed to send expense notification to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    /**
     * Notify a settlement (payer -> receiver) to the receiver.
     */
    @Async("notificationExecutor")
    public void notifySettlement(User payer, User receiver, Double amount, String groupName) {
        try {
            String subject = "Settlement received in \"" + groupName + "\"";

            String body = String.format(
                    "Hi %s,%n%n" +
                            "%s settled up ₹%.2f with you in the group \"%s\".%n%n" +
                            "- Splitease",
                    receiver.getName(),
                    payer.getName(),
                    amount,
                    groupName
            );

            sendEmail(receiver.getEmail(), subject, body);
        } catch (Exception e) {
            log.error("Failed to send settlement notification to {}: {}", receiver.getEmail(), e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Notification email sent to {}", to);
    }
}