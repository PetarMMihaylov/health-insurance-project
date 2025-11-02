package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.utility.RequestToUserMapper;
import app.web.dto.AccountBalanceRequest;
import app.web.dto.ClaimSubmissionRequest;
import app.web.dto.ProfileEditRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getUsers() {

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);

        return modelAndView;
    }

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        ProfileEditRequest profileEditRequest = RequestToUserMapper.fromUserToEditRequest(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile-menu");
        modelAndView.addObject("profileEditRequest", profileEditRequest);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PutMapping("/profile")
    public ModelAndView updateProfile(@Valid ProfileEditRequest profileEditRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationMetadata.getUserId());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile-menu");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        userService.updateProfile(authenticationMetadata.getUserId(), profileEditRequest);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/balance")
    public ModelAndView getBalancePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("change-balance");
        modelAndView.addObject("accountBalanceRequest", new AccountBalanceRequest());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PutMapping("/balance")
    public ModelAndView updateBalance(@Valid AccountBalanceRequest accountBalanceRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationMetadata.getUserId());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("change-balance");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        userService.updateBalance(authenticationMetadata.getUserId(), accountBalanceRequest);

        return new ModelAndView("redirect:/home");
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserRole(@PathVariable UUID id) {
        userService.changeRole(id);
        return "redirect:/users";
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserEmployment(@PathVariable UUID id) {
        userService.changeEmployment(id);
        return "redirect:/users";
    }
}
