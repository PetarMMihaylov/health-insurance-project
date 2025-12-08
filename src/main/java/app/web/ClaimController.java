package app.web;
import app.claim.model.Claim;
import app.claim.service.ClaimService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.utility.DocumentUtil;
import app.web.dto.ClaimSubmissionRequest;
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
@RequestMapping("/claims")
public class ClaimController {
    private final ClaimService claimService;
    private final UserService userService;
    private final DocumentUtil documentUtil;

    @Autowired
    public ClaimController(ClaimService claimService, UserService userService, DocumentUtil documentUtil) {
        this.claimService = claimService;
        this.userService = userService;
        this.documentUtil = documentUtil;
    }

    @GetMapping
    public ModelAndView getAllClaimsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        List<Claim> allClaims = claimService.getClaims(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("claims");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allClaims", allClaims);

        return modelAndView;
    }

    @GetMapping("/new-claim")
    public ModelAndView getNewClaimPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("create-claim");
        modelAndView.addObject("claimSubmissionRequest", new ClaimSubmissionRequest());
        modelAndView.addObject("documents", documentUtil.getAvailableDocuments());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getClaimDetails(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        Claim claim = claimService.getByIdForUser(id, user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("claim-details");
        modelAndView.addObject("claim", claim);

        return modelAndView;
    }

    @PostMapping("/new-claim")
    public ModelAndView submitClaim(@Valid ClaimSubmissionRequest claimSubmissionRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("create-claim");
            modelAndView.addObject("documents", documentUtil.getAvailableDocuments());
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        claimService.createClaim(claimSubmissionRequest, user);

        return new ModelAndView("redirect:/claims");
    }

    @DeleteMapping("/{id}/deletion")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('can_delete')")
    public String deleteClaim(@PathVariable UUID id) {
        claimService.softDelete(id);
        return "redirect:/claims";
    }
}
