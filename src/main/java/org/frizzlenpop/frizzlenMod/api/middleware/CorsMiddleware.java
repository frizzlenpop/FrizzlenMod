package org.frizzlenpop.frizzlenMod.api.middleware;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Middleware for handling CORS (Cross-Origin Resource Sharing) headers
 */
public class CorsMiddleware implements Filter {

    /**
     * Adds CORS headers to the response
     */
    @Override
    public void handle(Request request, Response response) {
        response.header("Access-Control-Allow-Origin", "*");
        response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Content-Length, Accept, Origin");
        response.header("Access-Control-Allow-Credentials", "true");
        
        // Handle preflight requests
        if (request.requestMethod().equals("OPTIONS")) {
            response.status(200);
            response.body("");
        }
    }
} 