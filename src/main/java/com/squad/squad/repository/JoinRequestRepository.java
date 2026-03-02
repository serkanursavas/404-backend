package com.squad.squad.repository;

import com.squad.squad.entity.JoinRequest;
import com.squad.squad.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Integer> {

    List<JoinRequest> findBySquadIdAndStatus(Integer squadId, RequestStatus status);

    List<JoinRequest> findByUserId(Integer userId);

    boolean existsByUserIdAndSquadIdAndStatus(Integer userId, Integer squadId, RequestStatus status);

    long countBySquadIdAndStatus(Integer squadId, RequestStatus status);
}
