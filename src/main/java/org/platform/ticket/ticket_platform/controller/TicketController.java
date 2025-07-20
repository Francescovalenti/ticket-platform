package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;
import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.repository.NoteRepository;
import org.platform.ticket.ticket_platform.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/tickets")  
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NoteRepository noteRepository;

    
    @GetMapping
    public String index(Model model) {
        model.addAttribute("tickets", ticketRepository.findAll());
        return "tickets/index";  
    }

   
    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato con id: " + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", noteRepository.findByTicketId(id));  
        model.addAttribute("newNote", new Note());  
        return "admin/show"; 
    }

    @PostMapping("/{id}/note")
    public String addNote(@PathVariable Integer id,
            @Valid @ModelAttribute("newNote") Note note,
            BindingResult bindingResult,
            Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));

        if (bindingResult.hasErrors()) {
            model.addAttribute("ticket", ticket);
            model.addAttribute("noteList", noteRepository.findByTicketId(id));
            return "admin/show";  
        }

        
        note.setTicket(ticket);
        note.setCreatedAt(LocalDateTime.now());
        note.setAuthor("admin");  
        noteRepository.save(note);  

        return "redirect:/tickets/" + id;  
    }

    
}
    
   