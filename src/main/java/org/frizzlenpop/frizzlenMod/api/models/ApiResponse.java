package org.frizzlenpop.frizzlenMod.api.models;

/**
 * Generic response for API endpoints
 * @param <T> The type of data in the response
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public ApiResponse() {
    }
    
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    /**
     * Creates a successful response with data
     * @param data The data to include in the response
     * @param <T> The type of data
     * @return A new ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    
    /**
     * Creates a successful response with a message
     * @param message The success message
     * @return A new ApiResponse instance
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }
    
    /**
     * Creates a successful response with a message and data
     * @param message The success message
     * @param data The data to include in the response
     * @param <T> The type of data
     * @return A new ApiResponse instance
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * Creates an error response with a message
     * @param message The error message
     * @return A new ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
} 