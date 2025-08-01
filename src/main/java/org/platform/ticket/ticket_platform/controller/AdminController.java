package org.platform.ticket.ticket_platform.controller;


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
    public String index(Authentication authentication,@RequestParam(name = "keywords", required = false) String keywords, Model model) {
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
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket non trovato");
        }
        Ticket ticket = ticketOptional.get();
        model.addAttribute("ticket", ticket);
        model.addAttribute("noteList", ticket.getNotes());
        model.addAttribute("newNote", new Note());
        return "admin/show";
    }

 
    // modifica ticket
    @GetMapping("/edit/ticket/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
       Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isEmpty()) {
            throw new RuntimeException("Ticket non trovato");
        }
        Ticket ticket = ticketOptional.get();
        model.addAttribute("ticket", ticket);
        model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/edit";
    }

    @PostMapping("/edit/ticket/{id}")
    public String update(@Valid @PathVariable("id") Integer id, @ModelAttribute("ticket") Ticket formTicket,BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findByRolesNameAndStatus("OPERATOR", UserStatus.ACTIVE));
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/edit";
        }
        formTicket.setId(id);
        ticketRepository.save(formTicket);
        return "redirect:/admin";
    }

    @PostMapping("/tickets/delete/{id}")
    public String delete(@PathVariable("id") Integer id,Model model) {
         Optional<Ticket> ticketOptional = ticketRepository.findById(id);
          if (ticketOptional.isEmpty()) {
              throw new RuntimeException("Ticket non trovato");
          }
        Ticket ticketToDelete = ticketOptional.get();
          for (Note note : ticketToDelete.getNotes()){
            noteRepository.delete(note);
          }
          ticketRepository.delete(ticketToDelete);
        
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
    public String updateprofile(@Valid @ModelAttribute("users") User FormUser, BindingResult bindingResult, Model model) {
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
    public String newCategory(@Valid @ModelAttribute("category") Category formCategory, BindingResult bindingResult,Model model) {
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