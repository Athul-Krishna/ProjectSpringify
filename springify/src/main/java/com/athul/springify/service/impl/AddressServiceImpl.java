package com.athul.springify.service.impl;

import com.athul.springify.io.entity.AddressEntity;
import com.athul.springify.io.entity.UserEntity;
import com.athul.springify.repository.AddressRepository;
import com.athul.springify.repository.UserRepository;
import com.athul.springify.service.AddressService;
import com.athul.springify.shared.dto.AddressDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressRepository addressRepository;

    @Override
    public List<AddressDto> getAddresses(String userId) {
        List<AddressDto> returnValue = new ArrayList<>();
        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null) return returnValue;
        Iterable<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);
        ModelMapper modelMapper = new ModelMapper();
        for(AddressEntity addressEntity : addresses) {
            returnValue.add(modelMapper.map(addressEntity, AddressDto.class));
        }
        return returnValue;
    }

    @Override
    public AddressDto getAddressById(String addressId) {
        AddressDto returnValue = null;
        AddressEntity addressEntity = addressRepository.findByAddressId(addressId);
        if(addressEntity != null) {
            returnValue = new ModelMapper().map(addressEntity, AddressDto.class);
        }
        return returnValue;
    }
}
