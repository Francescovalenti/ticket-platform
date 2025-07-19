package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;

import org.platform.ticket.ticket_platform.repository.NoteRepository;
import org.platform.ticket.ticket_platform.repository.TicketRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")

public class AdminController {

    @Autowired

    private TicketRepository ticketRepository;

    @Autowired
    private NoteRepository noteRepository;

    @GetMapping("/admin")
    public String index(@RequestParam(name = "search", required = false) String search, Model model) {
        List<Ticket> tickets;

        if (search != null && !search.isEmpty()) {
            tickets = ticketRepository.findByTitleContaining(search);
        } else {
            tickets = ticketRepository.findAll();
        }

        model.addAttribute("tickets", tickets);
        return "admin/index";
    }

    @GetMapping("/admin/{id}")
    public String show(@PathVariable("id") Integer id, Model model) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato");
        }

        Ticket ticket = ticketOptional.get();
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", ticket.getNotes());
        model.addAttribute("newNote", new Note());

        return "admin/show";
    }

    @PostMapping("admin/{id}/notes")
    public String store(@PathVariable Integer id, @Valid @ModelAttribute("newNote") Note note,
            BindingResult bindingResult, Model model) {

        Optional<Ticket> optionalTicket = ticketRepository.findById(id);
        if (optionalTicket.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato");
        }
        Ticket ticket = optionalTicket.get();

        note.setTicket(ticket);
        note.setId(null);
        note.setAuthor("Author");
        note.setContent("text");
        note.setCreatedAt(LocalDateTime.now());
        noteRepository.save(note);
        return "redirect:/admin";
    }

}
