package co.com.yam.api;

import co.com.yam.api.dto.ErrorResponse;
import co.com.yam.api.dto.SubscribeRequest;
import co.com.yam.api.mapper.ApiMapper;
import co.com.yam.model.exception.AlreadySubscribedException;
import co.com.yam.model.exception.BusinessException;
import co.com.yam.model.exception.ClientNotFoundException;
import co.com.yam.model.exception.FundNotFoundException;
import co.com.yam.model.exception.InsufficientBalanceException;
import co.com.yam.model.exception.InvalidAmountException;
import co.com.yam.model.exception.OptimisticConcurrencyException;
import co.com.yam.model.exception.SubscriptionNotFoundException;
import co.com.yam.model.vo.Money;
import co.com.yam.usecase.cancelsubscription.CancelSubscriptionUseCase;
import co.com.yam.usecase.getclient.GetClientUseCase;
import co.com.yam.usecase.gettransactionhistory.GetTransactionHistoryUseCase;
import co.com.yam.usecase.listfunds.ListFundsUseCase;
import co.com.yam.usecase.subscribetofund.SubscribeToFundUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class Handler {

    private final SubscribeToFundUseCase subscribeToFundUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final ListFundsUseCase listFundsUseCase;
    private final GetClientUseCase getClientUseCase;

    public Mono<ServerResponse> listFunds(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(listFundsUseCase.list().map(ApiMapper::toResponse),
                        co.com.yam.api.dto.FundResponse.class);
    }

    public Mono<ServerResponse> getClient(ServerRequest request) {
        String clientId = request.pathVariable("clientId");
        return getClientUseCase.getById(clientId)
                .map(ApiMapper::toResponse)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .onErrorResume(this::toErrorResponse);
    }

    public Mono<ServerResponse> subscribe(ServerRequest request) {
        String clientId = request.pathVariable("clientId");
        return request.bodyToMono(SubscribeRequest.class)
                .flatMap(body -> {
                    if (body == null || body.fundId() == null || body.fundId().isBlank()) {
                        return Mono.error(new InvalidAmountException("fundId es obligatorio"));
                    }
                    Money amount = body.amount() == null ? null : Money.of(body.amount());
                    return subscribeToFundUseCase.subscribe(clientId, body.fundId(), amount);
                })
                .map(ApiMapper::toResponse)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .onErrorResume(this::toErrorResponse);
    }

    public Mono<ServerResponse> cancel(ServerRequest request) {
        String clientId = request.pathVariable("clientId");
        String fundId = request.pathVariable("fundId");
        return cancelSubscriptionUseCase.cancel(clientId, fundId)
                .map(ApiMapper::toResponse)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .onErrorResume(this::toErrorResponse);
    }

    public Mono<ServerResponse> history(ServerRequest request) {
        String clientId = request.pathVariable("clientId");
        return getClientUseCase.getById(clientId)
                .thenMany(getTransactionHistoryUseCase.history(clientId))
                .map(ApiMapper::toResponse)
                .collectList()
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .onErrorResume(this::toErrorResponse);
    }

    private Mono<ServerResponse> toErrorResponse(Throwable error) {
        HttpStatus status = resolveStatus(error);
        String code = error instanceof BusinessException business
                ? business.getCode()
                : "UNEXPECTED_ERROR";
        String message = error.getMessage() == null ? "Error inesperado" : error.getMessage();
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponse(code, message, Instant.now()));
    }

    private HttpStatus resolveStatus(Throwable error) {
        if (error instanceof InsufficientBalanceException
                || error instanceof AlreadySubscribedException
                || error instanceof InvalidAmountException
                || error instanceof SubscriptionNotFoundException) {
            return HttpStatus.CONFLICT;
        }
        if (error instanceof ClientNotFoundException || error instanceof FundNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (error instanceof OptimisticConcurrencyException) {
            return HttpStatus.CONFLICT;
        }
        if (error instanceof BusinessException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
