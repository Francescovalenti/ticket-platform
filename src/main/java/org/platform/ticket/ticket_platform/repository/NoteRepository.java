package org.platform.ticket.ticket_platform.repository;

import java.util.List;

import org.platform.ticket.ticket_platform.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository <Note,Integer> {
   List <Note> findByTicketId(Integer id);
   List<Note> findByUserId(Integer userId);
   

    
}
