# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.0.4 application demonstrating OAuth2 integration with Keycloak. The project uses a multi-module Maven structure with Java 17.

**Key Technologies:**
- Spring Boot 3.0.4 with Spring Security
- OAuth2 Client (Authorization Code flow)
- Keycloak as the OAuth2 provider
- Maven multi-module project structure

## Project Structure

```
spring-boot-oauth2/
├── pom.xml (parent POM)
├── docker-compose.yml (Docker orchestration)
├── backend-service/ (REST API module)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/ch/nacht/
│       │   ├── Main.java (Spring Boot application entry point)
│       │   ├── config/SecurityConfiguration.java
│       │   └── controller/IndexController.java
│       └── resources/application.properties
└── keycloak/realms/
    └── external-realm.json (Keycloak realm configuration)
```

## Docker Commands (Recommended)

**Start all services (Keycloak + backend):**
```bash
docker-compose up -d
```

**View logs:**
```bash
docker-compose logs -f
docker-compose logs -f backend-service  # backend only
docker-compose logs -f keycloak         # keycloak only
```

**Stop all services:**
```bash
docker-compose down
```

**Rebuild and restart:**
```bash
docker-compose up -d --build
```

**Access Keycloak Admin Console:**
- URL: http://localhost:8080
- Username: `admin`
- Password: `admin`

**Test user credentials:**
- Username: `testuser`
- Password: `password123`
- Email: testuser@example.com

## Maven Commands (Local Development)

**Build the project:**
```bash
mvn clean install
```

**Run the application locally (requires Keycloak running):**
```bash
mvn spring-boot:run -pl backend-service
```

**Run tests:**
```bash
mvn test
```

**Package the application:**
```bash
mvn package
```

## Configuration

The OAuth2 configuration is in `backend-service/src/main/resources/application.properties`:
- Server runs on port 8081
- Keycloak realm: `external`
- Client ID: `external-client`
- Client secret: `mysecret123` (configured via environment variable)

**Environment Variables:**
- `KEYCLOAK_ISSUER_URI`: Override Keycloak issuer URL (default: http://localhost:8080/realms/external)
- `KEYCLOAK_CLIENT_SECRET`: OAuth2 client secret (default: mysecret123)

**Required scopes:** openid, offline_access, profile

**Keycloak Realm Configuration:**
The realm is auto-imported from `keycloak/realms/external-realm.json` on startup, which includes:
- Client configuration with secret `mysecret123`
- Test user: testuser/password123
- Configured redirect URIs for the backend service

## REST API Endpoints

**Public Endpoints:**
- `GET /unauthenticated` - Test endpoint accessible without authentication
- `POST /api/logout` - Logout endpoint that invalidates session and returns Keycloak logout URL

**Protected Endpoints (require authentication):**
- `GET /` - Returns authenticated user information (name, email)

**Logout Endpoint Response:**
```json
{
  "message": "Logged out successfully",
  "keycloakLogoutUrl": "http://localhost:8080/realms/external/protocol/openid-connect/logout?redirect_uri=http://localhost:8081/unauthenticated"
}
```

**Usage Example:**
```bash
# Logout (POST request)
curl -X POST http://localhost:8081/api/logout

# Client should redirect to the keycloakLogoutUrl for complete SSO logout
```

## Architecture Notes

**OAuth2 Flow:**
- Uses Authorization Code grant type
- Session management policy: ALWAYS (maintains sessions)
- Public endpoints: `/unauthenticated`, `/oauth2/**`, `/login/**`, `/api/logout`
- All other endpoints require full authentication
- Logout redirects to Keycloak's logout endpoint

**Security Configuration (SecurityConfiguration.java):**
- Configured via SecurityFilterChain bean
- OAuth2 client and login enabled
- Custom logout URL that redirects to Keycloak logout then back to the application
- All authenticated requests use fullyAuthenticated() requirement

**Controller Pattern:**
- IndexController demonstrates accessing OAuth2User principal
- User attributes (name, email) are retrieved from SecurityContextHolder
- Returns data as HashMap (simple JSON responses)
- Logout endpoint uses SecurityContextLogoutHandler to properly invalidate sessions

## Docker Architecture

**Network Configuration:**
- Services communicate via `spring-keycloak-network` bridge network
- Backend service uses `host.docker.internal` to connect to Keycloak (ensures both container-to-container and browser-to-Keycloak work)
- Keycloak health check ensures it's ready before backend starts

**Multi-stage Docker Build:**
- Stage 1: Maven build using `maven:3.9-eclipse-temurin-17`
- Stage 2: Runtime using `eclipse-temurin:17-jre-alpine` (minimal image)

## Development Notes

- Source files now follow standard Maven structure: `src/main/java/ch/nacht/`
- No test directory structure currently exists
- The application uses legacy Spring Security configuration methods (`.and()` chains) which work with Spring Boot 3.0.4
- Application properties support environment variable overrides for Docker deployment
