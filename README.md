#### 1.使用Redis进行幂等性token校验

- 拦截器实现

  AuthInterceptor+AuthChecker+WebConfiguration

- 切面实现

  AuthAspect+AuthChecker

#### 2.集成Swagger

SwaggerConfig

#### 3.全局通用异常处理

SpikeException

#### 4.i18n国际化

LocaleMessageSourceUtil+UtilInit

#### 5.秒杀

1. 接口层面限流，大部分请求过滤在业务层之外；
2. Redission分布式锁，只有拿到锁的客户端才能执行库存查验和库存扣减；
3. Lua脚本保证原子性扣减库存。

2，3选择其一即可。

#### 6.秒杀成功，异步下单

#### 7.TODO 多线程消费Kafka



