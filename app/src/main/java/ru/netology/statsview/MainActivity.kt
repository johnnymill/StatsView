package ru.netology.statsview

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import ru.netology.statsview.ui.StatsView

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = findViewById<StatsView>(R.id.stats)
        view.postDelayed(
            {
                view.data = listOf(
                    500F,
                    500F,
                    500F,
                    500F,
                )
            },
            3000
        )

//        view.startAnimation(
//            AnimationUtils.loadAnimation(this, R.anim.animation)
//        )
    }
}
