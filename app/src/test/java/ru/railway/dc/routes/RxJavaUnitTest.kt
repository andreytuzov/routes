package ru.railway.dc.routes

import android.util.Log
import io.reactivex.*
import io.reactivex.observers.DisposableObserver
import org.junit.Test
import junit.framework.Assert.assertTrue
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class RxJavaUnitTest {

    val list = mutableListOf(1, 2, 3, 4, 5)

    @Test
    fun test1() {
        val callable = Callable<Int> {
            1 + 1
        }
        Observable.create(ObservableOnSubscribe<Int> {

            it.onNext(1)
        })
    }
}