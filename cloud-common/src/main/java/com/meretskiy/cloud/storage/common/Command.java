package com.meretskiy.cloud.storage.common;

public enum Command {

    AUTH_OK(1),
    AUTH_ERR(2),
    FILE_DOES_NOT_EXIST(3),
    DELETE_FILE(10),
    DELETE_FILE_ERR(11),
    TRANSFER_FILE(20),
    TRANSFER_FILE_ERR(21),
    TRANSFER_DIRECTORY(22),
    DOWNLOAD_FILE(30),
    DOWNLOAD_FILE_ERR(31),
    DOWNLOAD_DIRECTORY(32),
    IS_FILE(40),
    IS_DIRECTORY(41),
    END_DIRECTORY(42);

    final int value;

    Command(int value) {
        this.value = value;
    }

    public int getByteValue() {
        return (byte) value;
    }
}
