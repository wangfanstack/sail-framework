package cn.sailstack.framework.desensitize.core.base.serializer;

import cn.sailstack.framework.desensitize.core.base.annotation.DesensitizeBy;
import cn.sailstack.framework.desensitize.core.base.handler.DesensitizationHandler;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 脱敏序列化器
 *
 * 实现 JSON 返回数据时，使用 {@link DesensitizationHandler} 对声明脱敏注解的字段，进行脱敏处理。
 *
 * @author gaibu
 */
@SuppressWarnings("rawtypes")
public class StringDesensitizeSerializer extends StdSerializer<String> implements ContextualSerializer {

    /**
     * 处理器实例缓存（等价 hutool Singleton）
     */
    private static final ConcurrentMap<Class<?>, DesensitizationHandler> HANDLER_CACHE = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private DesensitizationHandler desensitizationHandler;

    protected StringDesensitizeSerializer() {
        super(String.class);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
        DesensitizeBy annotation = beanProperty.getAnnotation(DesensitizeBy.class);
        if (annotation == null) {
            return this;
        }
        // 创建一个 StringDesensitizeSerializer 对象，使用 DesensitizeBy 对应的处理器
        StringDesensitizeSerializer serializer = new StringDesensitizeSerializer();
        serializer.setDesensitizationHandler(HANDLER_CACHE.computeIfAbsent(annotation.handler(), clazz -> {
            try {
                return (DesensitizationHandler) clazz.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("创建脱敏处理器失败：" + clazz.getName(), e);
            }
        }));
        return serializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if (StringUtils.isBlank(value)) {
            gen.writeNull();
            return;
        }
        // 获取序列化字段
        Field field = getField(gen);

        // 自定义处理器（支持组合注解，即元注解中声明 @DesensitizeBy）
        Set<DesensitizeBy> annotations = field == null ? null
                : AnnotatedElementUtils.getAllMergedAnnotations(field, DesensitizeBy.class);
        if (annotations == null || annotations.isEmpty()) {
            gen.writeString(value);
            return;
        }
        for (Annotation annotation : field.getAnnotations()) {
            if (AnnotatedElementUtils.hasAnnotation(annotation.annotationType(), DesensitizeBy.class)) {
                value = this.desensitizationHandler.desensitize(value, annotation);
                gen.writeString(value);
                return;
            }
        }
        gen.writeString(value);
    }

    /**
     * 获取字段
     *
     * @param generator JsonGenerator
     * @return 字段
     */
    private Field getField(JsonGenerator generator) {
        String currentName = generator.getOutputContext().getCurrentName();
        Object currentValue = generator.currentValue();
        if (currentValue == null) {
            return null;
        }
        Class<?> currentValueClass = currentValue.getClass();
        return ReflectionUtils.findField(currentValueClass, currentName);
    }

}
