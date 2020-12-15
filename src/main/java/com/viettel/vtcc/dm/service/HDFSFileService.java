package com.viettel.vtcc.dm.service;

import com.viettel.vtcc.dm.function.HDFSService;
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
import java.util.List;

public class HDFSFileService extends Thread{

    private final Logger logger;
    private final EncryptionAbstract encryption;
    private final TransformDataInfo transformDataInfo;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private volatile boolean isRun = true;

    public HDFSFileService(Logger logger, EncryptionAbstract encryption, TransformDataInfo transformDataInfo) {
        this.logger = logger;
        this.encryption = encryption;
        this.transformDataInfo = transformDataInfo;
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
                List<String> listFiles = getListFilesLocal(transformDataInfo.getLocalDir());
                long timestamp = System.currentTimeMillis();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
                    timestamp = timestamp - 60 * 60 * 1000;
                }
                String patternTime = sdf.format(timestamp);
                int index = 0;
                if (listFiles.size() > 0){
                    for (String filePath : listFiles){
                        index++;
                        logger.info("Index push HDFS = {}, Total = {}", index, listFiles.size());
                        List<String> dataFile = readData(filePath);
                        pushDataToHDFS(hdfsService, dataFile, filePath, patternTime);
                    }
                } else {
                    logger.info("There is no file to process");
                }
            }catch (Exception e){
                LogUtils.LOGGER.error("Error with msg = {}", e.getMessage(), e);
            }
            try{
                logger.info("Go to sleep 90000");
                Thread.sleep(90000);
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

    void pushDataToHDFS(HDFSService hdfsService, List<String> data, String pathFile, String patternTime) throws IOException {
        if (data.size() > 0) {
            String[] splits = pathFile.split("\\/");
            String fileName = splits[splits.length - 1];
            String baseFolder = Paths.get(transformDataInfo.getBaseFolder(), patternTime).toString();
            if (hdfsService.write2File(baseFolder, fileName, data)) {
                logger.info("Write file {} to folder {} successfully!!!", pathFile, baseFolder);
                deleteFileLocal(pathFile);
            }
        } else {
            logger.info("There is no data on path {}", pathFile);
        }
    }

    public void close(){
        isRun = false;
        logger.info("Close service at thread name = {}", Thread.currentThread().getName());
    }
}
