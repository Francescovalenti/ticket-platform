package org.platform.ticket.ticket_platform;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@SpringBootTest
@AutoConfigureMockMvc
public class TicketTest {
      
       @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllTickets() throws Exception {
        mockMvc.perform(get("/tickets"))
               .andExpect(status().isOk())
               .andExpect(view().name("tickets/index"));
    }


    }
