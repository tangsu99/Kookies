package org.kookies.mirai.commen.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.pojo.entity.api.baidu.ai.request.Message;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileManager {
    private static final Gson gson = new Gson();

    /**
     * 从指定路径读取模板文件的内容。
     *
     * @param templatePath 模板文件的路径，相对于项目资源目录。
     * @return 文件的字符串内容，包括换行符。
     * @throws IOException 如果读取文件时发生错误。
     */
    public static String readTemplateFile(String templatePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        // 使用BufferedReader从模板路径读取资源文件，行-by-行地读取并构建内容。
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(templatePath))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        }

        return contentBuilder.toString();
    }


    /**
     * 从指定的文件路径读取JSON文件内容，并将其解析为一个JsonObject。
     *
     * @param filePath 要读取的JSON文件的路径。
     * @return 解析后的JsonObject，如果文件读取或解析失败则抛出IOException。
     */
    public static JsonObject readJsonFile(String filePath) throws IOException {
        // 使用try-with-resources语句确保BufferedReader在使用完毕后能够自动关闭
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            // 将文件内容解析为JSON对象
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    /**
     * 将JsonObject写入指定的文件路径。
     *
     * @param filePath 要写入的JSON文件的路径。
     * @param jsonObject 要写入的JsonObject。
     * @throws IOException 如果写入文件时发生错误。
     */
    public static void writeJsonFile(String filePath, JsonObject jsonObject) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            gson.toJson(jsonObject, writer);
        }
    }

    /**
     * 将字符串内容写入指定的文件路径。
     *
     * @param filePath 要写入的文件路径。
     * @param content 要写入的字符串内容。
     * @throws IOException 如果写入文件时发生错误。
     */
    public static void write(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }


    /**
     * 从指定的文件路径读取答案书，并返回一个Map，其中键是答案书条目的索引，值是答案书条目的内容。
     *
     * @param filePath 要读取的答案书文件的路径。
     * @return 一个Map，其中键是答案书条目的索引，值是答案书条目的内容。
     * @throws IOException 如果读取文件时发生错误。
     */
    public static Map<Integer, String> readAnswerBook(String filePath) throws IOException{
        Map<Integer, String> answerBook = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(filePath))))) {
            String line;
            int index = 1;
            while ((line = reader.readLine()) != null) {
                answerBook.put(index++, line);
            }
        }
        return answerBook;
    }


    /**
     * 从指定文件路径读取信息，构造并返回一个包含消息的列表。
     * 每两行被视为一对对话，其中奇数行代表用户的消息，偶数行代表助手的消息。
     *
     * @param filePath 要读取的文件的路径。文件应包含一对对的用户和助手的对话。
     * @return 一个包含读取到的消息的列表，其中每个消息都标明了是用户还是助手发出的。
     * @throws IOException 如果读取文件时发生输入输出异常。
     */
    public static List<Message> readBotInfo (String filePath) throws IOException {
        List<Message> messages = new ArrayList<>();
        // 使用BufferedReader从指定的文件路径读取信息
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileManager.class.getResourceAsStream(filePath))))) {
            String line;
            int index = 1;
            // 遍历文件的每一行
            while ((line = reader.readLine()) != null) {
                Message message;
                // 根据行的索引奇偶性，区分用户消息和助手消息
                if (index % 2 == 1) {
                    message = Message.builder()
                            .role(AIRoleType.USER.getRole())
                            .content(line)
                            .build();
                } else {
                    message = Message.builder()
                            .role(AIRoleType.ASSISTANT.getRole())
                            .content(line)
                            .build();
                }
                messages.add(message);
                index++;
            }
        }
        return messages;
    }

    public static JsonArray readJsonArray(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }
}
