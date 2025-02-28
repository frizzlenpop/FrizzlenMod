package org.frizzlenpop.frizzlenMod.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.frizzlenpop.frizzlenMod.FrizzlenMod;
import org.frizzlenpop.frizzlenMod.api.controllers.AppealsController;
import org.frizzlenpop.frizzlenMod.api.controllers.AuthController;
import org.frizzlenpop.frizzlenMod.api.controllers.ModLogsController;
import org.frizzlenpop.frizzlenMod.api.controllers.PunishmentsController;
import org.frizzlenpop.frizzlenMod.api.middleware.AuthMiddleware;
import org.frizzlenpop.frizzlenMod.api.middleware.CorsMiddleware;
import spark.Spark;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;

import static spark.Spark.*;

public class WebApiManager {
    private final FrizzlenMod plugin;
    private final Gson gson;
    private final int port;
    private final String jwtSecret;
    
    private AppealsController appealsController;
    private AuthController authController;
    private ModLogsController modLogsController;
    private PunishmentsController punishmentsController;
    
    public WebApiManager(FrizzlenMod plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Load config
        this.port = plugin.getConfig().getInt("web-api.port", 8080);
        this.jwtSecret = plugin.getConfig().getString("web-api.jwt-secret", "change-me-in-config");
        
        // Check if JWT secret is default
        if (this.jwtSecret.equals("change-me-in-config")) {
            plugin.getLogger().warning("Using default JWT secret! Please change this in the config.yml for security.");
        }
        
        // Initialize controllers
        initializeControllers();
    }
    
    private void initializeControllers() {
        this.appealsController = new AppealsController(plugin, gson);
        this.authController = new AuthController(plugin, gson, jwtSecret);
        this.modLogsController = new ModLogsController(plugin, gson);
        this.punishmentsController = new PunishmentsController(plugin, gson);
    }
    
    /**
     * Starts the web server and sets up all routes
     */
    public void start() {
        try {
            // Configure Spark
            port(this.port);
            
            // Serve static files from web directory
            String webPath = plugin.getDataFolder() + File.separator + "web";
            staticFiles.externalLocation(webPath);
            
            // Initialize static web files if they don't exist
            initializeWebDirectory(webPath);
            
            // Set up CORS
            before(new CorsMiddleware());
            
            // Public routes
            path("/api", () -> {
                // Auth routes
                post("/auth/login", authController::login);
                
                // Appeal submission (public)
                post("/appeals/submit", appealsController::submitAppeal);
                get("/appeals/status/:uuid", appealsController::getAppealStatus);
                
                // Protected routes
                path("/admin", () -> {
                    before("/*", new AuthMiddleware(jwtSecret));
                    
                    // Punishment management
                    get("/punishments", punishmentsController::getAllPunishments);
                    get("/punishments/player/:player", punishmentsController::getPlayerPunishments);
                    post("/punishments/ban", punishmentsController::addBan);
                    post("/punishments/unban/:player", punishmentsController::removeBan);
                    
                    // Add warn endpoint
                    post("/punishments/warn/:player", punishmentsController::warnPlayer);
                    
                    // Add history endpoint
                    get("/punishments/history/:player", punishmentsController::getPlayerHistory);
                    
                    // Appeals management
                    get("/appeals", appealsController::getAllAppeals);
                    get("/appeals/:id", appealsController::getAppeal);
                    post("/appeals/:id/approve", appealsController::approveAppeal);
                    post("/appeals/:id/deny", appealsController::denyAppeal);
                    post("/appeals/:id/comment", appealsController::addComment);
                    
                    // Moderation logs
                    get("/logs", modLogsController::getAllLogs);
                    get("/logs/:player", modLogsController::getPlayerLogs);
                });
            });
            
            // Error handling
            exception(Exception.class, (e, req, res) -> {
                plugin.getLogger().log(Level.SEVERE, "API Error: " + e.getMessage(), e);
                res.status(500);
                res.type("application/json");
                res.body(gson.toJson(new ErrorResponse("Internal server error")));
            });
            
            // Log startup
            plugin.getLogger().info("Web API started on port " + this.port);
            plugin.getLogger().info("Web Panel URL: http://localhost:" + this.port);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start web API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Stops the web server
     */
    public void stop() {
        Spark.stop();
        plugin.getLogger().info("Web API stopped");
    }
    
    /**
     * Initialize the web directory with default files if they don't exist
     */
    private void initializeWebDirectory(String webPath) {
        try {
            File webDir = new File(webPath);
            if (!webDir.exists()) {
                webDir.mkdirs();
                
                // Copy web resources from plugin jar
                copyWebResources(webDir);
                
                plugin.getLogger().info("Created web panel directory at " + webPath);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize web directory: " + e.getMessage(), e);
        }
    }
    
    /**
     * Copy web resources from the plugin jar to the web directory
     */
    private void copyWebResources(File webDir) {
        // This will be implemented to extract web files from plugin jar
        // For now, we'll just create placeholder files
        try {
            File indexFile = new File(webDir, "index.html");
            if (!indexFile.exists()) {
                plugin.saveResource("web/index.html", false);
            }
            
            // Create admin panel directory
            File adminDir = new File(webDir, "admin");
            if (!adminDir.exists()) {
                adminDir.mkdirs();
                plugin.saveResource("web/admin/index.html", false);
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not copy web resources: " + e.getMessage(), e);
        }
    }
    
    /**
     * Error response class for API errors
     */
    private static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
} 