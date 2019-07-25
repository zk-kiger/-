package Server;

import javax.swing.*;

/**
 * @ClassName Server
 * @Description 服务端是唯一的，在该类里面定义一个私有的服务器窗口和服务器线程
 * @Author zk_kiger
 * @Date 2019/5/9 11:43
 * @Version 1.0
 */

public class Server {
    //封装服务器窗口
    private ServerFrame serverFrame;
    private ServerThread serverThread;

    //构造方法
    public Server() {}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        //创建服务器对象
        Server server = new Server();
        //为该对象的服务器窗口实例化
        ServerFrame serverFrame = new ServerFrame(server);
        server.setServerFrame(serverFrame);
        serverFrame.setVisible(true);
    }

    //ServerFarme的set方法
    public void setServerFrame(ServerFrame serverFrame) {
        this.serverFrame = serverFrame;
    }

    /**
     * 启动服务器
     */
    public void startServer() {
        serverThread = new ServerThread(serverFrame);
        serverThread.startServer();
    }

    /**
     * 停止服务器
     */
    public void stopServer() {
        serverThread.stopServer();
        serverThread.setFlag_eixt(false);
    }
}
