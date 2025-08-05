package com.squad.squad.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

@NoRepositoryBean
public interface SecureJpaRepository<T, ID> extends JpaRepository<T, ID> {

        // ❌ Geliştirici bunları kullanamaz - Güvenlik için deprecated
        @Override
        @Deprecated
        default @NonNull List<T> findAll() {
                throw new UnsupportedOperationException(
                                "Use findAllByGroupId() instead. Direct findAll() is not allowed for security reasons.");
        }

        @Override
        @Deprecated
        default @NonNull Optional<T> findById(@NonNull ID id) {
                throw new UnsupportedOperationException(
                                "Use findByIdAndGroupId() instead. Direct findById() is not allowed for security reasons.");
        }

        @Override
        @Deprecated
        default @NonNull List<T> findAllById(@NonNull Iterable<ID> ids) {
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
        default boolean existsById(@NonNull ID id) {
                throw new UnsupportedOperationException(
                                "Use existsByIdAndGroupId() instead. Direct existsById() is not allowed for security reasons.");
        }

        @Override
        @Deprecated
        default void deleteById(@NonNull ID id) {
                throw new UnsupportedOperationException(
                                "Use deleteByIdAndGroupId() instead. Direct deleteById() is not allowed for security reasons.");
        }

        @Override
        @Deprecated
        default void deleteAllById(@NonNull Iterable<? extends ID> ids) {
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
        default @NonNull List<T> findAll(@NonNull Sort sort) {
                throw new UnsupportedOperationException(
                                "Use findAllByGroupId() with Sort parameter instead. Direct findAll(Sort) is not allowed for security reasons.");
        }

        @Override
        @Deprecated
        default @NonNull Page<T> findAll(@NonNull Pageable pageable) {
                throw new UnsupportedOperationException(
                                "Use findAllByGroupId() with Pageable parameter instead. Direct findAll(Pageable) is not allowed for security reasons.");
        }

        // ✅ Güvenli method'lar - GroupId ile otomatik filtreleme
        @NonNull
        List<T> findAllByGroupId(@NonNull Integer groupId);

        @NonNull
        Optional<T> findByIdAndGroupId(@NonNull ID id, @NonNull Integer groupId);

        @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.groupId = :groupId")
        @NonNull
        List<T> findAllByIdAndGroupId(@NonNull Iterable<ID> ids, @NonNull Integer groupId);

        long countByGroupId(@NonNull Integer groupId);

        boolean existsByIdAndGroupId(@NonNull ID id, @NonNull Integer groupId);

        void deleteByIdAndGroupId(@NonNull ID id, @NonNull Integer groupId);

        @Modifying
        @Query("DELETE FROM #{#entityName} e WHERE e.id IN :ids AND e.groupId = :groupId")
        void deleteAllByIdAndGroupId(@Param("ids") @NonNull Iterable<? extends ID> ids,
                        @Param("groupId") @NonNull Integer groupId);

        @Modifying
        @Query("DELETE FROM #{#entityName} e WHERE e.groupId = :groupId")
        void deleteAllByGroupId(@Param("groupId") @NonNull Integer groupId);

        @NonNull
        List<T> findAllByGroupId(@NonNull Integer groupId, @NonNull Sort sort);

        @NonNull
        Page<T> findAllByGroupId(@NonNull Integer groupId, @NonNull Pageable pageable);

        // Custom query için helper - GroupContext'ten otomatik groupId alır
        // Sadece onaylanmış grup ID'sini kullan, null group_id durumunda sadece kendi
        // verilerini göster
        @Query("SELECT e FROM #{#entityName} e WHERE (e.groupId = :groupId OR (e.groupId IS NULL AND e.id = :userId))")
        @NonNull
        List<T> findAllByCurrentGroup(@Param("groupId") @NonNull Integer groupId,
                        @Param("userId") @NonNull Integer userId);

        @Query("SELECT e FROM #{#entityName} e WHERE (e.id = :id AND e.groupId = :groupId) OR (e.id = :id AND e.groupId IS NULL AND e.id = :userId)")
        @NonNull
        Optional<T> findByIdAndCurrentGroup(@Param("id") @NonNull ID id, @Param("groupId") @NonNull Integer groupId,
                        @Param("userId") @NonNull Integer userId);

        @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE (e.groupId = :groupId) OR (e.groupId IS NULL AND e.id = :userId)")
        long countByCurrentGroup(@Param("groupId") @NonNull Integer groupId, @Param("userId") @NonNull Integer userId);

        @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE (e.id = :id AND e.groupId = :groupId) OR (e.id = :id AND e.groupId IS NULL AND e.id = :userId)")
        boolean existsByIdAndCurrentGroup(@Param("id") @NonNull ID id, @Param("groupId") @NonNull Integer groupId,
                        @Param("userId") @NonNull Integer userId);

        @Query("SELECT e FROM #{#entityName} e WHERE (e.id IN :ids AND e.groupId = :groupId) OR (e.id IN :ids AND e.groupId IS NULL AND e.id = :userId)")
        @NonNull
        List<T> findAllByIdAndCurrentGroup(@Param("ids") @NonNull Iterable<ID> ids,
                        @Param("groupId") @NonNull Integer groupId,
                        @Param("userId") @NonNull Integer userId);
}