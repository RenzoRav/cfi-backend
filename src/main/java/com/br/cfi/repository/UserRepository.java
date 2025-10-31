package com.br.cfi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.cfi.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
