package com.viettel.vtcc.dm.service;

import com.viettel.vtcc.dm.function.FTPService;
import com.viettel.vtcc.dm.model.TransformDataInfo;
import com.viettel.vtcc.dm.utils.LogUtils;
import org.slf4j.Logger;
import java.util.List;


public class FTPDownloadService extends Thread{

    private final TransformDataInfo transformDataInfo;
    private final Logger logger;
    private volatile boolean isRun = true;

    public FTPDownloadService(TransformDataInfo transformDataInfo, Logger logger) {
        this.transformDataInfo = transformDataInfo;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (isRun) {
            logger.info("Process CDR with remote ftp folder = " + transformDataInfo.getRemoteFolder());
            long start = System.currentTimeMillis();
            FTPService ftpService = null;
            int numberFile = transformDataInfo.getNumberFile();
            try {
                ftpService = new FTPService(transformDataInfo.getHost(), transformDataInfo.getUser(), transformDataInfo.getPassword(), logger, transformDataInfo.getFtpDataTimeout());
                List<String> listFiles = ftpService.getRemoteListFile(transformDataInfo.getRemoteFolder());
                int index = 0;
                logger.info("Total files remote ftp file = {}", listFiles.size());
                for (String filePath : listFiles) {
                    index++;
                    if (index % numberFile == 0) {
                        logger.info("Reconnect ftp service at time {}", System.currentTimeMillis());
                        break;
                    }
                    logger.info("Index file = {}, Total files = {}, File name = {}", index, listFiles.size(), filePath);
                    boolean download = ftpService.downloadFileToLocal(filePath, transformDataInfo.getLocalDir());
                    if (download) {
                        logger.info("download success with file {}", filePath);
//                        ftpService.deleteFile(filePath);
                    }
                }
            } catch (Exception e) {
                LogUtils.LOGGER.error("Error get data from ftp server!, msg = {}", e.getMessage(), e);
            } finally {
                if (ftpService != null) {
                    ftpService.disconnect();
                }
            }
            logger.info("Process time = {}", (System.currentTimeMillis() - start));
            try {
                logger.info("Go to sleep {} millis", transformDataInfo.getTimeSleep());
                Thread.sleep(transformDataInfo.getTimeSleep());
            } catch (InterruptedException e) {
                LogUtils.LOGGER.error("Error sleep thread, msg = {}", e.getMessage(), e);
            }
        }
    }

    public void close(){
        this.isRun = false;
    }
}
