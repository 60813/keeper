package com.keeper.server;

import java.io.File;
import java.io.IOException;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.keeper.core.ZKUtils;

/**
 *@author huangdou
 *@at 2017年1月12日上午9:25:56
 *@version 0.0.1
 */
public class KeeperSimpleServer {
	Logger LOG = LoggerFactory.getLogger(KeeperSimpleServer.class);
	private final int minSessionTimeout;
	
	private final String snapdir;

    private final String datadir;
    
    private final int port;

    private final int tickTime;

	public KeeperSimpleServer(String snapdir, String datadir, int port, int tickTime, int minSessionTimeout) {
		this.minSessionTimeout = minSessionTimeout;
		this.snapdir = snapdir;
        this.datadir = datadir;
        this.port = port;
        this.tickTime = tickTime;
	}
	
	public static final int DEFAULT_PORT = 2181;

    public static final int DEFAULT_TICK_TIME = 5000;

    public static final int DEFAULT_MIN_SESSION_TIMEOUT = 2 * DEFAULT_TICK_TIME;
	
    
	public void startZkServer() {
		
        if (ZKUtils.isPortFree(port)) {
            final File dataDir = new File(datadir);
            final File dataLogDir = new File(snapdir);
            dataDir.mkdirs();
            dataLogDir.mkdirs();

            // single zk server
            LOG.info("Start single zookeeper server...");
            LOG.info("data dir: " + dataDir.getAbsolutePath());
            LOG.info("data log dir: " + dataLogDir.getAbsolutePath());
            startSingleZkServer(tickTime, dataDir, dataLogDir, port);
        } else {
            throw new IllegalStateException("Zookeeper port " + port + " was already in use.");
        }
    }

	private ZooKeeperServer zookeeperServer;
	private ServerCnxnFactory nioFactory;
	
	 public KeeperSimpleServer(String dataDir, String logDir) {
	        this(dataDir, logDir, DEFAULT_PORT);
	    }

	    public KeeperSimpleServer(String dataDir, String logDir, int port) {
	        this(dataDir, logDir, port, DEFAULT_TICK_TIME);
	    }

	    public KeeperSimpleServer(String dataDir, String logDir, int port, int tickTime) {
	        this(dataDir, logDir, port, tickTime, DEFAULT_MIN_SESSION_TIMEOUT);
	    }
	    
    private void startSingleZkServer(final int tickTime, final File dataDir, final File dataLogDir, final int port) {
        try {
        	zookeeperServer = new ZooKeeperServer(dataDir, dataLogDir, tickTime);
        	zookeeperServer.setMinSessionTimeout(minSessionTimeout);
        	nioFactory = ServerCnxnFactory.createFactory(port, 60);
        	nioFactory.startup(zookeeperServer);
        } catch (Exception e) {
            throw new RuntimeException("Unable to start simple ZooKeeper server.", e);
        } 
    }
    
    public void shutdown() {
        ZooKeeperServer zk = zookeeperServer;
        if (zk == null) {
            LOG.warn("shutdown duplication");
            return;
        }else {
        	zookeeperServer = null;
        }
        LOG.info("Shutting down ZkServer...");
        if (nioFactory != null) {
        	nioFactory.shutdown();
            try {
            	nioFactory.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            nioFactory = null;
        }
        zk.shutdown();
        if (zk.getZKDatabase() != null) {
            try {
                // release file description
                zk.getZKDatabase().close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        LOG.info("Shutting down ZkServer...done");
    }

}
