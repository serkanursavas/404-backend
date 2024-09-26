package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.squad.squad.entity.Roster;

public interface RosterRepository extends JpaRepository<Roster, Integer> {

}
