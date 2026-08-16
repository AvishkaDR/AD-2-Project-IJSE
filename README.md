# API Gateway

The **API Gateway** acts as the main entry point of the Smart Parking
Management System (SPMS). It handles authentication and routes incoming
requests to the required backend services. The gateway does not maintain
its own business data, entities, repositories, or database.

## Gateway Responsibilities

``` mermaid
flowchart LR
    Client[Client] -->|Bearer JWT| Gateway[API Gateway :8080]
    Gateway -->|lb:// via Eureka| Parking[parking-service :8081]
    Gateway -->|lb:// via Eureka| Vehicle[vehicle-service :8082]
    Gateway -->|direct URL| User[user-service :8083]
    Gateway -->|lb:// via Eureka| Payment[payment-service :8084]
```

All system requests go through this service. The project uses **Spring
Cloud Gateway with the MVC/servlet implementation**, together with
Spring MVC and Spring Security rather than reactive application code.

## Technology Stack

  ---------------------------------------------------------------------------------------------------------
  Layer                   Technology                                       Purpose
  ----------------------- ------------------------------------------------ --------------------------------
  Framework               Spring Cloud Gateway                             Provides request routing and
                          (`spring-cloud-starter-gateway-server-webmvc`)   gateway filters

  Security                Spring Security (`spring-boot-starter-security`) Handles stateless authentication
                                                                           using JWT

  JWT                     `jjwt-api` + `jjwt-impl` + `jjwt-jackson`        Creates and validates
                          (0.12.6)                                         HMAC-SHA256 JWT tokens

  HTTP Client             Spring WebFlux `WebClient`                       Used for outbound communication
                                                                           with the Node user-service

  Discovery               Spring Cloud Eureka Client + LoadBalancer        Finds service instances and
                                                                           distributes requests

  Configuration           Spring Cloud Config Client                       Loads gateway settings from
                                                                           `config-repo/api-gateway.yaml`
  ---------------------------------------------------------------------------------------------------------

## Project Layout

``` text
infastructure/api-gateway/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/
└── src/main/
    ├── java/com/spms/apigateway/
    │   ├── ApiGatewayApplication.java
    │   ├── client/
    │   │   └── UserServiceClient.java
    │   ├── config/
    │   │   ├── SecurityConfig.java
    │   │   └── WebClientConfig.java
    │   ├── controller/
    │   │   └── AuthController.java
    │   ├── dto/
    │   │   ├── req/LoginReq.java
    │   │   └── res/LoginRes.java
    │   ├── exceptions/
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── InvalidCredentialsException.java
    │   │   └── ServiceUnavailableException.java
    │   ├── security/
    │   │   ├── JwtAuthFilter.java
    │   │   └── JwtService.java
    │   ├── service/
    │   │   └── AuthService.java
    │   └── util/
    │       └── ApiResponse.java
    └── resources/
        └── application.yaml
```

## Main Components

### `ApiGatewayApplication`

This is the Spring Boot startup class. It uses `@SpringBootApplication`
and `@EnableDiscoveryClient`, allowing the gateway to register with
Eureka at `:8761`.

### `security/JwtService`

Responsible for JWT creation and validation.

-   `generateToken(userId, email, role)` creates a token containing the
    user ID, email, and role.
-   `parse(token)` verifies the token signature and extracts its claims.
-   `isValid(token)` checks whether the token can be successfully
    parsed. Invalid signatures, expired tokens, or tampered tokens
    result in `false`.

The token uses HMAC-SHA256 and follows the configured `jwt.secret` and
`jwt.expiration-ms` values.

### `security/JwtAuthFilter`

`JwtAuthFilter` extends `OncePerRequestFilter` and processes incoming
Bearer tokens.

-   `/api/auth/**` and `/actuator/**` are excluded from token filtering.
-   Other requests are checked for an `Authorization: Bearer <token>`
    header.
-   A valid token creates a `UsernamePasswordAuthenticationToken`.
-   The user ID is used as the principal and the role is converted into
    `ROLE_<role>`.
-   Missing or invalid tokens leave the request unauthenticated,
    allowing Spring Security to return `401`.

### `config/SecurityConfig`

Defines the application's servlet security rules.

-   CSRF, form login, and HTTP Basic authentication are disabled.
-   Sessions are stateless.
-   `/api/auth/**` and `/actuator/**` are publicly accessible.
-   All remaining endpoints require authentication.
-   Unauthorized requests receive a JSON `ApiResponse` instead of the
    default HTML response.

### `config/WebClientConfig`

Provides the standard `WebClient` used by the gateway.

It is intentionally **not** marked with `@LoadBalanced`, because the
gateway communicates with the Node-based user-service through its
configured direct URL rather than Eureka.

### `client/UserServiceClient`

This client communicates with the user-service to validate login
credentials.

-   Sends `{ email, password }` to `${user-service.url}/user/login`.
-   A `401` response becomes `InvalidCredentialsException`.
-   Other service errors become `ServiceUnavailableException`.
-   Connection problems also result in a `503` response.
-   The returned MongoDB `_id` is mapped to the DTO using
    `@JsonProperty("_id")`.
-   The Node service's JWT is ignored because the gateway creates its
    own JWT.

### `service/AuthService`

Controls the login process:

1.  Sends the credentials to the user-service.
2.  Receives the user information.
3.  Generates a gateway JWT.
4.  Returns `LoginRes` containing `token`, `userId`, `name`, `email`,
    and `role`.

### `controller/AuthController`

Provides the authentication endpoint:

`POST /api/auth/login`

The request is validated with Bean Validation before being passed to
`AuthService`. Responses use the common `ApiResponse` structure.

### `exceptions/`

Centralizes API error handling.

-   Invalid credentials → `401`
-   Service unavailable → `503`
-   Validation errors → `400`
-   Unexpected exceptions → `500`

All responses follow the `{ statusCode, message, data }` format.

## Request Routing

Gateway route definitions are maintained inside
`config-repo/api-gateway.yaml`.

  ------------------------------------------------------------------------------------------------------------
  Route ID            Predicate                Destination                                   Filter
  ------------------- ------------------------ --------------------------------------------- -----------------
  `parking-service`   `Path=/api/parking/**`   `lb://parking-service`                        `StripPrefix=1`

  `vehicle-service`   `Path=/api/vehicle/**`   `lb://vehicle-service`                        `StripPrefix=1`

  `payment-service`   `Path=/api/payment/**`   `lb://payment-service`                        `StripPrefix=1`

  `user-service`      `Path=/api/user/**`      `${user-service.url:http://localhost:8083}`   `StripPrefix=1`
  ------------------------------------------------------------------------------------------------------------

### Load Balancing

For routes using `lb://`, Spring Cloud LoadBalancer obtains available
service instances from Eureka and selects an instance for the request.

For example, if three `parking-service` instances are running, requests
can be distributed between them automatically.

`StripPrefix=1` removes the first path segment before forwarding the
request. Therefore:

``` text
/api/parking/5
```

is forwarded as:

``` text
/parking/5
```

The user-service uses a direct URL because the Node service is not
registered with Eureka.

## Configuration

The main gateway configuration is stored in
`config-repo/api-gateway.yaml`.

  ---------------------------------------------------------------------------------
  Property                                      Description
  --------------------------------------------- -----------------------------------
  `server.port`                                 Gateway port (`8080`)

  `spring.cloud.gateway.routes`                 Defines the gateway routes

  `jwt.secret`                                  Secret used to sign and validate
                                                JWTs

  `jwt.expiration-ms`                           JWT lifetime (`86400000` = 24
                                                hours)

  `user-service.url`                            URL of the Node user-service

  `eureka.client.*`                             Eureka registration and discovery
                                                settings

  `management.endpoints.web.exposure.include`   Actuator monitoring endpoints
  ---------------------------------------------------------------------------------

The local `application.yaml` mainly contains the application name and
Config Server import:

``` yaml
optional:configserver:http://localhost:8888
```

If the Config Server is unavailable, the gateway can still start using
its available defaults.

## Running the Gateway

From the gateway directory:

``` bash
cd infastructure/api-gateway
./mvnw spring-boot:run
```

The normal startup dependency order is:

``` text
Config Server :8888
       ↓
Eureka :8761
       ↓
API Gateway :8080
       ↓
Backend Services
```

Eureka is required for the `lb://` routes to resolve service instances.
The gateway may start without Eureka, but routed calls that depend on
service discovery will fail.

## Authentication Testing

### Login

``` bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret"}'
```

### Request Without JWT

``` bash
curl http://localhost:8080/api/parking
```

Expected result: `401 Unauthorized`.

### Request With JWT

``` bash
curl http://localhost:8080/api/parking \
  -H "Authorization: Bearer <token>"
```

A valid token allows the protected request to continue to the downstream
service.

## Docker

Build the image:

``` bash
docker build -t spms/api-gateway .
```

Run it on port `8080`:

``` bash
docker run -p 8080:8080 spms/api-gateway
```

The Dockerfile uses a multi-stage build with `maven:latest` for
compilation and `eclipse-temurin:latest` for runtime. The `.env` file is
excluded from the Docker build context, so values such as `JWT_SECRET`
should be provided through runtime environment variables.
