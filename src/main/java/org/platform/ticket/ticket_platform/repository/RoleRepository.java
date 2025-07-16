package org.platform.ticket.ticket_platform.repository;

import java.util.List;

import org.platform.ticket.ticket_platform.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository <Role,Integer> {
       List<Role> findByName(String name);
}
