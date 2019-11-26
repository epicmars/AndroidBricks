package com.androidpi.app.bricks.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.androidpi.app.bricks.R
import com.androidpi.app.bricks.base.activity.BaseActivity
import com.androidpi.app.bricks.databinding.ActivityGifBinding
import com.androidpi.app.bricks.ui.fragment.ImageFragment
import com.androidpi.bricks.libgifsicle.Gifsicle
import layoutbinder.annotations.BindLayout
import java.io.File

/*
 * Copyright 2019 yinpinjiu@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class GifActivity : BaseActivity() {
    @BindLayout(R.layout.activity_gif)
    lateinit var binding: ActivityGifBinding

    val gifsicle = Gifsicle()
    var imageFileAdapter : ImageFileAdapter? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, GifActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageFileAdapter = ImageFileAdapter(supportFragmentManager)

        val download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val gif = File(download, "/gif/bigfig.gif")
        val workspace = File(download, "/gif/explode/")
        if (workspace.exists()) {
            workspace.mkdirs()
        }
//        gifsicle.gifImages(gif.path, workspace.path)

        binding.viewPager.adapter = imageFileAdapter
        imageFileAdapter?.setData(workspace.listFiles().toList())
    }
}

class ImageFileAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var images = mutableListOf<File>()

    override fun getItem(position: Int): Fragment {
        return ImageFragment.newInstance(Uri.fromFile(images.get(position)))
    }

    override fun getCount(): Int {
        return images.size
    }

    fun setData(data: Collection<File>?) {
        if (data.isNullOrEmpty()) {
            return
        }
        images.clear()
        images.addAll(data)
        notifyDataSetChanged()
    }
}