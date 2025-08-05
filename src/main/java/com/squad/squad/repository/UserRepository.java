package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    void deleteByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findUserByUsername(String username);

    Optional<User> findByUsername(String name);

    // Manuel groupId yönetimi için - SecureJpaRepository extend etmez
    @Query(value = "SELECT * FROM public.\"user\" WHERE group_id = :groupId AND group_id != 0", nativeQuery = true)
    List<User> findAllByGroupId(@Param("groupId") Integer groupId);

    // Güvenlik kısıtlamalarını bypass etmek için özel metod
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findUserWithDetails(@Param("userId") Integer userId);
}