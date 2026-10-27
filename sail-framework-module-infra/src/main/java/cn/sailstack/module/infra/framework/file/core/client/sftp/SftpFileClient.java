package cn.sailstack.module.infra.framework.file.core.client.sftp;

import cn.sailstack.module.infra.framework.file.core.client.AbstractFileClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Sftp 文件客户端，基于 JSch 实现
 *
 * @author 农E链源码
 */
public class SftpFileClient extends AbstractFileClient<SftpFileClientConfig> {

    /**
     * 连接超时时间，单位：毫秒
     */
    private static final long CONNECTION_TIMEOUT = 3000L;
    /**
     * 读写超时时间，单位：毫秒
     */
    private static final long SO_TIMEOUT = 10000L;

    static {
        // 某些旧的 sftp 服务器仅支持 ssh-dss 协议，该协议并不安全，默认不支持该协议，按需添加
        JSch.setConfig("server_host_key", JSch.getConfig("server_host_key") + ",ssh-dss");
    }

    private Session session;
    private ChannelSftp channel;

    public SftpFileClient(Long id, SftpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        try {
            JSch jsch = new JSch();
            // 初始化 Session
            session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            session.setPassword(config.getPassword());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.setTimeout((int) SO_TIMEOUT);
            session.connect((int) CONNECTION_TIMEOUT);
            // 初始化 Channel
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect((int) CONNECTION_TIMEOUT);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("初始化 SFTP 服务器(%s:%s) 失败",
                    config.getHost(), config.getPort()), e);
        }
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        // 执行写入
        String filePath = getFilePath(path);
        String fileName = getFileName(filePath);
        String dir = getDir(filePath, fileName);
        reconnectIfTimeout();
        mkDirs(dir); // 需要创建父目录，不然会报错
        try (InputStream in = new ByteArrayInputStream(content)) {
            channel.put(in, filePath);
        }
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) throws Exception {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        channel.rm(filePath);
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        channel.get(filePath, out);
        return out.toByteArray();
    }

    private String getFilePath(String path) {
        return config.getBasePath() + "/" + path;
    }

    private static String getFileName(String filePath) {
        int index = filePath.lastIndexOf('/');
        return index >= 0 ? filePath.substring(index + 1) : filePath;
    }

    private static String getDir(String filePath, String fileName) {
        return filePath.substring(0, filePath.length() - fileName.length());
    }

    /**
     * 逐级创建远程目录
     *
     * @param dir 目录路径
     */
    private void mkDirs(String dir) throws SftpException {
        if (dir == null || dir.isEmpty()) {
            return;
        }
        String current = "";
        for (String part : dir.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            current = current.isEmpty() ? part : current + "/" + part;
            try {
                channel.stat(current);
            } catch (SftpException e) {
                channel.mkdir(current);
            }
        }
    }

    private synchronized void reconnectIfTimeout() {
        if (session != null && session.isConnected() && channel != null && !channel.isClosed()) {
            return;
        }
        close();
        doInit();
    }

    private void close() {
        if (channel != null && !channel.isClosed()) {
            channel.disconnect();
        }
        channel = null;
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        session = null;
    }

}
