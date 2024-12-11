package org.andy.democloudgatewayresource.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserRequestDTO {
    private String fullName;
    private String email;
    private LocalDate dob;
    private String phoneNumber;
    private String role;
    private String address;
    private String gender;
    private String department;
    private String note;
    private String status;
}
