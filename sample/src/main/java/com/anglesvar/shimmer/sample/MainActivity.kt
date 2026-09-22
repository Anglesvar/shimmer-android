package com.anglesvar.shimmer.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anglesvar.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var shimmerAlpha: ShimmerFrameLayout
    private lateinit var shimmerColor: ShimmerFrameLayout
    private lateinit var shimmerRadial: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shimmerAlpha = findViewById(R.id.shimmer_alpha)
        shimmerColor = findViewById(R.id.shimmer_color)
        shimmerRadial = findViewById(R.id.shimmer_radial)

        val btnStart = findViewById<MaterialButton>(R.id.btn_start)
        val btnStop = findViewById<MaterialButton>(R.id.btn_stop)

        btnStart.setOnClickListener {
            shimmerAlpha.startShimmer()
            shimmerColor.startShimmer()
            shimmerRadial.startShimmer()
        }

        btnStop.setOnClickListener {
            shimmerAlpha.stopShimmer()
            shimmerColor.stopShimmer()
            shimmerRadial.stopShimmer()
        }
    }

    override fun onResume() {
        super.onResume()
        shimmerAlpha.startShimmer()
        shimmerColor.startShimmer()
        shimmerRadial.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        shimmerAlpha.stopShimmer()
        shimmerColor.stopShimmer()
        shimmerRadial.stopShimmer()
    }
}
