package Server;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName ServerThread
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/9 11:44
 * @Version 1.0
 */

public class ServerThread extends Thread implements Runnable{
    //接收服务器线程
    private static AcceptClientThread acceptClientThread;
    //服务端ServerSocket
    private static ServerSocket serverSocket;
    //拼接uid所需元素
    private String userName = null;
    private Socket s = null;    //每个服务线程所连接的客户端Socket
    private String ip = null;
    private int port = 0;
    private String uid = null;
    //服务器窗口类 - 里面含有一些线程提示方法
    private static ServerFrame serverFrame;
    //存储uid
    public static ArrayList<String> uidArray;
    //每个uid对应的用户名 ? 为何arrayList可以在开启多个客户端存储uid，而HashMap不可以
    public static HashMap<String, String> userNameMap;
    //每个uid对应ServerThread
    public static HashMap<String, ServerThread> serverThreadMap;
    //表示服务器是否运行标志!!!
    private static boolean flag_eixt = false;

    public ServerThread() {}

    public ServerThread(ServerFrame serverFrame) {
        this.serverFrame = serverFrame;
        //实例化服务器ServerSocket
        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //为用户存储集合实例化
        uidArray = new ArrayList<String>();
        userNameMap = new HashMap<String, String>();
        serverThreadMap = new HashMap<String, ServerThread>();
    }

    public ServerThread(Socket s, String ip, int port) {
        this.s = s;
        this.ip = ip;
        this.port = port;
        this.uid = ip + ":" + port;
    }


    @Override
    public void run() {
        //获取当前客户端uid并存入uidArray
        uidArray.add(uid);
        //把当前uid和ServerThread存入uidMap
        serverThreadMap.put(uid, this);

        //时间显示格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  hh:mm:ss");

        try {
            //获取通道输出流和输入流
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            //用来标记客户端返回信息的次数
            int count = 1;
            //用于接收客户端
            String message = null;

            //持续监听并响应客户端信息进行相应的操作
            while(flag_eixt){
                //如果用户线程为0，那么更新服务端用户列表
                if(serverThreadMap.size() == 0) {
                    showOnlineList();
                }
                //获取客户端信息
                message = dis.readUTF();
                if(message.equals("")) {
                    break;
                }
                //在控制台打印
                System.out.println("客户端信息内容:" + message);
                //消息类型
                String type = message.substring(0, message.indexOf("/"));
                System.out.println("消息类型:" + type);
                //消息内容
                String content = message.substring(message.indexOf("/") + 1);
                System.out.println("消息内容:" + content);
                //根据消息类型分别处理

                //Login:客户端登录
                //服务端在线用户更新、记录uid对应用户名、客户端在线用户更新、信息窗口提示连接信息
                if("Login".equals(type)) {
                    System.out.println(uid + " " + content);
                    //记录uid对应用户名
                    userNameMap.put(uid, content);
                    System.out.println(userNameMap.get(uid));
                    count = 1;
                    //客户端在线用户更新
                    updateOnlineList();
                }

                //Exit:客户端退出
                //删除用户所在的三个集合、更新客户端和服务端、退出该线程
                else if("Exit".equals(type)){
                    //删除用户所在的三个集合
                    uidArray.remove(uid);
                    userNameMap.remove(uid);
                    serverThreadMap.remove(uid);
                    //更新客户端
                    updateOnlineList();
                }

                // Judge:判断userNameMap中是否存在username
                else if("Judge".equals(type)) {
                    // 截取查询的用户名以及对应的uid
                    String username = content.substring(0, content.indexOf("/"));
                    String uid = content.substring(content.indexOf("/") + 1);
                    // 遍历userNameMap，如果存在，那么就需要删除uidArray和serverThreadMap
                    if (userNameMap != null && userNameMap.size() > 0) {
                        for (String Uid:
                             userNameMap.keySet()) {
                            if(userNameMap.get(Uid).equals(username)) {
                                uidArray.remove(uid);
                                System.out.println(uidArray);
                                serverThreadMap.remove(uid);
                                dos.writeUTF("yes");
                            }
                        }
                    }
                    dos.writeUTF("no");
                }

                //ChatGroup:客户端群聊
                else if("ChatGroup".equals(type)) {
                    //向收信者发出聊天信息
                    chatGroupOnlineList(content);
                }

                //Chat:客户端私聊
                else if("Chat".equals(type)) {
                    //格式: uid/内容
                    //收信人uid
                    String receiver = content.substring(0, content.indexOf("/"));
                    //聊天内容
                    String word = content.substring(content.indexOf("/") + 1);

                    //向收信人发送聊天信息
                    chatSingleOnlineList(receiver, word);
                }

                //File:客户端上传文件，服务端接收
                else if("File".equals(type)) {
                    //格式: uid/内容
                    //收信人uid
                    String receiver = content.substring(0, content.indexOf("/"));
                    //文件名称
                    String word = content.substring(content.indexOf("/") + 1,content.lastIndexOf("/"));
                    // 文件大小
                    long length = new Long(content.substring(content.lastIndexOf("/") + 1));

                    System.out.println("reveiver:" + receiver);
                    System.out.println("word:" + word);
                    System.out.println("length:" + length);
                    // 将客户端上传的文件存储到一个临时文件
                    File tempFile = File.createTempFile(word, null);
                    tempFile.deleteOnExit();
                    try (
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
                            ) {

                        int len = -1;
                        long fileSize = 0;
                        byte[] bytes = new byte[1024];
                        while((len = dis.read(bytes)) != -1) {
                            fileSize += len;
                            System.out.println("fileSize:" + fileSize);
                            // 写入临时文件中
                            bos.write(bytes, 0, len);
                            bos.flush();
                            if(fileSize == length) {
                                break;
                            }
                        }
                        System.out.println("tempFile的大小:" + tempFile.length());
                        //向收信人发送文件
                        System.out.println("向另外的客户端发送文件!");
                        chatFile(tempFile,receiver,word,length);
                    }
                }

                //updataOverLogin:表示新的客户端数据更新完成，用来让服务端更新数据
                else if("updataOverLogin".equals(type) && count == 1) {
                    count++;
                    //服务端在线用户更新
                    showOnlineList();
                    //信息窗口提示连接信息
                    //滚动条自动到底 显示最新信息
                    serverFrame.getJtaMessage().setCaretPosition(serverFrame.getJtaMessage().getDocument().getLength());
                    serverFrame.getJtaMessage().append(sdf.format(new Date()) + " " + "<" +
                            userNameMap.get(uid) + ">" + "连接到服务器\n");
                }

                //updataOverExit:表示退出客户端更新完成，用来让服务端更新数据
                else if("updataOverExit".equals(type)) {
                    showOnlineList();
                }

            }
        } catch (Exception e){}
    }

    public boolean isFlag_eixt() {
        return flag_eixt;
    }

    public void setFlag_eixt(boolean flag_eixt) {
        this.flag_eixt = flag_eixt;
    }

    /**
     * 启动服务器
     */
    public void startServer() {
        //服务器标志变为true
        this.setFlag_eixt(true);
        //使用新的线程不断接收客户端
        acceptClientThread = new AcceptClientThread(this);
        acceptClientThread.start();
    }

    /**
     * 停止服务器
     */
    public void stopServer() {
        try {
            //将接受客户端线程关闭 - 暴力
            if(acceptClientThread != null){
                acceptClientThread.stop();
            }
            //清除存储的uid、用户线程、用户名
            if (uidArray.size() != 0) {
                for (String uid : uidArray
                     ) {
                    //获取uid对应ServerThread的通道输出流
                    OutputStream out = serverThreadMap.get(uid).s.getOutputStream();
                    //包装为数据输出流
                    DataOutputStream dos = new DataOutputStream(out);
                    //向每个客户端发送服务器停止信息
                    dos.writeUTF("ServerExit/");
                    ServerThread st = serverThreadMap.get(uid);
                    st.stop();
                    serverThreadMap.remove(uid);
                    userNameMap.remove(uid);
                }
                uidArray.clear();
            }
            //关闭服务器端口
            if(!serverSocket.isClosed()){
                serverSocket.close();
            }
            serverFrame.getJtaMessage().setText("");
            // 清空在线列表人数
            serverFrame.getJltOnline().removeAll();
            serverFrame.getJltOnline().setListData(new Vector());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新服务端在线名单
     */
    public void showOnlineList() {
        JList jltOnline = serverFrame.getJltOnline();
        //创建一个存储用户名的字符串数组
        String[] names = new String[userNameMap.size()];
        int i = 0;
        //遍历userNameMap，将用户名添加到在线列表模型
        Set<String> uidSet = userNameMap.keySet();
        for (String uid : uidSet
             ) {
            names[i++] = userNameMap.get(uid);
        }
        jltOnline.removeAll();
        jltOnline.setListData(names);
    }

    /**
     * 向所有已连接客户端更新在线名单
     * @throws IOException
     */
    public void updateOnlineList() throws IOException {
        //遍历每个在线客户端
        for (String uid : uidArray
        ) {
            //获取uid对应ServerThread的通道输出流
            OutputStream out = serverThreadMap.get(uid).s.getOutputStream();
            //包装为数据输出流
            DataOutputStream dos = new DataOutputStream(out);

            //拼接客户端提示信息：表示在线名单更新
            StringBuilder sb = new StringBuilder("OnlineListUpdata/");
            //拼接在线名单uid
            for (String memberUid : uidArray
            ) {
                //拼接uid/用户名
                sb.append(memberUid);
                sb.append("/");
                sb.append(userNameMap.get(memberUid));
                //以逗号分隔uid，除最后一个
                if(uidArray.indexOf(memberUid) != uidArray.size()-1){
                    sb.append(",");
                }
            }
            dos.writeUTF(sb.toString());
        }
    }

    /**
     * 发送群聊信息
     * @param word 信息内容
     * @throws IOException
     */
    public void chatGroupOnlineList(String word) throws IOException {
        OutputStream out = null;
        DataOutputStream dos = null;
        for (String member : uidArray
        ) {
            //获取uid对应ServerThread的Socket的通道输出流
            out = serverThreadMap.get(member).s.getOutputStream();
            //包装为数据输出流
            dos = new DataOutputStream(out);

            //拼接发送聊天信息：ChatGroup/发信人/信息内容 - 以便客户端接收时解析
            dos.writeUTF("ChatGroup/" + uid + "/" + word);
        }
    }

    /**
     * 发送私聊信息
     * @param receiver 接收人
     * @param word 聊天信息
     */
    public void chatSingleOnlineList(String receiver, String word) throws IOException {
        OutputStream out = null;
        DataOutputStream dos = null;

        //获取uid对应ServerThread的Socket的通道输出流
        out = serverThreadMap.get(receiver).s.getOutputStream();
        //包装为数据输出流
        dos = new DataOutputStream(out);

        //拼接发送聊天信息：Chat/发信人/信息内容 - 以便客户端接收时解析
        dos.writeUTF("Chat/" + uid + "/" + word);
    }

    /**
     * 发送文件
     * @param tempFile 客户端上传的文件
     * @param receiver 接收人的uid
     * @throws IOException
     */
    public void chatFile(File tempFile, String receiver, String word, long length) {
        // 创建临时文件输入流
        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tempFile))
        ) {
            //获取uid对应ServerThread的Socket的通道输出流
            DataOutputStream dos = new DataOutputStream(serverThreadMap.get(receiver).s.getOutputStream());
            DataInputStream dis = new DataInputStream(serverThreadMap.get(receiver).s.getInputStream());
            // 1.先关闭当前客户端服务线程
            setFlag_eixt(false);
            // 拼接发送聊天信息：Chat/发信人/信息内容 - 以便客户端接收时解析
            dos.writeUTF("File/" + uid + "/" + word + "/" + length);

            // 2.读取文件，传给客户端
            int len = -1;
            byte[] bytes = new byte[1024];
            while ((len = bis.read(bytes)) != -1) {
                dos.write(bytes,0, len);
            }
            setFlag_eixt(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AcceptClientThread getAcceptClientThread() {
        return acceptClientThread;
    }

    public static ServerSocket getServerSocket() {
        return serverSocket;
    }
}
