package com.liuwy.util;

import java.util.TreeMap;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author ImOkkkk
 * @date 2022/4/4 20:13
 * @since 1.0
 */
public class SpelUtil {
    public static String parse(String elString, TreeMap<String, Object> map) {
        elString = String.format("#{%s}", elString);
        // 创建表达式解析器
        ExpressionParser parser = new SpelExpressionParser();
        // 通过evaluationContext.setVariable可以在上下文中设定变量。
        EvaluationContext context = new StandardEvaluationContext();
        map.entrySet().forEach(entry -> context.setVariable(entry.getKey(), entry.getValue()));
        // 解析表达式
        Expression expression = parser.parseExpression(elString, new TemplateParserContext());
        // 使用Expression.getValue()获取表达式的值，这里传入了Evaluation上下文
        String value = expression.getValue(context, String.class);
        return value;
    }
}
