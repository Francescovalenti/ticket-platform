package org.platform.ticket.ticket_platform.repository;

import java.util.List;

import org.platform.ticket.ticket_platform.model.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository  extends JpaRepository <Ticket,Integer>{

      List<Ticket> findByTitleContaining(String title); 
      List<Ticket> findByCategoryId(Integer categoryId);
      List<Ticket> findByStatus(Ticket.StatusTicket status);
      List<Ticket> findByUserRolesNameAndStatus(String roleName, Ticket.StatusTicket status);


    
}
