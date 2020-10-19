package com.meretskiy.cloud.storage.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {

    public enum FileType {
        FILE("F"), DIRECTORY("D");
        private String name;
        public String getName() {
            return name;
        }
        FileType(String name) {
            this.name = name;
        }
    }

    private String name;
    private long size;
    private FileType fileType;

    public FileInfo(String name, long size, FileType fileType) {
        this.name = name;
        this.fileType = fileType;
        if (fileType == FileType.DIRECTORY) {
            this.size = -1L;
        } else this.size = size;
    }

    public FileInfo(Path path) {
        try {
            this.name = path.getFileName().toString();
            this.fileType = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (Files.isDirectory(path)) {
                this.size = -1L;
            } else this.size = Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }


}
