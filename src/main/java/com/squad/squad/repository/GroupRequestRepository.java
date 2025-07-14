package com.squad.squad.repository;

import com.squad.squad.entity.GroupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRequestRepository extends JpaRepository<GroupRequest, Integer> {

    // Bekleyen grup taleplerini getir (Super Admin için)
    List<GroupRequest> findByStatus(GroupRequest.RequestStatus status);

    // Belirli bir kullanıcının grup taleplerini getir
    List<GroupRequest> findByRequestedBy(Integer userId);

    // Aynı isimde bekleyen grup talebi var mı kontrol et
    boolean existsByGroupNameAndStatus(String groupName, GroupRequest.RequestStatus status);

    // Son 30 gün içinde belirli bir kullanıcının kaç tane grup talebi oluşturduğunu say
    @Query(value = "SELECT COUNT(*) FROM group_requests WHERE requested_by = :userId AND requested_at >= CURRENT_DATE - INTERVAL '30 days'", nativeQuery = true)
    Long countRecentRequestsByUser(@Param("userId") Integer userId);

    // Belirli bir kullanıcı için pending durumda olan grup taleplerini getir
    @Query("SELECT gr FROM GroupRequest gr WHERE gr.requestedBy = :userId AND gr.status = 'PENDING'")
    List<GroupRequest> findPendingRequestsByUser(@Param("userId") Integer userId);

    // Belirli bir kullanıcının intended admin olduğu talepleri getir
    List<GroupRequest> findByIntendedAdmin(Integer userId);
}