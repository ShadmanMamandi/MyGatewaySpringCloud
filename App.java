package com.fms.naka.messaging.platform.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.Routes;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.gateway.handler.predicate.RoutePredicates.path;

@SpringBootApplication
@EnableEurekaClient
@Component
public class App {

    private @Autowired
    DiscoveryClient discoveryClient;

    @Bean
    public RouteLocator customRouteLocator(RequestRateLimiterGatewayFilterFactory rateLimiter) {
        String xmppBrokerEndpoint = null;
        if (discoveryClient.getInstances("xmpp-broker").size() != 0) {
            String host = discoveryClient.getInstances("xmpp-broker").get(0).getHost();
            int port = discoveryClient.getInstances("xmpp-broker").get(0).getPort();
            xmppBrokerEndpoint = host + ":" + port;
        }
        return Routes.locator()
                .route("websocket_route")
                .predicate(path("/api/chat"))
                .uri("ws://" + xmppBrokerEndpoint + "/chat")
                .build();
    }

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http.httpBasic().and()
                .authorizeExchange()
                .pathMatchers("/anything/**").authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService reactiveUserDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
        return new MapReactiveUserDetailsService(user);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
