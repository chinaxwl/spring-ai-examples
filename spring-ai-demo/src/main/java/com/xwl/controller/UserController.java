package com.xwl.controller;

import com.xwl.advisor.MyCallAroundAdvisor;
import com.xwl.entity.ActorFilms;
import com.xwl.entity.Books;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
public class UserController {

    private final ChatClient chatClient;

    public UserController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/test")
    public String test(String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping("/test1")
    public void test1(String userInput) {
        ChatResponse chatResponse = chatClient.prompt()
                .user("Tell me a joke")
                .call()
                .chatResponse();
        System.out.println(chatResponse);
    }

    @Resource
    private ChatModel chatModel;

    @GetMapping("/test2")
    public String test2(String userInput) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        ChatClient chatClient = builder.build();

        return chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping("/test3")
    public String test3(String userInput) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        ChatClient chatClient = builder
                .defaultAdvisors(new MyCallAroundAdvisor(),
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                        )
                .build();

        return chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping("/entity")
    public ActorFilms entity() {
        return this.chatClient
                .prompt()
                .user("给我输出古天乐的三部电视剧")
                .call()
                .entity(ActorFilms.class);
    }
    @GetMapping("/entity1")
    public Books entity1() {
        return this.chatClient
                .prompt()
                .user("给我输出莫言的三本书信息")
                .call()
                .entity(Books.class);
    }

    @GetMapping("/entity2")
    public List<ActorFilms> entity2() {
        List<ActorFilms> actorFilms = chatClient.prompt()
                .user("为古天乐和刘青云生成5部电影作品.")
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
        return actorFilms;
    }

    @GetMapping(value = "/streamChat", produces = "text/html;charset=UTF-8")
    public Flux<String> streamChat() {
        return chatClient
                .prompt()
                .user("你是谁")
                .stream()
                .content();
    }

    @GetMapping(value = "/streamAndEntity")
    public List<ActorFilms> streamChat1() {
        BeanOutputConverter<List<ActorFilms>> converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorFilms>>() {});

        Flux<String> flux = this.chatClient.prompt()
                .user(u -> u.text("""
                        随机输出中国演员对应的三部电影.
                        {format}
                      """)
                        .param("format", converter.getFormat()))
                .stream()
                .content();

        String content = flux.collectList().block().stream().collect(Collectors.joining());

        List<ActorFilms> actorFilms = converter.convert(content);
        return actorFilms;
    }

    @GetMapping("/output")
    public List<ActorFilms> output() {
        BeanOutputConverter<List<ActorFilms>> outputConverter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<ActorFilms>>() {
                });

        String format = outputConverter.getFormat();
        String template = """
        给我周星驰和成龙的三部电影信息
        {format}
        """;

        Prompt prompt = new PromptTemplate(template, Map.of("format", format)).create();

        String content = chatClient.prompt(prompt).call().content();

        return outputConverter.convert(content);
    }

    @GetMapping(value = "/sseChat")
    public SseEmitter sseChat() {
        SseEmitter sseEmitter = new SseEmitter() {
            @Override
            protected void extendResponse(ServerHttpResponse outputMessage) {
                HttpHeaders headers = outputMessage.getHeaders();
                headers.setContentType(new MediaType("text", "event-stream", StandardCharsets.UTF_8));
            }
        };

        Flux<String> stream = chatClient
                .prompt()
                .user("你是谁")
                .stream()
                .content();

        stream.subscribe(token -> {
            try {
                sseEmitter.send(token);
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        }, sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

    @GetMapping("/systemChat")
    public String systemChat() {
        return this.chatClient
                .prompt()
                .system("请你用加勒比海盗主角的口气说话")
                .user("给我将一个笑话")
                .call()
                .content();
    }

    @GetMapping("/chatMemoryChat")
    public String chatMemoryChat(@RequestParam("chatId") String chatId, @RequestParam("message") String message) {
        return chatClient
                .prompt()
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/multimodalChat")
    public String multimodalChat() {
        ClassPathResource imageData = new ClassPathResource("/test.jpg");

        var userMessage = new UserMessage("图片里有什么?",
                List.of(new Media(MimeTypeUtils.IMAGE_PNG, imageData)));

        return chatClient.prompt().messages(userMessage).call().content();
    }
}
