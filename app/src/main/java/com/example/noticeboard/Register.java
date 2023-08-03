package com.example.noticeboard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    String[] faculty={"FCI", "FAST", "Science", "FOM", "FIS"};
    String[] course={"BIT", "BCS", "BSE"};
    String[] year={"1", "2", "3", "4", "5"};
    AutoCompleteTextView facultyTextView, courseTextView, yearTextView;
    ArrayAdapter<String> myFaculty, myCourse, myYear;
    private EditText full_name, reg_no, email, password, confirm_password;

    private TextInputLayout passwordLayout;
    private TextInputEditText passwordEditText;

    private TextView login;
    private Button register;
    private ImageView profile_image;
    private ProgressDialog progressDialog;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 200;
    private static final int REQUEST_IMAGE_UPLOAD = 300;

    private Uri imageURI;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private String deviceToken;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        // Obtain the device token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            // Device token obtained successfully
                            deviceToken = task.getResult();
                        } else {

                        }
                    }
                });


//Initializing the Views
        full_name=findViewById(R.id.full_name);
        reg_no=findViewById(R.id.reg_no);
        profile_image=findViewById(R.id.profile_image);
        facultyTextView=findViewById(R.id.faculty);
        courseTextView=findViewById(R.id.course);
        yearTextView=findViewById(R.id.year);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        confirm_password=findViewById(R.id.confirm_password);
        login=findViewById(R.id.login);
        register=findViewById(R.id.register);
        progressDialog=new ProgressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference("users");
        databaseReference=FirebaseDatabase.getInstance().getReference("users");

        //        Checking for Strong and Weak Password
        passwordLayout=findViewById(R.id.passwordLayout);
        passwordEditText=findViewById(R.id.password);

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String Password=charSequence.toString();
                if(Password.length() >= 6){
                    Pattern passwordPattern=Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher=passwordPattern.matcher(Password);
                    boolean isPasswordContainsCharacters=matcher.find();
                    if(isPasswordContainsCharacters){
                        passwordLayout.setHelperText("Strong Password");
                        passwordLayout.setError("");
                    }else{
                        passwordLayout.setHelperText("");
                        passwordLayout.setError("Weak Password, Enter a Special Character");
                    }
                }else{
                    passwordLayout.setHelperText("Enter Minimum 6 Characters");
                    passwordLayout.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        myFaculty=new ArrayAdapter<String>(this, R.layout.faculty_list, faculty);
        myCourse=new ArrayAdapter<String>(this, R.layout.course_list, course);
        myYear=new ArrayAdapter<String>(this, R.layout.year_list, year);

        facultyTextView.setAdapter(myFaculty);

        yearTextView.setAdapter(myYear);

        facultyTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFaculty=adapterView.getItemAtPosition(i).toString();
                if (selectedFaculty.isEmpty()) {
                    facultyTextView.setError("Faculty is required*");
                    facultyTextView.requestFocus();
                    return;
                } else if(selectedFaculty.equals("FCI")){
//                    If FCI is selected it displays courses in FCI only
                    courseTextView.setAdapter(myCourse);
                }else{
                    courseTextView.setText(null);
                    courseTextView.setAdapter(null);
                }
            }
        });


        courseTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedCourse=adapterView.getItemAtPosition(i).toString();
                if (selectedCourse.isEmpty()) {
                    courseTextView.setError("Course is required*");
                    courseTextView.requestFocus();
                    return;
                } else {
                    courseTextView.setError(null);
                }
            }
        });


        yearTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedYear=adapterView.getItemAtPosition(i).toString();
                if (selectedYear.isEmpty()) {
                    yearTextView.setError("Year is required*");
                    yearTextView.requestFocus();
                    return;
                } else {
                    yearTextView.setError(null);
                }
            }
        });


//        Login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });

//Register
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageURI != null) {
                    uploadDetails();
                } else {
                    Toast.makeText(Register.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        Upload Image Button
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionsDialog();
            }
        });

    }


    public void uploadDetails(){
        if(imageURI != null){
            String fullName=full_name.getText().toString().trim();
            String REG_NO=reg_no.getText().toString().trim();
            String Faculty=facultyTextView.getText().toString().trim();
            String Course=courseTextView.getText().toString().trim();
            String Year=yearTextView.getText().toString().trim();
            String Email=email.getText().toString().trim();
            String Password=password.getText().toString().trim();
            String Confirm=confirm_password.getText().toString().trim();

//            Validating the Fields
            if(fullName.isEmpty()){
                full_name.setError("Full Name Required*");
                full_name.requestFocus();
                return ;
            }else if(Email.isEmpty()){
                email.setError("Email Address Required*");
                email.requestFocus();
                return ;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                email.setError("Enter a valid email address");
                email.requestFocus();
                return;
            }else if(REG_NO.isEmpty()){
                reg_no.setError("REG NO Required*");
                reg_no.requestFocus();
                return ;
            }

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) facultyTextView.getAdapter();
            if (adapter != null && !adapter.isEmpty()) {
                boolean isValidSelection = false;
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).equals(Faculty)) {
                        isValidSelection = true;
                        break;
                    }
                }

                if (isValidSelection) {
                    // AutoCompleteTextView has a valid selection, proceed with registration
                    // Perform registration logic here
                } else {
                    facultyTextView.setError("Please Select Faculty*");
                    facultyTextView.requestFocus();
                    return;
                }
            }

            ArrayAdapter<String> courseAdapter = (ArrayAdapter<String>) courseTextView.getAdapter();
            if (courseAdapter != null && !courseAdapter.isEmpty()) {
                boolean isValidSelection = false;
                for (int i = 0; i < courseAdapter.getCount(); i++) {
                    if (courseAdapter.getItem(i).equals(Course)) {
                        isValidSelection = true;
                        break;
                    }
                }

                if (isValidSelection) {
                    // AutoCompleteTextView has a valid selection, proceed with registration
                    // Perform registration logic here
                } else {
                    courseTextView.setError("Please Select Course*");
                    courseTextView.requestFocus();
                    return;
                }
            }

            ArrayAdapter<String> yearAdapter = (ArrayAdapter<String>) yearTextView.getAdapter();
            if (yearAdapter != null && !yearAdapter.isEmpty()) {
                boolean isValidSelection = false;
                for (int i = 0; i < yearAdapter.getCount(); i++) {
                    if (yearAdapter.getItem(i).equals(Year)) {
                        isValidSelection = true;
                        break;
                    }
                }

                if (isValidSelection) {
                    // AutoCompleteTextView has a valid selection, proceed with registration
                    // Perform registration logic here
                } else {
                    yearTextView.setError("Please Select Year*");
                    yearTextView.requestFocus();
                    return;
                }
            }

            if(Password.isEmpty()){
                password.setError("Password Required*");
                password.requestFocus();
                return ;
            }else if(password.length() < 6){
                password.setError("Password Should be at least 6 characters");
                password.requestFocus();
                return ;
            }else if(!Password.equals(Confirm)){
                confirm_password.setError("Password Mismatched*");
                confirm_password.requestFocus();
                return;
            }else {
//                Check if User Exists or already registered
                Query checkUserEmail=databaseReference.orderByChild("email").equalTo(Email);
                checkUserEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Email already exists
                            Toast.makeText(Register.this, "Email already registered or taken", Toast.LENGTH_SHORT).show();
                        } else {
//                            Email does not exist continue with registering a user
                            firebaseAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        UploadDetailsToFirebase(imageURI);
                                    } else {
                                        Toast.makeText(Register.this, "Error  Failed to Register", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });

            }
        }
    }

    private void UploadDetailsToFirebase (Uri uri) {
        progressDialog.setTitle("Registration in Progress");
        progressDialog.setMessage("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        final StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String fullName=full_name.getText().toString().trim();
                        String REG_NO=reg_no.getText().toString().trim();
                        String Faculty=facultyTextView.getText().toString().trim();
                        String Course=courseTextView.getText().toString().trim();
                        String Year=yearTextView.getText().toString().trim();
                        String Email=email.getText().toString().trim();
                        String Password=password.getText().toString().trim();
                        String hashedPassword = hashPassword(Password);
                        String Role="user";

                        // Create a new user object with the data
                        UserRegistrationModal user = new UserRegistrationModal(fullName, REG_NO, uri.toString(), Faculty, Course, Year, Email, hashedPassword, Role, deviceToken);

                        // Get a reference to the "users" node
                        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toast.makeText(Register.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Register.this, Login.class));
                                    finish();
                                }else{
                                    Toast.makeText(Register.this, "Error Failed to register", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressDialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Register.this, "Registration Failed Try Again", Toast.LENGTH_LONG).show();
            }
        });
    }
    //Hashing Password using SHA-256 Algorithm
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    //Getting File Extension
    private String getFileExtension (Uri mUri){

        ContentResolver cr = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));

    }

    //    Options Dialog
    private void showOptionsDialog() {
        String[] options = {"Upload Picture", "Take Picture"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openGallery();
                } else if (which == 1) {
                    checkCameraPermission();
                }
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_UPLOAD);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request camera and storage permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera and storage permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    // Convert the bitmap to a URI
                    imageURI = getImageUriFromBitmap(bitmap);
                    // Set the imageURI to the ImageView
                    profile_image.setImageURI(imageURI);
                    // Rest of the code
                    Glide.with(this).load(imageURI).into(profile_image);
                    Toast.makeText(this, "Image uploaded from Camera", Toast.LENGTH_SHORT).show();
                }
            }else if (requestCode == REQUEST_IMAGE_UPLOAD) {
                imageURI = data.getData();
                profile_image.setImageURI(imageURI);
//                Loading Image
                if (imageURI != null) {
                    Glide.with(this).load(imageURI).into(profile_image);
                    Toast.makeText(this, "Image uploaded from gallery", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

//    Going back to Login
@Override
public void onBackPressed() {
    if (this.getClass() == Register.class) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

}


}