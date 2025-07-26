package org.platform.ticket.ticket_platform.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("note", noteRepository.findAll());
        return "admin/create";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("ticket") Ticket ticket, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ticket", new Ticket());
            model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("note", noteRepository.findAll());
            return "admin/create";
        }
        ticketRepository.save(ticket);
        return "redirect:/admin";
    }

@GetMapping("/{id}")
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

    @PostMapping("/note/{id}")
    public String store(@Valid @PathVariable Integer id, @ModelAttribute("newNote") Note note,BindingResult bindingResult, Model model) {
    Ticket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

    if (bindingResult.hasErrors()) {
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", ticket.getNotes());
        return "admin/show";
    }

    note.setTicket(ticket);
    note.setUser(ticket.getUser());
    note.setCreatedAt(LocalDateTime.now());
    noteRepository.save(note);

    return "redirect:/admin/" + id;
}

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));
        model.addAttribute("ticket", ticket);
        model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@Valid @PathVariable("id") Integer id, @ModelAttribute("ticket") Ticket formTicket, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/edit";
        }
        formTicket.setId(id);
        ticketRepository.save(formTicket);
        return "redirect:/admin";
    }

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