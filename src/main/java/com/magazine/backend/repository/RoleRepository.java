package com.magazine.backend.repository;

import java.util.Optional;

import com.magazine.backend.models.ERole;
import com.magazine.backend.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
