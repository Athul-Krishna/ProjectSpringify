package com.athul.springify.service;

import com.athul.springify.shared.dto.AddressDto;

import java.util.List;

public interface AddressService {
    List<AddressDto> getAddresses(String userId);
    AddressDto getAddressById(String addressId);
}
