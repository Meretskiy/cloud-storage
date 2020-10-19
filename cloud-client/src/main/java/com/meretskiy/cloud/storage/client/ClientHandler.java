package com.meretskiy.cloud.storage.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.meretskiy.cloud.storage.common.Command;
import com.meretskiy.cloud.storage.common.FileInfo;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private List<Callback> callbackList;

    public ClientHandler(List<Callback> callbackList) {
        this.callbackList = callbackList;
    }

    public enum State {
        IDLE,                                                                   // стартовая позиция
        FILE_NAME_LENGTH, FILE_NAME, FILE_LENGTH, FILE,                         // для чтения файлов
        DIR_NAME_LENGTH, DIR_NAME, FILE_TYPE_DIR,                               // для чтения директорий
        LIST_SIZE, NAME_LENGTH_LIST, FILE_TYPE, NAME_LIST, FILE_LENGTH_LIST     // для чтения списка файлов
    }

    private State currentState = State.IDLE;
    boolean fileReading = false;
    boolean fileListReading = false;

    private Path filePath;
    private byte readed;
    private int fileNameLength;
    private String fileName;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private final int tmpBufSize = 8192;
    private final byte[] tmpBuf = new byte[tmpBufSize];

    private int listSize;
    private FileInfo.FileType fileType;
    private boolean directoryReading = false;
    private Path pathBeforeDirReading;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                readed = buf.readByte();
                readCommand(readed);
            }
            if (directoryReading) {
                readDirectory(buf);
            }
            if (fileReading) {
                readFile(buf);
            }
            if (fileListReading) {
                if (currentState == State.LIST_SIZE) {
                    if (buf.readableBytes() >= 4) {
                        listSize = buf.readInt();
                        currentState = State.NAME_LENGTH_LIST;
                    }
                }
                if (currentState != State.LIST_SIZE && listSize > 0) {
                    readServerFilesList(buf);
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void readCommand(byte readed) {
        //TODO
    }

    private void readDirectory(ByteBuf buf) {
        if (currentState == State.DIR_NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                fileNameLength = buf.readInt();
                System.out.println("Get dirName length " + fileNameLength);
                currentState = State.DIR_NAME;
            }
        }

        if (currentState == State.DIR_NAME) {
            try {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] fileName = new byte[fileNameLength];
                    buf.readBytes(fileName);
                    GUIHelper.currentClientPath = GUIHelper.currentClientPath.resolve(new String(fileName, "UTF-8"));
                    if (!Files.exists(GUIHelper.currentClientPath)) {
                        Files.createDirectory(GUIHelper.currentClientPath);
                    }
                    System.out.println("Get dirName " + GUIHelper.currentClientPath);
                    currentState = State.FILE_TYPE_DIR;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (currentState == State.FILE_TYPE_DIR) {
            if (buf.readableBytes() > 0) {
                byte b = buf.readByte();
                System.out.println("FILE_TYPE " + b);
                if (b == Command.IS_DIRECTORY.getByteValue()) {
                    currentState = State.DIR_NAME_LENGTH;
                } else if (b == Command.IS_FILE.getByteValue()) {
                    currentState = State.FILE_NAME_LENGTH;
                    fileReading = true;
                } else if (b == Command.END_DIRECTORY.getByteValue()) {
                    GUIHelper.currentClientPath = GUIHelper.currentClientPath.getParent();
                    if (GUIHelper.currentClientPath.equals(pathBeforeDirReading)) {
                        currentState = State.IDLE;
                        directoryReading = false;
                        callbackList.get(2).callback();
                    }
                } else {
                    System.out.println("ERROR: Invalid first byte - " + b);
                    currentState = State.IDLE;
                }
            }
        }

    }

    private void readServerFilesList(ByteBuf buf) {
       //TODO
    }

    private void readFile(ByteBuf buf) {
        if (currentState == State.FILE_NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                fileNameLength = buf.readInt();
                currentState = State.FILE_NAME;
            }
        }

        if (currentState == State.FILE_NAME) {
            if (buf.readableBytes() >= fileNameLength) {
                byte[] fileNameBytes = new byte[fileNameLength];
                buf.readBytes(fileNameBytes);
                getFileName(fileNameBytes);
                filePath = GUIHelper.currentClientPath.resolve(fileName);
                deleteFileIfExist(filePath);
                currentState = State.FILE_LENGTH;
            }
        }

        if (currentState == State.FILE_LENGTH) {
            try {
                if (buf.readableBytes() >= 8) {
                    receivedFileLength = 0L;
                    fileLength = buf.readLong();
                    if (fileLength == 0) {
                        Files.createFile(filePath);
                        if (directoryReading) {
                            currentState = State.FILE_TYPE_DIR;
                        } else {
                            callbackList.get(2).callback();
                            currentState = State.IDLE;
                        }
                    } else {
                        out = new BufferedOutputStream(new FileOutputStream(filePath.toString()));
                        currentState = State.FILE;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (currentState == State.FILE) {
            try {
                while (buf.readableBytes() > 0) {
                    if (fileLength - receivedFileLength > tmpBufSize && buf.readableBytes() > tmpBufSize){
                        buf.readBytes(tmpBuf);
                        out.write(tmpBuf);
                        receivedFileLength += tmpBufSize;
                    } else {
                        out.write(buf.readByte());
                        receivedFileLength++;
                        if (fileLength == receivedFileLength) {
                            out.close();
                            fileReading = false;
                            if (directoryReading) {
                                currentState = State.FILE_TYPE_DIR;
                            } else {
                                callbackList.get(2).callback();
                                currentState = State.IDLE;
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getFileName(byte[] nextFileNameBytes) {
        try {
            fileName = new String(nextFileNameBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void deleteFileIfExist(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

