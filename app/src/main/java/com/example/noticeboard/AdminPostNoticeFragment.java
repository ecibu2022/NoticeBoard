package com.example.noticeboard;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminPostNoticeFragment extends Fragment {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 200;
    private static final int REQUEST_IMAGE_UPLOAD = 300;

    private final String[] faculty = {"FCI", "FAST", "Science", "FOM", "FIS"};
    private final String[] course = {"BIT", "BCS", "BSE"};
    private final String[] year = {"1", "2", "3", "4", "5"};
    private TextInputLayout Faculty, Course, Year;
    private AutoCompleteTextView facultyTextView, courseTextView, yearTextView;
    private ImageView uploadImage;
    private Uri imageUri, fileUri;
    private String imageUrl, fileUrl;
    private EditText fileEditText, title, body;
    private CheckBox everyone, terms;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef, noticeRef, adminTokenRef;
    private StorageReference storageReference;
    private FirebaseUser currentUser;
    private String noticeID, Title;

    private static final int REQUEST_PICK_FILE = 2;

    public AdminPostNoticeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_post_notice, container, false);
        uploadImage = view.findViewById(R.id.noticeImage);
        fileEditText = view.findViewById(R.id.fileEditText);
        Button browseFileButton = view.findViewById(R.id.browseFileButton);
        title = view.findViewById(R.id.title);
        body = view.findViewById(R.id.body);
        everyone = view.findViewById(R.id.everyone);
        terms = view.findViewById(R.id.terms);
        Faculty = view.findViewById(R.id.Faculty);
        Course = view.findViewById(R.id.Course);
        Year = view.findViewById(R.id.Year);
        facultyTextView = view.findViewById(R.id.faculty);
        courseTextView = view.findViewById(R.id.course);
        yearTextView = view.findViewById(R.id.year);
        progressDialog = new ProgressDialog(getContext());
        Button post = view.findViewById(R.id.post);

        firebaseAuth = FirebaseAuth.getInstance();
        noticeRef = FirebaseDatabase.getInstance().getReference("approved_notices");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        adminTokenRef = FirebaseDatabase.getInstance().getReference("users");

        ArrayAdapter<String> myFaculty = new ArrayAdapter<>(getContext(), R.layout.faculty_list, faculty);
        ArrayAdapter<String> myCourse = new ArrayAdapter<>(getContext(), R.layout.course_list, course);
        ArrayAdapter<String> myYear = new ArrayAdapter<>(getContext(), R.layout.year_list, year);

        facultyTextView.setAdapter(myFaculty);
        courseTextView.setAdapter(myCourse);
        yearTextView.setAdapter(myYear);

//        Choose image
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionsDialog();
            }
        });

        // Choose file
        browseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check storage permission
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // If permission is granted, open the file picker
                    openFilePicker();
                } else {
                    // Request storage permission
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PICK_FILE);
                }
            }
        });

        // Post Notice Button
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFields();
            }
        });

        // Everyone when it is checked Disable Faculty, Course and Year
        everyone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    terms.setError(null);
                    facultyTextView.setEnabled(false);
                    courseTextView.setEnabled(false);
                    yearTextView.setEnabled(false);
                    Faculty.setEnabled(false);
                    Course.setEnabled(false);
                    Year.setEnabled(false);
                } else {
                    everyone.setEnabled(false);
                    facultyTextView.setEnabled(true);
                    courseTextView.setEnabled(true);
                    yearTextView.setEnabled(true);
                    Faculty.setEnabled(true);
                    Course.setEnabled(true);
                    Year.setEnabled(true);
                }
            }
        });

        return view;
    }

    // Validate Fields
    public void validateFields() {
        String Title = title.getText().toString().trim();
        String Body = body.getText().toString().trim();

        if (Title.isEmpty()) {
            title.setError("Title is Required*");
            title.requestFocus();
            return;
        } else if (Body.isEmpty()) {
            body.setError("Body is Required*");
            body.requestFocus();
            return;
        } else if (!terms.isChecked()) {
            terms.setError("Agree on Terms*");
            terms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        terms.setError(null);
                    } else {
                        terms.setError("Agree on Terms");
                    }
                }
            });
        } else {
            uploadNotice();
        }
    }

    // Uploading Notice
    public void uploadNotice() {
        progressDialog.setTitle("Posting Notice in Progress");
        progressDialog.setMessage("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String userId = currentUser.getUid();

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    noticeID=noticeRef.push().getKey();
                    String submittedBy = snapshot.child("fullName").getValue(String.class);
                    Title = title.getText().toString().trim();
                    String Body = body.getText().toString().trim();
                    String Everyone = null;
                    String Faculty = null;
                    String Course = null;
                    String Year = null;
                    String Terms = "Agreed";
                    String SubmissionDate = getCurrentDateTime();

                    String selectedFaculty = facultyTextView.getText().toString().trim();
                    String selectedCourse = courseTextView.getText().toString().trim();
                    String selectedYear = yearTextView.getText().toString().trim();

                    if (everyone.isChecked()) {
                        Everyone = "Everyone";
                    } else if (!selectedCourse.isEmpty() && selectedYear.isEmpty()) {
                        // Specific Course
                        Faculty = facultyTextView.getText().toString().trim();
                        Course = courseTextView.getText().toString().trim();
                    } else if (!selectedYear.isEmpty() && selectedCourse.isEmpty()) {
                        // Specific Year
                        Faculty = facultyTextView.getText().toString().trim();
                        Year = yearTextView.getText().toString().trim();
                    } else if (!selectedCourse.isEmpty() && !selectedYear.isEmpty()) {
                        // Specific Course and Year
                        Faculty = facultyTextView.getText().toString().trim();
                        Course = courseTextView.getText().toString().trim();
                        Year = yearTextView.getText().toString().trim();
                    } else if (!selectedFaculty.isEmpty()) {
                        // Specific Faculty
                        Faculty = facultyTextView.getText().toString().trim();
                    }

                    // Create a new notice object with the data
                    PostNoticeModal notice = new PostNoticeModal(noticeID, Title, Body, imageUrl, fileUrl, Everyone, Faculty, Course, Year, Terms, submittedBy, SubmissionDate);

                    noticeRef.child(noticeID).setValue(notice).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Send notification to the audience
                                sendNotificationToTargetAudience(notice.getId(), notice.getTitle());
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Notice Posted Successfully", Toast.LENGTH_SHORT).show();
                                clearFields();

                            } else {
                                Toast.makeText(getContext(), "Error Failed to Post Notice", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //    Send Notification to Target Audience
    private void sendNotificationToTargetAudience(String noticeId, String title) {
        DatabaseReference noticeRef = FirebaseDatabase.getInstance().getReference("Notices").child(noticeId);
        noticeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the target audience from the notice
                String targetAudience = dataSnapshot.child("everyone").getValue(String.class);

                // Query the users with the specified target audience from the database
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                // If the target audience is everyone, query all users with the user role
                queryUsersByRole("user", Title);

                if ("everyone".equals(targetAudience)) {
                    queryAllUsers(Title);
                } else {
                    // If the target audience is not everyone, query based on faculty, course, and year
                    String faculty = dataSnapshot.child("faculty").getValue(String.class);
                    String course = dataSnapshot.child("course").getValue(String.class);
                    String year = dataSnapshot.child("year").getValue(String.class);

                    if (faculty != null && !faculty.isEmpty() && course != null && !course.isEmpty() && year != null && !year.isEmpty()) {
                        // Query based on faculty, course, and year
                        queryUsersByFacultyCourseYear(faculty, course, year, Title);
                    } else if (faculty != null && !faculty.isEmpty() && course != null && !course.isEmpty()) {
                        // Query based on faculty and course
                        queryUsersByFacultyCourse(faculty, course, Title);
                    } else {
                        // Query based on faculty only
                        queryUsersByFaculty(faculty, Title);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    // Query users based on their role
    private void queryUsersByRole(String role, String noticeTitle) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query roleQuery = usersRef.orderByChild("role").equalTo(role);

        roleQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, noticeTitle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    // Query all users
    private void queryAllUsers(String noticeTitle) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to all users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, noticeTitle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Query users based on faculty, course, and year
    private void queryUsersByFacultyCourseYear(String faculty, String course, String year, String noticeTitle) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query facultyCourseYearQuery = usersRef.orderByChild("faculty").equalTo(faculty)
                .orderByChild("course").equalTo(course)
                .orderByChild("year").equalTo(year);

        facultyCourseYearQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, noticeTitle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    // Query users based on faculty and course
    private void queryUsersByFacultyCourse(String faculty, String course, String noticeTitle) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query facultyCourseQuery = usersRef.orderByChild("faculty").equalTo(faculty)
                .orderByChild("course").equalTo(course);

        facultyCourseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, noticeTitle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    // Query users based on faculty only
    private void queryUsersByFaculty(String faculty, String noticeTitle) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query facultyQuery = usersRef.orderByChild("faculty").equalTo(faculty);

        facultyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Send notifications to the retrieved users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.child("deviceToken").getValue(String.class);
                    if (userToken != null) {
                        // Send the notification using FCM
                        sendFCMNotification(userToken, noticeTitle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database error that occurred while fetching the data
            }
        });
    }

    private void sendFCMNotification(String userToken, String title) {
        // Set the FCM server key from Firebase Console
        String serverKey = "AAAASxz6AZI:APA91bELTl9eqIThc_9kJ3eTYWUYoLtVr1H9MS3AQHHKtSQOPa237wk6VNoRKZMeZqEFy9gh_xxS0zw_CekNpcw-NuAlLohCB_etwwC5GNw_il-Hz39L9sv5IuCHoEdiLvKcICxtli5_";

        // Create the FCM message data payload (customize as needed)
        Map<String, String> data = new HashMap<>();
        data.put("title", "New Notice Posted");
        data.put("body", title);

        // Create the FCM message body
        Map<String, Object> message = new HashMap<>();
        message.put("to", userToken);
        message.put("data", data);

        // Send the FCM message using OkHttp
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, new Gson().toJson(message));
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=" + serverKey)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("FCM", "Failed to send notification to user", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Notification sent to user");
                } else {
                    Log.e("FCM", "Failed to send notification to user");
                }
                response.close();
            }
        });
    }


    // Clearing Fields
    private void clearFields() {
        // Clear the values of the fields to their initial state
        title.setText("");
        body.setText("");
        everyone.setChecked(false);
        terms.setChecked(false);
        facultyTextView.setText(null);
        courseTextView.setText(null);
        yearTextView.setText(null);
        imageUri = null;
        fileUri = null;
        imageUrl = null;
        fileUrl = null;
    }

    // Get current date and time
    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Upload the file to Firebase Storage
    private void uploadFileToStorage() {
        progressDialog.setTitle("Uploading File");
        progressDialog.setMessage("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // Get the file extension
        String fileExtension = getFileExtension(fileUri);

        // Create a reference to the Firebase Storage location where you want to store the file
        storageReference = FirebaseStorage.getInstance().getReference().child("files").child(System.currentTimeMillis() + "." + fileExtension);

        // Upload the file to Firebase Storage
        storageReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the file download URL from Firebase Storage
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                fileUrl = uri.toString();
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed to get file URL", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to upload file", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToStorage(Uri imageUri) {
        progressDialog.setTitle("Uploading Image");
        progressDialog.setMessage("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // Create a reference to the Firebase Storage location where you want to store the image
        storageReference = FirebaseStorage.getInstance().getReference().child("images").child(System.currentTimeMillis() + ".jpg");

        // Upload the image to Firebase Storage
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the image download URL from Firebase Storage
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString(); // Save the download URL to imageUrl
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                                // Display the uploaded image using Glide
                                Glide.with(getContext()).load(imageUrl).into(uploadImage);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //Getting File Extension
    private String getFileExtension (Uri mUri){
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));

    }

    //    Options Dialog
    private void showOptionsDialog() {
        String[] options = {"Upload Image", "Take Picture"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request camera and storage permissions
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }
    // File picker method
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera and storage permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    // Convert the bitmap to a URI
                    imageUri = getImageUriFromBitmap(bitmap);
                    // Set the imageURI to the ImageView
                    uploadImage.setImageURI(imageUri);
                    // Rest of the code
                    Glide.with(getContext()).load(imageUri).into(uploadImage);
                    Toast.makeText(getContext(), "Image uploaded from Camera", Toast.LENGTH_SHORT).show();
                    uploadImageToStorage(imageUri);
                }
            } else if (requestCode == REQUEST_IMAGE_UPLOAD) {
                imageUri = data.getData();
                uploadImage.setImageURI(imageUri);
//                Loading Image
                if (imageUri != null) {
                    Glide.with(getContext()).load(imageUri).into(uploadImage);
                    Toast.makeText(getContext(), "Image uploaded from gallery", Toast.LENGTH_SHORT).show();
                    uploadImageToStorage(imageUri);
                }
            } else if (requestCode == REQUEST_PICK_FILE) {
                // Handle the file picked from the file picker
                if (data != null && data.getData() != null) {
                    fileUri = data.getData();
                    fileEditText.setText(getFileName(fileUri)); // Display the selected file name in the EditText

                    // Upload the file to Firebase Storage
                    uploadFileToStorage();
                }
            }
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


}