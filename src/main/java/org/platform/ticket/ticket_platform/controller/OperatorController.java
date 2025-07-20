package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.model.User;
import org.platform.ticket.ticket_platform.repository.NoteRepository;
import org.platform.ticket.ticket_platform.repository.TicketRepository;
import org.platform.ticket.ticket_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/operator")
public class OperatorController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;


    @GetMapping
    public String index(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Ticket> myTickets = ticketRepository.findByCategoryId(user.getId());

        model.addAttribute("tickets", myTickets);
        return "operator/index";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            return "redirect:/operator?error=forbidden";
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", ticket.getNotes());
        model.addAttribute("newNote", new Note());
        return "operator/show";
    }

    public String updateStatus(@PathVariable Integer id,
            @RequestParam("status") Ticket.StatusTicket status,
            Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        if (!ticket.getUser().getId().equals(user.getId())) {
            return "redirect:/operator?error=forbidden";
        }
        ticket.setStatus(status);
        ticketRepository.save(ticket);
        return "redirect:/operator/" + id;
    }

    @PostMapping("/{id}/note")
    public String addNote(@PathVariable Integer id,
            @Valid @ModelAttribute("newNote") Note note,
            BindingResult bindingResult,
            Authentication authentication,
            Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        if (!ticket.getUser().getId().equals(user.getId())) {
            return "redirect:/operator?error=forbidden";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("ticket", ticket);
            model.addAttribute("noteList", ticket.getNotes());
            return "operator/show";
        }
        note.setTicket(ticket);
        note.setAuthor(user.getUsername());
        note.setCreatedAt(LocalDateTime.now());
        note.setUser(user);
        noteRepository.save(note);
        return "redirect:/operator/" + id;
    }
}