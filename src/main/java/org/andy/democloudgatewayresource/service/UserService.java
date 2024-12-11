package org.andy.democloudgatewayresource.service;

import nu.studer.sample.tables.Authorities;
import nu.studer.sample.tables.Userinfo;
import nu.studer.sample.tables.Users;
import nu.studer.sample.tables.records.AuthoritiesRecord;
import nu.studer.sample.tables.records.UserinfoRecord;
import nu.studer.sample.tables.records.UsersRecord;
import org.andy.democloudgatewayresource.dto.UserCredentialsDTO;
import org.andy.democloudgatewayresource.dto.UserRequestDTO;
import org.andy.democloudgatewayresource.dto.UserinfoRequestDto;
import org.andy.democloudgatewayresource.exception.UserCreationException;
import org.andy.democloudgatewayresource.record.User;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    private final DSLContext dslContext;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    public UserService(DSLContext dslContext, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.dslContext = dslContext;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public List<User> getUsers() {
        return dslContext.select(Users.USERS.USERNAME, Users.USERS.PASSWORD, Users.USERS.ENABLED)
                .from(Users.USERS)
                .fetch(r -> new User(r.get(Users.USERS.USERNAME), r.get(Users.USERS.PASSWORD), r.get(Users.USERS.ENABLED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserCredentialsDTO createUser(UserRequestDTO user) {
        try {
            String username = generateUsername(user.getFullName());
            String generatedPassword = generateRandomPassword();
            String encodedPassword = "{bcrypt}" + passwordEncoder.encode(generatedPassword);

            // Insert into users table using jOOQ DSL
            dslContext.insertInto(Users.USERS)
                    .set(Users.USERS.USERNAME, username)
                    .set(Users.USERS.PASSWORD, encodedPassword)
                    .set(Users.USERS.ENABLED, true)
                    .execute();

            // Insert into authorities table using jOOQ DSL
            dslContext.insertInto(Authorities.AUTHORITIES)
                    .set(Authorities.AUTHORITIES.USERNAME, username)
                    .set(Authorities.AUTHORITIES.AUTHORITY, "ROLE_" + user.getRole().toUpperCase())
                    .execute();

            // Insert into userinfo table using jOOQ DSL
            dslContext.insertInto(Userinfo.USERINFO)
                    .set(Userinfo.USERINFO.USERNAME, username)
                    .set(Userinfo.USERINFO.FULL_NAME, user.getFullName())
                    .set(Userinfo.USERINFO.EMAIL, user.getEmail())
                    .set(Userinfo.USERINFO.GENDER, user.getGender())
                    .set(Userinfo.USERINFO.BIRTHDATE, user.getDob())
                    .set(Userinfo.USERINFO.PHONE_NUMBER, user.getPhoneNumber())
                    .set(Userinfo.USERINFO.ADDRESS, user.getAddress())
                    .set(Userinfo.USERINFO.POSITION, user.getRole())
                    .set(Userinfo.USERINFO.DEPARTMENT, user.getDepartment())
                    .set(Userinfo.USERINFO.NOTE, user.getNote())
                    .set(Userinfo.USERINFO.CREATED_AT, LocalDateTime.now())
                    .set(Userinfo.USERINFO.UPDATED_AT, LocalDateTime.now())
                    .execute();

            emailService.sendRegistrationEmail(user.getEmail(), username, generatedPassword);

            return UserCredentialsDTO.builder()
                    .username(username)
                    .password(generatedPassword) // Return the non-encoded password
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .build();

        } catch (Exception e) {
            throw new UserCreationException("Failed to create user", e);
        }
    }

    private String generateUsername(String fullName) {
        // Split the full name
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("Full name must contain at least first and last name");
        }

        // Get the last name
        String lastName = nameParts[nameParts.length - 1];

        // Get the initials of the remaining parts
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < nameParts.length - 1; i++) {
            if (!nameParts[i].isEmpty()) {
                initials.append(nameParts[i].charAt(0));
            }
        }

        // Base pattern: LastNameInitials
        String basePattern = lastName + initials.toString();
        basePattern = basePattern.toUpperCase();

        // Find the next available number using jOOQ DSL
        int count = dslContext
                .selectCount()
                .from(Users.USERS)
                .where(Users.USERS.USERNAME.like(basePattern + "%"))
                .fetchOne(0, int.class);

        return basePattern + (count + 1);
    }

    private String generateRandomPassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialCharacters = "!@#$%^&*()_+";

        StringBuilder password = new StringBuilder();
        Random random = new Random();

        // Ensure at least one character from each category
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        // Fill the rest with random characters
        String allCharacters = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters;
        for (int i = 4; i < 12; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateUser(String username, UserinfoRequestDto user) {
        dslContext.update(Userinfo.USERINFO)
                .set(Userinfo.USERINFO.USERNAME, username)
                .where(Userinfo.USERINFO.USERNAME.eq(username)).execute();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String username) {
        dslContext.update(Users.USERS)
                .set(Users.USERS.ENABLED, false)
                .where(Users.USERS.USERNAME.eq(username)).execute();
    }
}
