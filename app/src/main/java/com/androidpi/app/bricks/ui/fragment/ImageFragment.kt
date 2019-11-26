package com.androidpi.app.bricks.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.androidpi.app.bricks.R
import com.androidpi.app.bricks.base.activity.BaseFragment
import com.androidpi.app.bricks.databinding.FragmentImageBinding
import com.bumptech.glide.Glide
import layoutbinder.annotations.BindLayout

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
class ImageFragment : BaseFragment() {

    @BindLayout(R.layout.fragment_image)
    lateinit var binding : FragmentImageBinding

    companion object {

        val ARG_URI = "ImageFragment.ARG_URI"

        fun newInstance(uri: Uri?) : ImageFragment{
            var bundle = Bundle()
            bundle.putParcelable(ARG_URI, uri)
            var fragment = ImageFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            val uri = arguments!!.getParcelable(ARG_URI) as Uri?
            Glide.with(this).load(uri).into(binding.imageView);
        }
    }

}