package co.edu.eci.blueprints.auth;

import co.edu.eci.blueprints.security.InMemoryUserService;
import co.edu.eci.blueprints.security.RsaKeyProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Emisión de tokens JWT (RS256) para consumir el API")
public class AuthController {

    private final JwtEncoder encoder;
    private final InMemoryUserService userService;
    private final RsaKeyProperties props;

    public AuthController(JwtEncoder encoder, InMemoryUserService userService, RsaKeyProperties props) {
        this.encoder = encoder;
        this.userService = userService;
        this.props = props;
    }

    @Schema(description = "Credenciales del usuario")
    public record LoginRequest(
            @Schema(description = "Nombre de usuario", example = "student") String username,
            @Schema(description = "Contraseña", example = "student123") String password) {}

    @Schema(description = "Token emitido")
    public record TokenResponse(
            @Schema(description = "JWT firmado con RS256; enviarlo como 'Authorization: Bearer <token>'",
                    example = "eyJhbGciOiJSUzI1NiJ9...") String access_token,
            @Schema(description = "Tipo de token", example = "Bearer") String token_type,
            @Schema(description = "Segundos de validez del token", example = "3600") long expires_in) {}

    @Operation(
            summary = "Iniciar sesión y obtener un JWT",
            description = "Valida las credenciales y emite un access token. El scope depende del usuario: "
                    + "'student' recibe 'blueprints.read'; 'assistant' recibe 'blueprints.read blueprints.write'.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas, token emitido",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content(examples = @ExampleObject(value = "{\"error\": \"invalid_credentials\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (!userService.isValid(req.username(), req.password())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }

        Instant now = Instant.now();
        long ttl = props.tokenTtlSeconds() != null ? props.tokenTtlSeconds() : 3600;
        Instant exp = now.plusSeconds(ttl);

        String scope = userService.scopesFor(req.username());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(req.username())
                .claim("scope", scope)
                .build();

        JwsHeader jws = JwsHeader.with(() -> "RS256").build();
        String token = this.encoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();

        return ResponseEntity.ok(new TokenResponse(token, "Bearer", ttl));
    }
}
