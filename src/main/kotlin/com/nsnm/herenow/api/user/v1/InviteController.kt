package com.nsnm.herenow.api.user.v1

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class InviteController(
    @Value("\${app.deeplink.scheme:herenow}") private val deepLinkScheme: String,
    @Value("\${app.store.url.ios:https://apps.apple.com/app/herenow}") private val iosStoreUrl: String,
    @Value("\${app.store.url.android:https://play.google.com/store/apps/details?id=com.nsnm.herenow}") private val androidStoreUrl: String
) {

    @GetMapping("/invite/{code}")
    @ResponseBody
    fun inviteLandingPage(
        @PathVariable code: String,
        @RequestParam(required = false) groupName: String?,
        @RequestParam(defaultValue = "herenow") scheme: String
    ): String {
        val appSchemeUrl = "$scheme://groups/join?code=$code"
        val clipboardText = "herenow-invite-$code"
        val title = if (groupName != null) "HereNow - '$groupName' 스페이스 초대" else "HereNow - 스페이스 초대"
        val description = "소중한 순간들을 무사히 보관하세요. 초대 코드로 함께 관리해요!"
        // TODO: 미리 준비된 og:image 경로 추가. 여기서는 기본 로고나 더미 이미지 경로 사용
        val imageUrl = "https://herenow.nsnm.com/images/og-default.png"

        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$title</title>
                
                <!-- Open Graph Data -->
                <meta property="og:title" content="$title" />
                <meta property="og:description" content="$description" />
                <meta property="og:type" content="website" />
                <meta property="og:image" content="$imageUrl" />
                
                <!-- Twitter Card Data -->
                <meta name="twitter:card" content="summary_large_image">
                <meta name="twitter:title" content="$title">
                <meta name="twitter:description" content="$description">
                <meta name="twitter:image" content="$imageUrl">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        height: 100vh;
                        margin: 0;
                        background-color: #f9fafb;
                        color: #111827;
                        text-align: center;
                    }
                    .container {
                        background: white;
                        border-radius: 20px;
                        padding: 40px 24px;
                        box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025);
                        max-width: 90%;
                        width: 360px;
                    }
                    .logo-placeholder {
                        width: 64px;
                        height: 64px;
                        background-color: #2563eb;
                        border-radius: 16px;
                        margin: 0 auto 20px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: white;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    h1 {
                        font-size: 22px;
                        margin-bottom: 12px;
                        color: #0f172a;
                    }
                    .desc {
                        color: #475569;
                        margin-bottom: 8px;
                        font-size: 15px;
                        line-height: 1.5;
                    }
                    .sub-desc {
                        color: #94a3b8;
                        margin-bottom: 32px;
                        font-size: 13px;
                        line-height: 1.5;
                    }
                    .btn {
                        background-color: #0f172a;
                        color: white;
                        border: none;
                        border-radius: 12px;
                        padding: 16px 32px;
                        font-size: 16px;
                        font-weight: 600;
                        cursor: pointer;
                        width: 100%;
                        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
                        transition: transform 0.1s;
                    }
                    .btn:active {
                        transform: scale(0.97);
                    }
                    .hidden-input {
                        position: absolute;
                        left: -9999px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="logo-placeholder">H</div>
                    <h1>$title</h1>
                    <p class="desc">$description</p>
                    <p class="sub-desc">앱이 이미 설치되어 있다면 스페이스로 자동 참여됩니다.<br/>미설치 시 스토어로 이동합니다.</p>
                    
                    <textarea id="codeText" class="hidden-input" readonly>$clipboardText</textarea>

                    <button class="btn" onclick="openAppManually()">앱으로 계속하기</button>
                </div>

                <script>
                    function executeDeepLink() {
                        var isAndroid = /android/i.test(navigator.userAgent);
                        var isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;

                        // 화면 숨김(앱 전환) 상태 확인용 타이머
                        var now = new Date().valueOf();
                        
                        // 현재 시간이 1.5초(1500ms) + 여유시간(500ms) 이상 흘렀다면 앱 구동/팝업 성공으로 간주, 아니면 미설치 스토어 이동
                        var fallbackTimer = setTimeout(function () {
                            if (new Date().valueOf() - now < 2000) {
                                if (isAndroid) {
                                    window.location.href = "$androidStoreUrl";
                                } else if (isIOS) {
                                    window.location.href = "$iosStoreUrl";
                                } else {
                                    alert("모바일 환경에서 앱을 설치해주세요.");
                                }
                            }
                        }, 1500);

                        // Clear timer if app is opened successfully and page is hidden
                        window.addEventListener('pagehide', function() {
                            clearTimeout(fallbackTimer);
                        });
                        document.addEventListener('visibilitychange', function() {
                            if (document.hidden) {
                                clearTimeout(fallbackTimer);
                            }
                        });

                        // 클립보드 복사(오토)
                        var copyText = document.getElementById("codeText");
                        copyText.select();
                        copyText.setSelectionRange(0, 99999);
                        try {
                            document.execCommand("copy");
                        } catch (err) {}

                        // 먼저 딥링크(앱 호출) 시도
                        window.location.href = "$appSchemeUrl";
                    }

                    function openAppManually() {
                        executeDeepLink();
                    }

                    // 스크립트 로드 시점에 자동 연결 시도
                    window.onload = executeDeepLink;
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
