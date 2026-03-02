package com.squad.squad.repository;

import com.squad.squad.entity.SquadRequest;
import com.squad.squad.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquadRequestRepository extends JpaRepository<SquadRequest, Integer> {

    List<SquadRequest> findByStatus(RequestStatus status);

    List<SquadRequest> findByRequestedByUserId(Integer userId);

    boolean existsByRequestedByUserIdAndStatus(Integer userId, RequestStatus status);
}
