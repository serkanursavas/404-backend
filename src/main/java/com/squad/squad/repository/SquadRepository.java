package com.squad.squad.repository;

import com.squad.squad.entity.Squad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SquadRepository extends JpaRepository<Squad, Integer> {

    Optional<Squad> findByInviteCode(String inviteCode);
}
