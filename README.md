# 南京大学编译原理课程实验2024sp

基于Antlr4生成的Java代码，实现支持SysY语言的编译器。

各个Lab对应的代码请到相应的分支进行查看。

# Progress

- [x] Lab1 实现词法分析
- [x] Lab2 实现语法分析，代码高亮展示，代码格式化功能
- [ ] Lab3 实现类型检查

# Reference

1. Lab3 类型检查开始，为了实现使用多个监听器的同时只遍历一遍语法树，引用了 [https://github.com/antlr/antlr4/issues/841] 中的`ProxyParseTreeListener`的实现。
