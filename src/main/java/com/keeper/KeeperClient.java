package com.keeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import com.keeper.exception.KeeperException;
import com.keeper.listener.KeeperChildListener;
import com.keeper.listener.KeeperNodeListener;
import com.keeper.listener.KeeperStateListener;

/**
 * @author huangdou
 * @at 2016年11月28日下午3:12:29
 * @version 0.0.1
 */
public class KeeperClient implements IKeeperClient {

	private ZooKeeper zk;

	private KeeperWatcher watcher;

	private int sessionTimeout;

	private int connectTimeout;

	private String connectString;

	private AtomicBoolean connected = new AtomicBoolean();

	private int concurrentProcessNum;

	public int getConcurrentProcessNum() {
		return concurrentProcessNum;
	}

	public void setConcurrentProcessNum(int concurrentProcessNum) {
		this.concurrentProcessNum = concurrentProcessNum;
	}

	public boolean isClientConnected() {
		return connected.get();
	}

	public void connected() {
		connected.set(true);
	}

	public void disconnected() {
		connected.set(false);
	}

	private Semaphore connectionLock = new Semaphore(0);

	public void releaseConnectionLock() {
		connectionLock.release();
	}

	public KeeperClient(String connectString, int sessionTimeout,
			int connectTimeout) {
		this(connectString, sessionTimeout, connectTimeout,
				DEFAULT_CONCURRENT_PROCESS);
	}

	public KeeperClient(String connectString, int sessionTimeout,
			int connectTimeout, int concurrentProcessNum) {
		this.sessionTimeout = sessionTimeout;
		this.connectTimeout = connectTimeout;
		this.connectString = connectString;
		this.concurrentProcessNum = concurrentProcessNum;
		watcher = new KeeperWatcher(this);
		connect();
	}

	public KeeperClient() {
		this(DEFAULT_CONNECTION_STRING);
	}

	public KeeperClient(String connectString) {
		this(connectString, DEFAULT_SESSION_TIMEOUT);
	}

	public KeeperClient(String connectString, int sessionTimeout) {
		this(connectString, sessionTimeout, DEFAULT_CONNECT_TIMEOUT);
	}

	public synchronized void reconnect() {
		closeConnection();
		connect();
	}
	
	
	public void closeClient(){
		//TODO:close pool and release resource
		closeConnection();
	}

	public synchronized void closeConnection() {
		if (zk != null) {
			try {
				zk.close();
				zk = null;
			} catch (InterruptedException e) {
				throw new KeeperException(e);
			}
		}
		this.disconnected();
	}

	private synchronized void connect() {
		try {
			if (connected.get()) {
				throw new KeeperException("Keeper Has been connected.");
			}
			zk = new ZooKeeper(connectString, sessionTimeout, watcher);
			if (!connectionLock.tryAcquire(connectTimeout,
					TimeUnit.MILLISECONDS)) {
				if (zk != null) {
					zk.close();
				}
				throw new KeeperException(
						String.format(
								"[TIMEOUT] unable to connect to ZK %d within %d milliseconds ",
								connectString, connectTimeout));
			}
			connected.set(true);
		} catch (IOException e) {
			throw new KeeperException(e);
		} catch (InterruptedException e) {
			throw new KeeperException(e);
		}
	}

	public boolean exist(String path) {
		try {
			return (zk.exists(path, this.watcher.PathListenning(path)) != null);
		} catch (Exception e) {
			throw new KeeperException(e);
		}
	}
	
	protected boolean exist(String path,boolean watch){
		try {
			return zk.exists(path, watch) != null;
		} catch (Exception e) {
			throw new KeeperException(e);
		}
	}

	public String create(String path, byte[] bytes) {
		return create(path, bytes, CreateMode.PERSISTENT);
	}

	public String create(String path, byte[] bytes, CreateMode createMode) {
		try {
			return zk.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,
					createMode);
		} catch (Exception e) {
			throw new KeeperException(e);
		}
	}

	public String createWtihParent(String path) {
		try {
			create(path, null, CreateMode.PERSISTENT);
		} catch (KeeperException e) {
			if (e.getProto() instanceof NoNodeException) {
				String parentDir = path.substring(0, path.lastIndexOf('/'));
				createWtihParent(parentDir);
				createWtihParent(path);
			}
		} catch (Exception e) {
			throw new KeeperException(e);
		}
		return path ;
	}

	public byte[] read(String path) {
		return read(path, false);
	}

	public byte[] read(String path, boolean watch) {
		try {
			return zk.getData(path, watch, null);
		} catch (Exception e) {
			throw new KeeperException(e);
		}
	}

	public void update(String path, byte[] bytes) {
		try {
			zk.setData(path, bytes, -1);
		} catch (Exception e) {
			throw new KeeperException(e);
		}
	}

	public boolean delete(String path) {
		try {
			zk.delete(path, -1);
			return true;
		} catch (NoNodeException e) {
			return false;
		} catch (Exception e) {
			throw new KeeperException(e);
		}
	}

	public boolean deleteRecurse(String path) {
		List<String> children = getChildren(path);
		if (children != null && !children.isEmpty()) {
			for (String child : children) {
				deleteRecurse(path + "/" + child);
			}
		}
		return delete(path);
	}

	public List<String> getChildren(String parent) {
		try {
			return zk.getChildren(parent, this.watcher.PathListenning(parent));
		} catch (org.apache.zookeeper.KeeperException.NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new KeeperException(e);
		}
	}

	public void listenNode(String path, KeeperNodeListener keeperNodeListener) {
		watcher.registKeeperNodeListener(path, keeperNodeListener);
	}

	public void listenChild(String path, KeeperChildListener keeperChildListener) {
		watcher.registKeeperChildListener(path, keeperChildListener);
	}

	public void listenState(KeeperStateListener keeperStateListener) {
		watcher.registKeeperStateListener(keeperStateListener);
	}

}
