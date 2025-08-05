package com.squad.squad.repository;

import com.squad.squad.context.GroupContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface SecureJpaRepository<T, ID> extends JpaRepository<T, ID> {

    // ❌ Geliştirici bunları kullanamaz - Güvenlik için deprecated
    @Override
    @Deprecated
    default List<T> findAll() {
        throw new UnsupportedOperationException(
                "Use findAllByGroupId() instead. Direct findAll() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default Optional<T> findById(ID id) {
        throw new UnsupportedOperationException(
                "Use findByIdAndGroupId() instead. Direct findById() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default List<T> findAllById(Iterable<ID> ids) {
        throw new UnsupportedOperationException(
                "Use findAllByIdAndGroupId() instead. Direct findAllById() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default long count() {
        throw new UnsupportedOperationException(
                "Use countByGroupId() instead. Direct count() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default boolean existsById(ID id) {
        throw new UnsupportedOperationException(
                "Use existsByIdAndGroupId() instead. Direct existsById() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default void deleteById(ID id) {
        throw new UnsupportedOperationException(
                "Use deleteByIdAndGroupId() instead. Direct deleteById() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default void deleteAllById(Iterable<? extends ID> ids) {
        throw new UnsupportedOperationException(
                "Use deleteAllByIdAndGroupId() instead. Direct deleteAllById() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default void deleteAll() {
        throw new UnsupportedOperationException(
                "Use deleteAllByGroupId() instead. Direct deleteAll() is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default List<T> findAll(Sort sort) {
        throw new UnsupportedOperationException(
                "Use findAllByGroupId() with Sort parameter instead. Direct findAll(Sort) is not allowed for security reasons.");
    }

    @Override
    @Deprecated
    default Page<T> findAll(Pageable pageable) {
        throw new UnsupportedOperationException(
                "Use findAllByGroupId() with Pageable parameter instead. Direct findAll(Pageable) is not allowed for security reasons.");
    }

    // ✅ Güvenli method'lar - GroupId ile otomatik filtreleme
    List<T> findAllByGroupId(Integer groupId);

    Optional<T> findByIdAndGroupId(ID id, Integer groupId);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.groupId = :groupId")
    List<T> findAllByIdAndGroupId(Iterable<ID> ids, Integer groupId);

    long countByGroupId(Integer groupId);

    boolean existsByIdAndGroupId(ID id, Integer groupId);

    void deleteByIdAndGroupId(ID id, Integer groupId);

    @Query("DELETE FROM #{#entityName} e WHERE e.id IN :ids AND e.groupId = :groupId")
    void deleteAllByIdAndGroupId(@Param("ids") Iterable<? extends ID> ids, @Param("groupId") Integer groupId);

    @Query("DELETE FROM #{#entityName} e WHERE e.groupId = :groupId")
    void deleteAllByGroupId(@Param("groupId") Integer groupId);

    List<T> findAllByGroupId(Integer groupId, Sort sort);

    Page<T> findAllByGroupId(Integer groupId, Pageable pageable);

    // Custom query için helper - GroupContext'ten otomatik groupId alır
    // Sadece onaylanmış grup ID'sini kullan, null group_id durumunda sadece kendi
    // verilerini göster
    @Query("SELECT e FROM #{#entityName} e WHERE (e.groupId = :groupId OR (e.groupId IS NULL AND e.id = :userId))")
    List<T> findAllByCurrentGroup(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

    @Query("SELECT e FROM #{#entityName} e WHERE (e.id = :id AND e.groupId = :groupId) OR (e.id = :id AND e.groupId IS NULL AND e.id = :userId)")
    Optional<T> findByIdAndCurrentGroup(@Param("id") ID id, @Param("groupId") Integer groupId,
            @Param("userId") Integer userId);

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE (e.groupId = :groupId) OR (e.groupId IS NULL AND e.id = :userId)")
    long countByCurrentGroup(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE (e.id = :id AND e.groupId = :groupId) OR (e.id = :id AND e.groupId IS NULL AND e.id = :userId)")
    boolean existsByIdAndCurrentGroup(@Param("id") ID id, @Param("groupId") Integer groupId,
            @Param("userId") Integer userId);

    @Query("SELECT e FROM #{#entityName} e WHERE (e.id IN :ids AND e.groupId = :groupId) OR (e.id IN :ids AND e.groupId IS NULL AND e.id = :userId)")
    List<T> findAllByIdAndCurrentGroup(@Param("ids") Iterable<ID> ids, @Param("groupId") Integer groupId,
            @Param("userId") Integer userId);
}