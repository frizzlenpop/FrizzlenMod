package org.frizzlenpop.frizzlenMod.api.middleware;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.frizzlenpop.frizzlenMod.api.models.ApiResponse;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Middleware for JWT authentication
 */
public class AuthMiddleware implements Filter {
    private final String jwtSecret;
    private final Gson gson;
    
    /**
     * Creates a new AuthMiddleware
     * @param jwtSecret The JWT secret key
     */
    public AuthMiddleware(String jwtSecret) {
        this.jwtSecret = jwtSecret;
        this.gson = new Gson();
    }
    
    /**
     * Handles the authentication
     * @param request The request
     * @param response The response
     */
    @Override
    public void handle(Request request, Response response) {
        // Get the Authorization header
        String authHeader = request.headers("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            halt(response, 401, "Unauthorized: No token provided");
            return;
        }
        
        // Extract the token
        String token = authHeader.substring(7);
        try {
            // Validate the token
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
            
            // Check if the token is expired
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                halt(response, 401, "Unauthorized: Token expired");
                return;
            }
            
            // Store user info in request attributes for later use
            request.attribute("username", claims.getSubject());
            request.attribute("role", claims.get("role", String.class));
            
        } catch (Exception e) {
            halt(response, 401, "Unauthorized: Invalid token");
        }
    }
    
    /**
     * Halts the request with an error response
     * @param response The response
     * @param status The HTTP status code
     * @param message The error message
     */
    private void halt(Response response, int status, String message) {
        response.status(status);
        response.type("application/json");
        response.body(gson.toJson(ApiResponse.error(message)));
        Spark.halt();
    }
} 