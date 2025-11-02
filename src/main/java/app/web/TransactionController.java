package app.web;

import app.security.AuthenticationMetadata;
import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getTransactions(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        List<Transaction> allTransactions = transactionService.getAllTransactions(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transactions");
        modelAndView.addObject("transactions", allTransactions);

        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getTransactionInformation(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        Transaction transaction = transactionService.getByIdForUser(id, user);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transaction-details");
        modelAndView.addObject("transaction", transaction);

        return modelAndView;
    }

    @DeleteMapping("/{id}/deletion")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('can_delete')")
    public String deleteTransaction(@PathVariable UUID id) {
        transactionService.softDelete(id);
        return "redirect:/transactions";
    }
}
