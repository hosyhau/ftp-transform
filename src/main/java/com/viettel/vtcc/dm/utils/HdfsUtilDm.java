package com.viettel.vtcc.dm.utils;

import org.apache.hadoop.hdfs.server.namenode.ha.proto.HAZKInfoProtos;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by anhvu on 12/1/16.
 */
public class HdfsUtilDm {
    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsUtilDm.class);

    /**
     * @param zkHostList           Example: dm02.nfw.vn:2181,dm03.nfw.vn:2181,dm04.nfw.vn:2181
     * @param defaultNameNode      Example: dm03.nfw.vn
     * @param sessionTimeoutMillis session timeout in milliseconds. Recommend 2000ms
     * @return nameNode, return default if can't get from zookeeper. Example: dm03.nfw.vn
     */
    public static String getActiveNameNode(String zkHostList, String defaultNameNode, int sessionTimeoutMillis) {
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = null;
        System.out.println("Start connect to zookeeper");
        try {
            zk = new ZooKeeper(zkHostList, sessionTimeoutMillis, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected || watchedEvent.getState() ==
                            Event.KeeperState.Expired) {
                        connectedSignal.countDown();
                    }
                }
            });
            connectedSignal.await();
            LOGGER.info("Zookeeper connection state {}", zk.getState().toString());
            System.out.println("Zookeeper connection state" + zk.getState().toString());
            if (zk.getState().isConnected()) {
                zk.getChildren("/hadoop-ha", false);
                LOGGER.info("Getting name node from zookeeper");
                System.out.println("Getting name node from zookeeper");
                List<String> children = zk.getChildren("/hadoop-ha", false);
                if (children.size() > 0) {
                    byte[] data = zk.getData("/hadoop-ha/" + children.get(0) + "/ActiveBreadCrumb", true, null);
                    HAZKInfoProtos.ActiveNodeInfo node = HAZKInfoProtos.ActiveNodeInfo.parseFrom(data); //HAZKInfoProtos.ActiveNodeInfo.parseFrom(data);
                    String nameNodeActive = node.getHostname();
                    System.out.println("Active name node from zookeeper: " + nameNodeActive);
                    return nameNodeActive;
                }
            }
        } catch (IOException | KeeperException | InterruptedException e) {
            connectedSignal.countDown();
            e.printStackTrace();
        } finally {
            connectedSignal.countDown();
            if (zk != null) try {
                LOGGER.info("Closing zookeeper connection");
                System.out.println("Closing zookeeper connection");
                zk.close();
                LOGGER.info("Close zookeeper connection successful");
                System.out.println("Close zookeeper connection successful");
            } catch (InterruptedException e) {
                LOGGER.info("Failed to close zookeeper connection");
                System.out.println("Failed to close zookeeper connection");
                e.printStackTrace();
            }
        }
        return defaultNameNode;
    }

    public static void main(String[] args) {
        String activeNameNode = HdfsUtilDm.getActiveNameNode("zk01.cbs.vn:2181,zk02.cbs.vn:2181", "nn01.cbs.vn", 2000);
        System.out.println(activeNameNode);
    }
}
