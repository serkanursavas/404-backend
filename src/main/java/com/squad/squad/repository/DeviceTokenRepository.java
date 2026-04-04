package com.squad.squad.repository;

import com.squad.squad.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Integer> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserIdInAndActiveTrue(List<Integer> userIds);

    List<DeviceToken> findByUserIdAndActiveTrueAndTokenNot(Integer userId, String token);
}
