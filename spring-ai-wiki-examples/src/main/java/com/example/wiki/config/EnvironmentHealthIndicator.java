package com.example.wiki.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * Environment health check on startup
 */
@Component
public class EnvironmentHealthIndicator implements ApplicationRunner {

  private final ChatModel chatModel;
  private final EmbeddingModel embeddingModel;
  private final MongoTemplate mongoTemplate;
  private final VectorStore vectorStore;
  private final Environment env;

  public EnvironmentHealthIndicator(
      ChatModel chatModel,
      EmbeddingModel embeddingModel,
      MongoTemplate mongoTemplate,
      VectorStore vectorStore,
      Environment env) {
    this.chatModel = chatModel;
    this.embeddingModel = embeddingModel;
    this.mongoTemplate = mongoTemplate;
    this.vectorStore = vectorStore;
    this.env = env;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    printEnvironmentVariables();

    System.out.println();
    System.out.println("Environment Health Check:");

    boolean allOk = true;

    allOk &= checkMiniMaxChatApi();
    allOk &= checkMiniMaxEmbeddingApi();
    allOk &= checkMongoDB();
    allOk &= checkChromaVectorStore();

    System.out.println();
    if (allOk) {
      System.out.println("Health Check: All OK");
    } else {
      throw new RuntimeException("Health Check failed");
    }
  }

  private boolean checkMiniMaxChatApi() {
    System.out.print("  MiniMax Chat API ............................. ");
    try {
      String response = chatModel.call("Hi");
      if (response != null && !response.isEmpty()) {
        System.out.println("OK");
        return true;
      }
      System.out.println("FAILED (empty response)");
      return false;
    } catch (Exception e) {
      System.out.println("FAILED (" + getErrorHint(e) + ")");
      return false;
    }
  }

  private boolean checkMiniMaxEmbeddingApi() {
    System.out.print("  MiniMax Embedding API ......................... ");
    try {
      EmbeddingResponse response = embeddingModel.embedForResponse(java.util.List.of("test"));
      if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
        int dim = response.getResults().get(0).getOutput().length;
        System.out.println("OK (dim=" + dim + ")");
        return true;
      }
      System.out.println("FAILED (empty response)");
      return false;
    } catch (Exception e) {
      System.out.println("FAILED (" + getErrorHint(e) + ")");
      return false;
    }
  }

  private boolean checkMongoDB() {
    System.out.print("  MongoDB ...................................... ");
    try {
      String dbName = mongoTemplate.getDb().getName();
      String collName = "spring_ai_wiki_health";
      String testId = "health-check-" + System.currentTimeMillis();
      String version = "1.0.0";

      // Insert
      org.bson.Document doc = new org.bson.Document()
          .append("_id", testId)
          .append("version", version)
          .append("timestamp", System.currentTimeMillis());
      mongoTemplate.getDb().getCollection(collName).insertOne(doc);

      // Query
      org.bson.Document found = mongoTemplate.getDb().getCollection(collName)
          .find(new org.bson.Document("_id", testId)).first();
      if (found == null || !version.equals(found.getString("version"))) {
        System.out.println("FAILED (query mismatch)");
        return false;
      }

      // Delete
      mongoTemplate.getDb().getCollection(collName).deleteOne(new org.bson.Document("_id", testId));

      System.out.println("OK (CRUD)");
      return true;
    } catch (Exception e) {
      System.out.println("FAILED (" + getErrorHint(e) + ")");
      return false;
    }
  }

  private boolean checkChromaVectorStore() {
    System.out.print("  Chroma VectorStore ............................ ");
    try {
      vectorStore.add(java.util.List.of(new Document("health-check-test")));
      vectorStore.similaritySearch("health-check-test");
      vectorStore.delete(java.util.List.of("health-check-test"));
      System.out.println("OK");
      return true;
    } catch (Exception e) {
      System.out.println("FAILED (" + getErrorHint(e) + ")");
      return false;
    }
  }

  private String getErrorHint(Exception e) {
    String msg = e.getMessage();
    if (msg == null) return "unknown";
    if (msg.contains("401")) return "invalid API key";
    if (msg.contains("403")) return "access denied";
    if (msg.contains("Connection refused")) return "connection refused";
    if (msg.contains("Authentication failed")) return "auth failed";
    if (msg.contains("connection")) return "connection error";
    return msg.length() > 30 ? msg.substring(0, 30) + "..." : msg;
  }

  private void printEnvironmentVariables() {
    String logLevel = env.getProperty("logging.level.root", "info");
    if (!"debug".equalsIgnoreCase(logLevel)) {
      return;
    }

    System.out.println("Environment Variables:");

    String[] keys = {
        "MINIMAX_API_KEY",
        "MINIMAX_API_KEY2",
        "spring.ai.minimax.base-url",
        "spring.data.mongodb.host",
        "spring.data.mongodb.port",
        "spring.data.mongodb.database",
        "spring.data.mongodb.username",
        "spring.ai.vectorstore.chroma.host",
        "spring.ai.vectorstore.chroma.port"
    };

    for (String key : keys) {
      String value = env.getProperty(key);
      if (value != null && !value.isEmpty()) {
        if (key.contains("KEY") || key.contains("PASSWORD") || key.contains("SECRET")) {
          if (value.length() > 8) {
            value = value.substring(0, 4) + "****" + value.substring(value.length() - 4);
          }
        }
        System.out.println("  " + key + "=" + value);
      }
    }
  }

  /**
   * Startup information printer
   */
  @Component
  public static class StartupInfo {

    private final Environment env;

    public StartupInfo(Environment env) {
      this.env = env;
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
      int port = env.getProperty("server.port", Integer.class, 8080);
      String baseUrl = "http://localhost:" + port;

      System.out.println();
      System.out.println("Spring AI Wiki");
      System.out.println("Welcome!");
      System.out.println("http://localhost:" + port);
      System.out.println();
    }
  }
}
