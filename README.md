# keeper
基于[zookeeper](http://zookeeper.apache.org)实现的分布式开发客户端
## features
* client
实现zookeeper watch，session过期处理，断线重连
* expansion
实现分布式锁(互斥锁)，分布式信号量(samephore),分布式闭锁(CountDownLatch)等

## maven dependency
    <dependency>
        <groupId>com.60813</groupId>
	      <artifactId>keeper</artifactId>
	      <version>0.0.1-SNAPSHOT</version>
    </dependency>
    
## usage
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
		
	
## DEMO
    KeeperClient client = new KeeperClient("127.0.0.1:2181");

### client CRUD

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
	
### client LISTEN
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


	
## Contributor
* huangdou
