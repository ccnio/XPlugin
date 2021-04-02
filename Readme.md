# implementation 对资源隔离无效
implementation 间接依赖的module, 代码无法访问,但资源R.xx.xxx可以访问,想隔离资源必须不依赖或使用自定义的brick
# R 文件会被打包到apk中,类名会被混淆
release/debug中都会打包进R文件, 在release中,R文件类名会被混淆(如ay$a.class),但资源名不会被混淆. 可以通过三方工具移除资源或者混淆资源名.