package app.web;
import app.claim.model.Claim;
import app.claim.service.ClaimService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ClaimSubmissionRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


@Controller
@RequestMapping("/claims")
public class ClaimController {
    private final ClaimService claimService;
    private final UserService userService;

    @Autowired
    public ClaimController(ClaimService claimService, UserService userService) {
        this.claimService = claimService;
        this.userService = userService;
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
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping
    public ModelAndView submitClaim(@Valid ClaimSubmissionRequest claimSubmissionRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("create-claim");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        Claim claim = claimService.createClaim(claimSubmissionRequest, user);

        return new ModelAndView("redirect:/claims");
    }
}
