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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {
    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping
    public ResponseEntity<List<Ticket>> index(@RequestParam(name = "keyword", required = false) String keyword) {
        List<Ticket> ticket;
        if (keyword != null && !keyword.isEmpty() && !keyword.isBlank()) {
            ticket = ticketRepository.findByTitleContainingIgnoreCase(keyword);

        } else {
            ticket = ticketRepository.findAll();
        }

        if (ticket.size() == 0) {
            return new ResponseEntity<List<Ticket>>(HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<List<Ticket>>(ticket, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> get(@PathVariable Integer id) {
        Optional<Ticket> ticketAttempt = ticketRepository.findById(id);

        if (ticketAttempt.isEmpty()) {
            return new ResponseEntity<Ticket>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Ticket>(ticketAttempt.get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@Valid @RequestBody Ticket Ticket) {
        ticketRepository.save(Ticket);
        return new ResponseEntity<Ticket>(HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> update(@Valid @RequestBody Ticket Ticket, @PathVariable Integer id) {

        if (ticketRepository.findById(id).isPresent()) {
            Ticket.setId(id);
            return new ResponseEntity<Ticket>(ticketRepository.save(Ticket), HttpStatus.OK);

        }

        return new ResponseEntity<Ticket>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Ticket> delete(@PathVariable Integer id) {
        Optional<Ticket> ticketAttempt = ticketRepository.findById(id);
        if (ticketAttempt.isPresent()) {
            ticketRepository.delete(ticketAttempt.get());
            return new ResponseEntity<Ticket>(HttpStatus.OK);
        }

        return new ResponseEntity<Ticket>(HttpStatus.NOT_FOUND);
    }


@GetMapping("/category")
public ResponseEntity<List<Ticket>> filterByCategory(@RequestParam("name") String categoryName) {
    List<Ticket> tickets = ticketRepository.findByCategory_NameIgnoreCase(categoryName);

    if (tickets.size() == 0) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(tickets, HttpStatus.OK);
}

@GetMapping("/status")
public ResponseEntity<List<Ticket>> filterByStatus(@RequestParam("status") Ticket.StatusTicket status) {
    List<Ticket> tickets = ticketRepository.findByStatus(status);

    if (tickets.size() == 0) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(tickets, HttpStatus.OK);
}

}
