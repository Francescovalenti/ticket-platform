package org.platform.ticket.ticket_platform.controller;

import java.util.List;

import org.platform.ticket.ticket_platform.model.Category;
import org.platform.ticket.ticket_platform.model.Role;
import org.platform.ticket.ticket_platform.model.User;
import org.platform.ticket.ticket_platform.repository.CategoryRepository;
import org.platform.ticket.ticket_platform.repository.RoleRepository;
import org.platform.ticket.ticket_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/function")
public class AdminFunctionController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired 
    private UserRepository userRepository;

     @Autowired
    private CategoryRepository categoryRepository;

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
            return "function/newprofile";
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
            return "function/categories";
        }
        categoryRepository.save(formCategory);
        return "redirect:/function/categories";
    }

    //cancellazione categoria

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id) {
        categoryRepository.deleteById(id);
        return "redirect:/function/categories";
    }
    
}
