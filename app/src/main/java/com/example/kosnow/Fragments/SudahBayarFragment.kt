package com.example.kosnow.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kosnow.R

class SudahBayarFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sudah_bayar, container, false)
        val ivLogo = view.findViewById<ImageView>(R.id.iv_logo)
        Glide.with(requireContext()).load(R.drawable.ic_logo).into(ivLogo)
        return view
    }
}