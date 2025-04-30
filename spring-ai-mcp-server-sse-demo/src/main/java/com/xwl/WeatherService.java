package com.xwl;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 大都督周瑜
 * 微信ID: dadudu6789
 * 专注帮助程序员提升技术实力，升职涨薪，面试跳槽
 */
@Service
public class WeatherService {

    @Tool(description = "获取指定城市的天气")
    public String getWeather(String cityName) {
        System.out.println("xxxx");
        if (cityName.equals("上海")) {
            return "天晴";
        } else if (cityName.equals("北京")) {
            return "下雨";
        }
        return "不知道";
    }
}
