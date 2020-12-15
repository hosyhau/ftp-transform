package com.viettel.vtcc.dm.model;

public class FileDirectionInfo {

    private String localFilePath;
    private String hdfsFilePath;

    public FileDirectionInfo(String localFilePath, String hdfsFilePath) {
        this.localFilePath = localFilePath;
        this.hdfsFilePath = hdfsFilePath;
    }

    public String getHdfsFilePath() {
        return hdfsFilePath;
    }

    public void setHdfsFilePath(String hdfsFilePath) {
        this.hdfsFilePath = hdfsFilePath;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

}
