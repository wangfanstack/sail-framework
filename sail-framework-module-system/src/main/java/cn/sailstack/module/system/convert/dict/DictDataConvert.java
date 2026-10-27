package cn.sailstack.module.system.convert.dict;

import cn.sailstack.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.sailstack.framework.common.pojo.PageResult;
import cn.sailstack.module.system.controller.admin.dict.vo.data.DictDataRespVO;
import cn.sailstack.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import cn.sailstack.module.system.controller.admin.dict.vo.data.DictDataSimpleRespVO;
import cn.sailstack.module.system.controller.admin.dict.vo.type.DictTypeRespVO;
import cn.sailstack.module.system.controller.admin.dict.vo.type.DictTypeSaveReqVO;
import cn.sailstack.module.system.controller.admin.dict.vo.type.DictTypeSimpleRespVO;
import cn.sailstack.module.system.controller.app.dict.vo.AppDictDataRespVO;
import cn.sailstack.module.system.dal.dataobject.dict.DictDataDO;
import cn.sailstack.module.system.dal.dataobject.dict.DictTypeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DictDataConvert {

    DictDataConvert INSTANCE = Mappers.getMapper(DictDataConvert.class);

    DictDataRespDTO convertToRespDTO(DictDataDO bean);

    List<DictDataRespDTO> convertRespDTOList(List<DictDataDO> list);

    DictDataDO convertToDataDO(DictDataSaveReqVO bean);

    DictDataRespVO convertToDataRespVO(DictDataDO bean);

    List<DictDataRespVO> convertDataRespVOList(List<DictDataDO> list);

    default PageResult<DictDataRespVO> convertDataRespVOPage(PageResult<DictDataDO> page) {
        return new PageResult<>(convertDataRespVOList(page.getList()), page.getTotal());
    }

    AppDictDataRespVO convertToAppRespVO(DictDataDO bean);

    List<AppDictDataRespVO> convertAppRespVOList(List<DictDataDO> list);

    DictTypeDO convertToTypeDO(DictTypeSaveReqVO bean);

    DictTypeRespVO convertToTypeRespVO(DictTypeDO bean);

    List<DictTypeRespVO> convertTypeRespVOList(List<DictTypeDO> list);

    default PageResult<DictTypeRespVO> convertTypeRespVOPage(PageResult<DictTypeDO> page) {
        return new PageResult<>(convertTypeRespVOList(page.getList()), page.getTotal());
    }

    List<DictDataSimpleRespVO> convertDataSimpleRespVOList(List<DictDataDO> list);

    List<DictTypeSimpleRespVO> convertTypeSimpleRespVOList(List<DictTypeDO> list);

}
