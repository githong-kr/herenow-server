package com.nsnm.herenow.lib.model.enum

enum class 작업종류코드(val code: String, val desc: String) {
    API등록작업("00", "API등록작업"),
    CODE등록작업("01", "CODE등록작업"),
    USER등록작업("02", "USER등록작업");

    companion object {
        // code 값으로 해당 열거형 항목을 찾는 메서드
        fun fromCode(code: String): 작업종류코드? {
            return 작업종류코드.entries.find { it.code == code }
        }

        // desc 값으로 해당 열거형 항목을 찾는 메서드
        fun valueOf(desc: String): 작업종류코드? {
            return 작업종류코드.entries.find { it.desc == desc }
        }
    }
}
