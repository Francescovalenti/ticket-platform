package org.platform.ticket.ticket_platform.controller;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();

        int status = response.getStatus();

        if (status == HttpStatus.NOT_FOUND.value()) {
            modelAndView.setViewName("error/error-404");
        } else if (status == HttpStatus.FORBIDDEN.value()) {
            modelAndView.setViewName("error/error-403");
        } else if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            modelAndView.setViewName("error/error-500");
        } else {
            modelAndView.setViewName("error/error"); 
        }

        modelAndView.addObject("status", status);
        return modelAndView;
    }
}