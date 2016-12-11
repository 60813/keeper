package com.keeper.expansion;

import java.util.concurrent.TimeUnit;

import com.keeper.client.KeeperClient;

/**
 *@author huangdou
 *@at 2016年12月11日下午9:15:32
 *@version 0.0.1
 */
public class KeeperSimpleSemaphore implements KeeperSemaphore {
	private String name ;
	
	private int permits ;
	
	private boolean fair ;
	
	private KeeperClient client;

	public void init(){
		
	}
	
	/**
     * Creates a {@code Semaphore} with the distreibuted name and the given number of
     * permits and nonfair fairness setting.
     *
     * @param name the global unique name in distributed
     * @param permits the initial number of permits available.
     *        This value may be negative, in which case releases
     *        must occur before any acquires will be granted.
     * @throws SemaphoreException throw an SemaphoreException if you are trying to create the KeeperSemaphore
     * 		   when the Semaphore already exits with this name in distributed
     */
	
	public KeeperSimpleSemaphore(String name ,int permits,KeeperClient client) throws SemaphoreException {
	}
	
	/**
     * Creates a {@code Semaphore} with the given number of
     * permits and the given fairness setting.
     *
     * @param permits the initial number of permits available.
     *        This value may be negative, in which case releases
     *        must occur before any acquires will be granted.
     * @param fair {@code true} if this semaphore will guarantee
     *        first-in first-out granting of permits under contention in distributed,
     *        else {@code false}
     * @throws SemaphoreException throw an SemaphoreException if you are trying to create the KeeperSemaphore
     * 		   when the Semaphore already exits with this name in distributed
     */
	public KeeperSimpleSemaphore(String name,int permits, boolean fair) throws SemaphoreException {
    }
	
	/**
	 * get the KeeperSemaphore with the name in distributed, 
	 * or create if KeeperSemaphore not exist in distributed
	 * 
	 * @param name the KeeperSemaphore you want to get or create
	 * @param permits the initial number of permits available.
     *        This value may be negative, in which case releases
     *        must occur before any acquires will be granted.
     * @param fair {@code true} if this semaphore will guarantee
     *        first-in first-out granting of permits under contention in distributed,
     *        else {@code false}
     * @throws SemaphoreException throw an SemaphoreException if you are trying to get When it already exists and holds a different permits or fair
	 * */
	public static KeeperSimpleSemaphore getOrCreate(String name,int permits,boolean fair,KeeperClient client){
		return null;
	}

	@Override
	public void acquire() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean tryAcquire() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tryAcquire(long timeout, TimeUnit unit)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

}
