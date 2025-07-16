package org.platform.ticket.ticket_platform.repository;

import org.platform.ticket.ticket_platform.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CategoryRepository extends JpaRepository<Category,Integer>{
     List<Category> findByName(String name);
    
} 
    

