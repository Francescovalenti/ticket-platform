package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;
import java.util.List;


import org.platform.ticket.ticket_platform.model.Category;
import org.platform.ticket.ticket_platform.model.Note;
import org.platform.ticket.ticket_platform.model.Role;
import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.model.User;
import org.platform.ticket.ticket_platform.model.User.UserStatus;
import org.platform.ticket.ticket_platform.repository.CategoryRepository;
import org.platform.ticket.ticket_platform.repository.NoteRepository;
import org.platform.ticket.ticket_platform.repository.RoleRepository;
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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private RoleRepository roleRepository;

    // visualizzazione dei ticket
    @GetMapping
    public String index(Authentication authentication,
            @RequestParam(name = "keywords", required = false) String keywords, Model model) {
        List<Ticket> tickets;
        if (keywords != null && !keywords.isEmpty()) {
            tickets = ticketRepository.findByTitleContainingIgnoreCase(keywords);
        } else {
            tickets = ticketRepository.findAll();
        }
        model.addAttribute("keywords", keywords);
        model.addAttribute("tickets", tickets);
        return "admin/index";
    }

    // creazione ticket
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("note", noteRepository.findAll());
        return "admin/create";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("note", noteRepository.findAll());
            return "admin/create";
        }
        ticketRepository.save(formTicket);
        return "redirect:/admin";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", ticket.getNotes());
        model.addAttribute("newNote", new Note());
        return "admin/show";
    }

    // possibilità di aggiungere note
    @PostMapping("/{id}/note")
    public String storeNote(@Valid @PathVariable Integer id, @ModelAttribute("newNote") Note formNote,
            BindingResult bindingResult, Authentication authentication, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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

    // modifica ticket
    @GetMapping("/edit/ticket/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));
        model.addAttribute("ticket", ticket);
        model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/edit";
    }

    @PostMapping("/edit/ticket/{id}")
    public String update(@Valid @PathVariable("id") Integer id, @ModelAttribute("ticket") Ticket formTicket,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/edit";
        }
        formTicket.setId(id);
        ticketRepository.save(formTicket);
        return "redirect:/admin";
    }

    // modifica note
    @GetMapping("/edit-note/{id}")
    public String editNote(@PathVariable("id") Integer id, Model model) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        model.addAttribute("note", note);
        return "admin/edit-note";
    }

    @PostMapping("/edit-note/{id}")
    public String update(@Valid @PathVariable("id") Integer id, @ModelAttribute("note") Note formNote,BindingResult bindingResult, Model model, Authentication authentication) {
           Note note = noteRepository.findById(id)
             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "nota non trovata"));
        Ticket ticket = note.getTicket();
        if (bindingResult.hasErrors()) {
            model.addAttribute("note", formNote);
            model.addAttribute("ticket", ticket);
            return "/admin/{id}";
        }
        formNote.setId(id);
        formNote.setTicket(ticket);
        formNote.setAuthor(authentication.getName());
        formNote.setCreatedAt(LocalDateTime.now());
        formNote.setUser(ticket.getUser());

        noteRepository.save(formNote);
        return "redirect:/admin";
    }

    // cancellazione note
    @PostMapping("/notes/delete/{noteId}")
    public String deleteNote(@PathVariable("noteId") Integer noteId, @RequestParam("ticketId") Integer ticketId) {
        noteRepository.deleteById(noteId);
        return "redirect:/admin/" + ticketId;
    }

    @PostMapping("/tickets/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        ticketRepository.deleteById(id);
        return "redirect:/admin";
    }

    // creazione profilo operatore
    @GetMapping("/newprofile")
    public String profile(Model model) {
        model.addAttribute("users", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/newprofile";
    }

    @PostMapping("/newprofile")
    public String updateprofile(@Valid @ModelAttribute("users") User FormUser, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/newprofile";
        }

        List<Role> operatorRoles = roleRepository.findByName("OPERATOR");
        if (operatorRoles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Operation denied");
        }
        FormUser.setPassword("{noop}" + FormUser.getPassword());
        userRepository.save(FormUser);

        return "redirect:/admin";
    }

    // creazione categorie
    @GetMapping("/categories")
    public String category(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("category", new Category());
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String newCategory(@Valid @ModelAttribute("category") Category formCategory, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/categories";
        }
        categoryRepository.save(formCategory);
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id) {
        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }
}