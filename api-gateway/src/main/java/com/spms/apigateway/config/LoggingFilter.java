package com.spms.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        logger.info("Incoming Gateway Request -> Method: {}, Path: {}", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("Outgoing Gateway Response -> Status: {} for Path: {}",
                    exchange.getResponse().getStatusCode(), path);
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
