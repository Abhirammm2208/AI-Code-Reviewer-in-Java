// package com.yourorg.aicode.controller;

// import com.yourorg.aicode.model.dto.ReviewRequest;
// import com.yourorg.aicode.model.dto.ReviewResponse;
// import com.yourorg.aicode.service.CodeReviewService;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping("/api/review")
// public class CodeReviewController {

//     private final CodeReviewService service;

//     public CodeReviewController(CodeReviewService service) {
//         this.service = service;
//     }

//     @PostMapping
//     public ResponseEntity<ReviewResponse> review(@RequestBody ReviewRequest request) {
//         ReviewResponse response = service.review(request);
//         return ResponseEntity.ok(response);
//     }
// }


package com.yourorg.aicode.controller;

import com.yourorg.aicode.model.dto.ReviewRequest;
import com.yourorg.aicode.model.dto.ReviewResponse;
import com.yourorg.aicode.service.CodeReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for code review endpoints.
 * Handles code submission and review operations.
 */
@RestController
@RequestMapping("/api/reviews")
public class CodeReviewController {

    private final CodeReviewService service;

    public CodeReviewController(CodeReviewService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ReviewResponse> review(@RequestBody ReviewRequest request) {
        return ResponseEntity.ok(service.review(request));
    }
}
