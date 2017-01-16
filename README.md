# KEEPER
[![Build Status](https://travis-ci.org/60813/keeper.svg?branch=master)](https://travis-ci.org/60813/keeper)
基于[zookeeper](http://zookeeper.apache.org)实现的分布式开发客户端

* [KEEPER](#keeper)
	* [FEATURES](#features)
	* [MAVEN-DEPENDENCY](#maven-dependency)
	* [USAGE](#usage)
	* [DEMO](#demo)
		* [CLIENT-CRUD](#client-crud)
		* [CLIENT-LISTEN](#client-listen)
		* [MutexLock](#mutexlock)
		* [Semaphore](#semaphore)
		* [CountDownLatch](#countdownlatch)
	* [CONTRIBUTOR](#contributor)
			


## FEATURES
* client
实现zookeeper watch，session过期处理，断线重连
* expansion
实现分布式锁(互斥锁)，分布式信号量(samephore),分布式闭锁(CountDownLatch)等

## MAVEN-DEPENDENCY
```Java
    <dependency>
        <groupId>com.keeper</groupId>
	      <artifactId>keeper</artifactId>
	      <version>0.0.1-SNAPSHOT</version>
    </dependency>
 ```
 
## USAGE
```Java
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
	//create 字符串数据
	String createStr(String path,String data,CreateMode createMode);
	//create 对象数据
	String createObject(String path,Object t,CreateMode createMode);
	
	//read
	byte[] read(String path);
	//read 读取字符串数据
	String readStr(String path);
	//read 读取对象数据
	<T> T readObject(String path,Class<T> clazz);
	
	//update
	void update(String path,byte[] bytes);
	//update 更新字符串数据
	void updateStr(String path,String data);
	//update 更新对象数据
	void updateObject(String path,Object data);
	
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
	
	//监听数据变化，删；传入指定listener,当变化发生时事件回调	
	void listenNode(String path,KeeperNodeListener keeperNodeListener);
	
	//监听子节点增删以及本节点删除；传入指定listener,当变化发生时事件回调
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
	
	//构造分布式latch，name是名称，count为需要创建的count数值，注意如果该name标识的latch已经存在的话，则不会重新创建，使用已有count
	KeeperCountDownLatch latch = KeeperCountDownLatch.getOrCreate("latch02", 2, client) ;
	//等待count变为0
	latch.await();
	//count -1变为0后继续countDown将无实际变化
	latch.countDown();
	//销毁latch,销毁后其他阻塞的线程将不再阻塞,后续继续对latch执行await,countdown等操作将抛出异常。
	latch.destroy();
```	
	
## DEMO
```Java
    KeeperClient client = new KeeperClient("127.0.0.1:2181");
```

### CLIENT-CRUD
```Java
	static String testPath =  "/hworld";
	static String testData  = "hello world";
    	@Test
    	public void testCreate(){
      		client.create(testPath, testData.getBytes());
      		Assert.assertTrue(testData .equals( new String(client.read(testPath))));
    	}
	
	@Test
	public void testCreateStr(){
		String str = "hello World! \\n ****";
		client.createStr(testPath, str,CreateMode.PERSISTENT);
		Assert.assertTrue(str .equals(client.readStr(testPath)));
	}
	
	@Test
	public void testCreateObject(){
		Person person = new Person();
		person.setAge(20);
		person.setJoinDate(new Date());
		person.setJoinTheTeam(true);
		person.setJoinTime(System.currentTimeMillis());
		person.setName("张三");
		client.createObject(testPath, person,CreateMode.PERSISTENT);
		Person p = client.readObject(testPath, Person.class);
		System.out.println(p);
		Assert.assertTrue(person.getName().equals(p.getName()));
		Assert.assertTrue(person.getAge()==p.getAge());
		Assert.assertTrue(person.getJoinDate().getTime()==p.getJoinDate().getTime());
		Assert.assertTrue(person.getJoinTime()==p.getJoinTime());
		Assert.assertTrue(p.isJoinTheTeam());
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
```

### CLIENT-LISTEN
```Java
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
```

### MutexLock
```Java
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
```

### Semaphore
```Java
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
```	

### CountDownLatch
```Java
	@Test
	public void testLock() throws InterruptedException, SemaphoreException {
		KeeperCountDownLatch latch = KeeperCountDownLatch.getOrCreate("latch02", 2, client) ;
		//一直等到countdown到0的线程1
		MyThread t1 = new MyThread(latch, "waitCountDownThread1", true);
		//指定较长时间，时间未到而countdown变为0，最终返回true的线程
		MyThread t2 = new MyThread(latch, "waitTimeDownThreadTrue", false);
		t2.setWaitMilliseconds(10000);
		//指定较短时间，时间到而countdown未变为0，最终返回false的线程
		MyThread t3 = new MyThread(latch, "waitTimeDownThreadFalse", false);
		t3.setWaitMilliseconds(3000);
		//一直等到countdown到0的线程2
		MyThread t4 = new MyThread(latch, "waitCountDownThread2", true);
		
		t1.start();t2.start();t3.start();t4.start();
		Thread.sleep(5000);
		latch.countDown();
		System.out.println("count down 1");
		latch.countDown();
		System.out.println("count down 2");
		Thread.sleep(5000l);
	}
	
	class MyThread extends Thread{
		private KeeperCountDownLatch keeperCountDownLatch ;		
		boolean waitUtilCountDown ;		
		private int waitMilliseconds ;
		
		public void setWaitMilliseconds(int waitMilliseconds) {
			this.waitMilliseconds = waitMilliseconds;
		}

		public MyThread(KeeperCountDownLatch keeperCountDownLatch,String name,boolean waitUtilCountDown) {
			this.keeperCountDownLatch = keeperCountDownLatch;
			this.waitUtilCountDown = waitUtilCountDown ;
			this.setName(name);
		}

		@Override
		public void run() {
			try {
				if (waitUtilCountDown){
					keeperCountDownLatch.await();
					System.out.println(Thread.currentThread().getName() + " return util countdown " + new Date().toLocaleString());
				}else {
					if (!keeperCountDownLatch.await(waitMilliseconds,TimeUnit.MILLISECONDS)){
						System.out.println(Thread.currentThread().getName() + " returnd false util the specified waiting time elapses at " + new Date().toLocaleString());
					}else {
						System.out.println(Thread.currentThread().getName() + " returnd true util countdown " + new Date().toLocaleString());
					}
				}
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
```

## CONTRIBUTOR
* huangdou
