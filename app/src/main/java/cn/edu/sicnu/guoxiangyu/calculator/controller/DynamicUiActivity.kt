package cn.edu.sicnu.guoxiangyu.calculator.controller

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.guoxiangyu.calculator.R
import cn.edu.sicnu.guoxiangyu.calculator.databinding.ActivityDynamicUiBinding
import cn.edu.sicnu.guoxiangyu.calculator.model.ProgramAdviserModel
import cn.edu.sicnu.guoxiangyu.calculator.view.DynamicUiHelper

/**
 * 动态 UI 与 MVC 程序建议器 控制器 (Controller 层)
 * 满足实验指导书两功能区要求：
 * 区1：代码动态添加 TextView 并支持 ScrollView 滚动
 * 区2：Spinner 下拉交互查询 ProgramAdviser 模型层并展示结果
 */
class DynamicUiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDynamicUiBinding
    private val adviserModel = ProgramAdviserModel()
    private var textViewCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDynamicUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.title_dynamic_ui)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 区1：纯代码动态添加 TextView
        binding.btnAddTextView.setOnClickListener {
            textViewCount++
            val tv = DynamicUiHelper.createDynamicTextView(this, textViewCount)
            binding.containerDynamicViews.addView(tv)

            // 自动滚动至底部
            binding.scrollViewZone1.post {
                binding.scrollViewZone1.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        // 区2：ProgramAdviser 模型交互
        binding.btnConfirm.setOnClickListener {
            val selectedCategory = binding.spinnerLanguages.selectedItem.toString()
            val advice = adviserModel.getAdvice(selectedCategory)
            binding.tvAdviserResult.text = advice
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dynamic, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home, R.id.action_switch_calc -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
