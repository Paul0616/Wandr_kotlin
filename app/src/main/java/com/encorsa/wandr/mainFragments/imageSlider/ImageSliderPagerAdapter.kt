package com.encorsa.wandr.mainFragments.imageSlider

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.encorsa.wandr.R
import com.encorsa.wandr.database.MediaDatabaseModel
import com.encorsa.wandr.databinding.SliderItemBinding

class ImageSliderPagerAdapter: PagerAdapter{
    var context: Context
    var images: List<MediaDatabaseModel>
    var onPageChangeListener: OnPageChangeListener

    lateinit var inflater: LayoutInflater
    var imageViews = ArrayList<ImageView>()

    constructor(context: Context, images: List<MediaDatabaseModel>, onPageChangeListener: OnPageChangeListener){
        this.context = context
        this.images = images
        this.onPageChangeListener = onPageChangeListener

    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view==`object` as LinearLayout

    override fun getCount(): Int = images.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = SliderItemBinding.inflate(inflater)
        imageViews.add(binding.sliderItemImageView)
//        val dis = DisplayMetrics()
//        activity.getWindowManager().getDefaultDisplay().getMetrics(dis)
//        val height = dis.heightPixels
//        val width = dis.widthPixels
//        image.minimumHeight = height
//        image.minimumWidth = width
        binding.progressBar6.visibility = View.VISIBLE
        Glide.with(context)
            .load(images[position].mediaUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(object : RequestListener<Drawable?>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar6.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar6.visibility = View.GONE
                    return false
                }
            })
            .into(binding.sliderItemImageView)
        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

    class OnPageChangeListener(private val pageSelected: (Int) -> Unit ={}): ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
           pageSelected(position)
        }
    }

}