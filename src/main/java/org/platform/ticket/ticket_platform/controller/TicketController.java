package org.platform.ticket.ticket_platform.controller;

import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.repository.NoteRepository;
import org.platform.ticket.ticket_platform.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String show(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato con id: " + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", noteRepository.findByTicketId(id));
        model.addAttribute("newNote", new Note());
        return "tickets/show";
    }

    @PostMapping("/{id}/note")
    public String addNote(@Valid @PathVariable ("id")Integer id,  @ModelAttribute("newNote") Note note,Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));

        
            model.addAttribute("ticket", ticket);
            model.addAttribute("noteList", noteRepository.findByTicketId(id));
            return "ticket/show";
        

    }

}
