package app.web;

import app.policy.model.Policy;
import app.policy.service.PolicyService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.utility.RequestToPolicyMapper;
import app.web.dto.PolicyLimitsChangeRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/{id}/policy-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getPolicyChangeLimitsForm(@PathVariable UUID id) {

        Policy policy = policyService.getById(id);

        PolicyLimitsChangeRequest policyLimitsChangeRequest = RequestToPolicyMapper.fromPolicyToEditRequest(policy);

        ModelAndView modelAndView = new ModelAndView("policy_limits_form");
        modelAndView.addObject("policy", policy);
        modelAndView.addObject("policyLimitsChangeRequest", policyLimitsChangeRequest);
        return modelAndView;
    }

    @PostMapping("/{id}/policy-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView changePolicyLimits(@PathVariable UUID id, @Valid PolicyLimitsChangeRequest policyLimitsChangeRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        Policy policy = policyService.getById(id);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("policy_limits_form");
            modelAndView.addObject("policy", policy);
            modelAndView.addObject("policyLimitsChangeRequest", policyLimitsChangeRequest);
            return modelAndView;
        }

        User admin = userService.getById(authenticationMetadata.getUserId());

        policyService.updatePolicyLimits(policy, policyLimitsChangeRequest, admin);

        return new ModelAndView("redirect:/policy");
    }
}
