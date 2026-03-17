package com.squad.squad.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/util")
public class UtilController {

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "maps.app.goo.gl",
            "goo.gl"
    );

    @GetMapping("/resolve-url")
    public ResponseEntity<Map<String, String>> resolveUrl(@RequestParam String url) {
        URI inputUri;
        try {
            inputUri = URI.create(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid URL"));
        }

        String host = inputUri.getHost();
        if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL not allowed"));
        }

        if (!"https".equalsIgnoreCase(inputUri.getScheme())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only HTTPS allowed"));
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest req = HttpRequest.newBuilder(inputUri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
            return ResponseEntity.ok(Map.of("url", resp.uri().toString()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("url", url));
        }
    }
}
