package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentStatePagerAdapter


// FeedFragment
class FeedFragment : Fragment(), ViewPager.OnPageChangeListener {

    // 変数定義
    private val LOG_TAG = FeedFragment::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var adapter: FragmentStatePagerAdapter
    private lateinit var viewPager: ViewPager


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FeedFragment.OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)

        // xmlからTabLayoutの取得
        val tabLayout = view.findViewById<TabLayout>(R.id.tabs)
        // xmlからViewPagerを取得
        viewPager = view.findViewById<ViewPager>(R.id.pager)
        // ページタイトル配列
        val pageTitle = arrayOf(
            "PR\nフィード",
            "ありがとう\nフィード"
        )

        // 表示Pageに必要な項目を設定
        adapter = object : FragmentStatePagerAdapter(getChildFragmentManager()) {
            // getItem
            override fun getItem(position: Int): Fragment {
                if (position == 0) {
                    return newPrFeedFragment()
                } else {
                    return newThanksFeedFragment()
                }
            }
            // getPageTitle
            override fun getPageTitle(position: Int): CharSequence? {
                return pageTitle[position]
            }
            // getCount
            override fun getCount(): Int = pageTitle.count()
        }

        // ViewPagerにページを設定
        viewPager.setAdapter(adapter)
        viewPager.addOnPageChangeListener(this)
        viewPager.setOffscreenPageLimit(1)

        // ViewPagerをTabLayoutを設定
        tabLayout.setupWithViewPager(viewPager);

        return view
    }

    // onDetach
    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
    }


    override fun onPageScrollStateChanged(state: Int) {
        return
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        return
    }

    override fun onPageSelected(position: Int) {
        return
    }

    // PRフィードデータの再取得(アダプターの再セット)
    fun PrFeedDataReload() {
        viewPager.setAdapter(adapter)
    }
}


fun newFeedFragment() : FeedFragment {
    return FeedFragment()
}
