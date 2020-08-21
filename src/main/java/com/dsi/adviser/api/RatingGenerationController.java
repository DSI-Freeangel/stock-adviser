package com.dsi.adviser.api;

import com.dsi.adviser.core.RatingUpdateRunner;
import com.dsi.adviser.core.model.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RatingGenerationController {
    private final RatingUpdateRunner ratingUpdateRunner;

    @GetMapping("/start")
    public Mono<Status> trigger() {
        return ratingUpdateRunner.start();
    }
}
