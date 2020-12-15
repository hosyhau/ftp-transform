package com.viettel.vtcc.dm.model;

import java.util.List;

/**
 * Created by thuyenhx on 25/05/2017.
 */
public class TransformDataInfo {
    private String job;
    private String host;
    private String user;
    private String password;
    private String rootPath;
    private String baseHDFSFolder;
    private String prexFileName;
    private String remoteFolder;
    private boolean isDeletedFile=false;
    private List<String> remoteListFolder;
    private String localDir;
    private int ftpDataTimeout = 300000;
    private int numberOfFileQueueSize = 20000;
    private int numberOfThread = 2;

    private int size;
    private long timeSleep;
    private int numberFile = 50;

    public TransformDataInfo(String job, String host, String user, String password, String rootPath, String baseHDFSFolder, String prexFileName, String remoteFolder, int size, long timeSleep) {
        this.job = job;
        this.host = host;
        this.user = user;
        this.password = password;
        this.rootPath = rootPath;
        this.baseHDFSFolder = baseHDFSFolder;
        this.prexFileName = prexFileName;
        this.remoteFolder = remoteFolder;
        this.size = size;
        this.timeSleep = timeSleep;
    }

    public TransformDataInfo(String job, String host, String user, String password, String rootPath, String baseHDFSFolder, String prexFileName, String remoteFolder, int size, long timeSleep, boolean isDeletedFile) {
        this.job = job;
        this.host = host;
        this.user = user;
        this.password = password;
        this.rootPath = rootPath;
        this.baseHDFSFolder = baseHDFSFolder;
        this.prexFileName = prexFileName;
        this.remoteFolder = remoteFolder;
        this.size = size;
        this.timeSleep = timeSleep;
        this.isDeletedFile=isDeletedFile;
    }

    public TransformDataInfo() {
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public long getTimeSleep() {
        return timeSleep;
    }

    public void setTimeSleep(long timeSleep) {
        this.timeSleep = timeSleep;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getBaseHDFSFolder() {
        return baseHDFSFolder;
    }

    public void setBaseHDFSFolder(String baseHDFSFolder) {
        this.baseHDFSFolder = baseHDFSFolder;
    }

    public String getRemoteFolder() {
        return remoteFolder;
    }

    public void setRemoteFolder(String remoteFolder) {
        this.remoteFolder = remoteFolder;
    }

    public String getPrexFileName() {
        return prexFileName;
    }

    public void setPrexFileName(String prexFileName) {
        this.prexFileName = prexFileName;
    }

    public boolean isDeletedFile() {
        return isDeletedFile;
    }

    public void setDeletedFile(boolean deletedFile) {
        isDeletedFile = deletedFile;
    }

    public List<String> getRemoteListFolder() {
        return remoteListFolder;
    }

    public void setRemoteListFolder(List<String> remoteListFolder) {
        this.remoteListFolder = remoteListFolder;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    public int getFtpDataTimeout() {
        return ftpDataTimeout;
    }

    public int getNumberFile() {
        return numberFile;
    }

    public void setNumberFile(int numberFile) {
        this.numberFile = numberFile;
    }

    public void setFtpDataTimeout(int ftpDataTimeout) {
        this.ftpDataTimeout = ftpDataTimeout;
    }

    public int getNumberOfFileQueueSize() {
        return numberOfFileQueueSize;
    }

    public void setNumberOfFileQueueSize(int numberOfFileQueueSize) {
        this.numberOfFileQueueSize = numberOfFileQueueSize;
    }

    public int getNumberOfThread() {
        return numberOfThread;
    }

    public void setNumberOfThread(int numberOfThread) {
        this.numberOfThread = numberOfThread;
    }

    @Override
    public String toString() {
        return "TransformDataInfo{" +
                "job='" + job + '\'' +
                ", host='" + host + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", rootPath='" + rootPath + '\'' +
                ", baseFolder='" + baseHDFSFolder + '\'' +
                ", prexFileName='" + prexFileName + '\'' +
                ", remoteFolder='" + remoteFolder + '\'' +
                ", isDeletedFile=" + isDeletedFile +
                ", remoteListFolder=" + remoteListFolder +
                ", size=" + size +
                ", timeSleep=" + timeSleep +
                '}';
    }
}
