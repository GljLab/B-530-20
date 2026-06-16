# 预订更新架构设计文档

## 一、核心设计模式

本次重构采用 **策略模式 + 工厂模式 + 模板方法模式** 的组合架构：

- **策略模式**：每个可更新字段对应一个 `FieldUpdateStrategy` 实现，封装该字段的验证、更新、格式化等逻辑
- **工厂模式**：`FieldUpdateStrategyFactory` 统一管理所有策略，通过字段名查找对应策略
- **模板方法模式**：`AbstractFieldUpdateStrategy` 提供默认实现，子类只需关注差异化逻辑

## 二、关键类职责

| 类名 | 职责 |
|------|------|
| `FieldUpdateStrategy<T>` | 策略接口，定义字段更新的标准方法 |
| `AbstractFieldUpdateStrategy<T>` | 抽象基类，封装通用逻辑（Getter/Setter引用、值比较） |
| `FieldUpdateStrategyFactory` | 策略工厂，Spring自动扫描注册，按字段名分发策略 |
| `BookingUpdateRequest` | 强类型更新请求DTO，替代`Map<String, Object>` |
| `FieldChange<T>` | 字段变更记录，封装字段名、旧值、新值、类型信息 |
| `PriceRecalculationService` | 价格重算服务，独立封装价格计算和明细重算逻辑 |
| `BookingFieldAccessor` | 字段值访问工具，优先用策略，反射为兜底 |

## 三、扩展点说明

### 新增可修改字段（如 remark）
1. 创建 `RemarkUpdateStrategy` 类，继承 `AbstractFieldUpdateStrategy<String>`
2. 在构造函数中指定字段名、类型、getter、setter
3. 按需重写 `validate()`、`triggersPriceRecalculation()`、`formatValue()` 等方法
4. 添加 `@Component` 注解，Spring 自动注册

### 字段权限验证
在策略的 `validate()` 方法中检查 `LoginUser` 的权限，例如：
```java
@Override
public void validate(BigDecimal oldValue, BigDecimal newValue, Booking booking, LoginUser loginUser) {
    if (!loginUser.getPermissions().contains("finance:edit")) {
        throw new BusinessException("无权限修改总金额");
    }
}
```

### 字段变更异步通知
可扩展策略接口增加 `onChanged()` 回调方法，或通过 Spring 事件机制发布变更事件。

### 批量更新
`BookingUpdateRequest` 支持任意字段组合，遍历多个预订ID调用即可实现批量更新。

## 四、与原实现对比优势

| 维度 | 原实现 | 重构后 |
|------|--------|--------|
| 开闭原则 | 新增字段需修改核心 switch-case | 新增字段只需添加策略类 |
| 代码复用 | 17个重复的 getFieldValue 分支 | 策略 + 反射，零重复 |
| 类型安全 | Map<String, Object> 强转有运行时风险 | 泛型策略 + 强类型 DTO，编译期检查 |
| 业务耦合 | 价格重算散落在主方法中 | 独立 PriceRecalculationService |
| 可测试性 | 170行方法难以单元测试 | 每个策略可独立测试 |
| 扩展性 | 权限/通知等需求需硬编码 | 策略可灵活扩展验证和回调 |
