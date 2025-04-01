package com.example.personalexpensemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.personalexpensemanager.utility.InputHintRemover;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    TextView tv_register, tv_forgotPassword;
    EditText etEmail, etPassword;
    Button btnLogin, btnGoogleLogin;

    FirebaseAuth auth;
    FirebaseFirestore db;

    Intent intent;

    //UI elements and Firebase objects
    private static final int RC_SIGN_IN = 100;

    //identify the result of the Google sign-in activity
    private GoogleSignInClient mGoogleSignInClient;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin = findViewById(R.id.btn_Login);
        btnGoogleLogin = findViewById(R.id.btn_google_login);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_Password);
        tv_forgotPassword = findViewById(R.id.text_view_forgot_password);
        tv_register = findViewById(R.id.text_view_register);

        //view password icon click event
        final boolean[] isVisible = {false};
        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index 0=left, 1=top, 2=right, 3=bottom

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                    if (isVisible[0]) {
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_security, 0, R.drawable.icon_eye_off_24, 0);
                        isVisible[0] = false;
                    } else {
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_security, 0, R.drawable.icon_eye, 0);
                        isVisible[0] = true;
                    }

                    etPassword.setSelection(etPassword.getText().length());
                    return true;
                }
            }

            return false;
        });

        //set input fields' Hint behavior, when user click hint gone
        InputHintRemover.setHintBehavior(etEmail, "Email");
        InputHintRemover.setHintBehavior(etPassword, "******");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //link to register page
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Google Sign-in Setup
        //initialise GoogleSignInOptions
        //Request the user’s email and ID token
        //The token is required to link with Firebase
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //create client -- initialise the google sign-in client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //set click listener, opens a pop-up to choose Google Account
        btnGoogleLogin.setOnClickListener(v -> {
            Log.d("LoginActivity", "Google Sign-In button clicked");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        //normal email + password login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = etEmail.getText().toString();
                String passwordInput = etPassword.getText().toString();

                //user input validation
                if(emailInput.isEmpty() || passwordInput.isEmpty()){
                    Toast.makeText(LoginActivity.this, "All fields are required!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (passwordInput.length() < 6) {  // Password length validation
                    Toast.makeText(LoginActivity.this, "Password must be at least 6 characters!", Toast.LENGTH_LONG).show();
                    return;
                }
                //check if user email exists in Firestore
                db.collection("users").whereEqualTo("email", emailInput).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if(queryDocumentSnapshots.isEmpty()){
                                Toast.makeText(LoginActivity.this, "User email does not exist!", Toast.LENGTH_LONG).show();
                            } else {
                                loginUser(emailInput, passwordInput);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error checking user: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Error checking users: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

            //Login user in firebase with email and password
            private void loginUser(String emailInput, String passwordInput) {
                auth.signInWithEmailAndPassword(emailInput, passwordInput)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userID = auth.getCurrentUser().getUid();
                                Log.d("LoginSuccess", "Logged in with UID = " + userID);
                                db.collection("users").document(userID).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                Boolean enabled = documentSnapshot.getBoolean("enabled");
                                                if (enabled == null || !enabled) {
                                                    FirebaseAuth.getInstance().signOut();
                                                    Toast.makeText(LoginActivity.this, "Your account has been disabled, please contact admin for support!.", Toast.LENGTH_LONG).show();
                                                    return;
                                                }

                                                String role = documentSnapshot.getString("role");
                                                if ("Admin".equalsIgnoreCase(role)) {
                                                    intent = new Intent(LoginActivity.this, DashboardAdminActivity.class);
                                                } else if ("Accountant".equalsIgnoreCase(role)) {
                                                    intent = new Intent(LoginActivity.this, DashboardAccountantActivity.class);
                                                } else {
                                                    intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                                }

                                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "User data not found!", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error fetching user role: " + e.getMessage(), e);
                                            Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                            }
                        });
            }

        });

        //forgot password
        tv_forgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();

            if (email.isEmpty()) {
                etEmail.setError("Enter your email first");
                etEmail.requestFocus();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }

    //Handle Google Sign-In Result
    //Android gives us the result of startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Try to get user’s Google account
        //If successful, get the token → use it to sign in with Firebase.
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("Google Sign-In", "signInResult:failed code=" + e.getStatusCode(), e);
                Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Convert the Google ID token to Firebase credentials and sign in
    //Once signed in, get the user's UID.
    private void firebaseAuthWithGoogle(String idToken) {
        auth.signInWithCredential(
                        com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        String userID = auth.getCurrentUser().getUid();

                        //check firestore if user already exists,
                        db.collection("users").document(userID).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // User already exists in Firestore, redirect by role
                                        Boolean enabled = documentSnapshot.getBoolean("enabled");
                                        if (enabled == null || !enabled) {
                                            FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(LoginActivity.this, "Account disabled, contact admin.", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        // 🎯 Role-based redirection
                                        String role = documentSnapshot.getString("role");
                                        if ("Admin".equalsIgnoreCase(role)) {
                                            intent = new Intent(LoginActivity.this, DashboardAdminActivity.class);
                                        } else if ("Accountant".equalsIgnoreCase(role)) {
                                            intent = new Intent(LoginActivity.this, DashboardAccountantActivity.class);
                                        } else {
                                            intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                        }
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        //if is a new user → add to Firestore
                                        String username = auth.getCurrentUser().getDisplayName();
                                        String email = auth.getCurrentUser().getEmail();

                                        // Store with default role: USER
                                        db.collection("users").document(userID)
                                                .set(new java.util.HashMap<String, Object>() {{
                                                    put("firebaseUid", userID);
                                                    put("username", username);
                                                    put("email", email);
                                                    put("role", "USER");
                                                    put("balance", "0.00");
                                                    put("enabled", true);
                                                }})
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(LoginActivity.this, "Welcome, new user!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(LoginActivity.this, "Firestore error", Toast.LENGTH_SHORT).show();
                                                    Log.e("GoogleSignIn", "Firestore error", e);
                                                });
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                    }
                });
    }


//    //remove hint when user click and type in input field
//    private void setHintBehavior(EditText editText, String hintText){
//        editText.setOnFocusChangeListener((v, hasFocus) -> {
//            if(hasFocus){
//                editText.setHint("");
//            } else {
//                if (editText.getText().toString().isEmpty()){
//                    editText.setHint(hintText);
//                }
//            }
//        });
//    }
}