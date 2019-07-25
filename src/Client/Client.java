package Client;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * @ClassName Client
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/9 11:43
 * @Version 1.0
 */

public class Client {
    //接收服务端并处理线程
    private AcceptMessageThread acceptMessageThread;
    //客户端Socket
    private Socket socket;
    //登录界面
    private LoginFrame loginFrame;
    //聊天界面
    private ChatFrame chatFrame;
    //时间显示格式
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  hh:mm:ss");
    //用户名
    public String username = null;
    public DataOutputStream dos = null;
    public DataInputStream dis = null;
    //每个uid对应的用户名
    public static HashMap<String, String> userNameMap;
    //与私聊用户对应的私聊窗口 - 用于判断该用户与私聊用户之间是否已经打开私聊窗口
    public static HashMap<String, SingleFrame> SingleMap;
    //表示该客户端是否打开
    private boolean flag_eixt = false;
    //单例模式 - 客户端唯一
    private static Client client = new Client();

    private Client() {
        userNameMap = new HashMap<String, String>();
        SingleMap = new HashMap<String, SingleFrame>();
    }

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

        Client client = Client.getClient();
        LoginFrame loginFrame = new LoginFrame(client);
        client.setLoginFrame(loginFrame);
        loginFrame.setVisible(true);
    }

    /**
     * 登录成功，进入该用的的聊天室
     * @param username 用户名
     */
    public void showChatFrame(String username) {
        //使用通道流将用户名传给服务器
        try {
            dos.writeUTF("Login/" + username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        chatFrame = new ChatFrame(this, username);
        chatFrame.setVisible(true);
        //启动接收服务器信息线程
        acceptMessageThread = new AcceptMessageThread(Client.getClient());
        acceptMessageThread.start();
    }

    /**
     * 获取客户端通道输入输出流并包装
     */
    private void getDataInit() {
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录进入聊天室
     * @param userName 用户名
     * @param ip 服务器地址
     * @param port 服务器端口号
     * @return
     */
    public String login(String userName, String ip, String port) {
        String login_mess = null;
        try {
            socket = new Socket(ip, Integer.parseInt(port));
            getDataInit();
            // 自己拼接一个uid - 客户端自己的uid
            String socketIp = socket.getInetAddress().getHostAddress();
            int socketPort = socket.getLocalPort();
            String uid = socketIp + ":" + socketPort;
            // 需要向服务器查询给用户名是否存在
            dos.writeUTF("Judge/" + userName + "/" + uid);
            // 阻塞等待接收服务器查询结果
            String info = dis.readUTF();
            if(info.equals("yes")) {
                login_mess = "用户名已经使用！";
                return login_mess;
            }
            this.username = userName;
        } catch (NumberFormatException e) {
            login_mess = "连接的服务器端口号port为整数,取值范围为：1024<port<65535";
            return login_mess;
        } catch (UnknownHostException e) {
            login_mess = "主机地址错误";
            return login_mess;
        } catch (IOException e) {
            login_mess = "连接服务其失败，请稍后再试";
            return login_mess;
        }
        client.setFlag_eixt(true);
        return "true";
    }

    /**
     * 退出登录界面
     */
    public void exitLogin() {
        System.exit(0);
    }

    /**
     * 向服务器传输信息、文件
     * @param message 信息内容
     */
    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出聊天室
     */
    public void exitChat() {
        try {
            dos.writeUTF("Exit/");
            //关闭接收信息线程
            if (acceptMessageThread != null) {
                acceptMessageThread.stop();
            }
            client.setFlag_eixt(false);
            System.exit(0);
        } catch (Exception e) {
            System.exit(0);
            e.printStackTrace();
        }
    }

    //get/set方法
    public static Client getClient() {
        return client;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public LoginFrame getLoginFrame() {
        return loginFrame;
    }

    public void setLoginFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
    }

    public ChatFrame getChatFrame() {
        return chatFrame;
    }

    public void setChatFrame(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
    }

    public boolean isFlag_eixt() {
        return flag_eixt;
    }

    public void setFlag_eixt(boolean flag_eixt) {
        this.flag_eixt = flag_eixt;
    }
}
