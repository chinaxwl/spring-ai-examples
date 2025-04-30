package com.xwl;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(description = "获取指定城市的天气")
    public String getWeather(String cityName) {
        if (cityName.equals("上海")) {
            return "天晴";
        } else if (cityName.equals("北京")) {
            return "下雨";
        }
        return "不知道啊！！！";
    }
}
