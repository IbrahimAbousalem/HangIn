package com.abousalem.hanginkotlin.view.auth

import androidx.lifecycle.ViewModel
import com.abousalem.hanginkotlin.domain.repository.IAuthenticationRepository
import com.abousalem.hanginkotlin.entities.request.SignInRequest
import com.abousalem.hanginkotlin.entities.state.AuthState
import com.abousalem.hanginkotlin.entities.state.ErrorState
import com.abousalem.hanginkotlin.entities.state.LoadingState
import com.abousalem.hanginkotlin.entities.state.SuccessState
import io.reactivex.Observable
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val userRepository: IAuthenticationRepository): ViewModel(){

    fun signInUser(signInRequest: SignInRequest): Observable<AuthState> {
        return userRepository.signInUser(signInRequest)
            .map<AuthState>{ SuccessState(it)}
            .onErrorReturn { ErrorState(it.message?: "Invalid Email and Password!") }
            .startWith(LoadingState(loading = true))
            .concatWith(Observable.just(LoadingState(loading = false))) // i want to add save to sharedPreference.
    }
}