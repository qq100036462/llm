package com.example.functioncall.controller;

import com.example.functioncall.function.CalculatorTools;
import com.example.functioncall.function.RestaurantTools;
import com.example.functioncall.function.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final CalculatorTools calculatorTools;
    private final RestaurantTools restaurantTools;

    public ChatController(ChatClient.Builder chatClientBuilder, 
                          WeatherTools weatherTools,
                          CalculatorTools calculatorTools,
                          RestaurantTools restaurantTools) {
        this.chatClient = chatClientBuilder.build();
        this.weatherTools = weatherTools;
        this.calculatorTools = calculatorTools;
        this.restaurantTools = restaurantTools;
    }

    @GetMapping("/weather")
    public String askWeather(@RequestParam(defaultValue = "北京今天天气怎么样？") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherTools)
                .call()
                .content();
    }

    @GetMapping("/calculate")
    public String calculate(@RequestParam(defaultValue = "帮我计算 123 加 456 等于多少") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(calculatorTools)
                .call()
                .content();
    }

    @GetMapping("/restaurant")
    public String restaurant(@RequestParam(defaultValue = "北京有什么好吃的饭店推荐？") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(restaurantTools)
                .call()
                .content();
    }

    @GetMapping("/multi")
    public String multiFunction(@RequestParam(defaultValue = "北京今天天气怎么样？顺便帮我算一下 100 除以 5 等于多少") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherTools, calculatorTools)
                .call()
                .content();
    }

    @GetMapping("/all")
    public String allTools(@RequestParam(defaultValue = "北京今天天气怎么样？有什么好吃的饭店推荐？顺便帮我算一下 100 除以 5 等于多少") String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherTools, calculatorTools, restaurantTools)
                .call()
                .content();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @PostMapping(value = "/test", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String testChat(@RequestBody String question,
                           @RequestParam(required = false, defaultValue = "false") boolean useWeather,
                           @RequestParam(required = false, defaultValue = "false") boolean useCalculator,
                           @RequestParam(required = false, defaultValue = "false") boolean useRestaurant) {
        var prompt = chatClient.prompt().user(question);
        
        if (useWeather && useCalculator && useRestaurant) {
            return prompt.tools(weatherTools, calculatorTools, restaurantTools).call().content();
        } else if (useWeather && useCalculator) {
            return prompt.tools(weatherTools, calculatorTools).call().content();
        } else if (useWeather && useRestaurant) {
            return prompt.tools(weatherTools, restaurantTools).call().content();
        } else if (useCalculator && useRestaurant) {
            return prompt.tools(calculatorTools, restaurantTools).call().content();
        } else if (useWeather) {
            return prompt.tools(weatherTools).call().content();
        } else if (useCalculator) {
            return prompt.tools(calculatorTools).call().content();
        } else if (useRestaurant) {
            return prompt.tools(restaurantTools).call().content();
        } else {
            return prompt.call().content();
        }
    }
}
