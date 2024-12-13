package org.andy.democloudgatewayresource.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserinfoResponseDTO {
    private String username;
    private boolean enabled;
    private String authority;
    private String fullName;
    private String picture;
    private String email;
    private Boolean emailVerified;
    private String gender;
    private LocalDate birthdate;
    private String phoneNumber;
    private Boolean phoneNumberVerified;
    private String address;
    private String position;
    private String department;
    private String note;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}