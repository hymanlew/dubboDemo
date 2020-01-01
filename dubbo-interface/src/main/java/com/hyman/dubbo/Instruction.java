package com.hyman.dubbo;

/**
 * 工程架构（根据 dubbo 《服务化最佳实践》）：
 *
 * 1、分包
 * 建议将服务接口，服务模型，服务异常等均放在 API 包中，因为服务模型及异常也是 API 的一部分，同时这样做也符合分包原则：重用发
 * 布等价原则(REP)，共同重用原则(CRP)。
 * 如果需要，也可以考虑在 API 包中放置一份 spring 的引用配置，这样使用方便，只需在 spring 加载过程中引用此配置即可，配置建议
 * 放在模块的包目录下，以免冲突，如：com/alibaba/china/xxx/dubbo-reference.xml。
 *
 * 2、粒度
 * 服务接口尽可能大粒度，每个服务方法应代表一个功能，而不是某功能的一个步骤，否则将面临分布式事务问题，Dubbo 暂未提供分布式事
 * 务支持。
 * 服务接口建议以业务场景为单位划分，并对相近业务做抽象，防止接口数量爆炸。
 * 不建议使用过于抽象的通用接口，如：Map query(Map)，这样的接口没有明确语义，会给后期维护带来不便。
 */
public class Instruction {

    /**
     * Dubbo 的属性配置：
     * 如果应用很简单（例如不需要多注册中心或多协议，并且需要在spring容器中共享配置），那么可以直接使用 dubbo.properties 或者
     * boot 自带的 application.properties 作为默认配置。
     * Dubbo 可以自动加载 classpath 根目录下的 dubbo.properties，但也可以使用 JVM 参数来指定路径：-Ddubbo.properties.file=xxx.properties。
     *
     * 重写与优先级，优先级从高到低：
     * 1，JVM -D参数，当你部署或者启动应用时，它可以轻易地重写配置。比如改变 dubbo 协议端口：-Ddubbo.protocol.port=20881。
     * 2，XML, XML中（等同于 boot 默认的 properties 文件）的当前配置会重写 dubbo.properties 中的配置。
     * 3，Properties，默认配置，仅仅作用于以上两者没有配置时。
     *
     * 如果在 classpath 下有超过一个dubbo.properties文件，比如两个jar包都各自包含了dubbo.properties，dubbo将随机选择一个加
     * 载，并且打印错误日志。
     *
     * 如果 id没有在protocol中配置，将使用name作为默认属性。
     */
}
