package com.pot.iot.webapp.Repository;

import com.pot.iot.webapp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUserId(String userId);
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    User findUserByEmailAndAccountStatus(String email,boolean accountStatus);
}
