# KEEPER
基于[zookeeper](http://zookeeper.apache.org)实现的分布式开发客户端

- [KEEPER](#keeper)
    - [FEATURES](#features)
    - [MAVEN-DEPENDENCY](#maven-dependency)
    - [USAGE](#usage)
    - [DEMO](#demo)
    	- [CLIENT-CRUD](#client-crud)
	- [CLIENT-LISTEN](#client-listen)
	- [MutexLock](#mutexlock)
	- [Semaphore](#semaphore)


## FEATURES
* client
实现zookeeper watch，session过期处理，断线重连
* expansion
实现分布式锁(互斥锁)，分布式信号量(samephore),分布式闭锁(CountDownLatch)等

## MAVEN-DEPENDENCY
    <dependency>
        <groupId>com.60813</groupId>
	      <artifactId>keeper</artifactId>
	      <version>0.0.1-SNAPSHOT</version>
    </dependency>
    
## USAGE
	//构造
	KeeperClient client = new KeeperClient("127.0.0.1:2181");
	
	//关闭	
	client.closeClient();
	
	//create
	String create(String path,byte[] bytes);
	//createMode 指定创建临时节点或者持久节点，或者序列节点
	String create(String path, byte[] bytes,CreateMode createMode);
	//create 自动创建父亲节点
	String createWtihParent(String path);
	
	//read
	byte[] read(String path);
	
	//update
	void update(String path,byte[] bytes);
	
	//delete	
	boolean delete(String path);
	//delete 含子节点
	boolean deleteRecurse(String path);
	
	//getChildren
	List<String> getChildren(String parent);
	//getSortedChildren 用于子节点全部为int名称的默认排序
	List<String> getSortedChildren(String parent) ;
	//getSortedChildren 传入指定比较器
	List<String> getSortedChildren(String parent,Comparator<String> comparator);
	
	//监听数据变化，增，删；传入指定listener,当变化发生时事件回调	
	void listenNode(String path,KeeperNodeListener keeperNodeListener);
	
	//监听子节点增删以及本节点增删；传入指定listener,当变化发生时事件回调
	void listenChild(String path,KeeperChildListener keeperChildListener);
	
	//构造分布式锁（互斥可重入），name是锁名称		
	final KeeperLock lock = new KeeperMutexLock("testlocka", client);
	//加锁	
	lock.lock();
	//解锁
	lock.unlock();
	
	//构造分布式信号量，name是名称，共享信号量的线程使用同一名称，int为最大许可总数，一经创建不可修改	
	KeeperSemaphore semaphore = KeeperSimpleSemaphore.getOrCreate("semaphore2", 3, client);
	//请求许可，无可用许可会阻塞线程，当然也可以使用tryAcquire直接返回			
	semaphore.acquire();
	//返还许可，若不主动返还，则本许可将一直被该线程占用，直到client离线
	semaphore.release();
	
	
## DEMO
    KeeperClient client = new KeeperClient("127.0.0.1:2181");

### CLIENT-CRUD

	static String testPath =  "/hworld";
	static String testData  = "hello world";
    	@Test
    	public void testCreate(){
      		client.create(testPath, testData.getBytes());
      		Assert.assertTrue(testData .equals( new String(client.read(testPath))));
    	}
	
	@Test
	public void testRead(){
		client.create(testPath, testData.getBytes());
		Assert.assertTrue(testData .equals( new String(client.read(testPath))));
	}
	
	@Test
	public void testGetChildren(){
		client.createWtihParent("/A/B/C");
		List<String> children = client.getChildren("/A");
		Assert.assertArrayEquals(children.toArray(), new Object[]{"B"});
	}
	
	@Test
	public void testUpdate(){
		client.create(testPath, testData.getBytes());
		client.update(testPath, "A".getBytes());
		Assert.assertEquals("A", new String(client.read(testPath)));
	}
	
	@Test
	public void testDelete(){
		client.create(testPath, testData.getBytes());
		client.delete(testPath);
		Assert.assertTrue(!client.exist(testPath));
	}
	
	public void testDeleteRecurse(){
		if (client.exist(testPath)){
			client.deleteRecurse(testPath);
		}
	}
	
### CLIENT-LISTEN
	static String testPath =  "/hworld";
	static String testData  = "hello world";
	@Test
	public void testNodeListen() throws InterruptedException{
		final AtomicInteger onDataCall = new AtomicInteger();
		final AtomicInteger onDeleteCall = new AtomicInteger();
		client.listenNode(testPath, new KeeperNodeListener() {
			public void onDelete(String path) {
				System.out.println(testPath + " is deleted");
				onDeleteCall.addAndGet(1);
			}
			
			public void onData(String path, byte[] bytes) {
				System.out.println(testPath + " is created or updated with the data : " + new String(bytes));
				onDataCall.addAndGet(1);
			};
		});
		
		client.create(testPath, testData.getBytes());
		Thread.sleep(100);
		client.update(testPath, (testData+"xx").getBytes());
		Thread.sleep(100);
		client.delete(testPath);
		Thread.sleep(100);
		Assert.assertEquals(onDataCall.get(), 2);
		Assert.assertEquals(onDeleteCall.get(), 1);
	}
	
	@Test
	public void testChildListen() throws InterruptedException{
		final AtomicInteger onChildCall = new AtomicInteger();
		client.listenChild(testPath, new KeeperChildListener() {
			public void onParentDelete(String path) {
				System.out.println("parent " + path +" was been deleted!");
			}
			
			public void onChild(String parent, List<String> subs) {
				System.out.println("child added or removed : " + subs);
				onChildCall.addAndGet(1);
			}
		});
		
		client.delete(testPath);
		Thread.sleep(1000);
		client.create(testPath, testData.getBytes());
		Thread.sleep(1000);
		client.create(testPath+"/"+"A", testData.getBytes());
		Thread.sleep(1000);
		client.create(testPath+"/"+"B", testData.getBytes());
		Thread.sleep(1000);
		client.delete(testPath+"/"+"B");
		Thread.sleep(1000);
		client.deleteRecurse(testPath);
		Thread.sleep(1000);
	}

### MutexLock
	final KeeperLock lock1 = new KeeperMutexLock("testlocka", client);
	new ReentrantLockThread(lock1,"lock1a").start();
	new ReentrantLockThread(lock1,"lock1b").start();
	new ReentrantLockThread(lock1,"lock1c").start();
	
	class ReentrantLockThread extends Thread{
		private KeeperLock lock ;
		public ReentrantLockThread(KeeperLock lock,String name) {
			this.lock = lock;
			this.setName(name);
		}

		@Override
		public void run() {
			try {
				lock.lock();
				System.out.println(Thread.currentThread().getName()+"  locked 1 ");
			} catch (InterruptedException e1) {
				return ;
			}
			try {
				lock.lock();
				System.out.println(Thread.currentThread().getName()+"  locked 2 ");
				lock.unlock();
				System.out.println(Thread.currentThread().getName()+"  release 1 ");
				//观察该线程占用锁5秒				
				Thread.sleep(5000);
				lock.unlock();
				System.out.println(Thread.currentThread().getName()+"  release 2 ");
			} catch (InterruptedException e) {
			}
		}
		
	}
### Semaphore
		//观察给出3个许可，允许3个线程同时执行		
		KeeperSemaphore semaphore = KeeperSimpleSemaphore.getOrCreate("semaphore2", 3, client);
		for (int i=1;i<=16;i++){
			Thread myThread = new MyThread(i+"", semaphore);
			myThread.start();
		}
		
		class MyThread extends Thread{
		KeeperSemaphore semaphore ;
		public MyThread(String name, KeeperSemaphore semaphore) {
			this.setName(name);
			this.semaphore = semaphore;
		}
		
		@Override
		public void run() {
			try {
				semaphore.acquire();
				System.out.println(Thread.currentThread().getName() + " got a release ,availablePermits is " +semaphore.availablePermits());
				//观察sleep一段时间，在release之前，许可不会被其他线程拿到				
				Thread.sleep(new Random().nextInt(1000));
				semaphore.release();
				System.out.println(Thread.currentThread().getName() + " returnd a release ,availablePermits is " +semaphore.availablePermits());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	} 
		

## Contributor
* huangdou
