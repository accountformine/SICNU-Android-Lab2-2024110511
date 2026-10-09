package cn.edu.sicnu.guoxiangyu.calculator.model

/**
 * 编程语言建议模型 (Model 层，对应实验指导书 MVC 教学要求)
 * 职责：纯业务领域模型，根据类别返回匹配的推荐语言。
 */
class ProgramAdviserModel {

    fun getAdvice(category: String): String {
        return when (category.trim()) {
            "新语言" -> "kotlin"
            "面向对象语言" -> "Java / C++"
            "脚本语言" -> "Python / JavaScript"
            "移动端与多平台" -> "Kotlin / Flutter (Dart)"
            "系统级编程语言" -> "Rust / Go / C"
            else -> "Kotlin"
        }
    }
}
