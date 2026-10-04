package com.oscarr.demoappgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.oscarr.demoappgym.model.entity.User;


public interface UserRepository extends JpaRepository<User,Long>  {

}
