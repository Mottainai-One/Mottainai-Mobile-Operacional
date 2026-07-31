package com.mottainai.operacional;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword("admin@mottainai.com", "123456")
                .addOnSuccessListener(authResult -> {

                    db.collection("users")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                Log.d("Firestore", "Conectado! Total: " + queryDocumentSnapshots.size());
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", e.getMessage());
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("LOGIN", e.getMessage());
                });
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("Firestore", "Conectado!");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", e.getMessage());
                });
    }


}