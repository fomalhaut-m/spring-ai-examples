package com.example.chatmemory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;

public class InMemoryChatMemoryRepositoryExample {

    public static void main(String[] args) {
        String apiKey = System.getenv("MINIMAX_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "sk-cp-0cx8-H-KKo14uqdNurVEZFw_U2KRjadkIGl3c41wfSVge75_ZE-v9GJHhtRyxZD96_l2461T8bK8KjGSRWwUj21Uhs_M1waHOZCuTViL3Vlvn10jh4iFPL0";
        }

        var chatModel = createChatModel(apiKey);
        var chatMemory = createChatMemory();

        runExamples(chatModel, chatMemory);
    }

    private static ChatModel createChatModel(String apiKey) {
        return new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .build());
    }

    private static ChatMemory createChatMemory() {
        return new ChatMemoryWrapper(MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build());
    }

    private static void runExamples(ChatModel chatModel, ChatMemory chatMemory) {
        System.out.println("=".repeat(60));
        System.out.println("Chat Memory 用例");
        System.out.println("=".repeat(60));

        singleConversation(chatModel, chatMemory);
        multiTurnConversation(chatModel, chatMemory);
        conversationSummary(chatModel, chatMemory);
        multiConversationWithId(chatModel, chatMemory);

        System.out.println("=".repeat(60));
        System.out.println("所有用例完成!");
        System.out.println("=".repeat(60));
    }

    private static void singleConversation(ChatModel chatModel, ChatMemory chatMemory) {
        System.out.println("\n--- 单轮对话 ---");
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个有帮助的助手。")
                .build();

        String response = chatClient.prompt("你好,我叫小明").call().content();
        System.out.println("用户: 你好,我叫小明");
        System.out.println("助手: " + response);
    }

    private static void multiTurnConversation(ChatModel chatModel, ChatMemory chatMemory) {
        System.out.println("\n--- 多轮对话 (带记忆) ---");

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个有帮助的助手。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        String response1 = chatClient.prompt("你好,我叫小明").call().content();
        System.out.println("用户: 你好,我叫小明");
        System.out.println("助手: " + response1);

        String response2 = chatClient.prompt("我的名字是什么?").call().content();
        System.out.println("用户: 我的名字是什么?");
        System.out.println("助手: " + response2);
    }

    private static void conversationSummary(ChatModel chatModel, ChatMemory chatMemory) {
        System.out.println("\n--- 对话摘要 ---");
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个有帮助的助手。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        chatClient.prompt("法国的首都是巴黎").call().content();
        chatClient.prompt("日本的首都是东京").call().content();
        chatClient.prompt("英国的首都是伦敦").call().content();

        String response = chatClient.prompt("请列出我们刚才讨论过的国家及其首都").call().content();
        System.out.println("用户: 请列出我们刚才讨论过的国家及其首都");
        System.out.println("助手: " + response);
    }

    private static void multiConversationWithId(ChatModel chatModel, ChatMemory chatMemory) {
        System.out.println("\n--- 多会话独立记忆 ---");

        ChatClient chatClient1 = ChatClient.builder(chatModel)
                .defaultSystem("你是一个有帮助的助手。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId("session-1")
                        .build())
                .build();

        chatClient1.prompt("我叫张三").call().content();
        System.out.println("会话1 - 用户: 我叫张三");

        chatClient1.prompt("我的名字是什么?").call().content();
        System.out.println("会话1 - 用户: 我的名字是什么?");

        ChatClient chatClient2 = ChatClient.builder(chatModel)
                .defaultSystem("你是一个有帮助的助手。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId("session-2")
                        .build())
                .build();

        chatClient2.prompt("我叫李四").call().content();
        System.out.println("会话2 - 用户: 我叫李四");

        chatClient2.prompt("我的名字是什么?").call().content();
        System.out.println("会话2 - 用户: 我的名字是什么?");

        ChatClient chatClient3 = ChatClient.builder(chatModel)
                .defaultSystem("你是一个有帮助的助手。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId("session-1")
                        .build())
                .build();

        String response = chatClient3.prompt("请再次告诉我我的名字").call().content();
        System.out.println("会话1 - 用户: 请再次告诉我我的名字");
        System.out.println("会话1 - 助手: " + response);
    }

    private record ChatMemoryWrapper(MessageWindowChatMemory chatMemory) implements ChatMemory {

        @Override
            public void add(String conversationId, Message message) {
                System.out.println("\tchatMemory -" + conversationId + " 添加消息: " + message.getText());
                chatMemory.add(conversationId, message);
            }

            @Override
            public void add(String conversationId, List<Message> messages) {
                System.out.println("\tchatMemory -" + conversationId + " 添加消息: " + messages.stream().map(Message::getText).collect(Collectors.joining(", \n")));
                chatMemory.add(conversationId, messages);
            }

            @Override
            public List<Message> get(String conversationId) {
                List<Message> list = chatMemory.get(conversationId);
                System.out.println("\tchatMemory -" + conversationId + " 获取消息: " + list.stream().map(Message::getText).collect(Collectors.joining(", \n")));
                return list;
            }

            @Override
            public void clear(String conversationId) {
                System.out.println("\tchatMemory -" + conversationId + " 清空消息");
                chatMemory.clear(conversationId);
            }
        }
}