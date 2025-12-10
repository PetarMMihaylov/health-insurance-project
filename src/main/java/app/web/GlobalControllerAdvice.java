package app.web;

import app.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException exception) {

        return "redirect:/reports";
    }

    @ExceptionHandler(ClaimNotFoundException.class)
    public String handleClaimNotFoundException(ClaimNotFoundException exception) {

        return "redirect:/claims";
    }

    @ExceptionHandler(InvalidCompanyException.class)
    public String handleInvalidCompanyException(InvalidCompanyException exception) {

        return "redirect:/register";
    }

    @ExceptionHandler(PolicyNotFoundException.class)
    public String handlePolicyNotFoundException(PolicyNotFoundException exception) {

        return "redirect:/policy";
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public String handleTransactionNotFoundException(TransactionNotFoundException exception) {

        return "redirect:/transactions";
    }

    @ExceptionHandler(UserAlreadyFoundException.class)
    public String handleUserAlreadyFoundException(UserAlreadyFoundException exception) {

        return "redirect:/register";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFoundException(UserNotFoundException exception) {

        ModelAndView modelAndView = new ModelAndView("not-found");

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException exception) {

        ModelAndView modelAndView = new ModelAndView("access-denied");

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleOtherExceptions(Exception exception) {

        ModelAndView modelAndView = new ModelAndView("internal-server-error");

        return modelAndView;
    }
}
