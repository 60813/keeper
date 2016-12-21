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
    
## Contributor
* huangdou

## usage
    KeeperClient client = new KeeperClient("127.0.0.1:2181");

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
