package com.xwl.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * xuewl
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
//                .defaultSystem("请用我提供的主题，写一首七言绝句")
//                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    @Resource
    private ChatModel chatModel;


    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @GetMapping("/chat")
    public String chat() {
        return this.chatClient.prompt()
                .user("你是谁")
                .call()
                .content();
    }

    @GetMapping("/chat1")
    public String chat1() {
        // 不使用公共的ChatClient，自己之间利用ChatModel构建一个
        ChatClient client = ChatClient.builder(chatModel).build();
        return client
                .prompt()
                .user("你是谁")
                .call()
                .content();
    }

    @GetMapping("/sse")
    public SseEmitter handleSse() throws IOException {
        SseEmitter emitter = new SseEmitter();
        // 设置超时时间（避免客户端长时间无响应）
        // 发送消息时检查会话状态
        emitter.send(SseEmitter.event().data("message"));
        return emitter;
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
