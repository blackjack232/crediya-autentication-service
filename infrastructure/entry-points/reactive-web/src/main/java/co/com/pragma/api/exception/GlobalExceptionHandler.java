package co.com.pragma.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

public class GlobalExceptionHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request,
                                       HandlerFunction<ServerResponse> next) {
        return next.handle(request)
                .onErrorResume(IllegalArgumentException.class, ex ->
                        ServerResponse.badRequest()
                                .bodyValue(Map.of("error", ex.getMessage()))
                )
                .onErrorResume(Exception.class, ex ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue(Map.of("error", "Ocurrió un error inesperado"))
                );
    }
}
