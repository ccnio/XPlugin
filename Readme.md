# 自定义 brick 依赖
- 壳 app module 使用 brick 依赖 library module，编译器就会隔离他们的依赖关系，即无法访问 library 的资源与代码，但不影响 assemble 的任务。
```xml
brick "com.google.code.gson:gson:2.8.6"//自定义依赖方式
brick project(':dest-library')
brick files('libs/pdf.aar')
```
# implementation 对间接引用的 module 资源隔离无效
implementation 间接依赖的 module, 虽然代码无法访问,但资源 R.xx.xxx 可以访问, 想隔离资源必须不依赖或使用自定义的依赖方式（brick）
# 资源迁移
组件化向外拆离 module 时，代码拆分后，还有许多资源(drawable/style/values)位于原 module中。但这些资源可能也在被其它 module 使用，而我们只想要属于拆分后 module 所单独使用的。
migrateRes 任务就可以实现这个迁移需求。
module 中引用的资源通过 解析 R.txt 来确认的
```xml
resourceConfig {
    migrateSrc = 'common-library'//迁移源目录
}
```
因为迁移脚本使用 python 实现的，故需要 python 环境。

# 资源冲突检测
组件化后，资源名没有规范定义的话，可能不同 module 同一类型资源会有同名字【内容】却不同的资源，打包后只会保留一份资源，可能会造成问题。
配置 scanConflict = true 后就会检测出哪些资源(drawable/style/values)有冲突问题。
```xml
resourceConfig {
    scanConflict = true
    interruptWhenConflict = false
}
```


