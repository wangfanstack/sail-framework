package cn.sailstack.module.infra.enums.codegen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码生成的场景枚举
 *
 * @author 农E链源码
 */
@AllArgsConstructor
@Getter
public enum CodegenSceneEnum {

    ADMIN(1, "管理后台", "admin", ""),
    APP(2, "用户 APP", "app", "App");

    /**
     * 场景
     */
    private final Integer scene;
    /**
     * 场景名
     */
    private final String name;
    /**
     * 基础包名
     */
    private final String basePackage;
    /**
     * Controller 和 VO 类的前缀
     */
    private final String prefixClass;

    public static CodegenSceneEnum valueOf(Integer scene) {
        for (CodegenSceneEnum sceneEnum : values()) {
            if (sceneEnum.getScene().equals(scene)) {
                return sceneEnum;
            }
        }
        return null;
    }

}
