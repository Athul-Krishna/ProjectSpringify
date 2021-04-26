package com.athul.springify;

import com.athul.springify.io.entity.AddressEntity;
import com.athul.springify.io.entity.RoleEntity;
import com.athul.springify.io.entity.UserEntity;
import com.athul.springify.shared.Roles;
import com.athul.springify.shared.dto.AddressDto;
import com.athul.springify.shared.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class TestUtils {

    public static UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setPassword("PASSWORD");
        userDto.setEmail("test@test.com");
        userDto.setEmailVerificationStatus(Boolean.FALSE);
        userDto.setEmailVerificationToken("Em4iLV3r1f1c4ti0nT0k3n");
        userDto.setUserId("us3rId");
        userDto.setEncryptedPassword("3ncrypt3dP455w0rd");
        userDto.setAddresses(getAddressesDtos());
        userDto.setRoles(new HashSet<>(Arrays.asList(Roles.ROLE_USER.name())));
        return userDto;
    }

    public static UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
        userEntity.setUserId("us3rId");
        userEntity.setEncryptedPassword("3ncrypt3dP455w0rd");
        userEntity.setEmail("test@test.com");
        userEntity.setEmailVerificationToken("Em4iLV3r1f1c4ti0nT0k3n");
        userEntity.setAddresses(getAddressesEntities());
        userEntity.setRoles(getRoleEntities());
        return userEntity;
    }

    public static List<AddressDto> getAddressesDtos() {
        AddressDto addressDto = new AddressDto();
        addressDto.setType("shipping");
        addressDto.setCity("Los Angeles");
        addressDto.setCountry("USA");
        addressDto.setPostalCode("ABC123");
        addressDto.setStreet("123 Street");

        AddressDto billingAddressDto = new AddressDto();
        billingAddressDto.setType("billing");
        billingAddressDto.setCity("Los Angeles");
        billingAddressDto.setCountry("USA");
        billingAddressDto.setPostalCode("ABC123");
        billingAddressDto.setStreet("123 Street");

        List<AddressDto> addresses = new ArrayList<>();
        addresses.add(addressDto);
        addresses.add(billingAddressDto);
        return addresses;
    }

    private static List<AddressEntity> getAddressesEntities() {
        List<AddressDto> addresses = getAddressesDtos();
        Type listType = new TypeToken<List<AddressEntity>>() {}.getType();
        return new ModelMapper().map(addresses, listType);
    }

    private static Collection<RoleEntity> getRoleEntities() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(Roles.ROLE_USER.name());
        Collection<RoleEntity> roleEntities = new HashSet<>();
        roleEntities.add(roleEntity);
        return roleEntities;
    }
}
