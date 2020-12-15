package com.viettel.vtcc.dm.monitor;

import com.viettel.vtcc.dm.utils.LogUtils;
import org.slf4j.Logger;

import java.util.Map;

public class MonitorService{

    private static MonitorService monitorService;
    private boolean isRun;
    private final Logger logger;
    private final Map<String, Thread> monitorThread;

    public MonitorService(Logger logger, Map<String, Thread> monitorThread) {
        this.monitorThread = monitorThread;
        this.isRun = true;
        this.logger = logger;
    }

    public void run() {
        while (isRun) {
            logger.info("Running monitor thread service {} ", System.currentTimeMillis());
            for (String key : monitorThread.keySet()) {
                try {
                    final Thread thread = monitorThread.get(key);
                    final boolean isAlive = thread.isAlive();
                    if (isAlive) {
                        logger.info("Thread {} is running at {} ", thread.getName(), System.currentTimeMillis());
                    } else {
                        logger.info("Thread {} is dead at {} ",thread.getName(), System.currentTimeMillis());
                        try {
                            logger.info("Thread {} is interrupted", thread.getName());
                        } catch (Exception ex) {
                            LogUtils.LOGGER.error("Error = {}", ex.getMessage(), ex);
                        }
                    }
                } catch (Exception e) {
                    LogUtils.LOGGER.error("Error at {} with message = {}", MonitorService.class.getSimpleName(), e.getMessage(), e);
                }
            }
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
    }
}