package com.moudjane.crud_spring.Repo;


import com.moudjane.crud_spring.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
