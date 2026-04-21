package Adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bgls.R
import com.example.bgls.BranchesFragment

class ViewPagerAdapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HeadOfficeFragment()
            1 -> BranchesFragment()
//            2 -> CalendarFragment()
//            3 -> ExceptionsFragment()
            else -> HeadOfficeFragment()
        }
    }
    class HeadOfficeFragment : Fragment(R.layout.fragment_head_office)

//    class CalendarFragment : Fragment(R.layout.fragment_empty)
//    class ExceptionsFragment : Fragment(R.layout.fragment_empty)

}
