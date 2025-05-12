package com.cyberlog.Repositories;

import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    User findUserByName(String name);
}
