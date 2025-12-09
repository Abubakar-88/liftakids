package org.liftakids.dto.contact;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactReplyDTO {
    @NotBlank(message = "Reply message is required")
    private String replyMessage;

    private boolean copyToAdmin = true;

}