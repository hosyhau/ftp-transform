package com.viettel.vtcc.dm.service;

import com.viettel.vtcc.dm.function.FTPService;
import com.viettel.vtcc.dm.model.FileDirectionInfo;
import com.viettel.vtcc.dm.model.TransformDataInfo;
import com.viettel.vtcc.dm.utils.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class FTPDownloadService extends Thread{

    private final TransformDataInfo transformDataInfo;
    private final Logger logger;
    private final BlockingQueue<FileDirectionInfo> fileDirectionInfos;
    private volatile boolean isRun = true;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    public FTPDownloadService(TransformDataInfo transformDataInfo, Logger logger, BlockingQueue<FileDirectionInfo> fileDirectionInfos) {
        this.transformDataInfo = transformDataInfo;
        this.logger = logger;
        this.fileDirectionInfos = fileDirectionInfos;
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
                for (String remoteFtpPath : listFiles) {
                    index++;
                    if (index % numberFile == 0) {
                        logger.info("Reconnect ftp service at time {}", System.currentTimeMillis());
                        break;
                    }
                    logger.info("Index file = {}, Total files = {}, File name = {}", index, listFiles.size(), remoteFtpPath);
                    String fileName = getFileName(remoteFtpPath);
                    String localFileLoc = Paths.get(transformDataInfo.getLocalDir(), fileName).toString();
                    String hdfsFileLoc = Paths.get(transformDataInfo.getBaseHDFSFolder(),getCurrentDate(), fileName).toString();
                    boolean download = ftpService.downloadFileToLocal(remoteFtpPath, localFileLoc);
                    if (download) {
                        logger.info("download success with file {}", remoteFtpPath);
                        synchronized (fileDirectionInfos){
                            logger.info("Push filename = {} into queue size = {}", remoteFtpPath, fileDirectionInfos.size());
                            FileDirectionInfo info = new FileDirectionInfo(localFileLoc, hdfsFileLoc);
                            fileDirectionInfos.put(info);
                        }
                        ftpService.deleteFile(remoteFtpPath);
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

    private String getFileName(String file){
        String [] splits = file.split("\\/");
        return splits[splits.length - 1];
    }

    private String getCurrentDate(){
        return sdf.format(new Date());
    }
}
