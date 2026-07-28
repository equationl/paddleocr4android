# 简介

该库是对多种部署方式运行 PaddleOCR 的二次封装。

对于只想体验或者快速上手使用的安卓开发者，该库对不同部署方法进行了简单的封装，使其可以直接上手使用，而无需关心 PaddleOCR 的实现，亦无需进行繁琐的配置。

![截图](/doc/screenshot4.png)


# 使用方法

目前支持以下三种部署方式，不同部署方式各有优缺点，您可以根据自己的需求选择使用：

| 部署方式                                                                             | 介绍                                                                          | 支持 PPOCR 版本     | 支持架构                                | 是否支持 16K Page | 
|----------------------------------------------------------------------------------|-----------------------------------------------------------------------------|-----------------|-------------------------------------|---------------|
| [fastDeploy](doc/fastDeploy.md)                                                  | 基于 fastDeploy 部署，支持 PPOCRv2、PPOCRv3 模型，包体积较大                                | PPOCRv2、PPOCRv3 | arm64-v8a, armeabi-v7a              | ❎             | 
| [Paddle-Lite](doc/paddlelite.md)                                                 | 基于 Paddle-Lite 部署，包体积较小，支持 PPOCRv4 及以下模型                                    | PPOCRv4 及以下     | arm64-v8a, armeabi-v7a              | ❎             | 
| [Ncnn](doc/ncnn.md)                                                              | 基于 Ncnn 部署，支持 PPOCRv5 模型，包体积较大                                              | PPOCRv5         | arm64-v8a, armeabi-v7a, x86, x86_64 | ✅             | 
| [ONNX](https://github.com/PaddlePaddle/PaddleOCR/tree/main/deploy/ppocr-android) | 官方仓库提供的基于 ONNX Runtime 实现移动端 OCR 推理。项目采用 SDK 与 Demo 分离 架构，SDK 模块可独立集成到第三方应用 | PPOCRv6         | ❓                                   | ❓             |


由于不同部署方式实现不同，具体使用方法请查看对应的文档：

- [fastDeploy 部署](/doc/fastDeploy.md)
- [Paddle-Lite 部署](/doc/paddlelite.md)
- [Ncnn 部署](/doc/ncnn.md)
- [ONNX 部署](https://github.com/PaddlePaddle/PaddleOCR/tree/main/deploy/ppocr-android)

**现在官方仓库已经提供了 ONNX 部署支持，如无特殊需求，推荐直接使用官方 ONNX 部署 SDK**

## 问题
官方的 ONNX 部署 SDK 存在一个问题，会导致出现类似 `java.lang.UnsatisfiedLinkError: dlopen failed: library "libopencv_info.so" not found` 这样的报错，如果你也遇到这个问题，请修改源码中的 `deploy/ppocr-android/gradle/libs.versions.toml` 将 `opencv = "4.5.3"` 修改为 `opencv = "4.5.3.0"` 即可。（问题原因：[Version 4.5.3 not working](https://github.com/QuickBirdEng/opencv-android/issues/58)）

# 其他

有任何问题请先尝试 demo 或阅读源码，如果无法解决请提 issue