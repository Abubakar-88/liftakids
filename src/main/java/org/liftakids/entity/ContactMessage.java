package org.liftakids.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contact_messages")
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    private Boolean isRead = false;
    // New fields for reply system
    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private LocalDateTime repliedAt;

    private String repliedBy; // Admin username


    private Boolean isResponded = false;


    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // ✅ Initialize Boolean fields if they are null
        if (isRead == null) {
            isRead = false;
        }
        if (isResponded == null) {
            isResponded = false;
        }
    }

    // ✅ Manual getters and setters for Boolean fields
    public Boolean getIsRead() {
        return isRead != null ? isRead : false;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead != null ? isRead : false;
    }

    public Boolean getIsResponded() {
        return isResponded != null ? isResponded : false;
    }

    public void setIsResponded(Boolean isResponded) {
        this.isResponded = isResponded != null ? isResponded : false;
    }

    // ✅ Helper methods for easier access
    public boolean isRead() {
        return getIsRead();
    }

    public boolean isResponded() {
        return getIsResponded();
    }
}
