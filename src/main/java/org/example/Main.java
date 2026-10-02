package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import org.springframework.boot.SpringApplication;

public class Main {

    public static void main(String[] args) {

        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey("sk-i0qTBK1p9r7lCfYgUkoGxw")
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model("claude-sonnet-4-6")
                .maxTokens(100)
                .addUserMessage("Say hello")
                .build();

        Message response = client.messages().create(params);

        System.out.println(response);
    }
}

