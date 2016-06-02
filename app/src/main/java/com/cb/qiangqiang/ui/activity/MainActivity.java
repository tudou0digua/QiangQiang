package com.cb.qiangqiang.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.cb.qiangqiang.R;
import com.cb.qiangqiang.data.Constants;
import com.cb.qiangqiang.ui.fragment.BanKuaiFragment;
import com.cb.qiangqiang.ui.fragment.PersonFragment;
import com.cb.qiangqiang.ui.fragment.TieZiListFragment;
import com.cb.qiangqiang.util.Utils;
import com.cb.qiangqiang.view.ChangeColorIconWithText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private static final int HOT = 0;
    private static final int BAN_KUAI = 1;
    private static final int PERSON = 2;

    private ViewPager viewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments;
    private List<ChangeColorIconWithText> mTabs;
    private ChangeColorIconWithText hot;
    private ChangeColorIconWithText bankuai;
    private ChangeColorIconWithText person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View top = findViewById(R.id.top);
        Utils.initStatusAfterSetContentView(this, top);
        initView();
        initData();
    }

    private void initView(){
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        hot = (ChangeColorIconWithText) findViewById(R.id.tab_hot);
        bankuai = (ChangeColorIconWithText) findViewById(R.id.tab_bankuai);
        person = (ChangeColorIconWithText) findViewById(R.id.tab_person);
        hot.setOnClickListener(this);
        bankuai.setOnClickListener(this);
        person.setOnClickListener(this);

        hot.setIconAlpha(1.0f);
    }


    private void initData(){
        mFragments = new ArrayList<>();
//        HotFragment tabHot = new HotFragment();
        TieZiListFragment tabHot = (TieZiListFragment) TieZiListFragment.newInstance(Constants.RE_TIE);
        BanKuaiFragment tabBanKuai = new BanKuaiFragment();
//        CollectionFragment tabBanKuai = new CollectionFragment();
//        TestFragment tabBanKuai = new TestFragment();
        PersonFragment tabPerson = new PersonFragment();
//        CollectionFragment tabPerson = new CollectionFragment();
        mFragments.add(tabHot);
        mFragments.add(tabBanKuai);
        mFragments.add(tabPerson);

        mTabs = new ArrayList<>();
        mTabs.add(hot);
        mTabs.add(bankuai);
        mTabs.add(person);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(positionOffset > 0){
                    ChangeColorIconWithText left = mTabs.get(position);
                    ChangeColorIconWithText right = mTabs.get(position + 1);
                    left.setIconAlpha(1 - positionOffset);
                    right.setIconAlpha(positionOffset);
                }

            }

            @Override
            public void onPageSelected(int position) {
                resetView();
                mTabs.get(position).setIconAlpha(1.0f);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void resetView() {
        for (ChangeColorIconWithText item : mTabs) {
            item.setIconAlpha(0);
        }
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tab_hot:
                viewPager.setCurrentItem(HOT,false);
                break;
            case R.id.tab_bankuai:
                viewPager.setCurrentItem(BAN_KUAI,false);
                break;
            case R.id.tab_person:
                viewPager.setCurrentItem(PERSON,false);
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intent);
                break;
        }
    }
}
