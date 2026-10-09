package cn.edu.sicnu.guoxiangyu.calculator.view

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cn.edu.sicnu.guoxiangyu.calculator.R

/**
 * 纯代码动态构建 UI 控件工具类 (View 辅助层)
 * 满足实验指导书区1要求："用代码动态添加TextView，ScrollView嵌套LinearLayout实现滚动"
 */
object DynamicUiHelper {

    fun createDynamicTextView(context: Context, index: Int): TextView {
        return TextView(context).apply {
            text = context.getString(R.string.dynamic_tv_template, index)
            textSize = 20f
            setTextColor(Color.parseColor("#004D40")) // 暗青色文本
            setPadding(0, 10, 0, 10)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
