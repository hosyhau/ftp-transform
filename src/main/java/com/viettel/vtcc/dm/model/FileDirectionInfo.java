package com.viettel.vtcc.dm.model;

public class FileDirectionInfo {

    private String baseFolder;
    private String localFilePath;

    public FileDirectionInfo(String baseFolder, String localFilePath) {
        this.baseFolder = baseFolder;
        this.localFilePath = localFilePath;
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

}
