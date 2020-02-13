package com.stn.punchpower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    // 펀치력을 저장하는 변수. 사용하는 시점에 초기화되도록 lazy 위임 사용
    // 전달받은 값에 100을 곱하는 이유는 가속도 측정값이 소숫점 단위의 차이므로 가시성을 높이기 위해
    val power by lazy {
        intent.getDoubleExtra("power", 0.0) * 100
    }

    // 화면이 최초 생성될 때 호출되는 함수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 앱 상단 제목 변경
        title = "결과화면"

        // 점수를 표시하는 TextView 에 점수를 표시
        scoreLabel!!.text = "${String.format("%.0f", power)} 점"

        // 다시하기 버튼을 클릭하면 현재 액티비티 종료
        restartButton.setOnClickListener {
            finish()
        }

        // 구글 리더보드는 당장 필요한 기능이 아니므로 보류(page 666)
        rankingButton.setOnClickListener {
            finish()
        }
    }

}
