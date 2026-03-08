package com.example.functioncall.function;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class RestaurantTools {

    @Tool(description = "获取饭店推荐")
    public String getRestaurant(@ToolParam(description = "城市名称") String city) {
        return "推荐在" + city + "的饭店有：" + "方中山胡辣汤、杭州小笼包";
    }
}
