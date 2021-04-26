package com.athul.springify.ui.controller;

import com.athul.springify.service.AddressService;
import com.athul.springify.service.UserService;
import com.athul.springify.shared.Roles;
import com.athul.springify.shared.dto.AddressDto;
import com.athul.springify.shared.dto.UserDto;
import com.athul.springify.ui.model.request.PasswordResetModel;
import com.athul.springify.ui.model.request.PasswordResetRequestModel;
import com.athul.springify.ui.model.request.UserDetailsRequestModel;
import com.athul.springify.ui.model.response.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/users")
//To allow requests from other origins
//@CrossOrigin(origins = {"http://localhost:8083", "http://localhost:8084"})
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AddressService addressService;

    //    http://localhost:8080/springify/users
    @Secured("ROLE_ADMIN")
    @ApiOperation(value = "Get All Users Details", notes = "${userController.GetUsers.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
    })
    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "limit", defaultValue = "25") int limit) {
        List<UserRest> returnValue = new ArrayList<>();
        List<UserDto> users = userService.getUsers(page, limit);
        for(UserDto userDto : users) {
            UserRest userModel = new UserRest();
            BeanUtils.copyProperties(userDto, userModel);
            returnValue.add(userModel);
        }
        return returnValue;
    }

    //    http://localhost:8080/springify/users/<userId>
    @PostAuthorize("hasRole('ADMIN') or returnObject.userId == principal.userId")
    @ApiOperation(value = "Get User Details", notes = "${userController.GetUser.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
    })
    @GetMapping(path = "/{id}")
    public UserRest getUser(@PathVariable String id) {
        UserDto userDto = userService.getUserByUserId(id);
        UserRest returnValue = setMapping(userDto);
        return returnValue;
    }

    //    http://localhost:8080/springify/users/<userId>/addresses
    @ApiOperation(value = "Get All Address Details of User", notes = "${userController.GetAddresses.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
    })
    @GetMapping(path = "/{id}/addresses")
    public CollectionModel<AddressesRest> getAddresses(@PathVariable String id) {
        List<AddressesRest> returnValue = new ArrayList<>();
        List<AddressDto> addressDto = addressService.getAddresses(id);
        if(addressDto != null && !addressDto.isEmpty()) {
            Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
            returnValue = new ModelMapper().map(addressDto, listType);
            for(AddressesRest addressRest : returnValue) {
                Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddress(id, addressRest.getAddressId())).withSelfRel();
                addressRest.add(selfLink);
            }
        }
        Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddresses(id)).withSelfRel();
        return CollectionModel.of(returnValue, userLink, selfLink);
    }

    //    http://localhost:8080/springify/users/<userId>/addresses/<addressId>
    @ApiOperation(value = "Get required Address Detail of User", notes = "${userController.GetAddress.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
    })
    @GetMapping(path = "/{userId}/addresses/{addressId}")
    public EntityModel<AddressesRest> getAddress(@PathVariable String userId, @PathVariable String addressId) {
        AddressDto addressDto = addressService.getAddressById(addressId);
        ModelMapper modelMapper = new ModelMapper();
        AddressesRest returnValue = modelMapper.map(addressDto, AddressesRest.class);

        Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
        Link addressesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddresses(userId)).withRel("addresses");
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddress(userId, addressId)).withSelfRel();
        return EntityModel.of(returnValue, Arrays.asList(userLink, addressesLink, selfLink));
    }

    //    http://localhost:8080/springify/users
    @ApiOperation(value = "Create new User", notes = "${userController.CreateUser.ApiOperation.Notes}")
    @PostMapping
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        UserDto userDto = modelMapper.map(userDetails, UserDto.class);
        userDto.setRoles(new HashSet<>(Arrays.asList(Roles.ROLE_USER.name())));
        UserDto createdUser = userService.createUser(userDto);
        UserRest returnValue = modelMapper.map(createdUser, UserRest.class);
        return returnValue;
    }

    //    http://localhost:8080/springify/users/<userId>
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @ApiOperation(value = "Update User Details", notes = "${userController.UpdateUser.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
    })
    @PutMapping("/{id}")
    public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(userDetails, userDto);
        UserDto updatedUser = userService.updateUser(id, userDto);
        UserRest returnValue = setMapping(updatedUser);
        return returnValue;
    }

    //    http://localhost:8080/springify/users/<userId>
    //    @PreAuthorize("hasAuthority('DELETE_AUTHORITY')")
    //    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    @ApiOperation(value = "Delete User", notes = "${userController.DeleteUser.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
    })
    @DeleteMapping("/{id}")
    public OperationStatusModel deleteUser(@PathVariable String id) {
        OperationStatusModel returnValue = new OperationStatusModel();
        userService.deleteUser(id);
        returnValue.setOperationName(RequestOperationName.DELETE.name());
        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }

    //    http://localhost:8080/springify/users/email-verification
    @ApiOperation(value = "Email Verification", notes = "${userController.VerifyEmailToken.ApiOperation.Notes}")
    @GetMapping(path = "/email-verification", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
        boolean isVerified = userService.verifyEmailToken(token);
        if(isVerified) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        } else {
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }
        return returnValue;
    }

    //    http://localhost:8080/springify/users/password-reset-request
    @ApiOperation(value = "Request for Password Reset", notes = "${userController.RequestReset.ApiOperation.Notes}")
    @PostMapping(path = "/password-reset-request", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
        OperationStatusModel returnValue = new OperationStatusModel();
        boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
        returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        if(operationResult) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }
        return returnValue;
    }

    //    http://localhost:8080/springify/users/password-reset
    @ApiOperation(value = "Reset Password", notes = "${userController.ResetPassword.ApiOperation.Notes}")
    @PostMapping(path = "/password-reset", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
        OperationStatusModel returnValue = new OperationStatusModel();
        boolean operationResult = userService.resetPassword(passwordResetModel.getToken(), passwordResetModel.getPassword());
        returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        if(operationResult) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }
        return returnValue;
    }

    //    Map UserDto to UserRest
    private UserRest setMapping(UserDto userDto) {
        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
        List<AddressDto> addressDtos = userDto.getAddresses();
        List<AddressesRest> addresses = modelMapper.map(addressDtos, listType);
        UserRest returnValue = modelMapper.map(userDto, UserRest.class);
        returnValue.setAddresses(addresses);
        return returnValue;
    }
}
