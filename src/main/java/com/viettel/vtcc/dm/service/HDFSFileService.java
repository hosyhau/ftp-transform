package com.viettel.vtcc.dm.service;

import com.viettel.vtcc.dm.function.HDFSService;
import com.viettel.vtcc.dm.model.FileDirectionInfo;
import com.viettel.vtcc.dm.model.TransformDataInfo;
import com.viettel.vtcc.dm.msisdn_encryption.EncryptionAbstract;
import com.viettel.vtcc.dm.utils.LogUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class HDFSFileService extends Thread{

    private final Logger logger;
    private final EncryptionAbstract encryption;
    private final BlockingQueue<FileDirectionInfo> fileDirectionInfos;
    private volatile boolean isRun = true;

    public HDFSFileService(Logger logger, EncryptionAbstract encryption, BlockingQueue<FileDirectionInfo> fileDirectionInfos) {
        this.logger = logger;
        this.encryption = encryption;
        this.fileDirectionInfos = fileDirectionInfos;
    }

    public List<String> getListFilesLocal(String localPath){
        List<String> listFiles = new ArrayList<>();
        File file = new File(localPath);
        File [] listOfFiles = file.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                listFiles.add(listOfFiles[i].getAbsolutePath());
            }
        }
        return listFiles;
    }

    public List<String> readData(String localPath) {
        List<String> dataList = new ArrayList<>();
        BufferedReader reader;
        logger.info("Read data from local path {}", localPath);
        try {
            String line;
            reader = new BufferedReader(new FileReader(localPath));
            while ((line = reader.readLine()) != null) {
                try {
                    line = encryption.encrypt(line);
                    dataList.add(line);
                } catch (Exception e) {
                    LogUtils.LOGGER.error("Read file line = " + line + ", msg error = " + e.getMessage(), HDFSFileService.class.getSimpleName(), e);
                }
            }
            reader.close();
        } catch (IOException e) {
            LogUtils.LOGGER.error("Error read data msg = {}", e.getMessage(), e);
        }

        return dataList;
    }

    public void run(){
        while (isRun){
            HDFSService hdfsService = null;
            try{
                hdfsService = new HDFSService(logger);
                synchronized (fileDirectionInfos){
                    FileDirectionInfo info = fileDirectionInfos.take();
                    List<String> dataFile = readData(info.getLocalFilePath());
                    if (dataFile.size() > 0){
                        boolean write2File = hdfsService.write2File(info.getHdfsFilePath(), dataFile);
                        if (write2File){
                            deleteFileLocal(info.getLocalFilePath());
                        } else{
                            fileDirectionInfos.put(info);
                        }
                    }else {
                        logger.info("There is no data on file local = {}", info.getLocalFilePath());
                    }
                }
            }catch (Exception e){
                LogUtils.LOGGER.error("Error with msg = {}", e.getMessage(), e);
            }
            try{
                logger.info("Go to sleep 60000");
                Thread.sleep(60000);
            }catch (InterruptedException e){
                LogUtils.LOGGER.error("Error sleep with msg = {}", e.getMessage(), e);
            }
        }
    }

    public void deleteFileLocal(String filePath){
        File file = new File(filePath);
        boolean deleted = file.delete();
        if (deleted){
            logger.info("Delete file {} local successfully!", filePath);
        }
    }

    public void close(){
        isRun = false;
        logger.info("Close service at thread name = {}", Thread.currentThread().getName());
    }
}
