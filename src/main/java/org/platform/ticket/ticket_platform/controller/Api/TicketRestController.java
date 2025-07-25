package org.platform.ticket.ticket_platform.controller.Api;

import java.util.List;
import java.util.Optional;

import org.platform.ticket.ticket_platform.model.Ticket;
import org.platform.ticket.ticket_platform.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {
     @Autowired
     private TicketRepository ticketRepository;

  
    @GetMapping
    public List<Ticket> index() {
        return ticketRepository.findAll();
    }
    
 
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> show(@PathVariable Integer id) {
        Optional<Ticket> ticketAttempt = ticketRepository.findById(id);

        if (ticketAttempt.isEmpty()) {
            return new ResponseEntity<Ticket>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Ticket>(ticketAttempt.get(), HttpStatus.OK);
    }


    
    @PostMapping
    public ResponseEntity<Ticket> store(@RequestBody Ticket Ticket) {
        return new ResponseEntity<Ticket>(ticketRepository.save(Ticket), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> update(@RequestBody Ticket Ticket, @PathVariable Integer id) {

        if (ticketRepository.findById(id).isEmpty()) {
            return new ResponseEntity<Ticket>(HttpStatus.NOT_FOUND);

        }

        Ticket.setId(id);
        return new ResponseEntity<Ticket>(ticketRepository.save(Ticket), HttpStatus.OK);
    }
   
    @DeleteMapping("/{id}")
    public ResponseEntity<Ticket> delete(@PathVariable Integer id) {
        if (ticketRepository.findById(id).isEmpty()) {
            return new ResponseEntity<Ticket>(HttpStatus.NOT_FOUND);
        }

        ticketRepository.deleteById(id);
        return new ResponseEntity<Ticket>(HttpStatus.OK);
    }
    
    @GetMapping("/category")
    public List<Ticket> filterByCategory(@RequestParam("name") String categoryName) {
        return ticketRepository.findByCategory_NameIgnoreCase(categoryName);
    }

   
    @GetMapping("/status")
    public List<Ticket> filterByStatus(@RequestParam("status") Ticket.StatusTicket status) {
        return ticketRepository.findByStatus(status);
    }
}

 