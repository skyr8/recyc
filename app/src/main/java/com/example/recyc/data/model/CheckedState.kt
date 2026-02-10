package com.example.recyc.data.model

enum class CheckedState {
    CONFIRM, SKIP, UNCHECKED
}

fun toCheckState(isConfirmed: Boolean, isSkipped: Boolean): CheckedState {
    return when {
        isConfirmed -> CheckedState.CONFIRM
        isSkipped -> CheckedState.SKIP
        else -> CheckedState.UNCHECKED
    }
}