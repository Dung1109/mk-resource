package org.andy.democloudgatewayresource.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCredentialsDTO {
    private String username;    // e.g., "ANHNN5"
    private String password;    // e.g., "Kj#9mP2$nL"
    private String fullName;    // e.g., "Nguyen Ngoc Anh"
    private String email;
}
