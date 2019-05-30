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


// RankingFragment
class RankingFragment : Fragment(), ViewPager.OnPageChangeListener {

    // 変数定義
    private val LOG_TAG = RankingFragment::class.java.simpleName // ログ用にクラス名を取得


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is RankingFragment.OnFragmentInteractionListener) {
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
        val viewPager = view.findViewById<ViewPager>(R.id.pager)
        // ページタイトル配列
        val pageTitle = arrayOf(
            "もらったSP\n(総計)",
            "もらったSP\n(月間)",
            "おくったSP\n(総計)",
            "おくったSP\n(月間)"
        )

        // 表示Pageに必要な項目を設定
        val adapter = object : FragmentStatePagerAdapter(getChildFragmentManager()) {
            // getItem
            override fun getItem(position: Int): Fragment {
                val rankingContentFragment = newRankingContentFragment()
                val bundle = Bundle()
                bundle.putInt("position", position)
                rankingContentFragment.setArguments(bundle)
                return rankingContentFragment
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
}


fun newRankingFragment() : RankingFragment {
    return RankingFragment()
}
