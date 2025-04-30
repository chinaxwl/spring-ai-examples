package com.xwl.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    private final ChatClient chatClient;

    public HelloController(ChatClient.Builder chatClientBuilder) {
        chatClient = chatClientBuilder.build();
    }
    @GetMapping("/mcpChat")
    public String mcpChat(@RequestParam("message") String message) {
        return chatClient
                .prompt()
                .user(message)
                .tools(toolCallbackProvider.getToolCallbacks())
                .call()
                .content();
    }
}
