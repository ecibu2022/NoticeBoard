package com.example.noticeboard;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminAccountFragment extends Fragment {
    private View formContainer;
    private Button editProfileButton, update_profile;
    private ImageView editImage;
    private CircleImageView profile_picture;
    String imageUrl;
    private TextView full_name, email, editEmail;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    public AdminAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_admin_account, container, false);

//        Initializing the Views
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        currentUser = mAuth.getCurrentUser();
        formContainer = view.findViewById(R.id.formContainer);
        editProfileButton = view.findViewById(R.id.editProfile);
        profile_picture=view.findViewById(R.id.profile_image);
        full_name=view.findViewById(R.id.full_name);
        email=view.findViewById(R.id.email);

        editImage=view.findViewById(R.id.editImage);
        editEmail=view.findViewById(R.id.editEmail);
        update_profile=view.findViewById(R.id.update_profile);

//        Showing Form For Editing Profile When Edit Profile Button Is Clicked
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (formContainer.getVisibility() == View.VISIBLE) {
                    formContainer.setVisibility(View.GONE);
                } else {
                    formContainer.setVisibility(View.VISIBLE);
                    // Retrieve the user data from TextViews
                    String Url=imageUrl;
                    String Email = email.getText().toString();

//                            Setting into Text Fields
                    Glide.with(getContext()).load(Url).into(editImage);
                    editEmail.setText(Email);
                }
            }
        });
//        End of the Form


        // Retrieve the user's profile data from Firebase
        mDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the user data from the snapshot
                imageUrl=snapshot.child("profileImage").getValue(String.class);
                String FullName = snapshot.child("fullName").getValue(String.class);
                String Email = snapshot.child("email").getValue(String.class);

                // Set the TextViews with the user data
                Glide.with(getContext()).load(imageUrl).into(profile_picture);
                full_name.setText(FullName);
                email.setText(Email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

        // Update Profile
        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve the user data from EditTexts
                String EditEmail = editEmail.getText().toString();

                // Update the user data in the Firebase database
                mDatabase.child(currentUser.getUid()).child("email").setValue(EditEmail);

                // Update the email in Firebase Authentication
                currentUser.updateEmail(EditEmail)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Email updated successfully
                                // Notify the user that the profile has been updated
                                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Email update failed
                                // Handle the failure, e.g., display an error message to the user
                                Toast.makeText(getContext(), "Email update failed", Toast.LENGTH_SHORT).show();
                            }
                        });

                // Setting new values updated
                // Retrieve the user's profile data from Firebase
                mDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get the user data from the snapshot
                        imageUrl = snapshot.child("profileImage").getValue(String.class);
                        String FullName = snapshot.child("fullName").getValue(String.class);
                        String Email = snapshot.child("email").getValue(String.class);

                        // Set the TextViews with the user data
                        Glide.with(getContext()).load(imageUrl).into(profile_picture);
                        full_name.setText(FullName);
                        email.setText(Email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle database error
                    }
                });

                // Close form
                formContainer.setVisibility(View.GONE);
            }
        });


        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an Intent to pick an image from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK ) {
            // Get the image URI from the Intent
            Uri imageUri = data.getData();

            // Upload the image to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("users");
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get the download URL of the uploaded image
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Update the user's profile image URL in Firebase Database
                            mDatabase.child(currentUser.getUid()).child("profileImage").setValue(uri.toString());
                            Glide.with(getContext()).load(imageUri).into(editImage);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle upload failure
                }
            });
        }
    }

    // Get the file extension of an image URI
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

}