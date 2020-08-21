package com.dsi.adviser.core;

import com.dsi.adviser.core.model.Status;
import com.dsi.adviser.core.model.StatusModel;
import com.dsi.adviser.rating.Rating;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingUpdateRunner {
    private final RatingCalculationProcessor ratingCalculationProcessor;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean inProgress = new AtomicBoolean(false);

    public Mono<Status> start() {
        return Mono.just(inProgress)
                .filter(value -> !inProgress.getAndSet(true))
                .doOnNext(p -> this.run())
                .map(p -> started())
                .switchIfEmpty(Mono.fromCallable(this::inProgress))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.fromCallable(this::error);
                } );
    }

    @SneakyThrows
    private void run() {
        log.info("Rating generation started!");
            ratingCalculationProcessor.updateRating()
                    .publishOn(Schedulers.newSingle("complete-ratings"))
                    .doOnNext(this::logNextElement)
                    .doOnError(this::handleError)
                    .doOnComplete(this::complete)
                    .doOnSubscribe((s) -> log.info("Subscribed!"))
                    .subscribe();
//                    .subscribe(this::logNextElement, this::handleError, this::complete, (s) -> log.info("Subscribed!"));
        log.info("Rating generation in progress.");
    }

    private void logNextElement(Rating rating) {
        log.info("Rating for {} calculated", rating.getStockCodeFull());
    }

    private void handleError(Throwable exception) {
        log.error("Error while rating calculation", exception);
        complete();
    }

    private void complete() {
        log.info("Rating generation complete!");
        inProgress.set(false);
    }

    private StatusModel error() {
        return StatusModel.builder().setInProgress(false).setMessage("Error!").build();
    }

    private StatusModel inProgress() {
        return StatusModel.builder().setInProgress(true).setMessage("Already in progress!").build();
    }

    private Status started() {
        return StatusModel.builder().setInProgress(true).setMessage("Started successfully!").build();
    }

}
