package com.example.bgls.Adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bgls.R
import com.example.bgls.fragments.BranchesFragment
import com.example.bgls.fragments.CalendarFragment
class ViewPagerAdapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HeadOfficeFragment()
            1 -> BranchesFragment()
            2 -> CalendarFragment()
//            3 -> ExceptionsFragment()
            else -> HeadOfficeFragment()
        }
    }
    class HeadOfficeFragment : Fragment(R.layout.fragment_head_office)

//    class com.example.bgls.CalendarFragment : Fragment(R.layout.fragment_empty)
//    class ExceptionsFragment : Fragment(R.layout.fragment_empty)

}
