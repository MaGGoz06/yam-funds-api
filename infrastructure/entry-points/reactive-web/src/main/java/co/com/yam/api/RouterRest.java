package co.com.yam.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/funds",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listFunds",
                    operation = @Operation(operationId = "listFunds", summary = "Listar fondos disponibles")
            ),
            @RouterOperation(
                    path = "/api/v1/clients/{clientId}",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getClient",
                    operation = @Operation(
                            operationId = "getClient",
                            summary = "Consultar cliente, saldo y suscripciones",
                            parameters = @Parameter(name = "clientId", in = ParameterIn.PATH, required = true)
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/clients/{clientId}/subscriptions",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "subscribe",
                    operation = @Operation(
                            operationId = "subscribe",
                            summary = "Suscribirse a un fondo (apertura)",
                            parameters = @Parameter(name = "clientId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(required = true, content = @Content(
                                    schema = @Schema(implementation = co.com.yam.api.dto.SubscribeRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Suscripción creada"),
                                    @ApiResponse(responseCode = "409", description = "Saldo insuficiente o ya suscrito")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/clients/{clientId}/subscriptions/{fundId}",
                    method = RequestMethod.DELETE,
                    beanClass = Handler.class,
                    beanMethod = "cancel",
                    operation = @Operation(
                            operationId = "cancel",
                            summary = "Cancelar suscripción a un fondo",
                            parameters = {
                                    @Parameter(name = "clientId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "fundId", in = ParameterIn.PATH, required = true)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/clients/{clientId}/transactions",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "history",
                    operation = @Operation(
                            operationId = "history",
                            summary = "Historial de aperturas y cancelaciones",
                            parameters = @Parameter(name = "clientId", in = ParameterIn.PATH, required = true)
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/funds"), handler::listFunds)
                .andRoute(GET("/api/v1/clients/{clientId}"), handler::getClient)
                .andRoute(POST("/api/v1/clients/{clientId}/subscriptions")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::subscribe)
                .andRoute(DELETE("/api/v1/clients/{clientId}/subscriptions/{fundId}"), handler::cancel)
                .andRoute(GET("/api/v1/clients/{clientId}/transactions"), handler::history);
    }
}
