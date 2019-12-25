package com.hyman.distributed.transaction.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 任意类的定义都可以添加 @EqualsAndHashCode 注解，让 lombok 自动生成 equals(Object other) 和 hashCode()方法的实现。
 *
 * 如果将 @EqualsAndHashCode 添加到继承至另一个类的类上（即添加到子类上），则此时就会有点棘手。一般情况下，为这样的类自动生成
 * equals 和 hashCode方法是一个坏思路，因为超类也有定义了一些字段，他们也需要 equals/hashCode 方法但是不会自动生成。通过设置
 * callSuper=true，可以在生成的 equals和hashCode 方法里包含超类的方法。
 * 对于hashCode，super.hashCode() 会被包含在hash算法内。而对于equals，如果超类实现认为它与传入的对象不一致则会返回false。
 * 注意：并非所有的equals都能正确的处理这样的情况。然而刚好lombok可以，若超类也使用lombok来生成equals方法，那么你可以安全的使
 * 用它的equals方法。如果你有一个明确的超类, 你得在callSuper上提供一些值来表示你已经斟酌过，要不然的话就会产生一条警告信息。
 *
 * 当你的类没有继承至任何类（非java.lang.Object, 当然任何类都是继承于Object类的），而你却将callSuer置为true, 这会产生编译错误。
 *
 * @Accessors 用于配置getter和setter方法的生成结果。
 * fluent，设置为true，则getter和setter方法的方法名都是基础属性名（方法名是属性名），且setter方法返回当前对象。
 * chain，设置为true，则setter方法返回当前对象。
 * prefix，用于生成getter和setter方法的字段名会忽视指定前缀（遵守驼峰命名）。
 *
 * @author hyman
 * @since 2019-10-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

}
