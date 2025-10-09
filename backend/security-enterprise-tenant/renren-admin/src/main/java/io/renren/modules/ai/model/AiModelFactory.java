package io.renren.modules.ai.model;

import cn.hutool.core.util.StrUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

/**
 * AI模型工厂
 *
 * @author Mark sunlightcs@gmail.com
 */
public class AiModelFactory {

    public static ChatModel buildChatModel(String platform, String model, String apiUrl, String apiKey) {
        if (StrUtil.equalsIgnoreCase(platform, "OpenAI")) {
            return buildOpenAiChatModel(model, apiUrl, apiKey);
        } else if (StrUtil.equalsIgnoreCase(platform, "DeepSeek")) {
            return buildOpenAiChatModel(model, apiUrl, apiKey);
        } else if (StrUtil.equalsIgnoreCase(platform, "Ollama")) {
            return buildOllamaChatModel(apiUrl, model);
        } else {
            throw new IllegalArgumentException(StrUtil.format("Unknown AI platform {}", platform));
        }
    }

    private static OpenAiChatModel buildOpenAiChatModel(String model, String url, String apiKey) {
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(url).apiKey(apiKey).build();

        return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(
                        OpenAiChatOptions.builder().model(model).build())
                .build();
    }


    private static OllamaChatModel buildOllamaChatModel(String url, String model) {
        OllamaApi ollamaApi = OllamaApi.builder().baseUrl(url).build();
        
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder().model(model).build())
                .build();
    }
}
