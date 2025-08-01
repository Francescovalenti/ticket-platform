package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;

import java.util.Optional;

import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.repository.NoteRepository;
import org.platform.ticket.ticket_platform.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/note")
public class NoteController {
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private TicketRepository ticketRepository;

    // possibilità di aggiungere note
    @PostMapping("/{id}/note")
    public String storeNote(@Valid @PathVariable Integer id, @ModelAttribute("newNote") Note formNote,BindingResult bindingResult, Authentication authentication, Model model) {
      Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Ticket ticket = ticketOptional.get();
        if (bindingResult.hasErrors()) {
            model.addAttribute("ticket", ticket);
            model.addAttribute("noteList", ticket.getNotes());
            return "admin/show";
        }
        formNote.setId(null);
        formNote.setTicket(ticket);
        formNote.setAuthor(authentication.getName());
        formNote.setCreatedAt(LocalDateTime.now());
        formNote.setUser(ticket.getUser());
        noteRepository.save(formNote);
        return "redirect:/admin/" + id;
    }

    // modifica note per Admin
    @GetMapping("/edit-note/{id}")
    public String editNote(@PathVariable("id") Integer id, Model model) {
       
        Optional<Note> noteOptional = noteRepository.findById(id);
        if(noteOptional.isEmpty()){
            throw new ResponseStatusException((HttpStatus.NOT_FOUND));

        }
        Note note = noteOptional.get();
        model.addAttribute("note", note);
        return "admin/edit-note";
    }

    @PostMapping("/edit-note/{id}")
    public String update(@Valid @PathVariable("id") Integer id, @ModelAttribute("note") Note formNote,BindingResult bindingResult, Model model, Authentication authentication) {
      Optional<Note> noteOptional = noteRepository.findById(id);
        if(noteOptional.isEmpty()){
            throw new ResponseStatusException((HttpStatus.NOT_FOUND));}
        Ticket ticket = formNote.getTicket();
        if (bindingResult.hasErrors()) {
            model.addAttribute("note", formNote);
            model.addAttribute("ticket", ticket);
            return "/admin/{id}";
        }
        Note note = noteOptional.get();
        formNote.setId(id);
        formNote.setTicket(ticket);
         formNote.setAuthor(note.getAuthor());
        formNote.setCreatedAt(LocalDateTime.now());
        formNote.setUser(ticket.getUser());
        noteRepository.save(formNote);
        return "redirect:/admin";
    
}

    // possibilità di gestione di note personale del operatore
    @PostMapping("/note/{id}")
    public String addNote(@Valid @PathVariable("id") Integer id, @ModelAttribute("newNote") Note formNote,  BindingResult bindingResult, Authentication authentication, Model model) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Ticket ticket = ticketOptional.get();
        if (bindingResult.hasErrors()) {
            model.addAttribute("ticket", ticket);
            model.addAttribute("noteList", ticket.getNotes());
            return "operator/show";
        }
        formNote.setId(null);
        formNote.setAuthor(authentication.getName());
        formNote.setTicket(ticket);
        formNote.setCreatedAt(LocalDateTime.now());
        formNote.setUser(ticket.getUser());
        noteRepository.save(formNote);
        return "redirect:/operator/" + id;
    }

    // possibilità di modifica note per operatore
    @GetMapping("/editnote/{id}")
    public String editNoteOper(@PathVariable("id") Integer id, Model model) {
        Optional<Note> noteOptional = noteRepository.findById(id);
        if(noteOptional.isEmpty()){
            throw new ResponseStatusException((HttpStatus.NOT_FOUND));}
        Note note = noteOptional.get();
        model.addAttribute("note", note);
        return "operator/editnote";
    }
    @PostMapping("/edit-note/{id}")
    public String updateOper(@Valid @PathVariable("id") Integer id, @ModelAttribute("note") Note formNote,BindingResult bindingResult, Model model, Authentication authentication) {
      Optional<Note> noteOptional = noteRepository.findById(id);
        if(noteOptional.isEmpty()){
            throw new ResponseStatusException((HttpStatus.NOT_FOUND));}
        Ticket ticket = formNote.getTicket();
        if (bindingResult.hasErrors()) {
            model.addAttribute("note", formNote);
            model.addAttribute("ticket", ticket);
            return "/operator/{id}";
        }
        Note note = noteOptional.get();
        formNote.setId(id);
        formNote.setTicket(ticket);
         formNote.setAuthor(note.getAuthor());
        formNote.setCreatedAt(LocalDateTime.now());
        formNote.setUser(ticket.getUser());
        noteRepository.save(formNote);
        return "redirect:/operator";
    
}

    // cancellazione note
    @PostMapping("/delete/{noteId}")
    public String deleteNote(@Valid @PathVariable("noteId") Integer noteId, @RequestParam("ticketId") Integer ticketId) {
        noteRepository.deleteById(noteId);
        return "redirect:/admin/" + ticketId;
    }

      @PostMapping("/deleteOper/{noteId}")
    public String deleteNoteOper(@Valid @PathVariable("noteId") Integer noteId, @RequestParam("ticketId") Integer ticketId) {
        noteRepository.deleteById(noteId);
        return "redirect:/operator/" + ticketId;
    }


}