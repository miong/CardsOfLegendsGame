package com.bubul.col.game.core.utils

interface LiveDataListener<T> {
    fun onChange(newValue: T, oldValue: T)
}

class LiveData<T>(private var data: T) {

    private val listeners = mutableListOf<LiveDataListener<T>>()

    fun set(value: T) {
        val oldData = data
        data = value
        for (l in listeners) {
            l.onChange(data, oldData)
        }
    }

    fun get(): T {
        return data
    }

    fun addListener(l: LiveDataListener<T>) {
        listeners.add(l)
    }

    fun removeListener(l: LiveDataListener<T>) {
        listeners.add(l)
    }

}