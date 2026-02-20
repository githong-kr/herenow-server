package com.nsnm.herenow.lib.model.enum

enum class 작업상태코드(val code: String, val desc: String) {
    시작전("00", "시작전"),
    시작("01", "시작"),
    완료("02", "완료"),
    실패("03", "실패"),
    생략("04", "생략");

    companion object {
        // code 값으로 해당 열거형 항목을 찾는 메서드
        fun fromCode(code: String): 작업상태코드? {
            return entries.find { it.code == code }
        }

        // desc 값으로 해당 열거형 항목을 찾는 메서드
        fun valueOf(desc: String): 작업상태코드? {
            return entries.find { it.desc == desc }
        }
    }
}
