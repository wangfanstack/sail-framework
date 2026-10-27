package cn.sailstack.module.infra.framework.file.core.client.ftp;

import cn.sailstack.module.infra.framework.file.core.client.AbstractFileClient;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Ftp 文件客户端，基于 commons-net 实现
 *
 * @author 农E链源码
 */
public class FtpFileClient extends AbstractFileClient<FtpFileClientConfig> {

    /**
     * 连接超时时间，单位：毫秒
     */
    private static final long CONNECTION_TIMEOUT = 3000L;
    /**
     * 读写超时时间，单位：毫秒
     */
    private static final long SO_TIMEOUT = 10000L;

    private FTPClient ftpClient;

    public FtpFileClient(Long id, FtpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding(StandardCharsets.UTF_8.name());
        ftpClient.setConnectTimeout((int) CONNECTION_TIMEOUT);
        try {
            ftpClient.setSoTimeout((int) SO_TIMEOUT); // SocketException extends IOException
            ftpClient.connect(config.getHost(), config.getPort());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                throw new IllegalArgumentException(String.format("连接 FTP 服务器(%s:%s) 失败", config.getHost(), config.getPort()));
            }
            if (!ftpClient.login(config.getUsername(), config.getPassword())) {
                throw new IllegalArgumentException(String.format("登录 FTP 服务器(%s:%s) 失败", config.getHost(), config.getPort()));
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // 连接模式：Passive（被动）/ Active（主动）
            if ("Passive".equalsIgnoreCase(config.getMode())) {
                ftpClient.enterLocalPassiveMode();
            } else {
                ftpClient.enterLocalActiveMode();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("初始化 FTP 服务器(%s:%s) 失败", config.getHost(), config.getPort()), e);
        }
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        // 执行写入
        String filePath = getFilePath(path);
        String fileName = getFileName(filePath);
        String dir = getDir(filePath, fileName);
        reconnectIfTimeout();
        // 切换目录，不存在时逐级创建
        cdOrCreateDir(dir);
        boolean success = ftpClient.storeFile(fileName, new ByteArrayInputStream(content));
        if (!success) {
            throw new IOException(String.format("上传文件到目标目录 (%s) 失败", filePath));
        }
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) throws Exception {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        if (!ftpClient.deleteFile(filePath)) {
            throw new IOException(String.format("删除文件 (%s) 失败", filePath));
        }
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        String filePath = getFilePath(path);
        String fileName = getFileName(filePath);
        String dir = getDir(filePath, fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        reconnectIfTimeout();
        // 切换目录，不存在时逐级创建
        cdOrCreateDir(dir);
        if (!ftpClient.retrieveFile(fileName, out)) {
            throw new IOException(String.format("下载文件 (%s) 失败", filePath));
        }
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
     * 切换工作目录；目录不存在时，逐级创建
     *
     * @param dir 目录路径
     */
    private void cdOrCreateDir(String dir) throws IOException {
        if (dir == null || dir.isEmpty() || ftpClient.changeWorkingDirectory(dir)) {
            return;
        }
        String current = "";
        for (String part : dir.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            current = current.isEmpty() ? part : current + "/" + part;
            if (!ftpClient.changeWorkingDirectory(current)) {
                ftpClient.makeDirectory(current);
                if (!ftpClient.changeWorkingDirectory(current)) {
                    throw new IOException(String.format("创建 FTP 目录 (%s) 失败", dir));
                }
            }
        }
    }

    private synchronized void reconnectIfTimeout() throws IOException {
        if (ftpClient == null) {
            doInit();
            return;
        }
        if (!ftpClient.isConnected()) {
            close();
            doInit();
            return;
        }
        try {
            ftpClient.sendCommand("NOOP");
        } catch (IOException e) {
            // 连接超时，重连
            close();
            doInit();
        }
    }

    private void close() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
            } catch (IOException ignored) {
            }
            try {
                ftpClient.disconnect();
            } catch (IOException ignored) {
            }
        }
    }

}
