package com.example.mateusz.colorpicker

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.jacekmarchwicki.universaladapter.BaseAdapterItem
import com.jacekmarchwicki.universaladapter.ViewHolderManager
import com.jakewharton.rxbinding2.support.v7.widget.RecyclerViewScrollEvent
import com.jakewharton.rxbinding2.support.v7.widget.scrollEvents
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.disposables.SerialDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_color_item.view.*
import kotlinx.android.synthetic.main.item_row_item.view.*
import java.io.IOException
import java.io.InputStream
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    data class RowScrollData(val id: Int, val offset: Int, val firstPosition: Int)

    private val disposable = SerialDisposable()
    private val scrollSubject = PublishSubject.create<RowScrollData>()

//    private val scrollOffsetObservable = scrollSubject
//            .scan(-1 to 0, { previous, current -> current.first to previous.second + current.second })
//            .replay(1)
//            .refCount()


    private fun loadColors(): List<SmallColor> {
        val inputStream: InputStream = try {
            assets.open("output.json")
        } catch (e: IOException) {
            throw RuntimeException(e.message)
        }

        val response: List<SmallColor> = try {
            val json = inputStream.bufferedReader().use { it.readText() }
            Gson().fromJson<List<SmallColor>>(json)
        } catch (e: IOException) {
            throw RuntimeException(e.message)
        }

        try {
            inputStream.close()
        } catch (e: IOException) {
            throw RuntimeException(e.message)
        }
        return response
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPool = RecyclerView.RecycledViewPool()
        val rxAdapter = RxUniversalAdapter(listOf(RowItemHolderManager(sharedPool)))

//        login_test.background= LoginDrawable(this)

        val parsed = loadColors()

        drawable_test.background = ColorPickerDrawable(this, parsed)

        scroll_test.setColor(parsed)

        main_recycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
                recycleChildrenOnDetach = true
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            setHasFixedSize(true)
            adapter = rxAdapter
        }

//        val snapHelper = StartSnapHelper()
//        snapHelper.attachToRecyclerView(main_recycler)


        val rowColors: IntArray = resources.getIntArray(R.array.row_colors)

        val shouldBlockScroll: Observable<Boolean> = scroll_test.verticalPositionObservable
                .switchMap { Observable.timer(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map { false }.startWith(true) }
                .startWith(false)
                .replay(1)
                .refCount()

        val scrollOffsetObservable: Observable<ScrollData> = scrollSubject
                .scan(-1 to 0, { previous, current -> current.id to previous.second + current.offset })
                .withLatestFrom(shouldBlockScroll, BiFunction { params: Pair<Int, Int>, block: Boolean -> ScrollData(params.first, params.second, block) })
                .replay(1)
                .refCount()

//        val scrollOffsetObservable123 = scrollOffset
//                .replay(1)
//                .refCount()

        //10
        val chunkedList = parsed.chunked(7)
        val colors: List<BaseAdapterItem> = listOf<BaseAdapterItem>()
                .plus(chunkedList.mapIndexed { index, color -> RowItem(index, color.map { ColorItem(Color.parseColor(it.hex)) }, scroll_test.verticalPositionObservable, scrollOffsetObservable, scrollSubject) })

        val colors1: List<BaseAdapterItem> = listOf<BaseAdapterItem>()
                .plus(RowItem(-1, parsed.map { ColorItem(Color.parseColor(it.hex)) }, scroll_test.verticalPositionObservable, scrollOffsetObservable, scrollSubject))
                .plus(rowColors.mapIndexed { index, color -> RowItem(index, generateColors(color), scroll_test.verticalPositionObservable, scrollOffsetObservable, scrollSubject) })

        disposable.setFrom(
                main_recycler.scrollEvents()
                        .withLatestFrom(shouldBlockScroll, BiFunction { _: RecyclerViewScrollEvent, shouldBlock: Boolean -> shouldBlock })
                        .filter { !it }
                        .map { (main_recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() }
                        .distinctUntilChanged()
                        .subscribe { scroll_test.moveHorizontally(it) },
                scrollSubject.map { it.firstPosition }
                        .withLatestFrom(shouldBlockScroll, BiFunction { firstPosition: Int, shouldScroll: Boolean -> firstPosition to shouldScroll })
                        .filter { (_, shouldScroll) -> !shouldScroll }
                        .distinctUntilChanged()
                        .subscribe { (firstPosition, _) -> scroll_test.moveVertically(firstPosition) },
                scroll_test.horizontalPositionObservable
                        .subscribe {
                            (main_recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(it, 0)
                        },
                Observable.fromCallable { colors }
                        .subscribe(rxAdapter))

    }

    data class ScrollData(val id: Int = -1, val offset: Int = 0, val block: Boolean = false)

    override fun onDestroy() {
        super.onDestroy()
        disposable.set(Disposables.empty())
    }

    private fun generateColors(color: Int): List<BaseAdapterItem> {
        return listOf<BaseAdapterItem>()
                .plus((0..9).map {
                    ColorItem(modifyBrightness(color, 1 - (it / 9F)))
                })
    }


    private fun modifyBrightness(color: Int, brightness: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= brightness // value component
        return Color.HSVToColor(hsv)
    }


    data class ColorItem(val color: Int) : KotlinBaseAdapterItem<Int> {
        override fun itemId(): Int = color
    }

    data class RowItem(val id: Int,
                       val items: List<BaseAdapterItem>,
                       val verticalPositionObservable: Observable<Int>,
                       val scrollObservable: Observable<ScrollData>,
                       val scrollObserver: Observer<RowScrollData>) : KotlinBaseAdapterItem<String> {
        override fun itemId(): String = items.toString()
    }

    class ColorItemHolderManager : ViewHolderManager {
        override fun createViewHolder(parent: ViewGroup, inflater: LayoutInflater) =
                ViewHolder(inflater.inflate(R.layout.item_color_item, parent, false))

        override fun matches(baseAdapterItem: BaseAdapterItem) = baseAdapterItem is ColorItem

        inner class ViewHolder(itemView: View) : ViewHolderManager.BaseViewHolder<ColorItem>(itemView) {
            override fun bind(item: ColorItem) {
                itemView.paint_view.setBackgroundColor(item.color)
            }
        }
    }

    class RowItemHolderManager(private val sharedPool: RecyclerView.RecycledViewPool) : ViewHolderManager {
        override fun createViewHolder(parent: ViewGroup, inflater: LayoutInflater) =
                ViewHolder(inflater.inflate(R.layout.item_row_item, parent, false))

        override fun matches(baseAdapterItem: BaseAdapterItem) = baseAdapterItem is RowItem

        inner class ViewHolder(itemView: View) : ViewHolderManager.BaseViewHolder<RowItem>(itemView) {
            private val disposable = SerialDisposable()
            private val snapHelper = StartSnapHelper()

            override fun bind(item: RowItem) {
                val rxAdapter = RxUniversalAdapter(listOf(ColorItemHolderManager()))
                itemView.row_recycler.apply {
                    layoutManager = LinearLayoutManager(context).apply {
                        recycleChildrenOnDetach = true
                    }
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                    recycledViewPool = sharedPool
                    adapter = rxAdapter
                }

//                itemView.row_recycler.onFlingListener = null
//                snapHelper.attachToRecyclerView(itemView.row_recycler)

                disposable.setFrom(Observable.fromCallable { item.items }
                        .subscribe(rxAdapter),
                        item.verticalPositionObservable
                                .distinctUntilChanged()
                                .subscribe {
                                    //                                    (itemView.row_recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, -(it*384))
                                    (itemView.row_recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(it, 0)
                                },
                        item.scrollObservable
                                .filter {
                                    when {
                                        it.block -> false
                                        item.id != it.id -> true
                                        else -> true
                                    }
                                }
                                .subscribe {
                                    val position = it.offset / 384
                                    Log.e("OffsetShit", "" + it + " Position " + position + " Offsetleft " + (it.offset - position * 384) + " CURRENTOFFWET " + itemView.row_recycler.computeVerticalScrollOffset())
//                                    (itemView.row_recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, -it.offset - position * 384)
                                    (itemView.row_recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, -it.offset)
//                                    (itemView.row_recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, itemView.row_recycler.computeVerticalScrollOffset()+it.offset)
                                },
                        itemView.row_recycler.scrollEvents()
                                .subscribe {
                                    item.scrollObserver.onNext(RowScrollData(item.id, it.dy(), (itemView.row_recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()))
                                }
                )
            }

            override fun onViewRecycled() {
                super.onViewRecycled()
                disposable.set(Disposables.empty())
            }
        }
    }
}


fun SerialDisposable.setFrom(vararg disposables: Disposable) = set(CompositeDisposable(disposables.asList()))

data class SmallColor(@SerializedName("name_en") val name: String,
                      @SerializedName("hex") val hex: String,
                      @SerializedName("code") val code: String)

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)