package com.athul.springify.repository;

import com.athul.springify.io.entity.AddressEntity;
import com.athul.springify.io.entity.RoleEntity;
import com.athul.springify.io.entity.UserEntity;
import com.athul.springify.shared.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Disabled
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    static boolean recordsCreated = false;
    static String userId = "us3rId";

    @BeforeEach
    void setUp() {
        if(!recordsCreated) createRecords();
    }

    @Test
    void testGetVerifiedUsers() {
        Pageable pageableRequest = PageRequest.of(0, 2);
        Page<UserEntity> pages = userRepository.findAllEmailConfirmedUsers(pageableRequest);
        assertNotNull(pages);

        List<UserEntity> userEntities = pages.getContent();
        assertNotNull(userEntities);
        assertTrue(userEntities.size() == 2);
    }

    @Test
    void testFindUserByFirstName() {
        String firstName = "John";
        List<UserEntity> users = userRepository.findUserByFirstName(firstName);
        assertNotNull(users);
        assertTrue(users.size() == 2);

        UserEntity user = users.get(0);
        assertEquals(firstName, user.getFirstName());
    }

    @Test
    void testFindUserByLastName() {
        String lastName = "Doe";
        List<UserEntity> users = userRepository.findUserByLastName(lastName);
        assertNotNull(users);
        assertTrue(users.size() == 2);

        UserEntity user = users.get(0);
        assertEquals(lastName, user.getLastName());
    }

    @Test
    void testFindUserByKeyword() {
        String keyword = "ohn";
        List<UserEntity> users = userRepository.findUserByKeyword(keyword);
        assertNotNull(users);
        assertTrue(users.size() == 2);

        UserEntity user = users.get(0);
        assertTrue(user.getFirstName().contains(keyword) || user.getLastName().contains(keyword));
    }

    @Test
    void findUserFirstNameAndLastNameByKeyword() {
        String keyword = "ohn";
        List<Object[]> users = userRepository.findUserFirstNameAndLastNameByKeyword(keyword);
        assertNotNull(users);
        assertTrue(users.size() == 2);

        Object[] user = users.get(0);
        assertTrue(user.length == 2);

        String firstName = String.valueOf(user[0]);
        String lastName = String.valueOf(user[1]);
        assertNotNull(firstName);
        assertNotNull(lastName);
        assertTrue(firstName.contains(keyword) || lastName.contains(keyword));
    }

    @Test
    void updateEmailVerificationStatus() {
        boolean newEmailVerificationStatus = false;
        userRepository.updateEmailVerificationStatus(newEmailVerificationStatus, userId);
        UserEntity storedUserEntity = userRepository.findByUserId(userId);
        boolean storedEmailVerificationStatus = storedUserEntity.getEmailVerificationStatus();
        assertTrue(storedEmailVerificationStatus == newEmailVerificationStatus);
    }

    @Test
    void findUserEntityByUserId() {
        UserEntity userEntity = userRepository.findUserEntityByUserId(userId);
        assertNotNull(userEntity);
        assertEquals(userId, userEntity.getUserId());
    }

    @Test
    void getUserEntityFullNameByUserId() {
        List<Object[]> records = userRepository.getUserEntityFullNameByUserId(userId);
        assertNotNull(records);
        assertTrue(records.size() == 1);

        Object[] record = records.get(0);
        assertTrue(record.length == 2);

        String firstName = String.valueOf(record[0]);
        String lastName = String.valueOf(record[1]);
        assertNotNull(firstName);
        assertNotNull(lastName);
    }

    @Test
    void updateUserEntityEmailVerificationStatus() {
        boolean newEmailVerificationStatus = true;
        userRepository.updateUserEntityEmailVerificationStatus(newEmailVerificationStatus, userId);
        UserEntity storedUserEntity = userRepository.findByUserId(userId);
        boolean storedEmailVerificationStatus = storedUserEntity.getEmailVerificationStatus();
        assertTrue(storedEmailVerificationStatus == newEmailVerificationStatus);
    }

    private void createRecords() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(Roles.ROLE_USER.name());
        Collection<RoleEntity> roleEntities = new HashSet<>();
        roleEntities.add(roleEntity);

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setType("shipping");
        addressEntity.setAddressId("a1b2c3d4");
        addressEntity.setCity("Los Angeles");
        addressEntity.setCountry("USA");
        addressEntity.setPostalCode("ABC123");
        addressEntity.setStreet("123 Street");
        List<AddressEntity> addresses = new ArrayList<>();
        addresses.add(addressEntity);

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
        userEntity.setUserId(userId);
        userEntity.setEncryptedPassword("3ncrypt3dP455w0rd");
        userEntity.setEmail("test@test.com");
        userEntity.setEmailVerificationStatus(true);
        userEntity.setAddresses(addresses);
        userEntity.setRoles(roleEntities);

        AddressEntity addressEntity2 = new AddressEntity();
        addressEntity2.setType("shipping");
        addressEntity2.setAddressId("a5b6c7d8");
        addressEntity2.setCity("Los Angeles");
        addressEntity2.setCountry("USA");
        addressEntity2.setPostalCode("ABC123");
        addressEntity2.setStreet("123 Street");
        List<AddressEntity> addresses2 = new ArrayList<>();
        addresses2.add(addressEntity2);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setFirstName("John");
        userEntity2.setLastName("Doe");
        userEntity2.setUserId("us3rId2");
        userEntity2.setEncryptedPassword("3ncrypt3dP455w0rd2");
        userEntity2.setEmail("test2@test.com");
        userEntity2.setEmailVerificationStatus(true);
        userEntity2.setAddresses(addresses2);
        userEntity2.setRoles(roleEntities);

        userRepository.save(userEntity);
        userRepository.save(userEntity2);
        recordsCreated = true;
    }
}