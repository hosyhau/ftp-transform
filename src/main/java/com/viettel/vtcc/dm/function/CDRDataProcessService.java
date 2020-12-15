//package com.viettel.vtcc.dm.function;
//
//import com.viettel.vtcc.dm.model.TransformDataInfo;
//import com.viettel.vtcc.dm.msisdn_encryption.EncryptionAbstract;
//import com.viettel.vtcc.dm.service.HDFSFileService;
//import com.viettel.vtcc.dm.utils.LogUtils;
//import org.slf4j.Logger;
//
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.List;
//
//
//public class CDRDataProcessService extends Thread {
//
//    private final Logger logger;
//
//    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//    private TransformDataInfo transformDataInfo;
//    private final long backupTime;
//    private EncryptionAbstract encryption;
//    private volatile boolean isRun = true;
//    private long processTime;
//
//    public CDRDataProcessService(TransformDataInfo info, Logger logger, long backupTime, EncryptionAbstract encryption) {
//        this.transformDataInfo = info;
//        this.logger = logger;
//        this.backupTime = backupTime;
//        this.encryption = encryption;
//        this.processTime = 0;
//    }
//
//    @Override
//    public void run() {
//        while (isRun) {
//            logger.info("Process CDR with remote ftp folder = " + transformDataInfo.getRemoteFolder());
//            long start = System.currentTimeMillis();
//            FTPService ftpService = null;
//            HDFSService hdfsService = null;
//            HDFSFileService fileService = null;
//            try {
//                long timestamp = System.currentTimeMillis();
//                if (backupTime > 0) {
//                    timestamp = backupTime;
//                }
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTimeInMillis(timestamp);
//                if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
//                    timestamp = timestamp - 60 * 60 * 1000;
//                }
//                String patternTime = sdf.format(timestamp);
//                ftpService = new FTPService(transformDataInfo.getHost(), transformDataInfo.getUser(), transformDataInfo.getPassword(), logger, transformDataInfo.getFtpDataTimeout());
//                hdfsService = new HDFSService(logger);
//                fileService = new HDFSFileService(logger, encryption, transformDataInfo);
//                List<String> listFiles = ftpService.getRemoteListFile(transformDataInfo.getRemoteFolder());
//                int index = 0;
//                logger.info("Total files remote ftp file = {}", listFiles.size());
//                for (String filePath : listFiles) {
//                    index++;
//                    if (index % 100 == 0) {
//                        ftpService.disconnect();
//                        logger.info("Reconnect ftp service at time {}", System.currentTimeMillis());
//                        ftpService = new FTPService(transformDataInfo.getHost(), transformDataInfo.getUser(), transformDataInfo.getPassword(), logger, transformDataInfo.getFtpDataTimeout());
//                    }
//                    logger.info("Index file = {}, Total files = {}, File name = {}", index, listFiles.size(), filePath);
//                    boolean download = ftpService.downloadFileToLocal(filePath, transformDataInfo.getLocalDir());
//                    if (download) {
//                        String [] splitFileNames = filePath.split("\\/");
//                        String fileName = splitFileNames[splitFileNames.length - 1];
//                        String localFilePath = Paths.get(transformDataInfo.getLocalDir(), fileName).toString();
//                        ftpService.deleteFile(filePath);
//                        List<String> dataFile = fileService.readData(localFilePath);
//                        pushDataToHdfs(hdfsService, fileService, dataFile, localFilePath, patternTime);
//                    }
//                }
//            } catch (Exception e) {
//                LogUtils.LOGGER.error("Error get data from ftp server!, msg = {}", e.getMessage(), e);
//            } finally {
//                if (ftpService != null) {
//                    ftpService.disconnect();
//                }
//                if (hdfsService != null) {
//                    hdfsService.close();
//                }
//            }
//            isRun = false;
//            logger.info("Process time = {}", (System.currentTimeMillis() - start));
//            try {
//                logger.info("Go to sleep {} millis", transformDataInfo.getTimeSleep());
//                Thread.sleep(transformDataInfo.getTimeSleep());
//            } catch (InterruptedException e) {
//                LogUtils.LOGGER.error("Error sleep thread, msg = {}", e.getMessage(), e);
//            }
//        }
//    }
//
//    private void pushDataToHdfs(HDFSService hdfsService, HDFSFileService fileService, List<String> data, String pathFile, String patternTime) throws IOException {
//        logger.info("PUSH DATA INTO HDFS: {}", data.size());
//        if (data.size() > 0) {
//            String[] splits = pathFile.split("\\/");
//            String fileName = splits[splits.length - 1];
//            String baseFolder = Paths.get(transformDataInfo.getBaseFolder(), patternTime).toString();
//            if (hdfsService.write2File(baseFolder, fileName, data)) {
//                logger.info("Write file {} to folder {} successfully!!!", fileName, baseFolder);
////                ftpService.deleteFile(pathFile);
//                fileService.deleteFileLocal(pathFile);
//            }
//        } else {
//            logger.info("There is no data to push to HDFS");
//        }
//        data.clear();
//    }
//
//    public void close() {
//        try {
//            this.isRun = false;
//        } catch (Exception e) {
//            LogUtils.LOGGER.error("Error = {}", e.getMessage(), e);
//        }
//    }
//}