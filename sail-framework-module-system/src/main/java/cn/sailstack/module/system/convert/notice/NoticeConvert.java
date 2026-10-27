package cn.sailstack.module.system.convert.notice;

import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.controller.admin.notice.vo.NoticeRespVO;
import cn.sailstack.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import cn.sailstack.module.system.dal.dataobject.notice.NoticeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface NoticeConvert {

    NoticeConvert INSTANCE = Mappers.getMapper(NoticeConvert.class);

    NoticeRespVO convertToRespVO(NoticeDO bean);

    List<NoticeRespVO> convertRespVOList(List<NoticeDO> list);

    default PageResult<NoticeRespVO> convertRespVOPage(PageResult<NoticeDO> page) {
        return new PageResult<>(convertRespVOList(page.getList()), page.getTotal());
    }

    NoticeDO convertToDO(NoticeSaveReqVO bean);

}
