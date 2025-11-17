package org.workshop.coffee.controller;

import org.workshop.coffee.domain.Product;
import org.workshop.coffee.domain.Person;
import org.workshop.coffee.domain.Review;
import org.workshop.coffee.service.ProductService;
import org.workshop.coffee.service.PersonService;
import org.workshop.coffee.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final PersonService personService;

    @Autowired
    public ReviewController(ReviewService reviewService, ProductService productService, PersonService personService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.personService = personService;
    }

    @GetMapping("/product/{productId}")
    public String showProductReviews(@PathVariable Long productId, Model model) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            return "redirect:/";
        }
        
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getReviewsByProduct(product));
        model.addAttribute("averageRating", reviewService.getAverageRating(product));
        model.addAttribute("reviewCount", reviewService.getReviewCount(product));
        return "review/list";
    }

    @GetMapping("/add/{productId}")
    public String showAddReview(@PathVariable Long productId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Product product = productService.getProduct(productId);
        if (product == null) {
            return "redirect:/";
        }

        Review review = new Review();
        review.setProduct(product);
        model.addAttribute("review", review);
        model.addAttribute("product", product);
        return "review/add";
    }

    @PostMapping("/add/{productId}")
    public String saveReview(@PathVariable Long productId, 
                            @Valid Review review, 
                            BindingResult result, 
                            Model model, 
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Product product = productService.getProduct(productId);
        if (product == null) {
            return "redirect:/";
        }

        if (result.hasErrors()) {
            model.addAttribute("product", product);
            return "review/add";
        }

        Person reviewer = personService.findByUsername(principal.getName());
        review.setProduct(product);
        review.setReviewer(reviewer);

        reviewService.save(review);
        redirectAttributes.addFlashAttribute("message", "Your review has been submitted successfully.");
        return "redirect:/reviews/product/" + productId;
    }

    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Review review = reviewService.getReview(id);
        if (review == null) {
            return "redirect:/";
        }

        // Only allow deletion if user is the reviewer or admin
        if (principal != null) {
            Person currentUser = personService.findByUsername(principal.getName());
            if (currentUser != null && 
                (review.getReviewer().getId().equals(currentUser.getId()) || 
                 currentUser.getRoles().toString().equals("ROLE_ADMIN"))) {
                Long productId = review.getProduct().getId();
                reviewService.delete(id);
                redirectAttributes.addFlashAttribute("message", "Review deleted successfully.");
                return "redirect:/reviews/product/" + productId;
            }
        }

        return "redirect:/";
    }
}

