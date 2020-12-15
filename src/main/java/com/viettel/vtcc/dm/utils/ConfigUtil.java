package com.viettel.vtcc.dm.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.viettel.vtcc.dm.model.TransformDataInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by thuyenhx on 25/05/2017.
 */
public class ConfigUtil {

    private static String fileConfigPath = "config.json";
    private static ConfigUtil instance;

    public static final String FTP_SERVER_HOST = "ftp.server.host";
    public static final String FTP_SERVER_USER = "ftp.server.user";
    public static final String FTP_SERVER_PASSWORD = "ftp.server.password";
    public static final String FTP_SERVER_FOLDER_LOG = "ftp.server.folder.log";
    public static final String HDFS_DATALAKE_ROOT_PATH = "hdfs.datalake.root.path";
    public static final String HDFS_DATALAKE_ZOOKEEPER_LIST = "hdfs.datalake.zookeeper.list";
    public static final String HDFS_DATALAKE_BASE_FOLDER = "hdfs.datalake.base.folder";
    public static final String HDFS_DATALAKE_PREFIX_NAME_LOG = "hdfs.datalake.prefix.name.log";
    public static final String HDFS_DATALAKE_FILE_SIZE = "hdfs.datalake.file.size";
    public static final String THREAD_TIME_SLEEP = "thread.time.sleep";
    public static final String IS_DELETED_FTP_FILE = "ftp.delete.file";
    public static final String FTP_SERVER_LIST_FOLDER_LOG = "ftp.server.list.folder.log";
    public static final String FTP_SERVER_CONNECT_TIMEOUT = "ftp.connect.timeout";
    public static final String LOCAL_DIR = "file.local.dir";
    public static final String BATCH_SIZE_FILE = "file.batch.size";
    public static final String NUMBER_THREAD = "hdfs.number.thread";
    public static final String FILE_BLOCKING_QUEUE_SIZE = "file.blocking.queue.size";

    private List<TransformDataInfo> transformDataInfoList = new ArrayList();

    public static ConfigUtil getInstance() {
        if (instance == null) {
            instance = new ConfigUtil();
        }
        return instance;
    }


    public static ConfigUtil getInstance(String fileConfig) {
        fileConfigPath = fileConfig;
        if (instance == null) {
            instance = new ConfigUtil();
        }
        return instance;
    }

    private ConfigUtil() {
        loadConfig();
    }

    private void loadConfig() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileConfigPath);
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
        JsonObject jsonRoot = new JsonParser().parse(jsonReader).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonRoot.entrySet()) {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            loadConfigFrom(jsonObject.getAsJsonObject("config"), transformDataInfoList, entry.getKey());
        }
    }

    private void loadConfigFrom(JsonObject jsonObject, List<TransformDataInfo> list, String jobName) {

        String zookeeperList = jsonObject.get(HDFS_DATALAKE_ZOOKEEPER_LIST).getAsString();
        String roothPath = HdfsUtilDm.getActiveNameNode(zookeeperList, "hdfs://hdp_userprofile", 2000);
        if (!roothPath.startsWith("hdfs://")) roothPath = "hdfs://" + roothPath + ":8020";

        TransformDataInfo svInfo = new TransformDataInfo();
        svInfo.setJob(jobName);
        svInfo.setRootPath(roothPath);
        if (jsonObject.get(FTP_SERVER_HOST) != null) {
            svInfo.setHost(jsonObject.get(FTP_SERVER_HOST).getAsString());
        }
        if (jsonObject.get(FTP_SERVER_USER) != null) {
            svInfo.setUser(jsonObject.get(FTP_SERVER_USER).getAsString());
        }
        if (jsonObject.get(FTP_SERVER_PASSWORD) != null) {
            svInfo.setPassword(jsonObject.get(FTP_SERVER_PASSWORD).getAsString());
        }
        if (jsonObject.get(HDFS_DATALAKE_BASE_FOLDER) != null) {
            svInfo.setBaseHDFSFolder(jsonObject.get(HDFS_DATALAKE_BASE_FOLDER).getAsString());
        }
        if (jsonObject.get(HDFS_DATALAKE_PREFIX_NAME_LOG) != null) {
            svInfo.setPrexFileName(jsonObject.get(HDFS_DATALAKE_PREFIX_NAME_LOG).getAsString());
        }
        if (jsonObject.get(FTP_SERVER_FOLDER_LOG) != null) {
            svInfo.setRemoteFolder(jsonObject.get(FTP_SERVER_FOLDER_LOG).getAsString());
        }
        if (jsonObject.get(HDFS_DATALAKE_FILE_SIZE) != null) {
            svInfo.setSize(jsonObject.get(HDFS_DATALAKE_FILE_SIZE).getAsInt());
        }
        if (jsonObject.get(THREAD_TIME_SLEEP) != null) {
            svInfo.setTimeSleep(jsonObject.get(THREAD_TIME_SLEEP).getAsLong());
        }
        if (jsonObject.get(IS_DELETED_FTP_FILE) != null) {
            svInfo.setDeletedFile(jsonObject.get(IS_DELETED_FTP_FILE).getAsBoolean());
        }

        if (jsonObject.get(FTP_SERVER_CONNECT_TIMEOUT) != null){
            svInfo.setFtpDataTimeout(jsonObject.get(FTP_SERVER_CONNECT_TIMEOUT).getAsInt());
        }

        if (jsonObject.get(LOCAL_DIR) != null){
            svInfo.setLocalDir(jsonObject.get(LOCAL_DIR).getAsString());
        }

        if (jsonObject.get(BATCH_SIZE_FILE) != null){
            svInfo.setNumberFile(jsonObject.get(BATCH_SIZE_FILE).getAsInt());
        }

        if (jsonObject.get(NUMBER_THREAD) != null){
            svInfo.setNumberOfThread(jsonObject.get(NUMBER_THREAD).getAsInt());
        }

        if (jsonObject.get(FILE_BLOCKING_QUEUE_SIZE) != null){
            svInfo.setNumberOfFileQueueSize(jsonObject.get(FILE_BLOCKING_QUEUE_SIZE).getAsInt());
        }

        if(jsonObject.getAsJsonArray(FTP_SERVER_LIST_FOLDER_LOG) != null){
            JsonArray jsonArray = jsonObject.getAsJsonArray(FTP_SERVER_LIST_FOLDER_LOG);
            List<String> arrFolder = new ArrayList<>();
            int arrLength = jsonArray.size();
            for (int i=0; i < arrLength; i++){
                arrFolder.add(jsonArray.get(i).getAsString());
            }
            svInfo.setRemoteListFolder(arrFolder);
        }

        list.add(svInfo);
    }

    public List<TransformDataInfo> getTransformDataInfoList() {
        return transformDataInfoList;
    }
}
