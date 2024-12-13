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
import org.andy.democloudgatewayresource.dto.UserinfoResponseDTO;
import org.andy.democloudgatewayresource.exception.UserCreationException;
import org.andy.democloudgatewayresource.record.User;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static nu.studer.sample.tables.Authorities.AUTHORITIES;
import static nu.studer.sample.tables.Userinfo.USERINFO;
import static nu.studer.sample.tables.Users.USERS;

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
        return dslContext.select(USERS.USERNAME, USERS.PASSWORD, USERS.ENABLED)
                .from(USERS)
                .fetch(r -> new User(r.get(USERS.USERNAME), r.get(USERS.PASSWORD), r.get(USERS.ENABLED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserCredentialsDTO createUser(UserRequestDTO user) {
        try {
            String username = generateUsername(user.getFullName());
            String generatedPassword = generateRandomPassword();
            String encodedPassword = "{bcrypt}" + passwordEncoder.encode(generatedPassword);

            // Insert into users table using jOOQ DSL
            dslContext.insertInto(USERS)
                    .set(USERS.USERNAME, username)
                    .set(USERS.PASSWORD, encodedPassword)
                    .set(USERS.ENABLED, true)
                    .execute();

            // Insert into authorities table using jOOQ DSL
            dslContext.insertInto(AUTHORITIES)
                    .set(AUTHORITIES.USERNAME, username)
                    .set(AUTHORITIES.AUTHORITY, "ROLE_" + user.getRole().toUpperCase())
                    .execute();

            // Insert into userinfo table using jOOQ DSL
            dslContext.insertInto(USERINFO)
                    .set(USERINFO.USERNAME, username)
                    .set(USERINFO.FULL_NAME, user.getFullName())
                    .set(USERINFO.EMAIL, user.getEmail())
                    .set(USERINFO.GENDER, user.getGender())
                    .set(USERINFO.BIRTHDATE, user.getDob())
                    .set(USERINFO.PHONE_NUMBER, user.getPhoneNumber())
                    .set(USERINFO.ADDRESS, user.getAddress())
                    .set(USERINFO.POSITION, user.getRole())
                    .set(USERINFO.DEPARTMENT, user.getDepartment())
                    .set(USERINFO.NOTE, user.getNote())
                    .set(USERINFO.CREATED_AT, LocalDateTime.now())
                    .set(USERINFO.UPDATED_AT, LocalDateTime.now())
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
                .from(USERS)
                .where(USERS.USERNAME.like(basePattern + "%"))
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
        dslContext.update(USERINFO)
                .set(USERINFO.USERNAME, username)
                .where(USERINFO.USERNAME.eq(username)).execute();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String username) {
        dslContext.update(USERS)
                .set(USERS.ENABLED, false)
                .where(USERS.USERNAME.eq(username)).execute();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUsersPage(Integer pageNo, Integer pageSize, String filterBy, String filterRole) {
        // Create base query joining all necessary tables
        SelectConditionStep<?> query = dslContext
                .select(
                        USERS.USERNAME,
                        USERS.ENABLED,
                        AUTHORITIES.AUTHORITY,
                        USERINFO.FULL_NAME,
                        USERINFO.PICTURE,
                        USERINFO.EMAIL,
                        USERINFO.EMAIL_VERIFIED,
                        USERINFO.GENDER,
                        USERINFO.BIRTHDATE,
                        USERINFO.PHONE_NUMBER,
                        USERINFO.PHONE_NUMBER_VERIFIED,
                        USERINFO.ADDRESS,
                        USERINFO.POSITION,
                        USERINFO.DEPARTMENT,
                        USERINFO.NOTE,
                        USERINFO.UPDATED_AT,
                        USERINFO.CREATED_AT
                )
                .from(USERS)
                .leftJoin(USERINFO).on(USERS.USERNAME.eq(USERINFO.USERNAME))
                .leftJoin(AUTHORITIES).on(USERS.USERNAME.eq(AUTHORITIES.USERNAME))
                .where(DSL.noCondition()); // Start with no conditions

        // Add search filter if provided
        if (!filterBy.isEmpty()) {
            query = query.and(
                    USERS.USERNAME.likeIgnoreCase("%" + filterBy + "%")
                            .or(USERINFO.FULL_NAME.likeIgnoreCase("%" + filterBy + "%"))
                            .or(USERINFO.EMAIL.likeIgnoreCase("%" + filterBy + "%"))
                            .or(USERINFO.DEPARTMENT.likeIgnoreCase("%" + filterBy + "%"))
            );
        }

        // Add role filter if provided
        if (!filterRole.isEmpty()) {
            query = query.and(AUTHORITIES.AUTHORITY.eq(filterRole));
        }

        // Get total count for pagination
        int totalItems = dslContext
                .fetchCount(query);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // Validate pageNo
        if (pageNo >= totalPages && totalPages > 0) {
            pageNo = totalPages - 1;
        }
        if (pageNo < 0) {
            pageNo = 0;
        }

        // Add pagination and fetch results
        List<UserinfoResponseDTO> users = query
                .orderBy(USERS.USERNAME.asc())
                .limit(pageSize)
                .offset(pageNo * pageSize)
                .fetchInto(UserinfoResponseDTO.class);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("content", users);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalItems);
        response.put("currentPage", pageNo);
        response.put("pageSize", pageSize);
        response.put("hasNext", pageNo < totalPages - 1);
        response.put("hasPrevious", pageNo > 0);

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserinfoResponseDTO getUserByUsername(String username) {
        return dslContext
                .select(
                        USERS.USERNAME,
                        USERS.ENABLED,
                        AUTHORITIES.AUTHORITY,
                        USERINFO.FULL_NAME,
                        USERINFO.PICTURE,
                        USERINFO.EMAIL,
                        USERINFO.EMAIL_VERIFIED,
                        USERINFO.GENDER,
                        USERINFO.BIRTHDATE,
                        USERINFO.PHONE_NUMBER,
                        USERINFO.PHONE_NUMBER_VERIFIED,
                        USERINFO.ADDRESS,
                        USERINFO.POSITION,
                        USERINFO.DEPARTMENT,
                        USERINFO.NOTE,
                        USERINFO.UPDATED_AT,
                        USERINFO.CREATED_AT
                )
                .from(USERS)
                .leftJoin(USERINFO).on(USERS.USERNAME.eq(USERINFO.USERNAME))
                .leftJoin(AUTHORITIES).on(USERS.USERNAME.eq(AUTHORITIES.USERNAME))
                .where(USERS.USERNAME.eq(username))
                .fetchOneInto(UserinfoResponseDTO.class);
    }

    public void updateUserStatus(String username, String status) {
        dslContext.update(USERS)
                .set(USERS.ENABLED, status.equals("active"))
                .where(USERS.USERNAME.eq(username)).execute();
    }
}
