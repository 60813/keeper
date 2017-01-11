package com.keeper.client;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.keeper.client.event.EventConsumerPool;
import com.keeper.client.listener.KeeperChildListener;
import com.keeper.client.listener.KeeperNodeListener;
import com.keeper.client.listener.KeeperStateListener;
/**
 *@author huangdou
 *@at 2016年11月28日下午3:25:55
 *@version 0.0.1
 */
public class KeeperWatcher implements Watcher{
	private final static Logger LOGGER = LoggerFactory.getLogger(KeeperWatcher.class);
	private KeeperClient client ;
	
	private EventConsumerPool pool ;
	
	private CopyOnWriteArraySet<KeeperStateListener> keeperStateListeners = new CopyOnWriteArraySet<KeeperStateListener>();
	private ConcurrentHashMap<String,Set<KeeperNodeListener>> keeperNodeListeners = new ConcurrentHashMap<String,Set<KeeperNodeListener>>();
	private ConcurrentHashMap<String,Set<KeeperChildListener>> keeperChildListeners = new ConcurrentHashMap<String,Set<KeeperChildListener>>();

	private Lock eventLock = new ReentrantLock();
	private Condition condition = eventLock.newCondition();
	public Lock getEventLock(){
		return eventLock;
	}
	public Condition getCondition(){
		return condition;
	}
	
	public EventConsumerPool getPool(){
		return pool ;
	}
	public Set<KeeperChildListener> getChildListeners(String path){
		return keeperChildListeners.get(path);
	}
	
	public Set<KeeperNodeListener> getNodeListeners(String path){
		return keeperNodeListeners.get(path);
	}
	
	public boolean PathListenning(String path){
		Set<KeeperChildListener> ls1 = getChildListeners(path);
		Set<KeeperNodeListener> ls2 = getNodeListeners(path);
		return (ls1!=null&&!ls1.isEmpty()) || (ls2!=null&&!ls2.isEmpty());
	}
	public CopyOnWriteArraySet<KeeperStateListener> registKeeperStateListener(KeeperStateListener keeperStateListener){
		synchronized (keeperStateListeners) {
			keeperStateListeners.add(keeperStateListener);
		}
		return keeperStateListeners;
	}
	
	public synchronized Set<KeeperNodeListener> registKeeperNodeListener(String path ,KeeperNodeListener keeperNodeListener){
		Set<KeeperNodeListener> tempSet ;
		synchronized (keeperNodeListeners) {
			tempSet = keeperNodeListeners.get(path);
			if (tempSet == null){
				tempSet = new CopyOnWriteArraySet<KeeperNodeListener>();
				keeperNodeListeners.put(path, tempSet);
			}
			tempSet.add(keeperNodeListener);
		}
		installNodeWatcher(path);
		return tempSet;
	}
	
	public Set<KeeperChildListener> registKeeperChildListener(String path ,KeeperChildListener keeperChildListener){
		Set<KeeperChildListener> tempSet ;
		synchronized (keeperChildListeners) {
			tempSet = keeperChildListeners.get(path);
			if (tempSet == null){
				tempSet = new CopyOnWriteArraySet<KeeperChildListener>();
				keeperChildListeners.put(path, tempSet);
			}
		}
		tempSet.add(keeperChildListener);
		installChildrenWatcher(path);
		return tempSet;
	}
	
	public void fireAllChildListener(){
		for (Entry<String,Set<KeeperChildListener>> entry : keeperChildListeners.entrySet()){
			String path = entry.getKey();
			fireChildListener(path);
		}
	}
	public void fireChildListener(String path){
		Set<KeeperChildListener> listeners = keeperChildListeners.get(path);
		if (listeners == null || listeners.isEmpty()){
			return ;
		}
		
		boolean exist = client.exist(path);
		List<String> children = client.getChildren(path);
		for (KeeperChildListener listener : listeners){
			if (!exist){
				listener.onParentDelete(path);
			}else {
				listener.onChild(path, children);
			}
		}
	}
	
	public void fireAllNodeListener(){
		for (Entry<String,Set<KeeperNodeListener>> entry : keeperNodeListeners.entrySet()){
			String path = entry.getKey();
			fireNodeListener(path);
		}
	}
	
	public void installNodeWatcher(String path){
		client.exist(path,true);
	}
	
	public void installChildrenWatcher(String path){
		client.exist(path,true);
		client.getChildren(path);
	}
	
	
	public void fireNodeListener(String path){
		Set<KeeperNodeListener> listeners = keeperNodeListeners.get(path);
		if (listeners == null || listeners.isEmpty()){
			return ;
		}
		
		boolean exist = client.exist(path);
		for (KeeperNodeListener listener : listeners){
			if (!exist){
				listener.onDelete(path);
			}else {
				try{
					byte[] bytes = client.read(path,true);
					listener.onData(path, bytes);
				}catch(Exception e){
					if (e instanceof KeeperException.NoNodeException){
						listener.onDelete(path);
					}
				}
			}
		}
	}
	public KeeperWatcher(KeeperClient client) {
		this.client = client ;
		pool = new EventConsumerPool(client.getConcurrentProcessNum(),this);
	}

	public void process(WatchedEvent event) {
		LOGGER.debug("event" + event);
		getEventLock().lock();
		try{
			if (client.isShutDown()){
				return ;
			}
			if (event.getPath()!=null && !"".equals(event.getPath().trim()) ){
				processNodeEvent(event);
			}else {
				switch (event.getState()) {
				case SyncConnected:
					client.connected();
					getCondition().signalAll();
					break;
				case Expired :
					client.reconnect();
					fireAllNodeListener();
					fireAllChildListener();
					break;
				default:
					break;
				}
			}
		}finally{
			getEventLock().unlock();
		}
	}
	
	private void processNodeEvent(WatchedEvent event){
		pool.submit(event);
	}
}


