package cn.sailstack.module.infra.framework.file.core.client.db;

import cn.sailstack.framework.common.util.spring.SpringUtils;
import cn.sailstack.module.infra.dal.dataobject.file.FileContentDO;
import cn.sailstack.module.infra.dal.mysql.file.FileContentMapper;
import cn.sailstack.module.infra.framework.file.core.client.AbstractFileClient;

import java.util.Comparator;
import java.util.List;

/**
 * 基于 DB 存储的文件客户端的配置类
 *
 * @author 农E链源码
 */
public class DBFileClient extends AbstractFileClient<DBFileClientConfig> {

    private FileContentMapper fileContentMapper;

    public DBFileClient(Long id, DBFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        fileContentMapper = SpringUtils.getBean(FileContentMapper.class);
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        FileContentDO contentDO = new FileContentDO().setConfigId(getId())
                .setPath(path).setContent(content);
        fileContentMapper.insert(contentDO);
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        fileContentMapper.deleteByConfigIdAndPath(getId(), path);
    }

    @Override
    public byte[] getContent(String path) {
        List<FileContentDO> list = fileContentMapper.selectListByConfigIdAndPath(getId(), path);
        if (list == null || list.isEmpty()) {
            return null;
        }
        // 排序后，拿 id 最大的，即最后上传的
        list.sort(Comparator.comparing(FileContentDO::getId));
        return list.get(list.size() - 1).getContent();
    }

}
