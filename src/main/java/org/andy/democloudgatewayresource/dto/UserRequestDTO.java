package org.andy.democloudgatewayresource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UserRequestDTO {
    private String username;
    private String password;
    private boolean enabled;
    private String role;
}
