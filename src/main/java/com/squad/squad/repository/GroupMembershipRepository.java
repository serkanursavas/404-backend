package com.squad.squad.repository;

import com.squad.squad.entity.GroupMembership;
import com.squad.squad.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Integer> {

    Optional<GroupMembership> findBySquadIdAndUserId(Integer squadId, Integer userId);

    List<GroupMembership> findByUserId(Integer userId);

    List<GroupMembership> findBySquadId(Integer squadId);

    boolean existsBySquadIdAndUserId(Integer squadId, Integer userId);

    long countBySquadId(Integer squadId);

    long countBySquadIdAndRole(Integer squadId, GroupRole role);

    Optional<GroupMembership> findFirstBySquadIdAndRole(Integer squadId, GroupRole role);

    List<GroupMembership> findBySquadIdAndRole(Integer squadId, GroupRole role);
}
