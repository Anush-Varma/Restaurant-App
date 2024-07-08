package com.example.myapplication

import android.app.Activity

import android.content.Intent
import android.os.Bundle


import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore

/**
 * Login Activity
 *
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser


    private lateinit var googleSignInClient: GoogleSignInClient
    private val database = Firebase.firestore


    private lateinit var emailTextFieldLogin: TextInputEditText
    private lateinit var passwordTextFieldLogin: TextInputEditText

    private lateinit var emailTextFieldSignUp: TextInputEditText
    private lateinit var passwordTextFieldSignUp: TextInputEditText


    /**
     * Always start app with Login or keep user logged in
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (mAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_ID))
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<MaterialButton>(R.id.email_login_button).setOnClickListener {
            emailLoginDialog()
        }

        findViewById<MaterialButton>(R.id.email_signup_button).setOnClickListener {
            emailSignUpDialog()
        }

        findViewById<MaterialButton>(R.id.google_login_button).setOnClickListener {
            googleSignIn()
        }
        findViewById<MaterialButton>(R.id.guest_login_button).setOnClickListener {
            guestLogin()
        }


    }

    /**
     * Login a user as a guest
     *
     */
    private fun guestLogin() {

        val rootView = findViewById<View>(android.R.id.content)
        if (mAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            mAuth.signInAnonymously().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    currentUser = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val guestError = Snackbar.make(
                        rootView,
                        "Error while creating guest Account",
                        Snackbar.LENGTH_LONG
                    )
                    guestError.show()
                }
            }
        }
    }

    /**
     * Setup an account using Email and password
     *
     */
    private fun emailSignUpDialog() {

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_register, null)


        val passwordTextLayout =
            dialogView.findViewById<TextInputLayout>(R.id.password_text_layout_register)

        passwordTextLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        val rootView = findViewById<View>(android.R.id.content)

        emailTextFieldSignUp = dialogView.findViewById(R.id.email_textfield_register)
        passwordTextFieldSignUp = dialogView.findViewById(R.id.password_textField_register)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Create an Account")
            .setView(dialogView)
            .setPositiveButton("Sign Up") { _, _ ->
                if (emailTextFieldSignUp.text.toString()
                        .isBlank() or emailTextFieldSignUp.text.toString().isBlank()
                ) {
                    val invalidSignUp =
                        Snackbar.make(rootView, "Invalid Details Entered", Snackbar.LENGTH_LONG)
                    invalidSignUp.show()
                } else {
                    signUpClick(rootView)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                val cancelledSignUp =
                    Snackbar.make(rootView, "Sign Up Cancelled", Snackbar.LENGTH_LONG)
                cancelledSignUp.show()
                dialog.dismiss()
            }
            .create()

        dialog.show()


    }


    /**
     * Display Login Dialog
     *
     */

    private fun emailLoginDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_login, null)


        val passwordTextLayout = dialogView.findViewById<TextInputLayout>(R.id.password_text_layout)

        passwordTextLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        val rootView = findViewById<View>(android.R.id.content)

        emailTextFieldLogin = dialogView.findViewById(R.id.email_textfield)
        passwordTextFieldLogin = dialogView.findViewById(R.id.password_textField)


        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Login")
            .setView(dialogView)
            .setPositiveButton("Login") { _, _ ->
                if (emailTextFieldLogin.text.toString()
                        .isBlank() or passwordTextFieldLogin.text.toString().isBlank()
                ) {
                    val invalidLogin =
                        Snackbar.make(rootView, "Invalid Details Entered", Snackbar.LENGTH_LONG)
                    invalidLogin.show()
                } else {
                    loginClick(rootView)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                val cancelledLogin =
                    Snackbar.make(rootView, "Login Cancelled", Snackbar.LENGTH_LONG)
                cancelledLogin.show()
                dialog.dismiss()
            }
            .create()


        dialog.show()
    }


    /**
     * Register a new user using email and password
     *
     * @param rootView
     */
    private fun signUpClick(rootView: View) {
        if (mAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            if (emailTextFieldSignUp.text.toString()
                    .isBlank() or passwordTextFieldSignUp.text.toString().isBlank()
            ) {
                val invalidLogin =
                    Snackbar.make(rootView, "Invalid Details Entered", Snackbar.LENGTH_LONG)
                invalidLogin.show()
            } else {
                mAuth.createUserWithEmailAndPassword(
                    emailTextFieldSignUp.text.toString(),
                    passwordTextFieldSignUp.text.toString()
                ).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val successfulSignUpSnackBar =
                            Snackbar.make(rootView, "Successful Sign Up", Snackbar.LENGTH_LONG)
                        successfulSignUpSnackBar.show()
                        currentUser?.let {

                            val defaultImage =
                                hashMapOf("profileImage" to "drawable://" + R.drawable.profile_image)

                            database.collection("Users").document(it.uid).set(defaultImage)
                        }

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        val failedSignUpSnackBar =
                            Snackbar.make(rootView, "Account already exits", Snackbar.LENGTH_SHORT)
                        failedSignUpSnackBar.show()
                    }

                }
            }
        }
    }


    /**
     * Login a returning user
     *
     * @param rootView
     */

    private fun loginClick(rootView: View) {
        mAuth.signInWithEmailAndPassword(
            emailTextFieldLogin.text.toString(), passwordTextFieldLogin.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val successfulLoginSnackBar =
                    Snackbar.make(rootView, "Successful Login", Snackbar.LENGTH_LONG)
                successfulLoginSnackBar.show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                val failedLoginSnackBar =
                    Snackbar.make(rootView, "Incorrect Email or Password", Snackbar.LENGTH_SHORT)
                failedLoginSnackBar.show()
            }
        }
    }


    /**
     * use Google to signUp
     *
     */
    private fun googleSignIn() {
        val signInClient = googleSignInClient.signInIntent
        launcher.launch(signInClient)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                manageResults(task)
            } else {
                Toast.makeText(this, "Login/sign up Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Manages google login types
     *
     * @param task
     */

    private fun manageResults(task: Task<GoogleSignInAccount>) {
        val account: GoogleSignInAccount? = task.result
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {
                if (task.isSuccessful) {



                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()

                }
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }


}


