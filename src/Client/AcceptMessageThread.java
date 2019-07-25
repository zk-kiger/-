package Client;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName AcceptMessageThread
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/13 16:43
 * @Version 1.0
 */

public class AcceptMessageThread extends Thread{
    //获得客户端
    private Client client;

    public AcceptMessageThread(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        //时间显示格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

        while(client.isFlag_eixt()) {
            try {
                //获取服务器发送来的信息
                String message = client.dis.readUTF();
                System.out.println("message:" + message);
                if(message.equals("")) {
                    break;
                }
                //处理信息
                //处理服务器传来信息
                //消息类型：更新在线名单、聊天
                String type = message.substring(0, message.indexOf("/"));
                System.out.println("\ntype:" + type);
                //消息本体：更新后的名单、聊天内容
                String content = message.substring(message.indexOf("/") + 1);
                System.out.println("\ncontent:" + content);
                //根据消息类型分别处理
                if("OnlineListUpdata".equals(type)){
                    JList jltOnline = client.getChatFrame().getJlsOnline();
                    //更新在线名单 - uid
                    String[] onlineList = content.split(",");
                    //创建一个存储用户名的字符串数组
                    String[] names = new String[onlineList.length];
                    int i = 0;
                    //记录当前在线用户 - 用于比较时用户增加用户还是减少用户
                    int userNum = Client.userNameMap.size();
                    //逐个添加当前在线用户
                    //需要通过uid获得对应用户名
                    for (String member : onlineList
                         ) {
                        String uid = member.substring(0, member.indexOf("/"));
                        String name = member.substring(member.indexOf("/") + 1);
                        Client.userNameMap.put(uid, name);
                        //如果是当前客户端的用户名，那么就跳过
                        if(name.equals(client.username)) {
                            continue;
                        }
                        names[i++] = name;
                    }
                    jltOnline.removeAll();
                    jltOnline.setListData(names);
                    System.out.println("onlineList.length:" + onlineList.length + "\nuserNum:" + userNum);
                    if(onlineList.length > userNum){
                        client.dos.writeUTF("updataOverLogin/");
                    } else {
                        client.dos.writeUTF("updataOverExit/");
                    }
                }

                //群聊
                else if("ChatGroup".equals(type)){
                    String sender = content.substring(0, content.indexOf("/"));
                    String word = content.substring(content.indexOf("/") + 1);
                    //找到发送人的用户名
                    String senderName = Client.userNameMap.get(sender);
                    //在聊天框打印聊天信息
                    ChatFrame cf = client.getChatFrame();
                    //滚动条自动到底 显示最新信息
                    cf.getJtaChatMessage().setCaretPosition(cf.getJtaChatMessage().getDocument().getLength());
                    cf.getJtaChatMessage().append(senderName + "  " + sdf.format(new Date())
                        + "\n" + word + "\n"
                    );
                }

                //私聊 - 需要私聊用户打开一个私聊客户端
                else if("Chat".equals(type)) {
                    //发送人的uid
                    String sender = content.substring(0, content.indexOf("/"));
                    //获取发送人的用户名
                    String name = client.userNameMap.get(sender);
                    //聊天内容
                    String word = content.substring(content.indexOf("/") + 1);
                    //查看该用户与发送人之间是否打开了私人聊天窗口
                    SingleFrame sf = client.SingleMap.get(sender);
                    //之间没有打开聊天窗口，那么就打开
                    if(sf == null) {
                        sf = new SingleFrame(client, name, sender);
                        sf.setVisible(true);
                        //将该私聊对象与窗口存入
                        client.SingleMap.put(sender, sf);
                        //在该可私聊窗口将用户信息显示出来
                        sf.getJtaSingleMessage().setCaretPosition(sf.getJtaSingleMessage().getDocument().getLength());
                        sf.getJtaSingleMessage().append(name + "  " + sdf.format(new Date())
                                + "\n" + word + "\n"
                        );
                    }
                    //之间已经存在聊天窗口
                    else {
                        //那么就直接打印聊天信息
                        sf.getJtaSingleMessage().setCaretPosition(sf.getJtaSingleMessage().getDocument().getLength());
                        sf.getJtaSingleMessage().append(name + "  " + sdf.format(new Date())
                                + "\n" + word + "\n"
                        );
                    }
                }

                //接收文件 - 默认存放文件地方：D:/chat/
                else if("File".equals(type)) {
                    //默认存放文件地址
                    String filePath = "D:\\ChatFile";
                    //创建一个文件对象
                    File file = new File(filePath);
                    //如果文件不存在
                    if(!file.exists()) {
                        //创建一个文件夹
                        file.mkdir();
                    }
                    //发送人的uid
                    String sender = content.substring(0, content.indexOf("/"));
                    //获取文件名称
                    String fileName = content.substring(content.indexOf("/") + 1,content.lastIndexOf("/"));
                    // 获取文件大小
                    long length = new Long(content.substring(content.lastIndexOf("/") + 1));
                    System.out.println("sender:" + sender);
                    System.out.println("fileName:" + fileName);
                    System.out.println("length:" + length);
                    //获取发送人的用户名
                    String name = client.userNameMap.get(sender);
                    //查看该用户与发送人之间是否打开了私人聊天窗口
                    SingleFrame sf = client.SingleMap.get(sender);
                    //之间没有打开聊天窗口，那么就打开
                    if(sf == null) {
                        sf = new SingleFrame(client, name, sender);
                        sf.setVisible(true);
                        //将该私聊对象与窗口存入
                        client.SingleMap.put(sender, sf);
                        //询问是否接收文件,接收文件
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(sf,
                                "有新的文件，是否接收？", "提示", JOptionPane.OK_CANCEL_OPTION)) {
                            filePath += "\\" + fileName;
                            File f = new File(filePath);
                            //创建一个写入文件流
                            try (
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f))
                            ) {

                                int len;
                                long fileSize = 0;
                                byte[] bytes = new byte[1024];
                                while ((len = client.dis.read(bytes)) != -1) {
                                    fileSize += len;
                                    System.out.println("fileSize:" + fileSize);
                                    bos.write(bytes, 0, len);
                                    bos.flush();
                                    if(fileSize == length) {
                                        break;
                                    }
                                }
                                System.out.println("出来了！");
                                //提示接收文件完成
                                sf.getJtaSingleMessage().setCaretPosition(sf.getJtaSingleMessage().getDocument().getLength());
                                sf.getJtaSingleMessage().append(sdf.format(new Date()) + "\n" + "接收文件完成!" + "\n"
                                );
                            }

                        }
                    }
                    //之间已经存在聊天窗口
                    else {
                        //询问是否接收文件,接收文件
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(sf,
                                "有新的文件，是否接收？", "提示", JOptionPane.OK_CANCEL_OPTION)) {
                            filePath += "\\" + fileName;
                            File f = new File(filePath);
                            //创建一个写入文件流
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));

                            int len = -1;
                            long fileSize = 0;
                            byte[] bytes = new byte[1024];
                            while ((len = client.dis.read(bytes)) != -1) {
                                fileSize += len;
                                bos.write(bytes, 0, len);
                                if(fileSize == length) {
                                    break;
                                }
                            }

                            //提示接收文件完成
                            sf.getJtaSingleMessage().setCaretPosition(sf.getJtaSingleMessage().getDocument().getLength());
                            sf.getJtaSingleMessage().append(sdf.format(new Date()) + "\n" + "接收文件完成!" + "\n"
                            );
                            bos.close();
                        }
                    }
                }

                //服务器退出
                else if("ServerExit".equals(type)) {
                    //提示用户关闭客户端
                    JOptionPane.showMessageDialog(null, "服务器挂了，点击确定关闭客户端窗口");
                    System.exit(0);
                }
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, "服务器挂了");
                client.setFlag_eixt(false);
                e.printStackTrace();
            }
        }
    }
}
