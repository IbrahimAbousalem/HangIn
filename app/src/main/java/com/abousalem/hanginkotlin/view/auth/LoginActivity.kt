package com.abousalem.hanginkotlin.view.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.abousalem.hanginkotlin.R
import com.abousalem.hanginkotlin.entities.request.SignInRequest
import com.abousalem.hanginkotlin.entities.response.AuthResponse
import com.abousalem.hanginkotlin.entities.state.ErrorState
import com.abousalem.hanginkotlin.entities.state.LoadingState
import com.abousalem.hanginkotlin.entities.state.SuccessState
import com.abousalem.hanginkotlin.extenstion.hideKeyboard
import com.abousalem.hanginkotlin.extenstion.isOnline
import com.abousalem.hanginkotlin.extenstion.toast
import com.abousalem.hanginkotlin.view.base.BaseActivity
import com.abousalem.hanginkotlin.view.base.ViewModelProvidersFactory
import com.abousalem.hanginkotlin.view.main.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvidersFactory
    private lateinit var authViewModel: AuthViewModel
    private val disposable: CompositeDisposable? = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        getActivityComponent().inject(this)
        authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        loginButton.setOnClickListener {
            validateAndSignIn()
        }
        loginNewAccountTV.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun validateAndSignIn() {
        hideKeyboard()

        val email = loginEmailET.text?.toString()?.trim()
        val password = loginPasswordET.text?.toString()?.trim()

        if(email!!.isEmpty()||password!!.isEmpty())return
        if(!isOnline()) {
            toast("No Internet Connection!")
            return
        }

        val user = SignInRequest(email = email, password = password)

        disposable?.add(authViewModel.signInUser(user)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {when(it) {
                is LoadingState ->{
                    if(it.loading) {
                        loginPB.visibility = View.VISIBLE
                        Timber.d("I'm Loading")
                    }else {
                        loginPB.visibility = View.GONE
                        Timber.d("I'm done Loading")
                    }
                }
                is ErrorState->{
                    toast(it.message)
                    Timber.d("I'm error State ${it.message}")
                }
                is SuccessState->{
                    Timber.d("I'm Success State ${it.data?.authToken}")
                    saveToSharedPref(it.data)
                    startActivity(Intent(this, MainActivity::class.java))
                }}},{}))
    }

    private fun saveToSharedPref(data: AuthResponse?) {
        authViewModel.saveUserToken(data!!)
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }
}
