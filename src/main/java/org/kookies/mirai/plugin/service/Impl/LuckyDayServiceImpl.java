package org.kookies.mirai.plugin.service.Impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import okhttp3.Response;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.enumeration.AIRoleType;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.exceptions.RequestException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.ApiRequester;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.plugin.auth.DuplicatePermission;
import org.kookies.mirai.plugin.auth.Permission;
import org.kookies.mirai.plugin.service.LuckyDayService;
import org.kookies.mirai.pojo.dto.LuckDayDTO;
import org.kookies.mirai.pojo.entity.api.baidu.ai.response.ChatResponse;
import org.kookies.mirai.pojo.entity.api.baidu.ai.request.Message;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class LuckyDayServiceImpl implements LuckyDayService {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .serializeNulls()
            .create();

    private final Random random = new Random();

    /**
     * 为指定用户在指定群组中发送幸运日消息。
     *
     * @param sender 发送请求的用户ID
     * @param group  目标群组
     */
    @Override
    public void luckyDay(Long sender, Group group) {
        MessageChainBuilder chain = new MessageChainBuilder();
        MessageChain at = MiraiCode.deserializeMiraiCode("[mirai:at:" + sender + "]");


        if (Permission.checkPermission(sender, group.getId())) {
            if (DuplicatePermission.checkPermission(sender)) {
                LuckDayDTO luckDayDTO = LuckDayDTO.builder()
                        .sender(sender)
                        .romanceFortune(random.nextInt(100))
                        .schoolFortune(random.nextInt(100))
                        .wealthFortune(random.nextInt(100))
                        .build();
                List<Message> messages = createMessages(luckDayDTO);
                ChatResponse response = getResponse(messages, sender);

                sendMsg(at, group, chain, luckDayDTO, response.getResult());
            } else {
                sendMsg(at, group, chain, MsgConstant.LUCKY_DAY_PERMISSION_DUPLICATE_ERROR);
            }
        }
    }

    /**
     * 发送消息给指定群组，关于指定用户的运势结果。
     *
     * @param at 指定接收消息的用户
     * @param group 发送消息的群组
     * @param chain 消息构建器，用于组装消息内容
     * @param luckDayDTO 运势数据传输对象，包含财运、桃花运和学业运等信息
     * @param response 个性化回复内容
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, LuckDayDTO luckDayDTO, String response) {
        // 将消息指向的用户添加到消息链中
        chain.add(at);

        chain.append(" "); // 添加一个空格，用于分隔用户名称和消息内容

        // 构建并添加运势消息内容，包括财运、桃花运、学业运以及个性化回复
        chain.append(new PlainText("你今天的运势结果来啦！\n"))
                .append(new PlainText("财运：" + luckDayDTO.getWealthFortune() + "%\n"))
                .append(new PlainText("桃花运：" + luckDayDTO.getRomanceFortune() + "%\n"))
                .append(new PlainText("学业：" + luckDayDTO.getSchoolFortune() + "%\n"))
                .append(new PlainText(response));

        // 发送构建完成的消息
        group.sendMessage(chain.build());
    }
    /**
     * 向指定群组发送消息，消息内容包括@某对象和后续的文本内容。
     *
     * @param at       消息中被@的对象，通常是一个用户或用户组。
     * @param group    消息要发送到的目标群组。
     * @param chain    消息内容的构建器，用于拼接消息。
     * @param response 要发送的消息内容。
     */
    private void sendMsg(MessageChain at, Group group, MessageChainBuilder chain, String response) {
        // 将消息@对象添加到消息链中
        chain.add(at);
        // 在消息链后添加一个空格，为消息内容做分隔
        chain.append(" ");
        // 添加消息内容到消息链
        chain.append(new PlainText(response));
        // 构建消息并发送到指定群组
        group.sendMessage(chain.build());
    }

    /**
     * 根据提供的 LuckDayDTO 创建一系列消息对象。
     * <p>
     * 这个方法首先尝试从指定路径读取机器人信息，如果读取成功，将创建一个包含用户请求消息的新消息列表。
     * 用户请求消息包含了财运、学业运和桃花运的运势信息。
     * 如果读取机器人信息时发生IO异常，则会抛出运行时异常。
     * </p>
     *
     * @param luckDayDTO 幸运日数据传输对象，包含了财运、学业运和桃花运的信息。
     * @return 返回一个包含用户请求消息的消息列表。
     */
    private List<Message> createMessages(LuckDayDTO luckDayDTO) {
        List<Message> messages;
        try {
            // 尝试从指定路径读取机器人信息
            messages = FileManager.readBotInfo(DataPathInfo.BOT_INFO_PATH);
        } catch (IOException e) {
            // 如果读取过程中发生IO异常，抛出运行时异常
            throw new DataLoadException(MsgConstant.BOT_INFO_LOAD_ERROR);
        }
        // 创建一个消息对象，包含用户请求的内容和相应的运势信息
        Message message = Message.builder()
                .role(AIRoleType.USER.getRole())
                .content("请你帮我分析今天的运势并且给我一个可爱的祝福语，要多多表现Kookie的可爱与天真，\n" +
                        "所有的对话都要充满日常感，不能让人感觉到跳脱。" +
                        "今天的运势是:" +
                        "财运：" + luckDayDTO.getWealthFortune() +
                        "学业：" + luckDayDTO.getSchoolFortune() +
                        "桃花运：" + luckDayDTO.getRomanceFortune())
                .build();
        // 将用户请求消息添加到消息列表中
        messages.add(message);

        return messages;
    }


    /**
     * 根据传入的消息列表获取百度聊天机器人的响应结果。
     *
     * @param messages 用户向机器人发送的消息列表。
     * @param sender   发送请求的用户ID。
     * @return ChatResponse 从百度API获取的聊天响应对象，包含具体的响应内容。
     * @throws RequestException 如果请求过程中发生IO异常，则抛出请求异常。
     */
    private ChatResponse getResponse(List<Message> messages, Long sender) {
        Response originalResponse;
        String json;
        try {
            // 向百度API发送请求并获取原始响应
            originalResponse = ApiRequester.sendBaiduRequest(messages, sender);
            // 将原始响应的主体内容转换为字符串
            json = originalResponse.body().string();
        } catch (IOException e) {
            // 若发生IO异常，抛出自定义的请求异常
            throw new RequestException(MsgConstant.REQUEST_ERROR);
        }

        // 使用Gson将JSON字符串解析为BaiduChatResponse对象
        return gson.fromJson(json, ChatResponse.class);
    }


}
