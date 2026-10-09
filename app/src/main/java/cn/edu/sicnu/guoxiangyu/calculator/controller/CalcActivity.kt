package cn.edu.sicnu.guoxiangyu.calculator.controller

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.guoxiangyu.calculator.R
import cn.edu.sicnu.guoxiangyu.calculator.databinding.ActivityCalcBinding
import cn.edu.sicnu.guoxiangyu.calculator.model.CalculatorModel

/**
 * 计算器控制器 (Controller 层)
 * 职责：使用 ViewBinding 监听用户按键事件，驱动 CalculatorModel 更新，刷新视图显示。
 */
class CalcActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalcBinding
    private val model = CalculatorModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. 开启 ViewBinding 并绑定视图
        binding = ActivityCalcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置 Activity 标题为姓名+学号
        supportActionBar?.title = getString(R.string.title_calculator)

        // 初始化显示
        updateDisplay()

        // 2. 绑定数字与小数点按键
        setupNumberButtons()

        // 3. 绑定运算符按键
        setupOperatorButtons()

        // 4. 绑定功能按键 (C, DEL, =, ±, %)
        setupActionButtons()
    }

    private fun setupNumberButtons() {
        val numberMap = mapOf(
            binding.btnZero to "0",
            binding.btnOne to "1",
            binding.btnTwo to "2",
            binding.btnThree to "3",
            binding.btnFour to "4",
            binding.btnFive to "5",
            binding.btnSix to "6",
            binding.btnSeven to "7",
            binding.btnEight to "8",
            binding.btnNine to "9",
            binding.btnDot to "."
        )

        for ((button, token) in numberMap) {
            button.setOnClickListener {
                if (model.append(token)) {
                    updateDisplay()
                }
            }
        }
    }

    private fun setupOperatorButtons() {
        val opMap = mapOf(
            binding.btnPlus to "+",
            binding.btnMinus to "−",
            binding.btnMultiply to "×",
            binding.btnDivide to "÷"
        )

        for ((button, op) in opMap) {
            button.setOnClickListener {
                if (model.append(op)) {
                    updateDisplay()
                }
            }
        }
    }

    private fun setupActionButtons() {
        // C 清除全部
        binding.btnClear.setOnClickListener {
            model.clear()
            binding.tvExpr.text = getString(R.string.calc_default_expr)
            binding.tvResult.text = getString(R.string.calc_default_result)
        }

        // DEL 退格删除
        binding.btnDel.setOnClickListener {
            if (model.del()) {
                updateDisplay()
            }
        }

        // = 计算求值
        binding.btnEqual.setOnClickListener {
            val res = model.calculate()
            when (res) {
                CalculatorModel.ERROR_DIV_ZERO -> {
                    binding.tvResult.text = getString(R.string.calc_error_div_zero)
                }
                CalculatorModel.ERROR_SYNTAX -> {
                    binding.tvResult.text = getString(R.string.calc_error_syntax)
                }
                else -> {
                    updateDisplay()
                }
            }
        }

        // ± 正负取反
        binding.btnNegate.setOnClickListener {
            if (model.toggleSign()) {
                updateDisplay()
            }
        }

        // % 百分比
        binding.btnPercent.setOnClickListener {
            if (model.percent()) {
                updateDisplay()
            }
        }
    }

    private fun updateDisplay() {
        val expr = model.expr
        binding.tvExpr.text = expr.ifEmpty { getString(R.string.calc_default_expr) }
        binding.tvResult.text = if (model.lastResult.isNotEmpty()) {
            model.lastResult
        } else if (expr.isNotEmpty()) {
            expr
        } else {
            getString(R.string.calc_default_result)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_calc, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_switch_dynamic) {
            startActivity(Intent(this, DynamicUiActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
