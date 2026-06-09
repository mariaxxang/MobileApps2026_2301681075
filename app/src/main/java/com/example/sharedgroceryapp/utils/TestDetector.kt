package com.example.sharedgroceryapp.utils

object TestDetector {
    val isUnderTest: Boolean by lazy {
        try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
