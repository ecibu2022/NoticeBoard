package com.example.noticeboard;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateOfficialFragment extends Fragment {
    private CircleImageView profile_image;
    private EditText name, title, department, email, password;
    private Button create;
    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    public static final int RESULT_OK = -1;

    public CreateOfficialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_create_official, container, false);
        profile_image=view.findViewById(R.id.profile_image);
        name=view.findViewById(R.id.name);
        title=view.findViewById(R.id.title);
        email=view.findViewById(R.id.email);
        password=view.findViewById(R.id.password);
        department=view.findViewById(R.id.department);
        create=view.findViewById(R.id.create);
        progressDialog= new ProgressDialog(getContext());
        storageReference = FirebaseStorage.getInstance().getReference("users");
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth=FirebaseAuth.getInstance();

        //        Choosing Image
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOfficial();
            }
        });

        return view;
    }

    //    Choosing Image
    private void chooseImage() {
        // Open gallery to select image
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profile_image.setImageURI(imageUri);
        }
    }

    public void createOfficial() {
        if(imageUri!=null) {
            // Upload drug details to database
            String Name = name.getText().toString();
            String Title = title.getText().toString();
            String Department = department.getText().toString();

            if (!TextUtils.isEmpty(Name) && !TextUtils.isEmpty(Title)
                    && !TextUtils.isEmpty(Department) && imageUri != null) {
                uploadOfficialToFirebase(imageUri);
            }else {
                Toast.makeText(getContext(), "Select Profile Image", Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void uploadOfficialToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(getContext(), "Please Select Profile Image", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.setTitle("Official Creation in Progress");
            progressDialog.setMessage("Please wait....");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            // Get the file extension from the imageUri
            ContentResolver contentResolver = getContext().getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));

            final StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + extension);
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String Name = name.getText().toString();
                            String Title = title.getText().toString();
                            String Department = department.getText().toString();
                            String Email=email.getText().toString();
                            String Password=password.getText().toString();
                            String officialID=databaseReference.push().getKey();
                            String Role="official";

                           firebaseAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                               @Override
                               public void onComplete(Task<AuthResult> task) {
                                   if(task.isSuccessful()){
                                       OfficialRegistration officialRegistration=new OfficialRegistration(uri.toString(), Name, Title, Department, Email, Password, Role);
                                       databaseReference.child(officialID).setValue(officialRegistration).addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(Task<Void> task) {
                                               progressDialog.dismiss();
                                               Toast.makeText(getContext(), "Official Created Successfully", Toast.LENGTH_SHORT).show();
                                               clearFields();
                                           }
                                       }).addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(Exception e) {
                                               progressDialog.dismiss();
                                               Toast.makeText(getContext(), "Failed Error", Toast.LENGTH_SHORT).show();
                                           }
                                       });
                                   }
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(Exception e) {
                                   progressDialog.dismiss();
                                   Toast.makeText(getContext(), "Error Failed", Toast.LENGTH_SHORT).show();
                               }
                           });

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void clearFields(){
        profile_image.setImageResource(R.drawable.logo);
        name.setText("");
        title.setText("");
        department.setText("");
        email.setText("");
        password.setText("");
    }

}