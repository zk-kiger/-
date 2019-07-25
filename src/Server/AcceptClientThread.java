package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName AcceptClientThread
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/10 19:35
 * @Version 1.0
 */

public class AcceptClientThread extends Thread{
    private ServerThread serverThread;

    public AcceptClientThread(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    @Override
    public void run() {
        Socket socket;
        ServerSocket serverSocket = serverThread.getServerSocket();
        //如果服务器开启标志打开
        System.out.println(serverThread.isFlag_eixt());
        System.out.println(serverSocket.isClosed());
        while (serverThread.isFlag_eixt()) {
            //如果服务端口关闭
            if(serverSocket.isClosed()) {
                serverThread.setFlag_eixt(false);
            } else {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    //没有接收到客户端，那么关闭服务器
                    socket = null;
                    serverThread.setFlag_eixt(false);
                }
                //如果接收到客户端,那么就为这个客户端开启一个服务线程
                if(socket != null) {
                    //提取客户端ip和port，用于记录用户信息
                    String ip = socket.getInetAddress().getHostAddress();
                    int port = socket.getPort();
                    //建立新的服务线程，向该线程提供服务端Socket、客户端ip、客户端port
                    new Thread(new ServerThread(socket, ip, port)).start();
                }
            }
        }
    }
}
