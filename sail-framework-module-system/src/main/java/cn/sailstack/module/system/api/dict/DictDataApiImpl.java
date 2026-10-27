package cn.sailstack.module.system.api.dict;

import cn.sailstack.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.sailstack.module.system.convert.dict.DictDataConvert;
import cn.sailstack.module.system.dal.dataobject.dict.DictDataDO;
import cn.sailstack.module.system.service.dict.DictDataService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 字典数据 API 实现类
 *
 * @author 农E链源码
 */
@Service
public class DictDataApiImpl implements DictDataApi {

    @Resource
    private DictDataService dictDataService;

    @Override
    public void validateDictDataList(String dictType, Collection<String> values) {
        dictDataService.validateDictDataList(dictType, values);
    }

    @Override
    public List<DictDataRespDTO> getDictDataList(String dictType) {
        List<DictDataDO> list = dictDataService.getDictDataListByDictType(dictType);
        return DictDataConvert.INSTANCE.convertRespDTOList(list);
    }

}
