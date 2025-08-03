package org.platform.ticket.ticket_platform.controller;

import java.util.List;
import java.util.Optional;

import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.model.User;

import org.platform.ticket.ticket_platform.repository.TicketRepository;
import org.platform.ticket.ticket_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/operator")
public class OperatorController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // lista dei ticket personali
    @GetMapping
    public String index(Model model, Authentication authentication) {
        Optional<User> userOptional = userRepository.findByUsername(authentication.getName());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Utente non trovato");
        }
        User user = userOptional.get();
        List<Ticket> myTickets = ticketRepository.findByUser(user);
        model.addAttribute("tickets", myTickets);

        return "operator/index";
    }

    // mostra i dettagli dei ticket personali
    @GetMapping("/{id}")
    public String show(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", ticket.getNotes());
        model.addAttribute("newNote", new Note());
        return "operator/show";
    }

    // metodo per passare lo stato di un ticket da fare a completato
    @PostMapping("/{id}")
    public String updateStatus(@Valid @PathVariable Integer id, @RequestParam("status") Ticket.StatusTicket status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));
        ticket.setStatus(status);
        ticketRepository.save(ticket);
        return "redirect:/operator/" + id;
    }

    // Profilo personale del operatore.
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("isEditable", false);
        return "operator/profile";
    }

    // Modifica il profilo
    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("isEditable", true);
        return "operator/profile";
    }

    // Aggiorna il profilo per il cambiamento di stato.
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("user") User userForm, BindingResult bindingResult, Model model,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userForm);
            model.addAttribute("isEditable", true);
            return "operator/profile";
        }

        if (userForm.hasUnfinishedTickets() && userForm.getStatus() == User.UserStatus.NOT_ACTIVE) {
            model.addAttribute("user", userForm);
            model.addAttribute("isEditable", true);
            model.addAttribute("errorMessage", "Non puoi diventare 'Non attivo' se hai ticket aperti!");
            return "operator/profile";
        }

        userRepository.save(userForm);

        return "redirect:/operator/profile";
    }
}
