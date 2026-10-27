package cn.sailstack.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class TelephoneValidator implements ConstraintValidator<Telephone, String> {

    /**
     * 座机号码：区号-号码，例如 010-12345678、0755-1234567
     */
    private static final Pattern PATTERN_TEL = Pattern.compile("^(?:0\\d{2,3}-?)?\\d{7,8}(?:-\\d{1,9})?$");

    /**
     * 手机号码：1 开头的 11 位数字
     */
    private static final Pattern PATTERN_PHONE = Pattern.compile("^1\\d{10}$");

    @Override
    public void initialize(Telephone annotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果手机号为空，默认不校验，即校验通过
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        // 校验座机 / 手机
        return PATTERN_TEL.matcher(value).matches() || PATTERN_PHONE.matcher(value).matches();
    }

}
