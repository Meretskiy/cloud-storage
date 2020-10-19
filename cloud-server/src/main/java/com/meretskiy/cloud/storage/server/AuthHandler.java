package com.meretskiy.cloud.storage.server;

import com.sun.xml.internal.bind.v2.TODO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private DataBaseAuthService authService;
    private static final Logger logger = LogManager.getLogger(AuthHandler.class);

    public AuthHandler(DataBaseAuthService authService) {
        this.authService = authService;
    }

    public enum State {
        LOGIN_LENGTH, LOGIN, PASSWORD_LENGTH, PASSWORD
    }

    private State currentState = State.LOGIN_LENGTH;
    private int loginLength;
    private int passwordLength;
    private String login;
    private String password;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.error(cause);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //TODO
    }

    private void getLoginAndPassword(ByteBuf buf) {
        //TODO
    }
}
