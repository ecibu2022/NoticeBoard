package com.example.noticeboard;

import static android.app.Activity.RESULT_OK;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PostNoticeFragment extends Fragment {
    String[] faculty = {"FCI", "FAST", "Science", "Medicine", "Interdisciplinary Studies"};
    String[] course = {"Information Technology", "Computer Science", "Software Engineering"};
    String[] year = {"1", "2", "3", "4", "5"};
    AutoCompleteTextView facultyTextView, courseTextView, yearTextView;
    ArrayAdapter<String> myFaculty, myCourse, myYear;
    private RelativeLayout pickImagesBtn;
    private ViewPager viewPager;
    private Uri ImageURI, fileUri;
    private ArrayList<Uri> chooseImageList;
    private EditText fileEditText, title, body;
    private RadioButton everyone;
    private CheckBox terms;
    private Button browseFileButton, post;
    private ProgressDialog progressDialog;
    private static final int REQUEST_PICK_IMAGES = 1;
    private static final int REQUEST_FILE_BROWSE = 2;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private DatabaseReference noticeRef;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    // Declare ArrayLists for image URLs and file URLs
    private ArrayList<String> imageUrls;
    private ArrayList<String> fileUrls;
    private ArrayList<Uri> chooseFileList;

    public PostNoticeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_notice, container, false);
        // Initialize the ArrayLists for image URLs and file URLs
        imageUrls = new ArrayList<>();
        fileUrls = new ArrayList<>();
        chooseFileList = new ArrayList<>();

        pickImagesBtn = view.findViewById(R.id.chooseImages);
        viewPager = view.findViewById(R.id.viewPager);
        chooseImageList = new ArrayList<>();
        fileEditText = view.findViewById(R.id.fileEditText);
        title = view.findViewById(R.id.title);
        body = view.findViewById(R.id.body);
        everyone = view.findViewById(R.id.everyone);
        terms = view.findViewById(R.id.terms);
        browseFileButton = view.findViewById(R.id.browseFileButton);
        facultyTextView = view.findViewById(R.id.faculty);
        courseTextView = view.findViewById(R.id.course);
        yearTextView = view.findViewById(R.id.year);
        progressDialog = new ProgressDialog(getContext());
        post = view.findViewById(R.id.post);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Notices");
        databaseReference = FirebaseDatabase.getInstance().getReference("Notices");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        myFaculty = new ArrayAdapter<String>(getContext(), R.layout.faculty_list, faculty);
        myCourse = new ArrayAdapter<String>(getContext(), R.layout.course_list, course);
        myYear = new ArrayAdapter<String>(getContext(), R.layout.year_list, year);

        facultyTextView.setAdapter(myFaculty);
        courseTextView.setAdapter(myCourse);
        yearTextView.setAdapter(myYear);

        pickImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        // Choose file
        browseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browseFiles();
            }
        });

        // Post Notice Button
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFields();
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
                    String userName = snapshot.child("fullName").getValue(String.class);
                    // Use the userName value as the SubmittedBy field
                    String SubmittedBy = userName;

                    String Title = title.getText().toString().trim();
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


                    noticeRef = databaseReference.push(); // Create a unique reference for the notice

                    // Create a new notice object with the data
                    PostNoticeModal notice = new PostNoticeModal(Title, Body, null, null, Everyone, Faculty, Course, Year, Terms, SubmittedBy, SubmissionDate);
                    notice.setImageUrls(new ArrayList<>()); // Initialize the imageUrls list

                    noticeRef.setValue(notice).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (!chooseImageList.isEmpty()) {
                                    // Upload the images if available
                                    uploadImages(noticeRef.getKey(), chooseImageList);
                                } else if (!chooseFileList.isEmpty()) {
                                    // Upload the files if available
                                    uploadFiles(noticeRef.getKey(), chooseFileList);
                                } else {
                                    // No images or files to upload, finish uploading process
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Notice Posted Successfully", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                }
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
//Clearing Fields
private void clearFields() {
    // Clear the values of the fields to their initial state
    title.setText("");
    body.setText("");
    everyone.setChecked(false);
    terms.setChecked(false);
    facultyTextView.setText(null);
    courseTextView.setText(null);
    yearTextView.setText(null);
    chooseImageList.clear();
    chooseFileList.clear();
}

    private void uploadImages(String noticeId, ArrayList<Uri> imageUris) {
        if (imageUris.isEmpty()) {
            // No more images to upload, check for files
            if (!chooseFileList.isEmpty()) {
                // Upload the files if available
                uploadFiles(noticeId, chooseFileList);
            } else {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Notice Posted Successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            }
            return;
        }

        Uri imageUri = imageUris.get(0);
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));

        StorageReference imageRef = storageReference.child("Notices").child(noticeId).child("images").child(System.currentTimeMillis() + "." + extension);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Retrieve the notice object from the database
                                noticeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            PostNoticeModal notice = snapshot.getValue(PostNoticeModal.class);
                                            if (notice != null) {
                                                if (notice.getImageUrls() == null) {
                                                    // Initialize the imageUrls list if it is null
                                                    notice.setImageUrls(new ArrayList<>());
                                                }
                                                // Add the image URL to the list in the notice object
                                                notice.getImageUrls().add(uri.toString());
                                                // Update the notice object in the database
                                                noticeRef.setValue(notice);
                                            }
                                        }
                                        // Remove the uploaded image from the list
                                        imageUris.remove(0);
                                        // Upload the next image
                                        uploadImages(noticeId, imageUris);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Posting Notice Failed. Try Again", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Posting Notice Failed. Try Again", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadFiles(String noticeId, ArrayList<Uri> fileUris) {
        if (fileUris.isEmpty()) {
            // No more files to upload, finish uploading process
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Notice Posted Successfully", Toast.LENGTH_SHORT).show();
            clearFields();
            return;
        }

        Uri fileUri = fileUris.get(0);
        StorageReference fileRef = storageReference.child("Notices").child(noticeId).child("files").child("file" + fileUrls.size());

        fileRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Retrieve the notice object from the database
                                noticeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            PostNoticeModal notice = snapshot.getValue(PostNoticeModal.class);
                                            if (notice != null) {
                                                if (notice.getFileUrls() == null) {
                                                    // Initialize the fileUrls list if it is null
                                                    notice.setFileUrls(new ArrayList<>());
                                                }
                                                // Add the file URL to the list in the notice object
                                                notice.getFileUrls().add(uri.toString());
                                                // Update the notice object in the database
                                                noticeRef.setValue(notice);
                                            }
                                        }
                                        // Remove the uploaded file from the list
                                        fileUris.remove(0);
                                        // Upload the next file
                                        uploadFiles(noticeId, fileUris);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Posting Notice Failed. Try Again", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Posting Notice Failed. Try Again", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Checking for permission
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 2);
            } else {
                pickImagesFromGallery();
            }
        } else {
            pickImagesFromGallery();
        }
    }

    // Picking Images from Gallery
    private void pickImagesFromGallery() {
        Intent openGallery = new Intent();
        openGallery.setType("image/*");
        openGallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        openGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(openGallery, REQUEST_PICK_IMAGES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_FILE_BROWSE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Multiple files selected
                int fileCount = data.getClipData().getItemCount();
                for (int i = 0; i < fileCount; i++) {
                    fileUri = data.getClipData().getItemAt(i).getUri();
                    String fileName = getFileName(fileUri);
                    // Process the selected file URI, name, and extension as needed
                    fileEditText.append("" + fileName + "\n");

                    // Add the file URI to the list
                    chooseFileList.add(fileUri);
                }
            } else if (data.getData() != null) {
                // Single file selected
                fileUri = data.getData();
                String fileName = getFileName(fileUri);
                // Process the selected file URI, name, and extension as needed
                fileEditText.setText("" + fileName + "\n");

                // Add the file URI to the list
                chooseFileList.add(fileUri);
            }
        } else if (requestCode == REQUEST_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    ImageURI = data.getClipData().getItemAt(i).getUri();
                    // List for adding images
                    chooseImageList.add(ImageURI);
                }
                setAdapter();
            } else if (data.getData() != null) {
                // Single image selected
                ImageURI = data.getData();
                chooseImageList.add(ImageURI);
                setAdapter();
            }
        }
    }

    private String getFileName(Uri fileUri) {
        String fileName = null;
        Cursor cursor = getActivity().getContentResolver().query(fileUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (displayNameIndex != -1) {
                fileName = cursor.getString(displayNameIndex);
            }
            cursor.close();
        }
        return fileName;
    }

    public void setAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getContext(), chooseImageList);
        viewPager.setAdapter(adapter);
    }

    private void browseFiles() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_FILE_BROWSE);
    }
}
