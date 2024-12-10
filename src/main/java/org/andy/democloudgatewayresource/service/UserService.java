package org.andy.democloudgatewayresource.service;

import nu.studer.sample.tables.Authorities;
import nu.studer.sample.tables.Userinfo;
import nu.studer.sample.tables.Users;
import nu.studer.sample.tables.records.AuthoritiesRecord;
import nu.studer.sample.tables.records.UsersRecord;
import org.andy.democloudgatewayresource.dto.UserRequestDTO;
import org.andy.democloudgatewayresource.dto.UserinfoRequestDto;
import org.andy.democloudgatewayresource.record.User;
import org.jooq.DSLContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final DSLContext dslContext;
    private final PasswordEncoder passwordEncoder;


    public UserService(DSLContext dslContext, PasswordEncoder passwordEncoder) {
        this.dslContext = dslContext;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getUsers() {
        return dslContext.select(Users.USERS.USERNAME, Users.USERS.PASSWORD, Users.USERS.ENABLED)
                .from(Users.USERS)
                .fetch(r -> new User(r.get(Users.USERS.USERNAME), r.get(Users.USERS.PASSWORD), r.get(Users.USERS.ENABLED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(UserRequestDTO user) {
        UsersRecord record = dslContext.newRecord(Users.USERS);
        record.setUsername(user.getUsername());
        record.setPassword("{bcrypt}" + passwordEncoder.encode(user.getPassword()));
        record.setEnabled(user.isEnabled());
        dslContext.insertInto(Users.USERS).set(record).execute();

        AuthoritiesRecord authoritiesRecord = dslContext.newRecord(Authorities.AUTHORITIES);
        authoritiesRecord.setUsername(user.getUsername());
        authoritiesRecord.setAuthority("ROLE_" + user.getRole());
        dslContext.insertInto(Authorities.AUTHORITIES).set(authoritiesRecord).execute();

//        dslContext.insertInto(Authorities.AUTHORITIES).set(Authorities.AUTHORITIES.USERNAME, user.getUsername()).set(Authorities.AUTHORITIES.AUTHORITY, "ROLE_" + user.getRole()).execute();

        return new User(record.getUsername(), record.getPassword(), record.getEnabled());
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
