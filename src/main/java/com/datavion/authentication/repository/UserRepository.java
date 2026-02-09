package com.datavion.authentication.repository;

import com.datavion.authentication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.deletedAt IS NULL")
    Optional<User> findByUsernameOrEmailAndDeletedAtIsNull(@Param("usernameOrEmail") String usernameOrEmail);
    
    boolean existsByUsernameAndDeletedAtIsNull(String username);
    
    boolean existsByEmailAndDeletedAtIsNull(String email);
    
    Page<User> findByDeletedAtIsNull(Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountLocked = true AND u.deletedAt IS NULL")
    long countLockedUsers();
}