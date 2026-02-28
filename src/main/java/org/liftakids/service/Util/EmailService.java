package org.liftakids.service.Util;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.liftakids.dto.contact.ContactRequestDTO;
import org.liftakids.entity.*;
import org.liftakids.service.SentEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class EmailService {
    @Autowired
    private SentEmailService sentEmailService;
    @Value("${app.email.from:contact@liftakid.org}")
    private String fromEmail;

    @Value("${app.email.admin:contact@liftakid.org}")
    private String adminEmail;
    @Value("${app.frontend-url:https://liftakid.org}")
    private String frontendUrl;
    @Value("${app.name:Lift A Kids}")
    private String appName;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

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
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
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

    // donor and Institution email notification
    private String buildInstitutionRegistrationHtml(Institutions institution) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .btn { display: inline-block; padding: 10px 20px; background: #4CAF50; 
                           color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>LiftAKids</h1>
                        <h2>Registration Submitted Successfully</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Thank you for registering with <strong>LiftAKids</strong>!</p>
                        
                        <p>Your registration request has been submitted successfully and is now under review by our admin team.</p>
                        
                        <h3>Registration Details:</h3>
                        <ul>
                            <li><strong>Institution:</strong> %s</li>
                            <li><strong>Email:</strong> %s</li>
                            <li><strong>Phone:</strong> %s</li>
                            <li><strong>Address:</strong> %s</li>
                            <li><strong>Registration Date:</strong> %s</li>
                        </ul>
                        
                        <p><strong>Status:</strong> <span style="color: #FF9800; font-weight: bold;">PENDING APPROVAL</span></p>
                        
                        <p>We will notify you via email once your account is approved. This process usually takes 1-2 business days.</p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="btn">Visit Our Website</a>
                        </div>
                        
                        <p>If you have any questions, please contact our support team.</p>
                        
                        <p>Best regards,<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 LiftAKids. All rights reserved.</p>
                        <p>This is an automated email, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                institution.getInstitutionName(),
                institution.getInstitutionName(),
                institution.getEmail(),
                institution.getPhone(),
                institution.getVillageOrHouse(),
                institution.getRegistrationDate().format(dateFormatter),
                appName

        );
    }

    @Async
    public void sendInstitutionRegistrationEmail(Institutions institution) {
        String subject = "LiftAKids - Registration Submitted Successfully";

        // Simple text version
        String text = String.format("""
            Dear %s,
            
            Thank you for registering with LiftAKids!
            
            Your registration request has been submitted successfully and is now under review by our admin team.
            
            Registration Details:
            - Institution: %s
            - Email: %s
            - Phone: %s
            - Address: %s
            - Registration Date: %s
            
            Status: PENDING APPROVAL
            
            We will notify you via email once your account is approved. This process usually takes 1-2 business days.
            
            If you have any questions, please contact our support team.
            
            Best regards,
            The LiftAKids Team
            """,
                institution.getInstitutionName(),
                institution.getInstitutionName(),
                institution.getEmail(),
                institution.getPhone(),
                institution.getVillageOrHouse(),
                institution.getRegistrationDate().format(dateFormatter)
        );

        sendSimpleEmail(institution.getEmail(), subject, text);

        // HTML version (optional)
        String html = buildInstitutionRegistrationHtml(institution);
        sendHtmlEmail(institution.getEmail(), subject, html);
    }

    @Async
    public void sendInstitutionApprovalEmail(Institutions institution, SystemAdmin approvedBy) {
        String subject = "LiftAKids - Your Account Has Been Approved!";

        // HTML version
        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .btn { display: inline-block; padding: 12px 25px; background: #4CAF50; 
                           color: white; text-decoration: none; border-radius: 5px; font-size: 16px; }
                    .success { color: #4CAF50; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Congratulations!</h1>
                        <h2>Your Account Has Been Approved</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p class="success">Great news! Your LiftAKids account has been approved!</p>
                        
                        <p>Your institution is now active on our platform. You can login and start managing your profile, students, and sponsorships.</p>
                        
                        <h3>Approval Details:</h3>
                        <ul>
                            <li><strong>Institution:</strong> %s</li>
                            <li><strong>Approved By:</strong> %s (Admin)</li>
                            <li><strong>Approval Date:</strong> %s</li>
                            <li><strong>Status:</strong> <span style="color: #4CAF50; font-weight: bold;">ACTIVE</span></li>
                        </ul>
                        
                        <div style="text-align: center; margin: 40px 0;">
                            <a href="%s/login" class="btn">Login to Your Account</a>
                        </div>
                        
                        <div style="background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                            <h4>📋 Next Steps:</h4>
                            <ol>
                                <li>Login to your account</li>
                                <li>Complete your profile setup</li>
                                <li>Add your students/institution details</li>
                                <li>Start receiving sponsorships</li>
                            </ol>
                        </div>
                        
                        <p>If you need any assistance, please don't hesitate to contact our support team.</p>
                        
                        <p>Welcome aboard!<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """,
                institution.getInstitutionName(),
                institution.getInstitutionName(),
                approvedBy.getName(),
                institution.getApprovalDate() != null ?
                        institution.getApprovalDate().format(dateFormatter) : "N/A",
                appName
        );

        sendHtmlEmail(institution.getEmail(), subject, html);
    }

    @Async
    public void sendInstitutionRejectionEmail(Institutions institution, SystemAdmin rejectedBy, String reason) {
        String subject = "LiftAKids - Registration Status Update";

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .reason-box { background: #ffebee; border-left: 4px solid #f44336; 
                                 padding: 15px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Registration Status Update</h1>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>We regret to inform you that your registration request has been reviewed and could not be approved at this time.</p>
                        
                        <div class="reason-box">
                            <h4>Reason for Rejection:</h4>
                            <p>%s</p>
                            <p><strong>Reviewed By:</strong> %s</p>
                        </div>
                        
                        <h3>Registration Details:</h3>
                        <ul>
                            <li><strong>Institution:</strong> %s</li>
                            <li><strong>Email:</strong> %s</li>
                            <li><strong>Registration Date:</strong> %s</li>
                            <li><strong>Status:</strong> <span style="color: #f44336; font-weight: bold;">REJECTED</span></li>
                        </ul>
                        
                        <p>If you believe this decision was made in error, or if you have additional information to provide, 
                        you may contact our support team:</p>
                        <ul>
                            <li>Email: support@liftakids.com</li>
                            <li>Phone: +8801700000000</li>
                        </ul>
                        
                        <p>Thank you for your interest in LiftAKids.</p>
                        
                        <p>Sincerely,<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """,
                institution.getInstitutionName(),
                reason,
                rejectedBy.getName(),
                institution.getInstitutionName(),
                institution.getEmail(),
                institution.getRegistrationDate().format(dateFormatter)
        );

        sendHtmlEmail(institution.getEmail(), subject, html);
    }

    @Async
    public void sendDonorRegistrationEmail(Donor donor) {
        String subject = "Welcome to LiftAKids - Thank You for Registering!";

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .btn { display: inline-block; padding: 10px 20px; background: #2196F3; 
                           color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to LiftAKids!</h1>
                        <h2>Thank You for Joining Our Mission</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Welcome to LiftAKids! We're excited to have you join our community of donors making a difference in children's lives.</p>
                        
                        <h3>Your Account Details:</h3>
                        <ul>
                            <li><strong>Name:</strong> %s</li>
                            <li><strong>Email:</strong> %s</li>
                            <li><strong>Account Status:</strong> <span style="color: #4CAF50; font-weight: bold;">ACTIVE</span></li>
                        </ul>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s/donor/dashboard" class="btn">Go to Your Dashboard</a>
                        </div>
                        
                        <div style="background: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h4>🌟 What You Can Do:</h4>
                            <ul>
                                <li>Browse children needing sponsorship</li>
                                <li>Make one-time or monthly donations</li>
                                <li>Track your donation history</li>
                                <li>Receive updates on sponsored children</li>
                            </ul>
                        </div>
                        
                        <p>If you have any questions or need assistance, our support team is here to help.</p>
                        
                        <p>Thank you for choosing to make a difference!<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """,
                donor.getName(),
                donor.getEmail(),
                appName
        );

        sendHtmlEmail(donor.getEmail(), subject, html);
    }

    @Async
    public void sendPaymentConfirmationEmail(Donor donor, Double amount, String transactionId) {
        String subject = "LiftAKids - Payment Confirmation";

        String currentTime = LocalDateTime.now().format(timeFormatter);
        String currentDate = LocalDateTime.now().format(dateFormatter);

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .receipt { background: white; border: 2px solid #4CAF50; 
                              border-radius: 10px; padding: 20px; margin: 20px 0; }
                    .amount { font-size: 28px; color: #4CAF50; font-weight: bold; text-align: center; }
                    .btn { display: inline-block; padding: 10px 20px; background: #4CAF50; 
                           color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💰 Payment Successful!</h1>
                        <h2>Thank You for Your Generosity</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Your donation to LiftAKids has been successfully processed. Thank you for supporting education for underprivileged children!</p>
                        
                        <div class="receipt">
                            <h3 style="text-align: center; color: #4CAF50;">PAYMENT RECEIPT</h3>
                            <div class="amount">৳%.2f</div>
                            
                            <table style="width: 100%%; margin-top: 20px; border-collapse: collapse;">
                                <tr>
                                    <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Transaction ID:</strong></td>
                                    <td style="padding: 8px; border-bottom: 1px solid #ddd;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Date:</strong></td>
                                    <td style="padding: 8px; border-bottom: 1px solid #ddd;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Time:</strong></td>
                                    <td style="padding: 8px; border-bottom: 1px solid #ddd;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px;"><strong>Status:</strong></td>
                                    <td style="padding: 8px; color: #4CAF50; font-weight: bold;">COMPLETED</td>
                                </tr>
                            </table>
                        </div>
                        
                        <p style="text-align: center;">Your contribution will help provide:</p>
                        <ul style="text-align: center; list-style: none; padding: 0;">
                            <li>📚 Educational materials</li>
                            <li>🍎 Nutritious meals</li>
                            <li>🏫 School supplies</li>
                            <li>👕 Uniforms and clothing</li>
                        </ul>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s/donor/payments/%s" class="btn">View Receipt Details</a>
                        </div>
                        
                        <p>If you have any questions about your donation, please contact our support team.</p>
                        
                        <p>Thank you for making a difference!<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """,
                donor.getName(),
                amount,
                transactionId,
                currentDate,
                currentTime,
                appName,
                transactionId
        );

        sendHtmlEmail(donor.getEmail(), subject, html);
    }
    @Async
    public void sendSponsorshipConfirmationEmail(Donor donor, Institutions institution, String studentName) {
        String subject = "LiftAKids - Sponsorship Confirmation";

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #9C27B0; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .confirmation { background: #f3e5f5; border: 2px solid #9C27B0; 
                                   border-radius: 10px; padding: 20px; margin: 20px 0; }
                    .btn { display: inline-block; padding: 10px 20px; background: #9C27B0; 
                           color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🤝 Sponsorship Confirmed!</h1>
                        <h2>You're Making a Difference</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Thank you for choosing to sponsor a child through LiftAKids! Your commitment will change a child's life.</p>
                        
                        <div class="confirmation">
                            <h3 style="text-align: center; color: #9C27B0;">SPONSORSHIP DETAILS</h3>
                            <table style="width: 100%%; margin-top: 15px;">
                                <tr>
                                    <td style="padding: 10px;"><strong>Sponsored Child:</strong></td>
                                    <td style="padding: 10px;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px;"><strong>Institution:</strong></td>
                                    <td style="padding: 10px;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px;"><strong>Sponsorship Date:</strong></td>
                                    <td style="padding: 10px;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px;"><strong>Status:</strong></td>
                                    <td style="padding: 10px; color: #9C27B0; font-weight: bold;">ACTIVE</td>
                                </tr>
                            </table>
                        </div>
                        
                        <h4>What Happens Next:</h4>
                        <ol>
                            <li>The institution will be notified of your sponsorship</li>
                            <li>You will receive monthly updates about %s's progress</li>
                            <li>Your first payment will be processed according to your chosen schedule</li>
                            <li>You can communicate with the child through our secure platform</li>
                        </ol>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s/donor/sponsorships" class="btn">View Your Sponsorships</a>
                        </div>
                        
                        <p>Thank you for your generosity and for being part of our mission to educate every child!</p>
                        
                        <p>With gratitude,<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """,
                donor.getName(),
                studentName,
                institution.getInstitutionName(),
                LocalDateTime.now().format(dateFormatter),
                studentName,
                appName
        );

        sendHtmlEmail(donor.getEmail(), subject, html);
    }
    @Async
    public void sendNewRegistrationAlertToAdmins(Institutions institution) {
        String subject = "⚠️ New Institution Registration - Requires Review";

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #FF9800; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .alert { background: #fff3cd; border: 2px solid #FF9800; 
                            padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .btn { display: inline-block; padding: 10px 20px; background: #FF9800; 
                           color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📋 New Registration Alert</h1>
                        <h2>Admin Action Required</h2>
                    </div>
                    
                    <div class="content">
                        <div class="alert">
                            <h3 style="color: #FF9800; margin-top: 0;">A new institution has registered and requires review.</h3>
                        </div>
                        
                        <h3>Institution Details:</h3>
                        <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                            <tr>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Institution Name:</strong></td>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Email:</strong></td>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Phone:</strong></td>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Address:</strong></td>
                                <td style="padding: 10px; border-bottom: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px;"><strong>Registration Date:</strong></td>
                                <td style="padding: 10px;">%s</td>
                            </tr>
                        </table>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s/admin/institutions/pending" class="btn">Review Registration</a>
                        </div>
                        
                        <p>Please review this registration within 48 hours.</p>
                        
                        <p>Regards,<br>
                        <strong>LiftAKids System</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """,
                institution.getInstitutionName(),
                institution.getEmail(),
                institution.getPhone(),
                institution.getVillageOrHouse(),
                institution.getRegistrationDate().format(dateFormatter),
                appName
        );

        // Get all active admins from repository
        List<SystemAdmin> activeAdmins = getActiveAdmins(); // Implement this method

        for (SystemAdmin admin : activeAdmins) {
            sendHtmlEmail(admin.getEmail(), subject, html);
            log.info("New registration alert sent to admin: {}", admin.getEmail());
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        String subject = "LiftAKids - Password Reset Request";

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #607D8B; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .reset-box { background: #eceff1; border: 2px dashed #607D8B; 
                                padding: 20px; text-align: center; margin: 20px 0; }
                    .btn { display: inline-block; padding: 12px 25px; background: #607D8B; 
                           color: white; text-decoration: none; border-radius: 5px; font-size: 16px; }
                    .note { background: #fff3cd; padding: 10px; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔒 Password Reset</h1>
                    </div>
                    
                    <div class="content">
                        <p>We received a request to reset your password for your LiftAKids account.</p>
                        
                        <div class="reset-box">
                            <h3>Click the button below to reset your password:</h3>
                            <a href="%s/reset-password?token=%s" class="btn">Reset Password</a>
                            <p style="margin-top: 15px; font-size: 14px; color: #666;">
                                This link will expire in 24 hours.
                            </p>
                        </div>
                        
                        <div class="note">
                            <p><strong>Note:</strong> If you didn't request a password reset, you can safely ignore this email. 
                            Your password will remain unchanged.</p>
                        </div>
                        
                        <p>For security reasons, this link can only be used once.</p>
                        
                        <p>If you're having trouble clicking the button, copy and paste this URL into your browser:</p>
                        <p style="background: white; padding: 10px; border-radius: 5px; word-break: break-all;">
                            %s/reset-password?token=%s
                        </p>
                        
                        <p>Stay secure,<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """,
                appName,
                resetToken,
                appName,
                resetToken
        );

        sendHtmlEmail(email, subject, html);
    }

    @Async
    public void sendPasswordChangedEmail(String email) {
        String subject = "LiftAKids - Password Changed Successfully";

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .success { color: #4CAF50; font-weight: bold; }
                    .security-tip { background: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Password Updated</h1>
                    </div>
                    
                    <div class="content">
                        <p class="success">Your password has been successfully changed!</p>
                        
                        <p>This is a confirmation that your LiftAKids account password was recently changed.</p>
                        
                        <div class="security-tip">
                            <h4>🔐 Security Tips:</h4>
                            <ul>
                                <li>Use a strong, unique password</li>
                                <li>Never share your password with anyone</li>
                                <li>Change your password regularly</li>
                                <li>Log out from shared computers</li>
                            </ul>
                        </div>
                        
                        <p>If you did not make this change, please contact our support team immediately.</p>
                        
                        <p>Stay secure,<br>
                        <strong>The LiftAKids Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """;

        sendHtmlEmail(email, subject, html);
    }

    // Helper method to get active admins
    private List<SystemAdmin> getActiveAdmins() {
        // Implement this based on your repository
        // return systemAdminRepository.findByActiveTrue();
        return List.of(); // Placeholder
    }

}