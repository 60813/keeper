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
