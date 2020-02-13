package com.stn.punchpower

import android.animation.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    // 최대 펀치력
    var maxPower = 0.0
    // 펀치력 측정이 시작되었는지 나타내는 변수
    var isStart = false
    // 펀치력 측정이 시작된 시간
    var startTime = 0L

    // Sensor 관리자 객체. lazy 로 실제 사용될 때 초기화
    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // 센서 이벤트를 처리하는 리스너
    val eventListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                // 측정된 센서 값이 선형 가속도 타입이 아니면 바로 리턴
                if (event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return@let

                // 각 좌표값을 제곱하여 음수값을 없애고, 값의 차이를 극대화
                val power = Math.pow(event.values[0].toDouble(), 2.0) + Math.pow(event.values[1].toDouble(), 2.0) + Math.pow(event.values[2].toDouble(), 2.0)

                // 측정된 편치력이 20 을 넘고 아직 측정이 시작되지 않은 경우
                if (power > 20 && !isStart) {
                    // 측정시작
                    startTime = System.currentTimeMillis()
                    isStart = true
                }

                // 측정이 시작된 경우
                if (isStart) {
                    // 애니메이션 제거
                    imageView.clearAnimation()

                    // 5초간 최대값을 측정. 현재 측정된 값이 지금까지 측정된 최대값보다 크면 최대값을 현재 값으로 변경
                    if (maxPower < power) maxPower = power

                    // 측정 중인 것을 사용자에게 알려줌
                    stateLabel.text = "측정중"

                    // 최초 측정 후 3초가 지났으면 종료
                    if (System.currentTimeMillis() - startTime > 3000) {
                        isStart = false
                        punchPowerTestComplete(maxPower)
                    }
                }
            }
        }
    }

    // 화면이 최초 생성될 때 호출되는 함수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Activity 가 사라졌다 다시 보일때마다 호출되는 함수
    override fun onStart() {
        super.onStart()
        initGame()
    }

    // 게임 초기화
    fun initGame() {
        maxPower = 0.0
        isStart = false
        startTime = 0L
        stateLabel.text = "흔들어 주세요~"

        // 센서의 변화 값을 처리할 리스너를 등록
        // TYPE_LINEAR_ACCELERATION 은 중력값을 제외하고 x, y, z 축에 측정된 가속도만 계산되어 나옴
        sensorManager.registerListener(
            eventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        // TYPE_LINEAR_ACCELERATION 외 센서
        // 1. TYPE_ACCELEROMETER: 중력을 포함해 3개의 모든 물리적 축((X, Y, Z)에서 장치에 적용되는 가속도(m/s2)를 측정
        // 2. TYPE_AMBIENT_TEMPERATURE: 실내 온도를 섭씨(℃)로 측정
        // 3. TYPE_GRAVITY: 3개의 모든 물리적 축 (X, Y, Z)에서 장치에 적용되는 가속도(m/s2)의 중력을 측정
        // 4. TYPE_GYROSCOPE: 3개의 물리적 축 (X, Y, Z) 각각을 중심으로 한 'rad/s'단위의 회전 속도를 측정
        // 5. TYPE_LIGHT: 주변 광 레벨(조명)을 측정
        // 6. TYPE_LINEAR_ACCELERATION: 중력을 제외한 3개의 모든 물리적 축 (X, Y, Z)에서 장치에 적용되는 가속도(m/s2)를 측정
        // 7. TYPE_MAGNETIC_FIELD: 3개의 모든 물리적 축 (X, Y, Z)에 대한 주변 자기장을 측정
        // 8. TYPE_ORIENTATION: 장치가 3개의 모든 물리적 축 (X, Y, Z)을 중심으로 회전하는 정도를 측정.
        //                      'API 레벨 3'에서 중력 센서와 지자기 센서를 사용하여 getRotationMatrix()
        //                      메소드와 함께 장치의 기울기 행렬 및 회전 행렬을 얻을 수 있음
        // 9. TYPE_PRESSURE: 주변 공기 압력을 hPa 또는 mbar 단위로 측정
        // 10. TYPE_PROXIMITY: 장치의 화면을 기준으로 한 cm 단위로 물체의 근접 거리를 측정.
        //                     이 센서는 일반적으로 핸드셋을 사람의 귀에 대고 있는지 여부를 확인하는데 사용
        // 11. TYPE_RELATIVE_HUMIDITY: 상대 습도(%)를 측정
        // 12. TYPE_ROTATION_VECTOR: 장치의 회전 벡터의 세 요소를 제공하여 장치의 방향을 측정
        // 13. TYPE_TEMPERATURE: 장치의 온도를 섭씨(℃)로 측정.
        //                       이 센서의 구현은 기기마다 다르기 때문에 API 레벨 14의 'TYPE_AMBIENT_TEMPERATURE' 센서로 대체됨

        // 안드로이드에서 '애니메이션'을 활용하느 가장 간다한 방법은 'XML 파일'을 이용하는 것
        // XML 파일을 사용하면, '애니메이션을 리소스로' 과닐할 수 있게 되기 때문에 재사용성이 높음
        // '애니메이션 리소스'를 만들기 위해서는 [app > res > anim] 디렉토리를 사용
        // 'View 애니메이션'은 안드로이드 초창기부터 지원한 기능으로서, 간단한 방법으로도 꽤 다양한 연출이 가능
        // [Android Resource File] 리소스 타입 : 'Animation'
        // View 애니메이션 시작
        /*val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.alpha_scale)
        imageView.startAnimation(animation)

        // 애니메이션의 리스너 설정
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                // 애니메이션이 반복될 때의 처리 코드를 작성
            }

            override fun onAnimationEnd(animation: Animation?) {
                // 애니메이션이 종료될 때의 코드를 작성
            }

            override fun onAnimationStart(animation: Animation?) {
                // 애니메이션이 시작될 때의 코드를 작성
            }
        })*/

        // 속성 애니메이션 시작
        // 속성 애니메이션을 불러옴 apply 함수를 사용하면 로딩된 Animator 가 this 로 지정됨
        // 속성 애니메이션을 XML 에서 로드할 때는 'AnimatorInflater' 클래스르 사용한다.
        AnimatorInflater.loadAnimator(this@MainActivity, R.animator.property_animation).apply {
            // 애니메이션 종료 리스너를 추가
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) { start() }
            })

            // 속성 애니메이션의 타겟을 글로브 이미지뷰로 지정
            // 뷰 애니메이션읜 경우 'View.startAnimation()' 함수에 파라미터로 애니메이션 객체를 넘겼지만,
            // 속성 애니메이션은 애니메이션 객체에서 타겟을 '이미지뷰'로 지정하는 점이 다르다.
            // 속성 애니메이션은 타겟이 꼭 '뷰'일 필요가 없기 때문이다.
            // 속성 애니메이션은 '뷰'가 아니더라도 애니메이션을 진행하며, 객체의 속성 이름이 같기만 하다면 속성값이 애니메이션이 된다.
            setTarget(imageView)
            // 애니메이션 시작
            start()
        }

        // 컬러 애니메이션 시작
        AnimatorInflater.loadAnimator(this@MainActivity, R.animator.color_anim).apply {
            // 컬러 애니메이션을 불러오고 ObjectAnimator 클래스로 캐스팅
            val colorAnimator = this@apply as? ObjectAnimator
            // colorAnimator 가 ObjectAnimator 인 경우에만 실행
            colorAnimator?.apply {
                // Evaluator 를 ArgbEvaluator() 로 설정
                // Evaluator 는 속성값이 어떤 식으로 변화해야 하는지 결정하는 클래스
                setEvaluator(ArgbEvaluator())
                // 타겟을 액티비티의 컨텐츠 뷰로 지정
                target = window.decorView.findViewById(android.R.id.content)
                // 애니메이션 시작
                start()
            }
        }
    }

    // 펀치력 측정이 완료된 경우 처리 함수
    fun punchPowerTestComplete(power: Double) {
        Log.d("MainActivity", "측정완료 - power: ${String.format("%.5f", power)}")
        sensorManager.unregisterListener(eventListener)
        val intent = Intent(this@MainActivity, ResultActivity::class.java)
        intent.putExtra("power", power)
        startActivity(intent)
    }

    // Activity 가 화면에서 사라지면 호출되는 함수
    override fun onStop() {
        super.onStop()
        try {
            sensorManager.unregisterListener(eventListener)
        } catch (e:Exception) {}
    }
}
