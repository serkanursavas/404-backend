package com.squad.squad.repository;

import com.squad.squad.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    // Grup adına göre arama (benzersizlik kontrolü için)
    Optional<Group> findByName(String name);

    // Grup adı var mı kontrolü
    boolean existsByName(String name);

    // Status'e göre grupları getir
    List<Group> findByStatus(Group.GroupStatus status);

    // Onaylanmış grupları getir
    @Query("SELECT g FROM Group g WHERE g.status = 'APPROVED'")
    List<Group> findApprovedGroups();

    // Belirli bir kullanıcının oluşturduğu grupları getir
    List<Group> findByCreatedBy(Integer userId);

    // Belirli bir kullanıcının admin olduğu grupları getir
    List<Group> findByGroupAdmin(Integer userId);

    // Grup admin bilgisi ile birlikte grup detaylarını getir
    @Query("SELECT g FROM Group g WHERE g.id = :groupId")
    Optional<Group> findGroupWithDetails(@Param("groupId") Integer groupId);
}