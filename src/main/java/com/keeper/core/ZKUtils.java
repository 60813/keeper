package com.keeper.core;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


/**
 *@author huangdou
 *@at 2017年1月12日上午9:54:40
 *@version 0.0.1
 */
public class ZKUtils {

    public static enum ZkVersion {
        V33, V34
    }

    public static final ZkVersion zkVersion;

    static {
        ZkVersion version = null;
        try {
            Class.forName("org.apache.zookeeper.OpResult");
            version = ZkVersion.V34;
        }
        catch (ClassNotFoundException e) {
            version = ZkVersion.V33;
        }
        finally {
            zkVersion = version;
        }

    }

    public static boolean isPortFree(int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", port), 200);
            socket.close();
            return false;
        }
        catch (SocketTimeoutException e) {
            return true;
        }
        catch (ConnectException e) {
            return true;
        }
        catch (SocketException e) {
            if (e.getMessage().equals("Connection reset by peer")) {
                return true;
            }
            throw new RuntimeException(e);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
