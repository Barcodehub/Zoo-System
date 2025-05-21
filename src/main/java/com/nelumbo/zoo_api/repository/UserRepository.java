package com.nelumbo.zoo_api.repository;

import com.nelumbo.zoo_api.models.Role;
import com.nelumbo.zoo_api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRole(Role role);


    @Query("SELECT u.email FROM User u WHERE u.name = :fullName")
    Optional<String> findEmailByFullName(@Param("fullName") String fullName);

}