package com.keeper.event;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeCreated;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;
import static org.apache.zookeeper.Watcher.Event.EventType.NodeDataChanged;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.WatchedEvent;

import com.keeper.KeeperWatcher;

/**
 *@author huangdou
 *@at 2016年12月1日上午9:25:50
 *@version 0.0.1
 */
public class EventConsumerPool {
	
	private ExecutorService pool = null;
	
	private KeeperWatcher watcher ;
	public EventConsumerPool(int concurrentEventNum,KeeperWatcher watcher) {
		this.watcher = watcher;
		pool = Executors.newFixedThreadPool(concurrentEventNum, EventConsumerFactory.getFactory());
	}

	public void submit(final WatchedEvent event){
		pool.submit(new Runnable() {
			public void run() {
				if (NodeCreated == event.getType() || NodeDeleted == event.getType() || NodeChildrenChanged == event.getType()){
					watcher.fireChildListener(event.getPath());
				}
				if (NodeCreated == event.getType() || NodeDeleted == event.getType() || NodeDataChanged == event.getType()){
					watcher.fireNodeListener(event.getPath());
				}
			}
		});
	}
}

class EventConsumerFactory implements ThreadFactory{

	private static EventConsumerFactory eventConsumerFactory ;
	AtomicInteger id  = new AtomicInteger(0);
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setName("EventConsumer-"+id.addAndGet(1));
		return t;
	}
	
	public static EventConsumerFactory getFactory(){
		if (eventConsumerFactory == null){
			synchronized (EventConsumerFactory.class) {
				if (eventConsumerFactory == null){
					eventConsumerFactory = new EventConsumerFactory();
				}
			}
		}
		return eventConsumerFactory;
	}
}


