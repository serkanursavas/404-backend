package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

}
