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
        List<Ticket> myTickets = ticketRepository.findByUser(user);

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

    @PostMapping("/{id}")
    public String updateStatus(@Valid @PathVariable Integer id, @RequestParam("status") Ticket.StatusTicket status,Authentication authentication) {
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
    public String addNote(@Valid @PathVariable Integer id,@ModelAttribute("newNote") Note note,BindingResult bindingResult,Authentication authentication,Model model) {
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
        note.setId(null);
        note.setAuthor(username);
        note.setTicket(ticket);
        note.setAuthor(user.getUsername());
        note.setCreatedAt(LocalDateTime.now());
        note.setUser(user);
        noteRepository.save(note);
        return "redirect:/operator/" + id;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("isEditable", false);
        return "operator/profile";
    }

   
    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("isEditable", true);
        return "operator/profile";
    }

  
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("user") User userForm,BindingResult bindingResult,Model model,Authentication authentication) {
       
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userForm);
            model.addAttribute("isEditable", true);
            return "operator/profile";
        }
        userRepository.save(userForm);

        List<Ticket> tickets = ticketRepository.findByUser(userForm);
        boolean hasOpenTickets = false;

        for (Ticketse: tickets) {
            if (t.getStatus() == Ticket.StatusTicket.TODO || t.getStatus() == Ticket.StatusTicket.IN_PROGRESS) {
                hasOpenTickets = true;
                break;
            }
        }

        if (hasOpenTickets && userForm.getStatus() == User.UserStatus.NOT_ACTIVE) {
            model.addAttribute("user", userForm);
            model.addAttribute("isEditable", true);
            model.addAttribute("errorMessage", "Non puoi diventare 'Non attivo' se hai ticket aperti!");
            return "operator/profile";
        }

         userRepository.save(userForm);

        return "redirect:/operator/profile";
    }
}


