package org.example.prac2;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShoppingAgent {

    private static final String SYSTEM_PROMPT = """
            You are an Agentic AI Shopping Assistant.

            You have exactly two capabilities:

            1. SEARCH PRODUCTS
            2. COMPARE PRODUCTS

            IMPORTANT RULES:

            - Always use the live web data provided by the application.
            - Never invent product information.
            - Never invent prices.
            - Never invent specifications.
            - Never invent availability.
            - Never invent reviews.
            - If information is unavailable, write "Not available".
            - Do not treat category pages or buying guides as individual products.
            - Prefer pages containing an exact product/model.
            - Use the source URL provided with the product.
            - Prices must be treated as current only if they are visible in the
              retrieved page content.
            - Mention when a price or availability could not be verified.

            For SEARCH requests, return individual products with:

            1. Product name/model
            2. Current price
            3. Retailer
            4. Processor
            5. RAM
            6. Storage
            7. Display
            8. Graphics/GPU
            9. Availability
            10. Source URL

            For COMPARE requests, create a clear comparison containing:

            - Product
            - Current price
            - Processor
            - RAM
            - Storage
            - Display
            - GPU
            - Features
            - Reviews, if available
            - Availability
            - Source URL

            Only compare products for which information was actually retrieved.

            Do not recommend a winner. Present factual differences and trade-offs.

            If the user asks something unrelated to shopping, say that you can
            only search for or compare products.
            """;

    private final AnthropicClient client;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final List<MessageParam> history = new ArrayList<>();

    public ShoppingAgent() {

        String apiKey = System.getenv("ANTHROPIC_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException(
                    "ANTHROPIC_API_KEY is missing. Configure it in IntelliJ Run Configuration."
            );
        }

        this.client = AnthropicOkHttpClient.builder().baseUrl(System.getenv("BASE_URL"))
                .apiKey(apiKey)
                .build();
    }

    public String handleUserRequest(String userInput) {

        System.out.println("Agent: Searching the live web...");

        String searchResults = searchWeb(userInput);

        if (searchResults.isBlank()) {
            return "I couldn't find accessible web results for that request.";
        }

        String prompt = """
                USER REQUEST:
                %s

                LIVE WEB DATA RETRIEVED AT RUNTIME:
                =====================================
                %s
                =====================================

                Analyze the retrieved pages.

                For a product search:
                - Identify individual products.
                - Ignore category-only pages.
                - Extract current displayed price when available.
                - Extract specifications.
                - Extract retailer.
                - Extract availability.
                - Include the source URL.

                For a comparison:
                - Identify the requested products.
                - Compare only information found in the retrieved data.
                - Do not guess missing information.

                If a page cannot provide individual product information,
                do not present that page as a product.
                """.formatted(userInput, searchResults);

        history.add(
                MessageParam.builder()
                        .role(MessageParam.Role.USER)
                        .content(prompt)
                        .build()
        );

        MessageCreateParams params = MessageCreateParams.builder()
                .model("claude-sonnet")
                .maxTokens(4096)
                .system(SYSTEM_PROMPT)
                .messages(history)
                .build();

        Message response = client.messages().create(params);

        history.add(response.toParam());

        return extractText(response);
    }

    /**
     * Performs live DuckDuckGo search and fetches accessible result pages.
     */
    private String searchWeb(String query) {

        try {

            String searchQuery =
                    query +
                            " India exact model current price specifications retailer";

            String encodedQuery = URLEncoder.encode(
                    searchQuery,
                    StandardCharsets.UTF_8
            );

            String searchUrl =
                    "https://html.duckduckgo.com/html/?q="
                            + encodedQuery;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                    )
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                return "Search failed. HTTP status: "
                        + response.statusCode();
            }

            return processSearchResults(response.body());

        } catch (Exception e) {

            return "Live search failed: " + e.getMessage();
        }
    }

    /**
     * Extract search results and fetch accessible pages.
     */
    private String processSearchResults(String html) {

        StringBuilder finalResults = new StringBuilder();

        Pattern pattern = Pattern.compile(
                "<a[^>]*class=\"result__a\"[^>]*href=\"([^\"]+)\"[^>]*>(.*?)</a>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(html);

        int processed = 0;

        while (matcher.find() && processed < 5) {

            String rawUrl = matcher.group(1);

            String title = cleanText(matcher.group(2));

            String actualUrl = decodeDuckDuckGoUrl(rawUrl);

            if (actualUrl == null || actualUrl.isBlank()) {
                continue;
            }

            // Skip obvious non-product/category results where possible.
            if (isLikelyCategoryPage(actualUrl, title)) {
                continue;
            }

            System.out.println(
                    "Agent: Fetching " + title
            );

            String pageText = fetchPage(actualUrl);

            if (pageText.isBlank()) {
                continue;
            }

            // Keep the amount of content sent to Claude manageable.
            pageText = limitText(pageText, 12000);

            finalResults
                    .append("\n====================================\n")
                    .append("SEARCH RESULT ")
                    .append(processed + 1)
                    .append("\n")
                    .append("TITLE: ")
                    .append(title)
                    .append("\n")
                    .append("URL: ")
                    .append(actualUrl)
                    .append("\n")
                    .append("PAGE CONTENT:\n")
                    .append(pageText)
                    .append("\n");

            processed++;
        }

        if (finalResults.length() == 0) {
            return "No accessible individual product pages were found.";
        }

        return finalResults.toString();
    }

    /**
     * Fetch an individual web page.
     */
    private String fetchPage(String url) {

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                    )
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                return "";
            }

            return cleanHtml(response.body());

        } catch (Exception e) {

            // Some retailers may block automated requests.
            // Do not try to bypass those protections.
            return "";
        }
    }

    /**
     * Convert HTML into readable text.
     */
    private String cleanHtml(String html) {

        return html
                .replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("(?is)<noscript.*?</noscript>", " ")
                .replaceAll("(?is)<svg.*?</svg>", " ")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Clean search-result title.
     */
    private String cleanText(String text) {

        return text
                .replaceAll("<[^>]+>", "")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * DuckDuckGo sometimes returns:
     * https://duckduckgo.com/l/?uddg=<encoded-real-url>
     */
    private String decodeDuckDuckGoUrl(String url) {

        try {

            if (url.contains("uddg=")) {

                int start = url.indexOf("uddg=") + 5;

                String encoded = url.substring(start);

                int amp = encoded.indexOf('&');

                if (amp >= 0) {
                    encoded = encoded.substring(0, amp);
                }

                return URLDecoder.decode(
                        encoded,
                        StandardCharsets.UTF_8
                );
            }

            return url;

        } catch (Exception e) {

            return url;
        }
    }

    /**
     * Avoid obvious category/listing pages.
     */
    private boolean isLikelyCategoryPage(
            String url,
            String title) {

        String value =
                (url + " " + title).toLowerCase();

        return value.contains("category")
                || value.contains("category-list")
                || value.contains("laptops-under")
                || value.contains("best-laptops")
                || value.contains("top-laptops")
                || value.contains("price-list")
                || value.contains("buying-guide")
                || value.contains("list-of-laptops");
    }

    /**
     * Limit text sent to Claude.
     */
    private String limitText(
            String text,
            int maxCharacters) {

        if (text.length() <= maxCharacters) {
            return text;
        }

        return text.substring(0, maxCharacters)
                + "\n[Page content truncated]";
    }

    private static String extractText(Message message) {

        StringBuilder sb = new StringBuilder();

        for (ContentBlock block : message.content()) {

            if (block.isText()) {

                if (sb.length() > 0) {
                    sb.append('\n');
                }

                sb.append(block.asText().text());
            }
        }

        if (sb.length() == 0) {
            return "(No response text was returned.)";
        }

        return sb.toString();
    }
}
