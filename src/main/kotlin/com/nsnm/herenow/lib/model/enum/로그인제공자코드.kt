package com.nsnm.herenow.lib.model.enum

enum class 로그인제공자코드(val code: String, val desc: String) {
    구글("google", "구글"),
    카카오("kakao", "카카오");

    companion object {
        // code 값으로 해당 열거형 항목을 찾는 메서드
        fun fromCode(code: String): 로그인제공자코드? {
            return entries.find { it.code == code }
        }

        // desc 값으로 해당 열거형 항목을 찾는 메서드
        fun valueOf(desc: String): 로그인제공자코드? {
            return entries.find { it.desc == desc }
        }
    }
}
