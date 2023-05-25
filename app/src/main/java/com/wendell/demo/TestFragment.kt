package com.wendell.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wendell.demo.databinding.FragmentTestBinding
import com.wendell.thirdlogin.AuthorizationInfo
import com.wendell.thirdlogin.ErrorInfo
import com.wendell.thirdlogin.LoginCallback
import com.wendell.thirdlogin.LoginType
import com.wendell.thirdlogin.ThirdPartyLogin

class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThirdPartyLogin.initOnCreate(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGoogle.setOnClickListener {

            ThirdPartyLogin.login(requireContext(),
                LoginType.GoogleLogin,object : LoginCallback {
                override fun onSuccess(result: AuthorizationInfo, type: LoginType) {
                    Toast.makeText(context,result.toString(),Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ErrorInfo) {
                    Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                }

            })
        }
        binding.buttonFacebook.setOnClickListener {
            ThirdPartyLogin.login(requireContext(),
                LoginType.FacebookLogin,object : LoginCallback {
                override fun onSuccess(result: AuthorizationInfo, type: LoginType) {
                    Toast.makeText(context,result.toString(),Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ErrorInfo) {
                    Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                }

            })
        }
        binding.buttonTwitter.setOnClickListener {
            ThirdPartyLogin.login(requireContext(),
                LoginType.TwitterLogin,object : LoginCallback {
                override fun onSuccess(result: AuthorizationInfo, type: LoginType) {
                    Toast.makeText(context,result.toString(),Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ErrorInfo) {
                    Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                }

            })
        }
        binding.buttonLinkedin.setOnClickListener {
            ThirdPartyLogin.login(requireContext(),
                LoginType.LinkedInLogin,object : LoginCallback {
                override fun onSuccess(result: AuthorizationInfo, type: LoginType) {
                    Toast.makeText(context,result.toString(),Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ErrorInfo) {
                    Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}