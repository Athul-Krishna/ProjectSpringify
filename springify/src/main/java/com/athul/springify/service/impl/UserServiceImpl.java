package com.athul.springify.service.impl;

import com.athul.springify.exceptions.UserServiceException;
import com.athul.springify.io.entity.AddressEntity;
import com.athul.springify.io.entity.PasswordResetTokenEntity;
import com.athul.springify.io.entity.RoleEntity;
import com.athul.springify.io.entity.UserEntity;
import com.athul.springify.repository.PasswordResetTokenRepository;
import com.athul.springify.repository.RoleRepository;
import com.athul.springify.repository.UserRepository;
import com.athul.springify.security.UserPrincipal;
import com.athul.springify.service.UserService;
import com.athul.springify.shared.AmazonSES;
import com.athul.springify.shared.Utils;
import com.athul.springify.shared.dto.AddressDto;
import com.athul.springify.shared.dto.UserDto;
import com.athul.springify.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
public class    UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AmazonSES amazonSES;

    @Override
    public UserDto createUser(UserDto user) {
        if(userRepository.findByEmail(user.getEmail()) != null) throw new UserServiceException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

        for(int i=0; i<user.getAddresses().size(); i++) {
            AddressDto address = user.getAddresses().get(i);
            address.setUserDetails(user);
            address.setAddressId(utils.generateAddressId(30));
            user.getAddresses().set(i, address);
        }

        ModelMapper modelMapper = new ModelMapper();
        UserEntity userEntity = modelMapper.map(user, UserEntity.class);

        String publicUserId = utils.generateUserId(30);
        userEntity.setUserId(publicUserId);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
        userEntity.setEmailVerificationStatus(false);

        Collection<RoleEntity> roleEntities = new HashSet<>();
        for(String role: user.getRoles()) {
            RoleEntity roleEntity = roleRepository.findByName(role);
            if(roleEntity != null) {
                roleEntities.add(roleEntity);
            }
        }
        userEntity.setRoles(roleEntities);

        UserEntity storedUserDetails = userRepository.save(userEntity);

        UserDto returnValue = modelMapper.map(storedUserDetails, UserDto.class);
        //        Send a verification email
        amazonSES.verifyEmail(returnValue);
        return returnValue;
    }

    @Override
    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity == null) throw new UsernameNotFoundException(email);
        UserDto returnValue = new UserDto();
        BeanUtils.copyProperties(userEntity, returnValue);
        return returnValue;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null) throw new UsernameNotFoundException("User with ID: " + userId + " not found!");
        UserDto returnValue = setMapping(userEntity);
        return returnValue;
    }

    @Override
    public UserDto updateUser(String userId, UserDto user) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        UserEntity updatedUser = userRepository.save(userEntity);
        if(updatedUser == null) throw new UserServiceException(ErrorMessages.COULD_NOT_UPDATE_RECORD.getErrorMessage());
        UserDto returnValue = setMapping(updatedUser);
        return returnValue;
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        userRepository.delete(userEntity);
    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {
        if(page > 0) page-=1;
        List<UserDto> returnValue = new ArrayList<>();
        Pageable pageableRequest = PageRequest.of(page, limit);
        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
        List<UserEntity> users = usersPage.getContent();
        for(UserEntity userEntity : users) {
            returnValue.add(setMapping(userEntity));
        }
        return returnValue;
    }

    @Override
    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;
        UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
        if(userEntity != null) {
            boolean hasTokenExpired = Utils.hasTokenExpired(token);
            if(!hasTokenExpired) {
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Override
    public boolean requestPasswordReset(String email) {
        boolean returnValue = false;
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity == null) {
            return returnValue;
        }

        String token = utils.generatePasswordResetToken(userEntity.getUserId());
        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUserDetails(userEntity);
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        returnValue = amazonSES.sendPasswordResetRequest(
                userEntity.getFirstName(),
                userEntity.getEmail(),
                token
        );

        return returnValue;
    }

    @Override
    public boolean resetPassword(String token, String password) {
        boolean returnValue = false;
        if(Utils.hasTokenExpired(token)) {
            return returnValue;
        }

        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);
        if(passwordResetTokenEntity == null) {
            return returnValue;
        }

        String encodedPassword = bCryptPasswordEncoder.encode(password);
        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);
        if(savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
            returnValue = true;
        }

        passwordResetTokenRepository.delete(passwordResetTokenEntity);
        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity == null) throw new UsernameNotFoundException(email);
        if(!userEntity.getEmailVerificationStatus()) throw new UserServiceException(ErrorMessages.EMAIL_ADDRESS_NOT_VERIFIED.getErrorMessage());
        return new UserPrincipal(userEntity);
    }

    //    Map UserEntity to UserDto
    private UserDto setMapping(UserEntity userEntity) {
        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<AddressDto>>() {}.getType();
        List<AddressEntity> addressEntities = userEntity.getAddresses();
        List<AddressDto> addressDtos = modelMapper.map(addressEntities, listType);
        UserDto returnValue = modelMapper.map(userEntity, UserDto.class);
        returnValue.setAddresses(addressDtos);
        return returnValue;
    }
}
