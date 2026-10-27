package cn.sailstack.module.system.convert.ip;

import cn.sailstack.framework.ip.core.Area;
import cn.sailstack.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import cn.sailstack.module.system.controller.app.ip.vo.AppAreaNodeRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AreaConvert {

    AreaConvert INSTANCE = Mappers.getMapper(AreaConvert.class);

    AreaNodeRespVO convert(Area bean);

    List<AreaNodeRespVO> convertList(List<Area> list);

    AppAreaNodeRespVO convertApp(Area bean);

    List<AppAreaNodeRespVO> convertAppList(List<Area> list);

}
