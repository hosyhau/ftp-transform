package com.viettel.vtcc.dm;

import com.viettel.vtcc.dm.model.FileDirectionInfo;
import com.viettel.vtcc.dm.model.TransformDataInfo;
import com.viettel.vtcc.dm.msisdn_encryption.EncryptionAbstract;
import com.viettel.vtcc.dm.msisdn_encryption.EncryptionFactory;
import com.viettel.vtcc.dm.service.FTPDownloadService;
import com.viettel.vtcc.dm.service.HDFSFileService;
import com.viettel.vtcc.dm.utils.ConfigUtil;
import com.viettel.vtcc.userprofile.common.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CdrMyanmarMain {
    public static void main(final String[] args){
        Configs configs = Configs.create("production");
        final String key = configs.getConfigString("encryption.keyString");
        final String iv = configs.getConfigString("encryption.ivString");

        ConfigUtil configUtil = ConfigUtil.getInstance("config_cdr.json");

        long backupTime = 0L;
        if (args.length > 0) {
            try {
                backupTime = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Params 2 do not parse to long => System exit!");
                System.exit(1);
            }
        }

        List<TransformDataInfo> transformDataInfoList = configUtil.getTransformDataInfoList();
        List<Thread> threads = new ArrayList<>();
        TransformDataInfo svInfo = transformDataInfoList.get(0);
        BlockingQueue<FileDirectionInfo> fileDirectionInfos = new ArrayBlockingQueue<>(svInfo.getNumberOfFileQueueSize());
        String baseHDFSFolder = svInfo.getBaseHDFSFolder();
        List<String> ftpFolderLogs = svInfo.getRemoteListFolder();
        final Logger logger = LoggerFactory.getLogger(svInfo.getJob());

        for (String folder : ftpFolderLogs) {
            TransformDataInfo info = getInfo(svInfo);
            info.setRemoteFolder(folder);
            info.setBaseHDFSFolder(baseHDFSFolder + folder);
            String localDir = info.getLocalDir();
            info.setLocalDir(Paths.get(localDir, folder).toString());
            FTPDownloadService downloadService = new FTPDownloadService(info, logger, fileDirectionInfos);
            String threadName = "FTP_CDR" + info.getRemoteFolder().replaceAll("\\/", "_");
            threads.add(new Thread(downloadService, threadName));
        }
        for (int i =0; i < svInfo.getNumberOfThread(); i++){
            EncryptionAbstract encryption = EncryptionFactory.factory(svInfo.getJob());
            encryption.setUp(key, iv);
            String threadName = "HDFS_CDR" + i;
            HDFSFileService hdfsFileService = new HDFSFileService(logger, encryption, fileDirectionInfos);
            threads.add(new Thread(hdfsFileService, threadName));
        }

        for (Thread thread : threads) {
            thread.start();
        }
    }

    private static TransformDataInfo getInfo(TransformDataInfo info) {
        TransformDataInfo newInfo = new TransformDataInfo();
        newInfo.setBaseHDFSFolder(info.getBaseHDFSFolder());
        newInfo.setRemoteFolder(info.getRemoteFolder());
        newInfo.setRemoteListFolder(info.getRemoteListFolder());
        newInfo.setDeletedFile(info.isDeletedFile());
        newInfo.setHost(info.getHost());
        newInfo.setJob(info.getJob());
        newInfo.setPassword(info.getPassword());
        newInfo.setPrexFileName(info.getPrexFileName());
        newInfo.setRootPath(info.getRootPath());
        newInfo.setSize(info.getSize());
        newInfo.setTimeSleep(info.getTimeSleep());
        newInfo.setUser(info.getUser());
        newInfo.setFtpDataTimeout(info.getFtpDataTimeout());
        newInfo.setLocalDir(info.getLocalDir());
        newInfo.setNumberFile(info.getNumberFile());
        newInfo.setNumberOfThread(info.getNumberOfThread());
        newInfo.setNumberOfFileQueueSize(info.getNumberOfFileQueueSize());
        return newInfo;
    }
}
