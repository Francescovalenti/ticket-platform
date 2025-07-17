package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;

import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.model.User.UserStatus;
import org.platform.ticket.ticket_platform.repository.CategoryRepository;
import org.platform.ticket.ticket_platform.repository.NoteRepository;
import org.platform.ticket.ticket_platform.repository.TicketRepository;
import org.platform.ticket.ticket_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NoteRepository noteRepository;
    @Autowired private CategoryRepository categoryRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("tickets", ticketRepository.findAll());
        return "tickets/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
        model.addAttribute("categories", categoryRepository.findAll());
        return "tickets/create";
    }

    @PostMapping
    public String store(@Valid @ModelAttribute("ticket") Ticket formTicket,
                        BindingResult bindingResult,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
            model.addAttribute("categories", categoryRepository.findAll());
            return "tickets/create";
        }

        ticketRepository.save(formTicket);
        return "redirect:/tickets";
    }

    @GetMapping("/{id}/edit")
    public String edit(Model model, @PathVariable Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato con id: " + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
        model.addAttribute("categories", categoryRepository.findAll());
        return "tickets/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("ticket") Ticket formTicket,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
            model.addAttribute("categories", categoryRepository.findAll());
            return "tickets/edit";
        }

        ticketRepository.save(formTicket);
        return "redirect:/tickets/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato con id: " + id));
        ticketRepository.delete(ticket);
        return "redirect:/tickets";
    }

    @GetMapping("/{id}")
    public String show(Model model, @PathVariable Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato con id: " + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", noteRepository.findByTicketId(id));
        model.addAttribute("newNote", new Note());
        return "tickets/show";
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
            return "tickets/show";
        }

        note.setTicket(ticket);
        note.setCreatedAt(LocalDateTime.now());
        note.setAuthor("admin");
        noteRepository.save(note);

        return "redirect:/tickets/" + id;
    }
}
