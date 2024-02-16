package com.aurora.remote;

import cn.hutool.core.util.ObjectUtil;
import com.aurora.exception.BizException;
import com.aurora.model.dto.*;
import com.aurora.model.vo.*;
import com.aurora.remote.adapter.RemoteArticleHandler;
import com.aurora.service.RedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.simpleframework.xml.core.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "文章模块")
@RestController
public class RemoteArticleController {
    
    private final static String REMOTE_SOURCE_PREFIX = "REMOTE_ARTICLE_SOURCE:";
    
    @Autowired
    private RedisService redisService;
    
    @PostConstruct
    public void initRemoteSource() {
        // gysy.ltd
        RemoteArticleSource gysy_ltd = new RemoteArticleSource();
        gysy_ltd.setSource("gysy.ltd");
        gysy_ltd.setSourceName("公益事业.ltd");
        gysy_ltd.setApiUrl("https://gysy.ltd/api/articles/all");
        gysy_ltd.setHandlerName("com.aurora.remote.adapter.DefaultRemoteArticleAdapter"); // 目标实现类
        
        redisService.set(REMOTE_SOURCE_PREFIX + gysy_ltd.getSource(), gysy_ltd);
        
        // orangejun.cn
        RemoteArticleSource orangejun = new RemoteArticleSource();
        orangejun.setSource("orangejun.cn");
        orangejun.setSourceName("橘子君");
        orangejun.setApiUrl("https://orangejun.cn/api/articles/all");
        orangejun.setHandlerName("com.aurora.remote.adapter.DefaultRemoteArticleAdapter");
        redisService.set(REMOTE_SOURCE_PREFIX + orangejun.getSource(), orangejun);
        
        // blog.jonk.top
        RemoteArticleSource blog_jonk_top = new RemoteArticleSource();
        blog_jonk_top.setSource("jonk.top");
        blog_jonk_top.setSourceName("一个懒猫");
        blog_jonk_top.setApiUrl("https://blog.jonk.top/api/articles/all");
        blog_jonk_top.setHandlerName("com.aurora.remote.adapter.DefaultRemoteArticleAdapter");
        redisService.set(REMOTE_SOURCE_PREFIX + blog_jonk_top.getSource(), blog_jonk_top);
        
        // www.linhaojun.top
        RemoteArticleSource linhaojun = new RemoteArticleSource();
        linhaojun.setSource("jonk.top");
        linhaojun.setSourceName("花未眠");
        linhaojun.setApiUrl("https://www.linhaojun.top/api/articles/all");
        linhaojun.setHandlerName("com.aurora.remote.adapter.DefaultRemoteArticleAdapter");
        redisService.set(REMOTE_SOURCE_PREFIX + linhaojun.getSource(), linhaojun);
    }
   
    private Map<String, RemoteArticleSource> sourceMap = new HashMap<>();
    
    @ApiOperation("获取所有文章-带分页")
    @GetMapping("/remote/article/all")
    public ResultVO<PageResultDTO<RemoteArticle>> listArticles(@Validate RemoteArticleParam param) {
        RemoteArticleSource remoteSource = sourceMap.get(REMOTE_SOURCE_PREFIX + param.getTargetSource());
        RemoteArticleHandler handler = null;
        
        try {
            // todo 从数据库或redis中获得 目标源配置类
            if (ObjectUtil.isNull(remoteSource)) {
                // 首次使用
                remoteSource = (RemoteArticleSource) redisService.get(REMOTE_SOURCE_PREFIX + param.getTargetSource());
                System.out.println("目标源: " + remoteSource.toString());
                
                if (ObjectUtil.isNull(remoteSource)) {
                    throw new BizException("找不到数据源");
                }
                
                // 适配器类
                Class<?> clazz = null;
                clazz = Class.forName(remoteSource.getHandlerName());
                handler = (RemoteArticleHandler) clazz.newInstance();
                
                remoteSource.setHandler(handler);
                remoteSource.setPage(new RemoteArticlePage());
                sourceMap.put(param.getTargetSource(), remoteSource); // todo
                //param.setSource(remoteSource);
            } else {
                handler = remoteSource.getHandler();
            }
            // todo 分页参数处理
            remoteSource.getPage().setCurrent(param.getCurrent());
            remoteSource.getPage().setSize(param.getSize());
            
            param.setSource(remoteSource);
            // todo 断言
            assert handler != null;
            return ResultVO.ok(handler.getRemoteData(param));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
