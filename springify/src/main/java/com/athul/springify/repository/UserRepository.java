package com.athul.springify.repository;

import com.athul.springify.io.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepository extends PagingAndSortingRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
    UserEntity findByUserId(String userId);
    UserEntity findUserByEmailVerificationToken(String token);

//  ------------------------  For testing and learning purpose ------------------------

    @Query(value = "select * from Users u where u.EMAIL_VERIFICATION_STATUS = 'true'",
            countQuery = "select count(*) from Users u where u.EMAIL_VERIFICATION_STATUS = 'true'", nativeQuery = true)
    Page<UserEntity> findAllEmailConfirmedUsers(Pageable pageableRequest);

    @Query(value = "select * from Users u where u.FIRST_NAME = ?1", nativeQuery = true)
    List<UserEntity> findUserByFirstName(String firstName);

    @Query(value = "select * from Users u where u.LAST_NAME = :name", nativeQuery = true)
    List<UserEntity> findUserByLastName(@Param("name") String lastName);

    @Query(value = "select * from Users u where u.LAST_NAME like %:keyword% or u.FIRST_NAME like %:keyword%", nativeQuery = true)
    List<UserEntity> findUserByKeyword(@Param("keyword") String keyword);

    @Query(value = "select u.FIRST_NAME, u.LAST_NAME from Users u where u.LAST_NAME like %:keyword% or u.FIRST_NAME like %:keyword%",
            nativeQuery = true)
    List<Object[]> findUserFirstNameAndLastNameByKeyword(@Param("keyword") String keyword);

    @Transactional
    @Modifying
    @Query(value = "update Users u set u.EMAIL_VERIFICATION_STATUS = :status where u.USER_ID = :id", nativeQuery = true)
    void updateEmailVerificationStatus(@Param("status") boolean emailVerificationStatus, @Param("id") String userId);

    @Query("select u from UserEntity u where u.userId = :id")
    UserEntity findUserEntityByUserId(@Param("id") String userId);

    @Query("select u.firstName, u.lastName from UserEntity u where u.userId = :id")
    List<Object[]> getUserEntityFullNameByUserId(@Param("id") String userId);

    @Transactional
    @Modifying
    @Query("update UserEntity u set u.emailVerificationStatus = :status where u.userId = :id")
    void updateUserEntityEmailVerificationStatus(@Param("status") boolean emailVerificationStatus, @Param("id") String userId);
}
