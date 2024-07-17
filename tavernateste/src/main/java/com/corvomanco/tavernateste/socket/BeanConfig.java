package com.corvomanco.tavernateste.socket;

import com.corvomanco.tavernateste.socket.util.JsonUtils;
import com.corvomanco.tavernateste.socket.util.SocketUtils;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;

import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    EngineIoServer engineIoServer() {
        var opt = EngineIoServerOptions.newFromDefault();
        opt.setCorsHandlingDisabled(true);
        var eioServer = new EngineIoServer(opt);
        return eioServer;
    }

    @Bean
    SocketIoServer socketIoServer(EngineIoServer eioServer) {
        var sioServer = new SocketIoServer(eioServer);

        var namespace = sioServer.namespace("/mynamespace");

        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            System.out.println("Client " + socket.getId() + " (" + socket.getInitialHeaders().get("remote_addr") + ") has connected.");

            var connectData = socket.getConnectData();
            if (connectData instanceof JSONObject jsonObject) {
                var authorization = jsonObject.get("token");


                jsonObject.put("uId", authorization);
            }

            socket.on("message", args1 -> {

                JSONObject o = (JSONObject) args1[0];

                var messageVo = JsonUtils.toPojoObj(o, MessageVo.class);

                var uId = SocketUtils.getUserId(socket);

                System.out.println("[Client " + socket.getId() + "][" + uId + "]" + messageVo);
                socket.send("hello", JsonUtils.toJsonObj(new MessageVo("Server", "Heo khô đi những kỉ niệm xưa kia")));
            });
        });

        return sioServer;
    }

    record MessageVo(
            String author,
            String msg) {

    }

}