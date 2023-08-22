package com.cn.bdth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.common.FunCommon;

import com.cn.bdth.config.FreeChatConfig;
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
import org.springframework.beans.factory.annotation.Value;
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

    private final FreeChatConfig freeChatConfig;

    @Value("${gpt.free.newBingCookie}")
    private String newBingCookie;

    @Override
    public Flux<String> concatenationGpt(final GptModel model) {
        final ServerStructure server = funCommon.getServer();
        return webClient.baseUrl(server.getOpenAiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + server.getOpenKey())
                .build()
                .post()
                .uri(ServerConstant.GPT_DIALOGUE)
                .body(BodyInserters.fromValue(model))
                .retrieve()
                .bodyToFlux(String.class);
    }


    @Override
    public Flux<String> concatenationNewBing(final String messages) {
        Chat chat = new Chat("_U=" + newBingCookie, false);
        return Flux.create(f -> chat.newChat().newQuestion(messages, new Callback() {
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
        }));
    }

    @Override
    public Flux<String> concatenationClaude(final String message) {

        final ClaudeModel claudeModel = freeChatConfig.getClaudeModel()
                .setCompletion(new ClaudeModel.Completion().setPrompt(message))
                .setText(message);
        return webClient
                .baseUrl("https://claude.ai/api/append_message")
                .clientConnector(new ReactorClientHttpConnector())
                .defaultHeader(HttpHeaders.ORIGIN, "https://claude.ai")
                .defaultCookie("sessionKey", claudeModel.getSessionKey())
                .build()
                .post()
                .body(BodyInserters.fromValue(claudeModel))
                .retrieve()
                .bodyToFlux(String.class);
    }

}
