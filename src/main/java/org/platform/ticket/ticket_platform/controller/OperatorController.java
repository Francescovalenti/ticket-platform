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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/Operator")
public class OperatorController {
    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private TicketRepository ticketRepository;

    @Autowired
    private NoteRepository noteRepository;
     // dettagli della lista dei ticket assegnati
    @GetMapping("/detaiils/{id}")
   
    public String show(@PathVariable("id") Integer id, Model model) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato");
        }

        Ticket ticket = ticketOptional.get();
        model.addAttribute("ticket", ticket);
        model.addAttribute("user", ticket.getUser()); 
        return "operator/index";
}
  

 //dettaglio ticket assegnato al operatore
@PostMapping("/details/{id}/status")
 public String showTicketStatus(@PathVariable("id") Integer id, @AuthenticationPrincipal User loggedUser, Model model) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato");
        }
        Ticket ticket = ticketOptional.get();
        
       if (!ticket.getUser().getId().equals(loggedUser.getId()) ) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Non sei autorizzato");
}

    

      return ("operator/status"); 


}
  

//cambia lo stato di un ticket

@GetMapping("/operator/profile")
public String showProfile(@AuthenticationPrincipal User loggedUser, Model model) {
    model.addAttribute("user", loggedUser);
    return "operator/profile";
}


@PostMapping("/ticket/{ticketId}/status")
public String updateStatus(@PathVariable("ticketId") Integer id,@RequestParam("status") Ticket.StatusTicket status,@AuthenticationPrincipal User loggedUser,Model model) {
          
    
    Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato");
        }
        Ticket ticket = ticketOptional.get();
        
        if (!ticket.getUser().equals(loggedUser)) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Non sei autorizzato");
}
ticket.setStatus(status);        
ticketRepository.save(ticket);  
return "redirect:/operator";



        }

// cambia lo stato in attivo
@PostMapping("/operator/details")
public String setOperatorActive(@AuthenticationPrincipal User loggedUser)
{
    loggedUser.setStatus(User.UserStatus.ACTIVE);
    userRepository.save(loggedUser);
    return "redirect:/operator";
}

// cambia lo stato in inattivo 
@PostMapping ("/operator/details/inactive")
public String setOperatorInactive(@AuthenticationPrincipal User loggedUser){
    List<Ticket>tickets= ticketRepository.findByUser(loggedUser);
    for (Ticket ticket :tickets){
        if (ticket.getStatus() == Ticket.StatusTicket.IN_PROGRESS|| ticket.getStatus() == Ticket.StatusTicket.COMPLETED){
            return "redirect:/operator/details/active";
        } 
    }
    return "redirect:/operator/details/inactive";
}



    @PostMapping("/tickets/{id}/notes")
public String storeNoteForOperator(@PathVariable("id") Integer id, @Valid @ModelAttribute("newNote") Note note,BindingResult bindingResult,@AuthenticationPrincipal User loggedUser,Model model) {

    Optional<Ticket> optionalTicket = ticketRepository.findById(id);
    if (optionalTicket.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato");
    }

    Ticket ticket = optionalTicket.get();

    if (!ticket.getUser().getId().equals(loggedUser.getId())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Non sei autorizzato");
    }

    if (bindingResult.hasErrors()) {
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", ticket.getNotes());
        return "operator/status";
    }

    note.setTicket(ticket);
    note.setAuthor(loggedUser.getUsername()); 
    note.setCreatedAt(LocalDateTime.now());

    noteRepository.save(note);

    return "redirect:/operator/details/{id}/status";
}

}
