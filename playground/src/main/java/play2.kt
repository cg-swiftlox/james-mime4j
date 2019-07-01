package playground

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.UnicastProcessor
import io.reactivex.rxkotlin.*
import java.util.concurrent.TimeUnit


fun main(){
    val myObservable = Observable.interval(1000, TimeUnit.MILLISECONDS)
    myObservable.take(20).subscribe { println("First: $it") }
    myObservable.take(20).subscribe { println("Second: $it") }

    Thread.sleep(10000)
}


fun playWithRx() {
    val myCounter = Observable.range(0,100000).doOnNext{
        println("counter: $it")
    }

    val myObs = Observable.just("aaa", "aasdf", "b343423333", "c", "d")
    val myObsCounter = Observables.zip(myObs,myCounter)
    myObs.subscribe { ele ->
        println(ele)
    }

    myObs.scan(Triple("",0, 0)){ pair, obj ->
        Triple(obj,pair.third+1,pair.third+obj.length)
    }.skip(1).subscribe { (o,f,t)->
        println("$o: $f-$t")
    }

    myObs.scan(Triple("",0, 0)){ pair, obj ->
        Triple(obj,pair.third+1,pair.third+obj.length)
    }.skip(1).doOnNext { println(it) }.map { it.first }.subscribe {
        println(it)
    }


    val myObs2 = BehaviorProcessor.create<String>()
    val nrLines: Single<Long> = myObs2.count()
    nrLines.subscribe { nrLines ->
        println(nrLines)
    }
    myObs2.onNext("abc1")
    myObs2.onNext("abc2")
    myObs2.onNext("abc3")
    myObs2.onNext("abc4")
    myObs2.onComplete()

    println()

//    myObs2.subscribe({ println(it) }, {
//        throw it
//    })
}