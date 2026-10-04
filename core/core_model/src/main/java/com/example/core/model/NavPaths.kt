package com.example.core.model

/**
 * 全局路由路径集中定义（TheRouter）。
 *
 * 约定（审查建议）：
 * - 跨模块页面跳转只允许使用这里的常量，禁止散落魔法字符串；
 * - 新增路由需在目标页 [com.therouter.router.Route] 与本文件同步登记（路径即契约）；
 * - 目标页所在模块必须配置 ksp(libs.therouter.compiler)，否则 @Route 静默不注册
 *   （CI 有 router-processor 一致性检查兜底）。
 */
object NavPaths {
    /** 通知页（ept_notify / NotifyActivity） */
    const val NOTIFY = "http://therouter.com/notify"

    /** 搜索页（ept_search / SearchActivity） */
    const val SEARCH = "http://therouter.com/search"

    /** 个人主页（ept_person / PersonActivity），参数：uid */
    const val PERSON = "http://therouter.com/person"
}
