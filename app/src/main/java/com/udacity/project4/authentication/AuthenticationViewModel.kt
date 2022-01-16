package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.base.BaseViewModel
import org.koin.core.inject

class AuthenticationViewModel (override val app : Application) :
    BaseViewModel(app) {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    private val auth: LiveData<FirebaseUser?> by inject()

    val authenticationState = Transformations.map(auth) { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}