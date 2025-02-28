<?php
// Simple static file server
$request = $_SERVER['REQUEST_URI'];

// Default to index.html for root path
if ($request == '/' || $request == '') {
    $file = 'index.html';
} else {
    // Remove leading slash
    $file = ltrim($request, '/');
}

// Determine content type
$extension = pathinfo($file, PATHINFO_EXTENSION);
switch ($extension) {
    case 'css':
        $contentType = 'text/css';
        break;
    case 'js':
        $contentType = 'application/javascript';
        break;
    case 'json':
        $contentType = 'application/json';
        break;
    case 'png':
        $contentType = 'image/png';
        break;
    case 'jpg':
    case 'jpeg':
        $contentType = 'image/jpeg';
        break;
    case 'svg':
        $contentType = 'image/svg+xml';
        break;
    default:
        $contentType = 'text/html';
}

// Check if file exists
if (file_exists($file)) {
    header("Content-Type: $contentType");
    readfile($file);
} else {
    // If file doesn't exist, serve index.html for SPA routing
    if (file_exists('index.html')) {
        header("Content-Type: text/html");
        readfile('index.html');
    } else {
        // Return 404 if index.html doesn't exist either
        http_response_code(404);
        echo "404 Not Found";
    }
}
?> 