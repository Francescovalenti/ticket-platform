package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.model.User;
import org.platform.ticket.ticket_platform.repository.NoteRepository;
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

    @Autowired
    private NoteRepository noteRepository;

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

    // // possibilità di gestione di note personale
    // @PostMapping("/note/{id}")
    // public String addNote(@Valid @PathVariable("id") Integer id, @ModelAttribute("newNote") Note formNote, BindingResult bindingResult, Authentication authentication, Model model) {
    //     Ticket ticket = ticketRepository.findById(id)
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

    //     if (bindingResult.hasErrors()) {
    //         model.addAttribute("ticket", ticket);
    //         model.addAttribute("noteList", ticket.getNotes());
    //         return "operator/show";
    //     }
    //     formNote.setId(null);
    //       formNote.setAuthor(authentication.getName());
    //     formNote.setTicket(ticket);
    //     formNote.setCreatedAt(LocalDateTime.now());
    //     formNote.setUser(ticket.getUser());
    //     noteRepository.save(formNote);
    //     return "redirect:/operator/" + id;
    // }

    //     @GetMapping("/editnote/{id}")
    // public String editNote(@PathVariable("id") Integer id, Model model) {
    //     Note note = noteRepository.findById(id)
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

    //     model.addAttribute("note", note);
    //     return "operator/editnote";
    // }

    // @PostMapping("/editnote/{id}")
    // public String update(@Valid @PathVariable("id") Integer id, @ModelAttribute("note") Note formNote,BindingResult bindingResult, Model model, Authentication authentication) {
    //        Note note = noteRepository.findById(id)
    //          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "nota non trovata"));
    //     Ticket ticket = note.getTicket();
    //     if (bindingResult.hasErrors()) {
    //         model.addAttribute("note", formNote);
    //         model.addAttribute("ticket", ticket);
    //         return "/admin/{id}";
    //     }
    //     formNote.setId(id);
    //     formNote.setTicket(ticket);
    //     formNote.setAuthor(authentication.getName());
    //     formNote.setCreatedAt(LocalDateTime.now());
    //     formNote.setUser(ticket.getUser());

    //     noteRepository.save(formNote);
    //     return "redirect:/operator";
    // }

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
    public String updateProfile(@Valid @ModelAttribute("user") User userForm, BindingResult bindingResult, Model model,Authentication authentication) {
      if (bindingResult.hasErrors()) {
            model.addAttribute("user", userForm);
            model.addAttribute("isEditable", true);
            return "operator/profile";
        }

        List<Ticket> tickets = ticketRepository.findByUser(userForm);
        boolean hasOpenTickets = false;
        // modifica se lo stato è attivo
        for (Ticket oper : tickets) {
            if (oper.getStatus() == Ticket.StatusTicket.TODO || oper.getStatus() == Ticket.StatusTicket.IN_PROGRESS) {
                hasOpenTickets = true;
                break;
            }
        }
        // Non si può modificare lo stato in non attivo se ci sono ticket aperti
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
