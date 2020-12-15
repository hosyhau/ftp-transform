package com.viettel.vtcc.dm.function;

import com.viettel.vtcc.dm.utils.LogUtils;
import com.viettel.vtcc.userprofile.common.Configs;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thuyenhx on 24/05/2017.
 */
public class HDFSService {

    private Logger logger;

    private FileSystem fs;
    private String rootPath;
    Configs configs = Configs.create("production");

    public HDFSService(Logger logger) throws IOException {
        this.logger = logger;
        Configuration configuration = new Configuration();
        configuration.addResource("conf/core-site.xml");
        configuration.addResource("conf/hdfs-site.xml");
        this.fs = FileSystem.get(configuration);
        logger.info("Init hdfs service at {}", System.currentTimeMillis());
    }

    public List<String> getListFile(String pathFolder) throws IOException {
        List<String> listFiles = new ArrayList<>();
        Path path = new Path(pathFolder);
        if (fs.exists(path)) {
            RemoteIterator<LocatedFileStatus> dir = fs.listFiles(path, true);
            while (dir.hasNext()) {
                LocatedFileStatus file = dir.next();
                if (!file.getPath().toString().endsWith(".tmp")) {
                    if (file.getLen() > 10) {
                        listFiles.add(file.getPath().toString());
                    }
                }
            }
        }

        return listFiles;
    }

    public InputStream readFileStream(String pathFile) {
        Path path = new Path(pathFile);
        try {
            if (fs.exists(path)) {
                return fs.open(path);
            }
        } catch (IOException e) {
            LogUtils.LOGGER.error("ERROR = "+e.getMessage(), e);
        }

        return null;
    }

    public boolean write2File(String baseFolder, String fileName, List<String> data) throws IOException {

        String filePath = Paths.get(baseFolder, fileName).toString();
        Path path = new Path(filePath);
        try {
            if (fs.exists(path)) {
                fs.delete(path, true);
                logger.info("Delete old file HDFS = {}", path);
            }
            logger.info("Push data into path: {}, Size = {}", path, data.size());
            FSDataOutputStream fos = fs.create(path);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
            for (String line : data) {
                bufferedWriter.write(line);
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            LogUtils.LOGGER.error("HDFS write2File error" + e.getMessage(), e);
            throw e;
        }
        return true;
    }

    public boolean isExistFolder(String baseFolder) throws IOException {
        Path path = new Path(baseFolder);
        return fs.exists(path);
    }

    public void close() {
        try {
            fs.close();
        } catch (IOException e) {
            LogUtils.LOGGER.error("{} Error close File system hadoop!", HDFSService.class.getSimpleName(), e);
        }
    }

}
