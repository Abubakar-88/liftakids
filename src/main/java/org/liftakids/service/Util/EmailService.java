package org.liftakids.service.Util;



import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.liftakids.dto.contact.ContactRequestDTO;
import org.liftakids.entity.ContactMessage;
import org.liftakids.entity.SentEmail;
import org.liftakids.service.SentEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.naming.Context;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


@Service
public class EmailService {
    @Autowired
    private SentEmailService sentEmailService;
    @Value("${app.email.from:contact@liftakid.org}")
    private String fromEmail;

    @Value("${app.email.admin:contact@liftakid.org}")
    private String adminEmail;

    @Value("${app.name:Lift A Kids}")
    private String appName;


    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
//    @Value("${app.email.from:info@liftakids.org}")
//    private String fromEmail;
//
//    @Value("${app.email.admin:admin@liftakids.org}")
//    private String adminEmail;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send notification to admin about new contact form submission
     */
    public boolean sendContactNotification(ContactRequestDTO contactRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("📧 New Contact Form: " + contactRequest.getSubject());
            message.setText(createNotificationEmailBody(contactRequest));

            mailSender.send(message);
            System.out.println("✅ Contact notification email sent to admin");
            return true;

        } catch (MailException e) {
            System.err.println("❌ Failed to send notification email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean sendCustomEmail(String toEmail, String subject, String message, String senderName) {
        // ✅ প্রথমে SentEmail object তৈরি করুন
        SentEmail sentEmail = new SentEmail();
        sentEmail.setToEmail(toEmail);
        sentEmail.setSubject(subject);
        sentEmail.setMessage(message);
        sentEmail.setSenderName(senderName != null ? senderName : appName);
        sentEmail.setSentAt(LocalDateTime.now());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Use provided sender name or default to app name
            String actualSenderName = senderName != null && !senderName.trim().isEmpty() ?
                    senderName : appName;

            helper.setFrom(fromEmail, actualSenderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Add reply-to header
            helper.setReplyTo(fromEmail);

            // HTML email template
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { 
                            font-family: 'Arial', sans-serif; 
                            line-height: 1.6; 
                            color: #333; 
                            margin: 0; 
                            padding: 0; 
                            background-color: #f4f4f4;
                        }
                        .container { 
                            max-width: 600px; 
                            margin: 0 auto; 
                            background: white;
                        }
                        .header { 
                            background: #4F46E5; 
                            color: white; 
                            padding: 30px 20px; 
                            text-align: center; 
                        }
                        .header h2 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content { 
                            padding: 30px 20px; 
                        }
                        .message { 
                            background: #f8f9fa; 
                            padding: 20px; 
                            border-left: 4px solid #4F46E5; 
                            margin: 20px 0; 
                            border-radius: 4px;
                        }
                        .footer { 
                            margin-top: 30px; 
                            padding: 20px; 
                            background: #e9ecef; 
                            text-align: center; 
                            font-size: 12px; 
                            color: #6c757d; 
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>%s</h2>
                        </div>
                        <div class="content">
                            <div class="message">
                                %s
                            </div>
                        </div>
                        <div class="footer">
                            <p>This email was sent from %s administration panel.</p>
                            <p>&copy; %d %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                    escapeHtml(subject),
                    message.replace("\n", "<br>"),
                    appName,
                    LocalDateTime.now().getYear(),
                    appName
            );

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);

            // ✅ Email successful হলে database-এ save করুন
            sentEmail.setSuccess(true);
            sentEmailService.saveSentEmail(sentEmail);

            System.out.println("✅ Custom email sent to: " + toEmail + " | Subject: " + subject);
            return true;

        } catch (MessagingException e) {
            // ✅ Email fail হলে database-এ error সহ save করুন
            sentEmail.setSuccess(false);
            sentEmail.setErrorMessage(e.getMessage());
            sentEmailService.saveSentEmail(sentEmail);

            System.err.println("❌ Failed to send custom email to " + toEmail + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            // ✅ Other errors
            sentEmail.setSuccess(false);
            sentEmail.setErrorMessage("Unexpected error: " + e.getMessage());
            sentEmailService.saveSentEmail(sentEmail);

            System.err.println("❌ Unexpected error sending email: " + e.getMessage());
            return false;
        }
    }


    // ✅ Better HTML escaping method
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }




    /**
     * Send auto-reply to the user who submitted the contact form
     */
    public boolean sendAutoReply(ContactRequestDTO contactRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(contactRequest.getEmail());
            message.setSubject("Thank you for contacting Lift A Kids");
            message.setText(createAutoReplyBody(contactRequest));

            mailSender.send(message);
            System.out.println("✅ Auto-reply email sent to: " + contactRequest.getEmail());
            return true;

        } catch (MailException e) {
            System.err.println("❌ Failed to send auto-reply email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Send HTML email (alternative method)
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML

            mailSender.send(message);
            System.out.println("✅ HTML email sent to: " + to);
            return true;

        } catch (MessagingException | MailException e) {
            System.err.println("❌ Failed to send HTML email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Email body creation methods (updated for Lift a kids)
    private String createNotificationEmailBody(ContactRequestDTO contact) {
        return String.format(
                "🌟 NEW CONTACT FORM SUBMISSION 🌟\n\n" +
                        "Name: %s\n" +
                        "Email: %s\n" +
                        "Phone: %s\n" +
                        "Subject: %s\n\n" +
                        "Message:\n%s\n\n" +
                        "---\n" +
                        "Please respond within 24 hours.\n" +
                        "Lift A Kids Team\n" +
                        "Time: %s",
                contact.getName(),
                contact.getEmail(),
                contact.getPhone() != null ? contact.getPhone() : "Not provided",
                contact.getSubject(),
                contact.getMessage(),
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    private String createAutoReplyBody(ContactRequestDTO contact) {
        return String.format(
                "Dear %s,\n\n" +
                        "Thank you for contacting Lift A Kids! We have received your message and truly appreciate you taking the time to reach out to us.\n\n" +
                        "🔹 **Your Message Details:**\n" +
                        "   • Subject: %s\n" +
                        "   • Message: %s\n\n" +
                        "Our team will review your message and get back to you within 24-48 hours. We strive to respond to all inquiries as quickly as possible.\n\n" +
                        "In the meantime, you can:\n" +
                        "• Visit our website\n" +
                        "• Follow us on social media for updates\n\n" +
                        "If your matter is urgent, please feel free to call us.\n\n" +
                        "Warm regards,\n" +
                        "The Lift A Kids Team\n" +
                        "📧 %s\n" +
                        "⏰ %s",
                contact.getName(),
                contact.getSubject(),
                contact.getMessage(),
                fromEmail,
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    /**
     * Test email service with detailed error information
     */
    public boolean sendTestEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Test Email from Lift A Kids");
            message.setText("This is a test email from Lift A Kids server. If you received this, email service is working correctly!\n\nServer: smtp.titan.email\nPort: 465\nProtocol: SMTP/SSL");

            mailSender.send(message);
            System.out.println("✅ Test email sent successfully to: " + toEmail);
            return true;

        } catch (MailException e) {
            System.err.println("❌ Failed to send test email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email service test failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send admin reply to user
     */
    public boolean sendReplyToUser(ContactMessage message, String replyMessage) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(message.getEmail());
            email.setSubject("Re: " + message.getSubject());
            email.setText(createReplyToUserBody(message, replyMessage));

            mailSender.send(email);
            System.out.println("✅ Reply sent to user: " + message.getEmail());
            return true;

        } catch (MailException e) {
            System.err.println("❌ Failed to send reply to user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send copy of reply to admin
     */
    public boolean sendReplyCopyToAdmin(ContactMessage message, String replyMessage, String repliedBy) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(adminEmail);
            email.setSubject("[COPY] Reply sent to: " + message.getName());
            email.setText(createReplyCopyBody(message, replyMessage, repliedBy));

            mailSender.send(email);
            System.out.println("✅ Reply copy sent to admin");
            return true;

        } catch (MailException e) {
            System.err.println("❌ Failed to send reply copy to admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private String createReplyToUserBody(ContactMessage message, String replyMessage) {
        return String.format(
                "Dear %s,\n\n" +
                        "Thank you for your message. Here is our response:\n\n" +
                        "---\n" +
                        "%s\n" +
                        "---\n\n" +
                        "Original Message:\n" +
                        "Subject: %s\n" +
                        "Message: %s\n\n" +
                        "If you have any further questions, please don't hesitate to contact us again.\n\n" +
                        "Best regards,\n" +
                        "Lift A kids Team\n" +
                        "📧 %s\n" +
                        "⏰ %s",
                message.getName(),
                replyMessage,
                message.getSubject(),
                message.getMessage(),
                fromEmail,
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    private String createReplyCopyBody(ContactMessage message, String replyMessage, String repliedBy) {
        return String.format(
                "Reply sent by: %s\n" +
                        "Sent at: %s\n\n" +
                        "To: %s (%s)\n" +
                        "Phone: %s\n" +
                        "Original Subject: %s\n\n" +
                        "Admin Reply:\n" +
                        "---\n" +
                        "%s\n" +
                        "---\n\n" +
                        "Original User Message:\n" +
                        "---\n" +
                        "%s\n" +
                        "---\n\n" +
                        "Message ID: %d",
                repliedBy != null ? repliedBy : "Admin",
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                message.getName(),
                message.getEmail(),
                message.getPhone() != null ? message.getPhone() : "Not provided",
                message.getSubject(),
                replyMessage,
                message.getMessage(),
                message.getId()
        );
    }

    // send email for payment confirmation

    @Async
    public void sendPaymentConfirmationEmail(String donorEmail, String donorName, String studentName,
                                             BigDecimal amount, String period, String receiptNumber) {
        try {
            String subject = "Payment Confirmed - LiftAKids Sponsorship";
            String htmlBody = buildPaymentConfirmationEmailBody(donorName, studentName, amount, period, receiptNumber);

            sendHtmlEmail(donorEmail, subject, htmlBody);
            log.info("Payment confirmation email sent successfully to: {}", donorEmail);

        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to: {}", donorEmail, e);
            // Don't throw exception - email failure shouldn't block payment processing
        }
    }


    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            sendwithHtmlEmail(to, subject, body);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }

    private void sendwithHtmlEmail(String to, String subject, String htmlBody) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML
        helper.setFrom("noreply@liftakids.org", "LiftAKids");

        mailSender.send(message);
    }

    private String buildPaymentConfirmationEmailBody(String donorName, String studentName,
                                                     BigDecimal amount, String period, String receiptNumber) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Payment Confirmation</title>
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        padding: 0;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .header p {
                        margin: 10px 0 0 0;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        margin-bottom: 20px;
                        color: #333;
                    }
                    .message {
                        margin-bottom: 25px;
                        color: #555;
                        line-height: 1.8;
                    }
                    .payment-details {
                        background-color: #f8f9fa;
                        border: 1px solid #e9ecef;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 25px 0;
                    }
                    .payment-details h3 {
                        margin-top: 0;
                        color: #28a745;
                        border-bottom: 2px solid #28a745;
                        padding-bottom: 10px;
                    }
                    .detail-row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 8px;
                        padding: 5px 0;
                    }
                    .detail-label {
                        font-weight: bold;
                        color: #555;
                    }
                    .detail-value {
                        color: #333;
                    }
                    .amount {
                        color: #28a745;
                        font-weight: bold;
                    }
                    .footer {
                        text-align: center;
                        padding: 25px;
                        color: #666;
                        font-size: 14px;
                        border-top: 1px solid #eee;
                        background-color: #f8f9fa;
                    }
                    .thank-you {
                        text-align: center;
                        font-style: italic;
                        color: #666;
                        margin: 25px 0;
                    }
                    .contact-info {
                        background-color: #e7f3ff;
                        border-radius: 6px;
                        padding: 15px;
                        margin: 20px 0;
                        border-left: 4px solid #007bff;
                    }
                    .contact-info h4 {
                        margin-top: 0;
                        color: #007bff;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Payment Confirmed</h1>
                        <p>LiftAKids Sponsorship Program</p>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            Dear <strong>%s</strong>,
                        </div>
                        
                        <div class="message">
                            <p>Thank you for your generous sponsorship payment! Your payment has been successfully processed and confirmed by the institution.</p>
                            <p>Your support is making a real difference in the student's education journey.</p>
                        </div>
                        
                        <div class="payment-details">
                            <h3>Payment Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Student Name:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Amount Paid:</span>
                                <span class="detail-value amount">৳%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Sponsorship Period:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Receipt Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Confirmation Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="contact-info">
                            <h4>Need Help?</h4>
                            <p>If you have any questions about your sponsorship, please contact the institution directly or email us at support@liftakids.org</p>
                        </div>
                        
                        <div class="thank-you">
                            <p>Thank you for supporting education and making a positive impact!</p>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p><strong>LiftAKids Team</strong></p>
                        <p>Email: support@liftakids.org | Phone: +880 XXXX XXXXX</p>
                        <p>© 2024 LiftAKids. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                donorName,
                studentName,
                amount.toString(),
                period,
                receiptNumber,
                java.time.LocalDate.now().toString()
        );
    }
}