package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscarr.demoappgym.model.entity.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long>  {

    Optional<User> findByMail(String mail);
}
