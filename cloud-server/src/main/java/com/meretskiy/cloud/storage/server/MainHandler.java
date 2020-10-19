package com.meretskiy.cloud.storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import com.meretskiy.cloud.storage.common.Command;
import com.meretskiy.cloud.storage.common.Message;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.core.util.FileUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private Path currentServerPathGUI;
    private Path rootServerPath;

    public MainHandler(Path currentServerPath) {
        this.currentServerPathGUI = currentServerPath;
        this.rootServerPath = currentServerPath;
    }

    public enum State {
        IDLE,                                               // стартовая позиция
        FILE_NAME_LENGTH, FILE_NAME, FILE_LENGTH, FILE,     // для чтения директорий
        DIR_NAME_LENGTH, DIR_NAME, FILE_TYPE;               // для чтения файлов
    }

    private State currentState = State.IDLE;
    private int fileNameLength;
    private long fileLength;
    private long receivedFileLength;
    private byte readed;
    private Path filePath;
    private BufferedOutputStream out;
    private boolean directoryReading;
    private boolean fileReading;
    private Path pathBeforeDirReading;
    private final int tmpBufSize = 8192;
    private final byte[] tmpBuf = new byte[tmpBufSize];

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //TODO
    }

//    private void readCommand(byte readed, ChannelHandlerContext ctx) {
//        //TODO
//    }
//
//    private void readDirectory(ByteBuf buf, ChannelHandlerContext ctx) {
//        //TODO
//    }
//
//    private void readFile(ByteBuf buf, ChannelHandlerContext ctx) {
//        if (currentState == State.FILE_NAME_LENGTH) {
//            if (buf.readableBytes() >= 4) {
//                fileNameLength = buf.readInt();
//                currentState = State.FILE_NAME;
//            }
//        }
//    }
//
//    private void readCommand(byte readed, ChannelHandlerContext ctx) {
//        //TODO
//    }
//
//    private void readDirectory(ByteBuf buf, ChannelHandlerContext ctx) {
//        //TODO
//    }
//
//    private void readFile(ByteBuf buf, ChannelHandlerContext ctx) {
//        //TODO
//    }

    private void deleteFileIfExist(Path delPath) {
        //TODO
    }

    private void getFilePath(Path path, byte[] fileName) {
        try {
            filePath = path.resolve(new String(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
