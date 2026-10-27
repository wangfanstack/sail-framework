package cn.sailstack.framework.common.util.object;

import cn.sailstack.framework.common.pojo.PageParam;
import cn.sailstack.framework.common.pojo.PageResult;

import java.util.List;

/**
 * {@link cn.sailstack.framework.common.pojo.PageParam} 工具类
 *
 * @author 农E链源码
 */
public class PageUtils {

    public static int getStart(PageParam pageParam) {
        return (pageParam.getPageNo() - 1) * pageParam.getPageSize();
    }

    /**
     * 对内存列表分页，不改变列表顺序
     *
     * @param pageParam 分页参数，支持 {@link PageParam#PAGE_SIZE_NONE} 返回全部数据
     * @param list 已排序的完整列表
     * @param <T> 数据类型
     * @return 分页结果，超出末页时保留总数并返回空列表
     */
    public static <T> PageResult<T> buildPageResult(PageParam pageParam, List<T> list) {
        if (list == null || list.isEmpty()) {
            return PageResult.empty();
        }
        long total = list.size();
        if (PageParam.PAGE_SIZE_NONE.equals(pageParam.getPageSize())) {
            return new PageResult<>(list, total);
        }
        long offset = (long) (pageParam.getPageNo() - 1) * pageParam.getPageSize();
        int fromIndex = (int) Math.min(offset, total);
        int toIndex = (int) Math.min((long) fromIndex + pageParam.getPageSize(), total);
        return new PageResult<>(list.subList(fromIndex, toIndex), total);
    }

}
