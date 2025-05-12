package com.cyberlog.Repositories;

import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    User findUserByName(String name);
    User findUserByEmail(String email);
}
