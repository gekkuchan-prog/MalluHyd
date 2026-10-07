package com.example.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirebaseAuthRepository
 * Manages Google Sign-In, silent authentication, user session state, and sign-out.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Reactive Stream observing Firebase Authentication state transitions.
     * Emits the currently authenticated FirebaseUser or null if signed out.
     */
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.flowOn(ioDispatcher)

    /**
     * Retrieves the currently active user directly.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Checks if a user is currently signed in.
     */
    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    /**
     * Attempts background silent sign-in using existing Google credentials.
     * Useful for automatic session recovery on cold app launch.
     */
    suspend fun attemptSilentSignIn(context: Context): Result<FirebaseUser> = withContext(ioDispatcher) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return@withContext Result.failure(IllegalStateException("default_web_client_id not found", e))
        }

        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw IllegalStateException("Firebase user is null after sign-in")
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unsupported credential type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Interactive Google Sign-In using Jetpack CredentialManager.
     * Shows the Google account selection bottom sheet to authenticate or register.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> = withContext(Dispatchers.Main) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return@withContext Result.failure(IllegalStateException("default_web_client_id not found in strings.xml", e))
        }

        try {
            val credentialManager = CredentialManager.create(activity)
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = withContext(ioDispatcher) {
                    auth.signInWithCredential(authCredential).await()
                }
                val user = authResult.user ?: throw IllegalStateException("Firebase user is null after sign-in")
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type received: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("FirebaseAuthRepo", "Google Sign-In dismissed or cancelled: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    /**
     * Signs out from Firebase Authentication and clears CredentialManager credentials state.
     */
    suspend fun signOut(context: Context): Result<Unit> = withContext(ioDispatcher) {
        try {
            auth.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "Failed to clear credential state during sign-out", e)
            Result.failure(e)
        }
    }
}
