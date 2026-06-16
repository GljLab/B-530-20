package com.example.permission.service.strategy;

import com.example.permission.common.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FieldUpdateStrategyFactory {

    private final Map<String, FieldUpdateStrategy<?>> strategyMap = new HashMap<>();

    @Autowired
    private List<FieldUpdateStrategy<?>> strategies;

    @PostConstruct
    public void init() {
        for (FieldUpdateStrategy<?> strategy : strategies) {
            strategyMap.put(strategy.getFieldName(), strategy);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> FieldUpdateStrategy<T> getStrategy(String fieldName) {
        FieldUpdateStrategy<?> strategy = strategyMap.get(fieldName);
        if (strategy == null) {
            throw new BusinessException("不支持的字段：" + fieldName);
        }
        return (FieldUpdateStrategy<T>) strategy;
    }

    public boolean hasStrategy(String fieldName) {
        return strategyMap.containsKey(fieldName);
    }

    public Map<String, FieldUpdateStrategy<?>> getAllStrategies() {
        return strategyMap;
    }
}
