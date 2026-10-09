package cn.edu.sicnu.guoxiangyu.calculator.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Stack

/**
 * 计算器业务逻辑模型 (Model 层)
 * 职责：独立封装表达式状态、字符合法性追加、退格、清空以及数学求值算法。
 * 完全解耦于 Android View 与控制器。
 */
class CalculatorModel {

    var expr: String = ""
        private set

    var lastResult: String = ""
        private set

    private var isCalculated: Boolean = false

    companion object {
        const val ERROR_DIV_ZERO = "DIV_ZERO"
        const val ERROR_SYNTAX = "SYNTAX_ERROR"
    }

    /**
     * 追加字符或运算符
     */
    fun append(token: String): Boolean {
        if (isCalculated) {
            // 如果刚刚计算出结果且输入数字或点，则重置开启新运算
            if (isNumber(token) || token == ".") {
                expr = ""
            }
            isCalculated = false
        }

        // 基础合法性校验
        val isOp = isOperator(token)
        if (expr.isEmpty()) {
            // 表达式为空时，不能直接以乘、除、百分号开头，允许负号
            if (token == "×" || token == "÷" || token == "%") {
                return false
            }
            if (token == ".") {
                expr = "0."
                return true
            }
            expr += token
            return true
        }

        val lastChar = expr.last().toString()

        // 如果连续输入运算符，替换最后一个运算符
        if (isOp && isOperator(lastChar)) {
            expr = expr.substring(0, expr.length - 1) + token
            return true
        }

        // 小数点合法性检测：当前正在输入的数字片段中不能已有小数点
        if (token == ".") {
            if (isOperator(lastChar)) {
                expr += "0."
                return true
            }
            val lastNumberSegment = getLastNumberSegment()
            if (lastNumberSegment.contains(".")) {
                return false
            }
        }

        expr += token
        return true
    }

    /**
     * 退格删除最后一个字符
     */
    fun del(): Boolean {
        if (expr.isNotEmpty()) {
            expr = expr.substring(0, expr.length - 1)
            isCalculated = false
            return true
        }
        return false
    }

    /**
     * 清空表达式与状态
     */
    fun clear() {
        expr = ""
        lastResult = ""
        isCalculated = false
    }

    /**
     * 正负号取反 (±)
     */
    fun toggleSign(): Boolean {
        if (expr.isEmpty()) return false
        val lastSegment = getLastNumberSegment()
        if (lastSegment.isEmpty()) return false

        val prefix = expr.substring(0, expr.length - lastSegment.length)
        expr = if (prefix.endsWith("−")) {
            prefix.substring(0, prefix.length - 1) + lastSegment
        } else if (prefix.endsWith("+")) {
            prefix.substring(0, prefix.length - 1) + "−" + lastSegment
        } else if (prefix.isEmpty()) {
            "−$lastSegment"
        } else {
            "$prefix(−$lastSegment)"
        }
        return true
    }

    /**
     * 百分比换算 (%)
     */
    fun percent(): Boolean {
        if (expr.isEmpty()) return false
        val lastSegment = getLastNumberSegment()
        if (lastSegment.isEmpty() || lastSegment == ".") return false

        try {
            val num = lastSegment.toDouble() / 100.0
            val formatted = formatNumber(num)
            val prefix = expr.substring(0, expr.length - lastSegment.length)
            expr = prefix + formatted
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 执行四则混合运算并返回结果
     */
    fun calculate(): String {
        if (expr.isEmpty()) return ""

        // 去除末尾多余的运算符
        var sanitized = expr
        while (sanitized.isNotEmpty() && isOperator(sanitized.last().toString())) {
            sanitized = sanitized.substring(0, sanitized.length - 1)
        }
        if (sanitized.isEmpty()) return ""

        try {
            val resultValue = evaluateExpression(sanitized)
            val formatted = formatNumber(resultValue)
            lastResult = formatted
            isCalculated = true
            expr = formatted
            return formatted
        } catch (e: ArithmeticException) {
            return ERROR_DIV_ZERO
        } catch (e: Exception) {
            return ERROR_SYNTAX
        }
    }

    private fun isOperator(s: String): Boolean {
        return s == "+" || s == "−" || s == "×" || s == "÷" || s == "-"
    }

    private fun isNumber(s: String): Boolean {
        return s.all { it.isDigit() }
    }

    private fun getLastNumberSegment(): String {
        val ops = charArrayOf('+', '−', '×', '÷', '-', '(', ')')
        val lastOpIndex = expr.lastIndexOfAny(ops)
        return if (lastOpIndex == -1) expr else expr.substring(lastOpIndex + 1)
    }

    private fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) {
            throw ArithmeticException("Invalid number")
        }
        val bd = BigDecimal(value.toString())
        return if (bd.scale() <= 0 || bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            bd.toBigInteger().toString()
        } else {
            bd.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }
    }

    /**
     * 标准四则运算解析（双栈法评估）
     */
    private fun evaluateExpression(raw: String): Double {
        val standardized = raw.replace("−", "-").replace("×", "*").replace("÷", "/")
        val tokens = tokenize(standardized)

        val numbers = Stack<Double>()
        val ops = Stack<Char>()

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            when {
                token.length == 1 && isStandardOp(token[0]) -> {
                    val op = token[0]
                    while (ops.isNotEmpty() && hasPrecedence(op, ops.peek())) {
                        applyOp(ops.pop(), numbers)
                    }
                    ops.push(op)
                }
                token == "(" -> ops.push('(')
                token == ")" -> {
                    while (ops.isNotEmpty() && ops.peek() != '(') {
                        applyOp(ops.pop(), numbers)
                    }
                    if (ops.isNotEmpty() && ops.peek() == '(') {
                        ops.pop()
                    }
                }
                else -> {
                    numbers.push(token.toDouble())
                }
            }
            i++
        }

        while (ops.isNotEmpty()) {
            applyOp(ops.pop(), numbers)
        }

        if (numbers.isEmpty()) throw IllegalArgumentException("Empty expression")
        return numbers.pop()
    }

    private fun tokenize(str: String): List<String> {
        val list = mutableListOf<String>()
        var buffer = StringBuilder()
        var i = 0
        while (i < str.length) {
            val c = str[i]
            if (c.isDigit() || c == '.') {
                buffer.append(c)
            } else if (isStandardOp(c) || c == '(' || c == ')') {
                // 判断负号是否为负数前缀
                if (c == '-' && (i == 0 || str[i - 1] == '(' || isStandardOp(str[i - 1]))) {
                    buffer.append(c)
                } else {
                    if (buffer.isNotEmpty()) {
                        list.add(buffer.toString())
                        buffer = StringBuilder()
                    }
                    list.add(c.toString())
                }
            }
            i++
        }
        if (buffer.isNotEmpty()) {
            list.add(buffer.toString())
        }
        return list
    }

    private fun isStandardOp(c: Char) = c == '+' || c == '-' || c == '*' || c == '/'

    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        if (op2 == '(' || op2 == ')') return false
        return !((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
    }

    private fun applyOp(op: Char, numbers: Stack<Double>) {
        if (numbers.size < 2) throw IllegalArgumentException("Invalid syntax")
        val b = numbers.pop()
        val a = numbers.pop()
        when (op) {
            '+' -> numbers.push(a + b)
            '-' -> numbers.push(a - b)
            '*' -> numbers.push(a * b)
            '/' -> {
                if (b == 0.0) throw ArithmeticException("Division by zero")
                numbers.push(a / b)
            }
        }
    }
}
