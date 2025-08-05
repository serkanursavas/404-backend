package com.squad.squad.repository;

import com.squad.squad.entity.GroupMembership;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends SecureJpaRepository<GroupMembership, Integer> {

        @Query(value = "SELECT * FROM group_memberships WHERE status = :status AND group_id = :groupId ORDER BY requested_at DESC", nativeQuery = true)
        List<GroupMembership> findByStatusNative(@Param("status") String status, @Param("groupId") Integer groupId);

        // Belirli bir kullanıcının belirli bir gruptaki üyeliğini bul
        Optional<GroupMembership> findByUserIdAndGroupId(Integer userId, Integer groupId);

        // Belirli bir kullanıcının belirli bir gruptaki ve statüdeki üyeliğini bul
        Optional<GroupMembership> findByUserIdAndGroupIdAndStatus(Integer userId, Integer groupId,
                        GroupMembership.MembershipStatus status);

        // Kullanıcının zaten başvuru yapmış olup olmadığını kontrol et
        boolean existsByUserIdAndGroupIdAndStatus(Integer userId, Integer groupId,
                        GroupMembership.MembershipStatus status);

        // Belirli bir gruptaki tüm üyelikleri getir (status'e göre filtrelenebilir)
        List<GroupMembership> findByGroupIdAndStatus(Integer groupId, GroupMembership.MembershipStatus status);

        // Belirli bir gruptaki bekleyen üyelik taleplerini getir
        @Query("SELECT gm FROM GroupMembership gm WHERE gm.groupId = :groupId AND gm.status = 'PENDING'")
        List<GroupMembership> findPendingMembershipsByGroupId(@Param("groupId") Integer groupId);

        // Belirli bir kullanıcının tüm üyeliklerini getir - Güvenli versiyon
        @Query("SELECT gm FROM GroupMembership gm WHERE gm.userId = :userId")
        List<GroupMembership> findByUserId(@Param("userId") Integer userId);

        // Belirli bir kullanıcının onaylanmış üyeliklerini getir
        List<GroupMembership> findByUserIdAndStatus(Integer userId, GroupMembership.MembershipStatus status);

        // Belirli bir kullanıcının belirli bir rol ve statüdeki üyeliklerini getir
        List<GroupMembership> findByUserIdAndRoleAndStatus(Integer userId, GroupMembership.MembershipRole role,
                        GroupMembership.MembershipStatus status);

        // Belirli bir gruptaki aktif üye sayısını getir
        @Query("SELECT COUNT(gm) FROM GroupMembership gm WHERE gm.groupId = :groupId AND gm.status = 'APPROVED'")
        Long countApprovedMembersByGroupId(@Param("groupId") Integer groupId);

        // Kullanıcının herhangi bir gruptaki aktif üyeliği var mı?
        @Query("SELECT COUNT(gm) > 0 FROM GroupMembership gm WHERE gm.userId = :userId AND gm.status = 'APPROVED'")
        boolean hasActiveGroupMembership(@Param("userId") Integer userId);

        boolean existsByUserIdAndStatusAndRole(Integer userId, GroupMembership.MembershipStatus status,
                        GroupMembership.MembershipRole role);

        // Security check için: Belirli bir kullanıcının belirli bir rol ve statüdeki
        // üyeliklerini getir
        List<GroupMembership> findByUserIdAndStatusAndRole(Integer userId, GroupMembership.MembershipStatus status,
                        GroupMembership.MembershipRole role);

        // Security check için: Kullanıcının belirli grubta belirli rol ve statüde
        // üyeliği var mı?
        boolean existsByUserIdAndGroupIdAndStatusAndRole(Integer userId, Integer groupId,
                        GroupMembership.MembershipStatus status, GroupMembership.MembershipRole role);

        // Tüm status'deki üyelikleri getir
        List<GroupMembership> findByStatus(GroupMembership.MembershipStatus status);

        // Kullanıcının belirli bir rol ve statüdeki üyeliği var mı?
        boolean existsByUserIdAndRoleAndStatus(Integer userId, GroupMembership.MembershipRole role,
                        GroupMembership.MembershipStatus status);

        // Kullanıcının belirli bir grupta belirli bir rol ve statüdeki üyeliği var mı?
        boolean existsByUserIdAndGroupIdAndRoleAndStatus(Integer userId, Integer groupId,
                        GroupMembership.MembershipRole role, GroupMembership.MembershipStatus status);

        // Güvenlik kısıtlamalarını bypass etmek için özel metod
        @Query("SELECT gm FROM GroupMembership gm WHERE gm.id = :membershipId")
        Optional<GroupMembership> findGroupMembershipWithDetails(@Param("membershipId") Integer membershipId);
}