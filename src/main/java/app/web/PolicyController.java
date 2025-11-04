package app.web;

import app.claim.model.Claim;
import app.policy.model.Policy;
import app.policy.service.PolicyService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/policy")
public class PolicyController {

    private final PolicyService policyService;
    private final UserService userService;

    public PolicyController(PolicyService policyService, UserService userService) {
        this.policyService = policyService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getAllPoliciesPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        List<Policy> allPolicies = policyService.getPolicies();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("policy");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allPolicies", allPolicies);

        return modelAndView;
    }

    @PatchMapping("/{id}/user-policy")
    public String changeUserPolicy(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        userService.changePolicy(id, user);
        return "redirect:/policy";
    }
}
