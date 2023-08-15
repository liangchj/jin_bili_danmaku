package com.lchj.jin_bili_danmaku

object DanmakuConstant {
    const val JIN_BILI_DANMAKU_VIEW_ID = "ANDROID/JIN_BILI_DANMAKU_VIEW_ID"
    const val JIN_BILI_DANMAKU_METHOD_CHANNEL = "JIN_BILI_DANMAKU_METHOD_CHANNEL"
    // view 日志
    const val DANMAKU_VIEW_LOG_TAG: String = "DANMAKU_VIEW_LOG_TAG"
    // 哔哩哔哩弹幕日志
    const val BILI_DANMAKU_LOG_TAG: String = "BILI_DANMAKU_LOG_TAG"
    // 弹幕工具日志
    const val DANMAKU_UTILS_LOG_TAG: String = "DANMAKU_UTILS_LOG_TAG"

    // 弹幕速度列表，数字越小越快，越大的越慢
    val DANMAKU_SPEED_LIST: List<Float> = listOf(2.0f, 1.75f, 1.5f, 1.25f, 1.0f)
    // 弹幕速度选项总数
    const val DANMAKU_SPEED_LIST_TOTAL: Int = 5

    // 显示区域["1/4屏", "半屏", "3/4屏", "不重叠", "无限"]，选择下标，默认半屏（下标1）
    // "不重叠", "无限" 显示区域都是满屏，仅重叠不一致
    val DANMAKU_DISPLAY_AREA_LIST: List<Float> = listOf<Float>(0.25f, 0.5f, 0.75f, 1.0f, 1.0f)
    // 显示区域选项总数
    const val DANMAKU_DISPLAY_AREA_LIST_TOTAL: Int = 5

}