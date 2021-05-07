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

- 数据库乐观锁实现
- 数据库乐观锁+Redis Lua脚本限流
- 数据库乐观锁+Redis校验库存

#### 6.多线程消费Kafka



