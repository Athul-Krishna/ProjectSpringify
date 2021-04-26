package com.athul.springify.service.impl;

import com.athul.springify.TestUtils;
import com.athul.springify.exceptions.UserServiceException;
import com.athul.springify.io.entity.UserEntity;
import com.athul.springify.repository.RoleRepository;
import com.athul.springify.repository.UserRepository;
import com.athul.springify.shared.AmazonSES;
import com.athul.springify.shared.Utils;
import com.athul.springify.shared.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    Utils utils;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    AmazonSES amazonSES;

    String userId = "us3rId";
    String password = "PASSWORD";
    String encryptedPassword = "3ncrypt3dP455w0rd";
    String email = "test@test.com";
    String addressId = "addr355Id";
    UserEntity userEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userEntity = TestUtils.getUserEntity();
    }

    @Test
    void getUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);
        UserDto userDto = userService.getUser(email);

        assertNotNull(userDto);
        assertEquals(userEntity.getFirstName(), userDto.getFirstName());
    }

    @Test
    void getUser_UsernameNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getUser(email);
        });
    }

    @Test
    void createUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(utils.generateAddressId(anyInt())).thenReturn(addressId);
        when(utils.generateUserId(anyInt())).thenReturn(userId);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptedPassword);
        when(roleRepository.findByName(anyString())).thenReturn(userEntity.getRoles().iterator().next());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        doNothing().when(amazonSES).verifyEmail(any(UserDto.class));

        UserDto userDto = TestUtils.getUserDto();
        UserDto storedUserDetails = userService.createUser(userDto);

        assertNotNull(storedUserDetails);
        assertNotNull(storedUserDetails.getUserId());
        assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
        assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
        assertEquals(userEntity.getAddresses().size(), storedUserDetails.getAddresses().size());

        verify(utils, times(storedUserDetails.getAddresses().size())).generateAddressId(30);
        verify(bCryptPasswordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(roleRepository, times(storedUserDetails.getRoles().size())).findByName(anyString());
    }

    @Test
    void createUser_CreateUserServiceException() {
        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

        UserDto userDto = TestUtils.getUserDto();

        assertThrows(UserServiceException.class, () -> {
            userService.createUser(userDto);
        });
    }
}