# 简介

该库是对多种部署方式运行 PaddleOCR 的二次封装。

对于只想体验或者快速上手使用的安卓开发者，该库对不同部署方法进行了简单的封装，使其可以直接上手使用，而无需关心 PaddleOCR 的实现，亦无需进行繁琐的配置。

![截图](/doc/screenshot4.png)


# 使用方法

目前支持以下三种部署方式，不同部署方式各有优缺点，您可以根据自己的需求选择使用：

| 部署方式                             | 介绍                                           | 支持 PPOCR 版本     | 支持架构                                | 是否支持 16K Page | 
|----------------------------------|----------------------------------------------|-----------------|-------------------------------------|---------------|
| [fastDeploy](doc/fastDeploy.md)  | 基于 fastDeploy 部署，支持 PPOCRv2、PPOCRv3 模型，包体积较大 | PPOCRv2、PPOCRv3 | arm64-v8a, armeabi-v7a              | ❎             | 
| [Paddle-Lite](doc/paddlelite.md) | 基于 Paddle-Lite 部署，包体积较小，支持 PPOCRv4 及以下模型     | PPOCRv4 及以下     | arm64-v8a, armeabi-v7a              | ❎             | 
| [Ncnn](doc/ncnn.md)              | 基于 Ncnn 部署，支持 PPOCRv5 模型，包体积较大               | PPOCRv5         | arm64-v8a, armeabi-v7a, x86, x86_64 | ✅             | 


由于不同部署方式实现不同，具体使用方法请查看对应的文档：

- [fastDeploy 部署](/doc/fastDeploy.md)
- [Paddle-Lite 部署](/doc/paddlelite.md)
- [Ncnn 部署](/doc/ncnn.md)

# 其他

有任何问题请先尝试 demo 或阅读源码，如果无法解决请提 issue