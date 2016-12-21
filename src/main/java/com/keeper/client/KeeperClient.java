package com.keeper.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.keeper.client.exception.KeeperException;
import com.keeper.client.listener.KeeperChildListener;
import com.keeper.client.listener.KeeperNodeListener;
import com.keeper.client.listener.KeeperStateListener;

/**
 * @author huangdou
 * @at 2016年11月28日下午3:12:29
 * @version 0.0.1
 */
public class KeeperClient implements IKeeperClient {
	Logger logger = LoggerFactory.getLogger(KeeperClient.class);
	private ZooKeeper zk;

	private KeeperWatcher watcher;

	private int sessionTimeout;

	private int connectTimeout;

	private String connectString;

	private AtomicBoolean connected = new AtomicBoolean();
	
	private volatile boolean shutdonwClient = false;

	private int concurrentProcessNum;
	
	public boolean isShutDown(){
		return shutdonwClient;
	}
	public void setClientIhutDown(boolean down){
		shutdonwClient = down;
	}
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
		watcher.getEventLock().lock();
		try{
			closeConnection();
			connect();
		}finally{
			watcher.getEventLock().unlock();
		}
		
	}
	
	
	public void closeClient(){
		watcher.getEventLock().lock();
		try{
			closeConnection();
			watcher.getPool().shutdownPool();
			setClientIhutDown(true);
		}finally{
			watcher.getEventLock().unlock();
		}
		
	}

	public synchronized void closeConnection() {
		watcher.getEventLock().lock();
		try{
			if (zk != null) {
				try {
					zk.close();
					zk = null;
				} catch (InterruptedException e) {
					throw new KeeperException(e);
				}
			}
			this.disconnected();
		}finally{
			watcher.getEventLock().unlock();
		}
	}

	private synchronized void connect() {
		try {
			watcher.getEventLock().lock();
			if (connected.get()) {
				throw new KeeperException("Keeper Has been connected.");
			}
			zk = new ZooKeeper(connectString, sessionTimeout, watcher);
			if (!watcher.getCondition().await(connectTimeout,TimeUnit.MILLISECONDS)) {
//			if (!connectionLock.tryAcquire(connectTimeout,TimeUnit.MILLISECONDS)) {	
				if (zk != null) {
					zk.close();
				}
				String msg = String.format(
						"[TIMEOUT] unable to connect to ZK %d within %d milliseconds ",
						connectString, connectTimeout);
				logger.error(msg);
				throw new KeeperException(msg);
			}
			connected.set(true);
		} catch (Exception e) {
			throw new KeeperException(e);
		}finally{
			watcher.getEventLock().unlock();
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
	
	@Override
	public String createSequential(String path, byte[] bytes) {
		return create(path, bytes, CreateMode.PERSISTENT_SEQUENTIAL);
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
	
	public List<String> getSortedChildren(String parent) {
		return getSortedChildren(parent, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.parseInt(o1)-Integer.parseInt(o2);
			}
		});
	}
	
	public List<String> getSortedChildren(String parent,Comparator<String> comparator){
		List<String> children  = getChildren(parent);
		if (children != null && children.size() > 1){
			Collections.sort(children, comparator);
		}
		return children ;
	}

	public void listenNode(String path, KeeperNodeListener keeperNodeListener) {
		watcher.registKeeperNodeListener(path, keeperNodeListener);
	}

	public void listenChild(String path, KeeperChildListener keeperChildListener) {
		watcher.registKeeperChildListener(path, keeperChildListener);
	}

	public void listenState(KeeperStateListener keeperStateListener) {
		throw new KeeperException("not supported!");
//		watcher.registKeeperStateListener(keeperStateListener);
	}

}
