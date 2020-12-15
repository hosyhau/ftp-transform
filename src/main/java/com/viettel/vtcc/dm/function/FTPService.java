package com.viettel.vtcc.dm.function;

import com.viettel.vtcc.dm.model.MessageInfo;
import com.viettel.vtcc.dm.model.TransformDataInfo;
import com.viettel.vtcc.dm.msisdn_encryption.EncryptionAbstract;
import com.viettel.vtcc.dm.utils.LogUtils;
import com.viettel.vtcc.dm.utils.RegexUtil;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by thuyenhx on 24/05/2017.
 */
public class FTPService {

    private FTPClient ftpClient = null;
    private final Logger logger;

    public FTPService(String host, String user, String pwd, Logger logger, int timeout) {
        this.logger = logger;
        ftpClient = new FTPClient();
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        if (timeout <= 0){
            timeout = 120000;
        }
        int reply;
        try {
            ftpClient.connect(host);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                LogUtils.LOGGER.error("ERROR connect ftp server {}, user {}, pass {}", host, user, pwd);
            }
            boolean checkLogin = ftpClient.login(user, pwd);
            if (checkLogin) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setDataTimeout(timeout);
                ftpClient.setConnectTimeout(2*timeout);
                logger.info("Connected successful to ftp server {}", host);
            } else {
                logger.warn("Connected failed to ftp server {}", host);
            }

        } catch (IOException e) {
            LogUtils.LOGGER.error("ERROR connect ftp server {}, user {}, pass {}", host, user, pwd, e);
        }
    }

    public List<String> getRemoteListFile(String remoteFolder, String time, String prefixName) {
        List<String> res = new ArrayList();
        try {
            FTPFile[] listFiles = this.ftpClient.listFiles(remoteFolder);
            for (FTPFile ftpFile : listFiles) {
                String pathFile = remoteFolder + "/" + ftpFile.getName();
                if (RegexUtil.dateFileMatcher(prefixName, time, pathFile)) {
                    res.add(pathFile);
                }
            }
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} Error get remote list file! ", FTPService.class.getSimpleName(), e);
        }
        return res;
    }

    public List<String> getRemoteListFile(String remoteFolder) {
        List<String> res = new ArrayList();
        try {
            FTPFile[] listFiles = this.ftpClient.listFiles(remoteFolder);
            for (FTPFile ftpFile : listFiles) {
                String pathFile = remoteFolder + "/" + ftpFile.getName();
                res.add(pathFile);
            }
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} Error get remote list file! ", FTPService.class.getSimpleName(), e);
        }
        return res;
    }

    public void storeFile(String pathRemote, String fileName, InputStream is) {
        try {
            if (!ftpClient.changeWorkingDirectory(pathRemote)) {
                ftpClient.makeDirectory(pathRemote);
            }
            boolean check = ftpClient.storeFile(pathRemote + "/" + fileName, is);
            if (check) {
                logger.info("Push data success to path {}", pathRemote + "/" + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.LOGGER.error("Error push data to path {}", pathRemote + "/" + fileName, e);
        }
    }

    public void downloadFile(String remotePath, BlockingQueue queue, TransformDataInfo transformDataInfo) {
        List<String> data = new ArrayList();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = this.ftpClient.retrieveFileStream(remotePath);
            long timeCur = System.currentTimeMillis();
//            long timeModified = sdf.parse(this.ftpClient.getModificationTime(remotePath)).getTime();
//            System.out.println("test: " + (Math.abs(timeCur - timeModified) >= 30*60*1000) + ", " + Math.abs(timeCur - timeModified));
//            if (inputStream != null && (Math.abs(timeCur - timeModified) >= 30*60*1000)) {
            if (inputStream != null) {
                logger.info("Start processing for file {}", remotePath);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    data.add(line);
                    if (data.size() >= transformDataInfo.getSize()) {
                        queue.add(new MessageInfo(data));
                        data.clear();
                    }
                }
                if (data.size() > 0) {
                    queue.add(new MessageInfo(data));
                    data.clear();
                }
            } else {
                logger.info("Do not read file {}", remotePath);
            }
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} Error get ftp file! ", FTPService.class.getSimpleName(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                while (!this.ftpClient.completePendingCommand()) ;
            } catch (IOException e) {
                LogUtils.LOGGER.error("{} Error close input stream! ", FTPService.class.getSimpleName(), e);
            }
        }
    }

    public List<String> downloadFile(String remotePath, EncryptionAbstract encryption) {
        List<String> res = new ArrayList();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = this.ftpClient.retrieveFileStream(remotePath);
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        line = encryption.encrypt(line);
                        res.add(line);
                    } catch (Exception e) {
                        LogUtils.LOGGER.error("Read file line = " + line + ", msg error = " + e.getMessage(), FTPService.class.getSimpleName(), e);
                    }
                }
                logger.info("Download file = "+remotePath + " successfully!");
            } else {
                logger.info("Do not read file {}", remotePath);
            }
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} Error get ftp file! ", FTPService.class.getSimpleName(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                while (!this.ftpClient.completePendingCommand()) ;
            } catch (IOException e) {
                LogUtils.LOGGER.error("{} Error close input stream! ", FTPService.class.getSimpleName(), e);
            }
        }
        return res;
    }

    public boolean downloadFileToLocal(String remoteFilePath, String localFilePath) {
        logger.info("Downloading to local with filename = {}", remoteFilePath);
        OutputStream out = null;
        try {
            out = new FileOutputStream(localFilePath);
            this.ftpClient.retrieveFile(remoteFilePath, out);
            out.close();
            logger.info("Download file to local successfully with name = {}", remoteFilePath);
            return true;
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} error downloading to local = " + e.getMessage(), FTPService.class.getSimpleName(), e);
        }
        return false;
    }


    public void deleteListFile(List<String> listFile) {
        try {
            for (String file : listFile) {
                boolean deleted = this.ftpClient.deleteFile(file);
                if (deleted) {
                    logger.info("{} was deleted successfully!", file);
                } else {
                    logger.warn("Could not delete {}", file);
                }
            }
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} Error delete file! ", FTPService.class.getSimpleName(), e);
        }
    }

    public void deleteFile(String remotePath) {
        try {
            boolean deleted = this.ftpClient.deleteFile(remotePath);
            if (deleted) {
                logger.info("{} was deleted successfully!", remotePath);
            } else {
                logger.info("Could not delete {}", remotePath);
            }
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} {} Error delete file!", FTPService.class.getSimpleName(), e);
        }
    }

    public void disconnect() {
        if (this.ftpClient.isConnected()) {
            try {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
            } catch (Exception e) {
                LogUtils.LOGGER.error("{} Error disconnect!", FTPService.class.getSimpleName(), e);
            }
        }
    }
}
