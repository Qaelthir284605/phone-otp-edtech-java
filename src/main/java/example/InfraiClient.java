package example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String key;

    public InfraiClient(String baseUrl, String key) {
        this.baseUrl = baseUrl;
        this.key = key;
    }

    public String post(String path, String json) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(15)).header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() == 429) {
                long delay = 250L * (1L << attempt);
                String retryAfter = response.headers().firstValue("Retry-After").orElse("");
                try { delay = Long.parseLong(retryAfter) * 1000L; } catch (NumberFormatException ignored) { }
                Thread.sleep(delay);
                continue;
            }
            boolean ok = body.contains("\"ok\":true");
            if (!ok) throw new IOException("Infrai request rejected: " + body);
            return body;
        }
        throw new IOException("Infrai request could not be completed");
    }
}
