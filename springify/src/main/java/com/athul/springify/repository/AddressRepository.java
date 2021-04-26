package com.athul.springify.repository;

import com.athul.springify.io.entity.AddressEntity;
import com.athul.springify.io.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AddressRepository extends CrudRepository<AddressEntity, Long> {
    List<AddressEntity> findAllByUserDetails(UserEntity userEntity);
    AddressEntity findByAddressId(String addressId);
}
