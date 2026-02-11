package ink.moling.mocklocation.utils.extensions

fun String.isFloat(allowNegative: Boolean = false, strict: Boolean = false): Boolean {
    // 允许中间状态
    if (this.isEmpty()) return true
    if (allowNegative && this == "-") return true

    // 如果不允许负数但出现负号
    if (!allowNegative && this.contains("-")) return false

    // 严格模式: 禁止小数点出现在开头
    if (strict && this.startsWith(".")) return false
    // 严格模式: 禁止的未完成输入
    if (strict && (this == "-" || this == "-." || this == ".")) return false

    return (
        this.all { it.isDigit() || it == '.' || it == '-' } &&  // 只能数字或小数点或负号
        this.count { it == '.' } <= 1 &&                        // 小数点最多一个
        this.count { it == '-' } <= 1 &&                        // 负号最多一个
        !this.drop(1).contains('-')                             // 负号只能在第一位
    )
}