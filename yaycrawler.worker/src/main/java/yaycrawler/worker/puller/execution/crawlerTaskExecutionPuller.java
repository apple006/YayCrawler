package yaycrawler.worker.puller.execution;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.pamirs.schedule.TaskItemDefine;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yaycrawler.dao.domain.CrawlerTask;
import yaycrawler.worker.mapper.CrawlerTaskMapper;
import yaycrawler.worker.model.status.CrawlerStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: crawlerTaskExecutionPuller
 * @Description:    拉取工单表数据,执行工单解析调度
 * @Author Abi
 * @Email 380285138@qq.com
 * @Date 2017/6/15 15:26
 */
@Service("crawlerTaskExecutionPuller")
public class crawlerTaskExecutionPuller extends AbstractExecutionDataPuller<CrawlerTask> {

    @Autowired
    private CrawlerTaskMapper crawlerTaskMapper;
    private static final Logger logger = LoggerFactory.getLogger(crawlerTaskExecutionPuller.class);

    @Override
    public List<CrawlerTask> select(Map<String, Object> conditions, Long offset, Long limit){

        List<TaskItemDefine> taskItemList = (List<TaskItemDefine>) conditions.get("taskItemList");
        List<Integer> taskItemIds = Lists.newArrayList();
        taskItemList.forEach(taskItemDefine -> {
            taskItemIds.add(Integer.parseInt(taskItemDefine.getTaskItemId()));
        });
        List<CrawlerTask> orders = crawlerTaskMapper.selectListForParse(offset, limit, MapUtils.getInteger(conditions,"taskItemNum"),taskItemIds, CrawlerStatus.INIT.getStatus());
        if(orders == null || orders.isEmpty()){
            return null;
        } else {
            Map<Integer,CrawlerTask> queryBatchInfoMap = new HashMap<>();
            orders.forEach(order ->  {
                queryBatchInfoMap.put(order.getId(),order);
            });
            List<Integer> ids = Lists.newArrayList(queryBatchInfoMap.keySet());
            logger.debug(JSON.toJSONString(ids));
            if(ids != null && !ids.isEmpty() ) {
                crawlerTaskMapper.updateCrawlerTaskByStatus(CrawlerStatus.READY.getStatus(),CrawlerStatus.READY.getMsg(), CrawlerStatus.INIT.getStatus(), ids);
            }
        }
        return orders;
    }

}
