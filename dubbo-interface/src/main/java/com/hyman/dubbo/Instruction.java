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
     * dubbo 项目配置：
     *
     * 将服务提供者注册到注册中心（暴露服务）：
     * 1，导入dubbo依赖（2.6.2）\操作zookeeper的客户端(curator)
     * 2，配置服务提供者 xml 文件
     *
     * 2、让服务消费者去注册中心订阅服务提供者的服务地址
     * @author lfy
     *
     */
}
