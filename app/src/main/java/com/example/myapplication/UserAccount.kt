package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore

import de.hdodenhof.circleimageview.CircleImageView


/**
 * User Fragment manages user data changes
 *
 */
class UserAccount : Fragment() {


    private lateinit var signOutButton: MaterialButton

    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val database = Firebase.firestore
    private lateinit var dialogView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_user_account, container, false)
    }


    /**
     * Manages user data changes:
     *  name change and ability to sign out
     *
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val changeAccountName = view.findViewById<MaterialTextView>(R.id.account_name)

        val userReviewsCount = view.findViewById<MaterialTextView>(R.id.reviews_count)
        val queryReviewCount = database.collectionGroup("Reviews").whereEqualTo(
            "userID", currentUser?.uid.toString()
        )

        queryReviewCount.get().addOnSuccessListener { snapshot ->
            val count = snapshot.size()
            userReviewsCount.text = count.toString()
        }


        val userCollectionsCount = view.findViewById<MaterialTextView>(R.id.collections_count)

        val query = database.collectionGroup("Reviews").whereEqualTo(
            "userID", currentUser?.uid.toString()
        ).whereEqualTo("isChecked", true)

        query.get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.size()
                userCollectionsCount.text = count.toString()
            }
            .addOnFailureListener { exception ->

            }


        if (currentUser?.displayName.equals(currentUser?.email)) {
            changeAccountName.text = currentUser?.email.toString()
        } else {
            changeAccountName.text = currentUser?.displayName.toString()
        }


        signOutButton = view.findViewById(R.id.sign_out_button)

        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val signOut = Snackbar.make(view, "Successfully Signed Out", Snackbar.LENGTH_LONG)
            signOut.show()

            val intent = Intent(requireContext(), LoginActivity::class.java)

            startActivity(intent)


        }

        val editButton = view.findViewById<MaterialButton>(R.id.edit_button)

        editButton.setOnClickListener {
            editAccount(view)
        }

    }


    /**
     * Edit the users account
     *
     * @param view
     */
    private fun editAccount(view: View) {

        val inflater = LayoutInflater.from(this.context)
        dialogView = inflater.inflate(R.layout.dialog_edit_account, null)
        view.findViewById<CircleImageView>(R.id.profile_image_icon)
        val accountNameEditText =
            dialogView.findViewById<TextInputEditText>(R.id.account_name_textField)


        val dialog = this.context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Create a new review")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->
                    currentUser?.updateProfile(userProfileChangeRequest {

                        displayName = accountNameEditText.text.toString()
                        view.findViewById<MaterialTextView>(R.id.account_name).text = displayName

                    })

                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()

                }.create()
        }
        dialog?.show()

    }

}