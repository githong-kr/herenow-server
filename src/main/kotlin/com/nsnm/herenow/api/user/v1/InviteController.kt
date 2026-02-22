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
        @RequestParam(defaultValue = "herenow") scheme: String
    ): String {
        val appSchemeUrl = "$scheme://groups/join?code=$code"
        val clipboardText = "herenow-invite-$code"

        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>HereNow 스페이스 참여</title>
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
                    <h1>HereNow 스페이스 참여</h1>
                    <p class="desc">
                        <b>HereNow</b>는 친구, 가족, 룸메이트와 함께<br/>
                        물건의 위치와 상태를 공동으로 관리하는<br/>
                        스마트 공간 관리 서비스입니다.
                    </p>
                    <p class="sub-desc">버튼을 눌러 앱에서 스페이스에 합류하세요.<br/>미설치 시 스토어로 이동합니다.</p>
                    
                    <textarea id="codeText" class="hidden-input" readonly>$clipboardText</textarea>

                    <button class="btn" onclick="openApp()">앱으로 계속하기</button>
                </div>

                <script>
                    function openApp() {
                        var copyText = document.getElementById("codeText");
                        copyText.select();
                        copyText.setSelectionRange(0, 99999);
                        try {
                            document.execCommand("copy");
                        } catch (err) {}

                        // Open targeted scheme
                        window.location.href = "$appSchemeUrl";

                        // Finally Fallback to Store after 2.5s
                        var fallbackTimer = setTimeout(function() {
                            var userAgent = navigator.userAgent || navigator.vendor || window.opera;
                            if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) {
                                window.location.href = "$iosStoreUrl";
                            } else if (/android/i.test(userAgent)) {
                                window.location.href = "$androidStoreUrl";
                            } else {
                                alert("모바일 환경에서 앱을 설치해주세요.");
                            }
                        }, 2500);

                        // Clear timer if app is opened successfully and page is hidden
                        window.addEventListener('pagehide', function() {
                            clearTimeout(fallbackTimer);
                        });
                        document.addEventListener('visibilitychange', function() {
                            if (document.hidden) {
                                clearTimeout(fallbackTimer);
                            }
                        });
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
