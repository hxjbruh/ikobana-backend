package com.ikobana.backend.repository;

import com.ikobana.backend.entity.User;
import com.ikobana.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link User} entity.
 * Provides data access operations for user management and authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their registered email address.
     *
     * @param email unique email address
     * @return Optional containing the User if found, or empty Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their unique username.
     *
     * @param username unique username
     * @return Optional containing the User if found, or empty Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their registered phone number.
     *
     * @param phone unique phone number
     * @return Optional containing the User if found, or empty Optional
     */
    Optional<User> findByPhone(String phone);

    /**
     * Check whether an account exists with the given email address.
     *
     * @param email email address to check
     * @return true if an account exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check whether an account exists with the given username.
     *
     * @param username username to check
     * @return true if an account exists with this username, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find all users that belong to a specific role.
     *
     * @param role role enum (ADMIN, RETAILER, CUSTOMER)
     * @return list of matching users
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all users filtered by their active status.
     *
     * @param isActive active status flag
     * @return list of matching users
     */
    List<User> findByIsActive(boolean isActive);

    /**
     * Search users by matching keyword against email or username (case-sensitive or wildcard).
     *
     * @param keyword search term
     * @return list of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %?1% OR u.username LIKE %?1%")
    List<User> searchUsers(String keyword);
}
