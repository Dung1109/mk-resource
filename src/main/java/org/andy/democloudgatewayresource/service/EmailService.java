package org.andy.democloudgatewayresource.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendRegistrationEmail(String email, String username, String password) {
        Resend resend = new Resend("re_4UdgmH4v_9xM7jGAfu11YV4pwYHZG8Fy5");

        CreateEmailOptions createEmailOptions = CreateEmailOptions.builder()
                .from("IMS Team <admin@tayduong.works>")
                .to(email)
                .subject("no-reply-email-IMS-system <Account created>")
                .html("""
                        Your account has been created. Please use the following credentials to login:<br/>
                            * User name: %s<br/>
                            * Password: %s<br/>
                        <br/>
                        If anything is wrong, please reach out to the recruiter at <offer recruiter owner account>. We are so sorry for this inconvenience.
                        <br/>
                        Thanks & Regards,
                        <br/>
                        IMS Team""".formatted(username, password))
                .build();

        try {
            CreateEmailResponse createEmailResponse = resend.emails().send(createEmailOptions);
            createEmailResponse.getId();
        } catch (ResendException e) {
            e.printStackTrace();
        }

    }
}