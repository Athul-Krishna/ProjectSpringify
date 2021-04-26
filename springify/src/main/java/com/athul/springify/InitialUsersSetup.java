package com.athul.springify;

import com.athul.springify.io.entity.AuthorityEntity;
import com.athul.springify.io.entity.RoleEntity;
import com.athul.springify.io.entity.UserEntity;
import com.athul.springify.repository.AuthorityRepository;
import com.athul.springify.repository.RoleRepository;
import com.athul.springify.repository.UserRepository;
import com.athul.springify.security.SecurityConstants;
import com.athul.springify.shared.Authorities;
import com.athul.springify.shared.Roles;
import com.athul.springify.shared.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;

@Component
public class InitialUsersSetup {

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @EventListener
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        AuthorityEntity readAuthority = createAuthority(Authorities.READ_AUTHORITY.name());
        AuthorityEntity writeAuthority = createAuthority(Authorities.WRITE_AUTHORITY.name());
        AuthorityEntity deleteAuthority = createAuthority(Authorities.DELETE_AUTHORITY.name());

        createRole(Roles.ROLE_USER.name(), Arrays.asList(readAuthority, writeAuthority));
        RoleEntity adminRole = createRole(Roles.ROLE_ADMIN.name(), Arrays.asList(readAuthority, writeAuthority, deleteAuthority));
        UserEntity userEntity = userRepository.findByEmail(GeneralConstants.adminEmail);

        if(adminRole == null) return;
        if(userEntity != null) return;

        UserEntity adminUser = new UserEntity();
        adminUser.setFirstName(GeneralConstants.adminFirstName);
        adminUser.setLastName(GeneralConstants.adminLastName);
        adminUser.setEmail(GeneralConstants.adminEmail);
        adminUser.setEmailVerificationStatus(true);
        adminUser.setUserId(utils.generateUserId(30));
        adminUser.setEncryptedPassword(bCryptPasswordEncoder.encode(SecurityConstants.getAdminPassword()));
        adminUser.setRoles(Arrays.asList(adminRole));
        userRepository.save(adminUser);
    }

    @Transactional
    protected AuthorityEntity createAuthority(String name) {
        AuthorityEntity authority = authorityRepository.findByName(name);
        if(authority == null) {
            authority = new AuthorityEntity(name);
            authorityRepository.save(authority);
        }
        return authority;
    }

    @Transactional
    protected RoleEntity createRole(String name, Collection<AuthorityEntity> authorities) {
        RoleEntity role = roleRepository.findByName(name);
        if(role == null) {
            role = new RoleEntity(name);
            role.setAuthorities(authorities);
            roleRepository.save(role);
        }
        return role;
    }
}
