package com.cn.bdth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.common.FunCommon;
import com.cn.bdth.constants.AiModelConstant;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.exceptions.BingException;
import com.cn.bdth.exceptions.ExceptionMessages;
import com.cn.bdth.handler.Chat;
import com.cn.bdth.interfaces.Callback;
import com.cn.bdth.model.ClaudeModel;
import com.cn.bdth.model.GptModel;
import com.cn.bdth.service.GptService;
import com.cn.bdth.structure.ServerStructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GptServiceImpl implements GptService {

    private final WebClient.Builder webClient;

    private final FunCommon funCommon;


    @Override
    public Flux<String> concatenationGpt(final GptModel model, final boolean isAdvanced) {
        //设置请求模型
        model.setModel(isAdvanced ? AiModelConstant.ADVANCED : AiModelConstant.BASIC);
        final ServerStructure server = funCommon.getServer();
        return webClient.baseUrl(server.getOpenAiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + (isAdvanced ? server.getOpenPlusKey() : server.getOpenKey()))
                .build()
                .post()
                .uri(ServerConstant.GPT_DIALOGUE)
                .body(BodyInserters.fromValue(model))
                .retrieve()
                .bodyToFlux(String.class);
    }


    @Override
    public Flux<String> concatenationNewBing(final String messages) {
        Chat chat = new Chat("_U=" + funCommon.getServer().getNewBingCookie(), false)
                .setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)));
        return Flux.create(f -> {
            chat.newChat().newQuestion(messages, new Callback() {
                @Override
                public void onSuccess(JSONObject rawData) {
                    f.complete();
                }

                @Override
                public void onFailure(JSONObject rawData, String cause) {
                    log.error("NEW BING现在已经不可用 原因:{}", cause);
                    throw new BingException(ExceptionMessages.BING_ERR);
                }

                @Override
                public void onUpdate(JSONObject rawData) {
                    f.next(String.valueOf(rawData));
                }
            });
        });
    }

    @Override
    public Flux<String> concatenationClaude(final String message) {
        final ServerStructure server = funCommon.getServer();

        final ClaudeModel claudeModel = new ClaudeModel()
                .setSessionKey(server.getSessionKey())
                .setCompletion(new ClaudeModel.Completion().setPrompt(message))
                .setOrganization_uuid(server.getOrganizationUuid())
                .setConversation_uuid(server.getConversationUuid())
                .setText(message);
        return webClient
                .baseUrl("https://claude.ai/api/append_message")
                .clientConnector(new ReactorClientHttpConnector())
                .defaultHeader(HttpHeaders.ORIGIN, "https://claude.ai")
                .defaultCookie("sessionKey", claudeModel.getSessionKey())
                .clientConnector(new ReactorClientHttpConnector(
                        //configure proxy connections
                        HttpClient.create()
                                .proxy(proxy -> proxy
                                        .type(ProxyProvider.Proxy.HTTP)
                                        //set proxy
                                        .address(new InetSocketAddress("127.0.0.1", 7890)))
                ))
                .build()
                .post()
                .body(BodyInserters.fromValue(claudeModel))
                .retrieve()
                .bodyToFlux(String.class);
    }

}
