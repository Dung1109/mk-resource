package org.andy.democloudgatewayresource.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserinfoRequestDto implements Serializable {
    Long id;
    String username;
    String fullName;
    String picture;
    String email;
    Boolean emailVerified;
    String gender;
    LocalDate birthdate;
    String phoneNumber;
    Boolean phoneNumberVerified;
    String address;
    String position;
    String department;
    String note;
    LocalDateTime updatedAt;
    LocalDateTime createdAt;
}