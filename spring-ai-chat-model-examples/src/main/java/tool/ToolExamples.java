package tool;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.util.ReflectionUtils;

public class ToolExamples {

    public static void main(String[] args){
        String apiKey = System.getenv("MINIMAX_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "MINIMAX_API_KEY";
        }

        var chatModel = new MiniMaxChatModel(
                new MiniMaxApi(apiKey),
                MiniMaxChatOptions.builder()
                        .model("MiniMax-M2.7")
                        .build());

        var chatClient = ChatClient.create(chatModel);

        System.out.println("=".repeat(60));
        System.out.println("Spring AI Tool Calling 示例");
        System.out.println("=".repeat(60));

        annotationBasedTool(chatClient);
        // programmaticToolCallback(chatClient); todo有问题
        // functionToolCallback(chatClient); todo有问题
        returnDirectMode(chatClient);

        System.out.println("=".repeat(60));
        System.out.println("所有用例完成!");
        System.out.println("=".repeat(60));
    }

    private static void annotationBasedTool(ChatClient chatClient) {
        System.out.println("\n--- 注解式 @Tool (简单、常用、自动 Schema) ---");

        var tools = new DateTimeTools();

        String response = chatClient.prompt()
                .user("现在几点了?")
                .tools(tools)
                .call()
                .content();

        System.out.println("用户: 现在几点了?");
        System.out.println("助手: " + response);
    }

    private static void programmaticToolCallback(ChatClient chatClient) {
        System.out.println("\n--- 编程式 MethodToolCallback (反射、动态注册) ---");

        Method method = ReflectionUtils.findMethod(StaticDateTimeTools.class, "getCurrentDateTime");
        assert method != null;
        ToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinitions.builder(method)
                        .description("Get the current date and time in the user's timezone")
                        .build())
                .toolMethod(method)
                .toolObject(StaticDateTimeTools.class)
                .build();

        String response = chatClient.prompt()
                .user("今天的日期是什么?")
                .toolCallbacks(toolCallback)
                .call()
                .content();

        System.out.println("用户: 今天的日期是什么?");
        System.out.println("助手: " + response);
    }

    private static void functionToolCallback(ChatClient chatClient) throws NoSuchMethodException {
        System.out.println("\n--- 函数式 FunctionToolCallback (纯函数、无状态) ---");

        Function<String, String> reverseFunction = s -> new StringBuilder(s).reverse().toString();

        var toolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .name("reverse_string")
                        .description("将输入的字符串反转")
                        .inputSchema("""
                                {
                                  "type": "object",
                                  "properties": {
                                    "input": {
                                      "type": "string",
                                      "description": "要反转的字符串"
                                    }
                                  },
                                  "required": ["input"]
                                }
                                """)
                        .build())
                .toolMethod(String.class.getMethod("toString"))
                .toolObject(reverseFunction)
                .build();

        String response = chatClient.prompt()
                .user("请把 'hello' 反转一下")
                .toolCallbacks(toolCallback)
                .call()
                .content();

        System.out.println("用户: 请把 'hello' 反转一下");
        System.out.println("助手: " + response);
    }

    private static void returnDirectMode(ChatClient chatClient) {
        System.out.println("\n--- 执行模式: returnDirect (直返用户) ---");

        Method method = null;
        try {
            method = DateTimeTools.class.getMethod("getCurrentDateTime");
        } catch (NoSuchMethodException e) {
            System.out.println("方法未找到: " + e.getMessage());
            return;
        }

        var toolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinitions.builder(method)
                        .description("获取当前时间(直返模式)")
                        .build())
                .toolMethod(method)
                .toolObject(new DateTimeTools())
                .build();

        String response = chatClient.prompt()
                .user("直接返回当前时间")
                .toolCallbacks(toolCallback)
                .call()
                .content();

        System.out.println("用户: 直接返回当前时间");
        System.out.println("助手: " + response);
    }

}