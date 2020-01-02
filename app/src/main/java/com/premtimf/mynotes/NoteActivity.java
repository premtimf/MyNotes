package com.premtimf.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.premtimf.mynotes.model.Note;
import com.premtimf.mynotes.persistence.NoteRepository;
import com.premtimf.mynotes.util.Utility;

public class NoteActivity extends AppCompatActivity implements
        View.OnTouchListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        View.OnClickListener,
        TextWatcher
{
    private static final String TAG = "Note Activity";
    private static final int EDIT_MODE_ENABLED = 1;
    private static final int EDIT_MODE_DISABLED = 0;

    private LinedEditText mLinedEditText;
    private EditText mEditTitle;
    private TextView mTextTitle;
    private RelativeLayout mCheckContainer, mBackContainer;
    private ImageButton mCheckBtn, mBackBtn;

    private Boolean mIsNewNote;
    private Note mInitialNote;
    private Note mFinalNote;
    private GestureDetector mGestureDetector;
    private int mMode;
    private NoteRepository mNoteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        mEditTitle = findViewById(R.id.note_edit_title);
        mTextTitle = findViewById(R.id.note_text_title);
        mLinedEditText = findViewById(R.id.note_text);
        mCheckContainer = findViewById(R.id.checkContainer);
        mBackContainer = findViewById(R.id.backContainer);
        mCheckBtn = findViewById(R.id.toolbar_check_button);
        mBackBtn = findViewById(R.id.toolbar_back_button);
        mNoteRepository = new NoteRepository(this);

        if (getIncomingNote()){

            //this is a new note (Edit Mode)
            setNewNoteProperties();
            setEditModeEnabled();


        } else {
            //this is an existing note (View Mode)
            setInitialNoteProperties();
            disableContentInteraction();
        }

        setListeners();

    }

    private void setListeners(){
        mLinedEditText.setOnTouchListener(this);
        mGestureDetector = new GestureDetector(this, this);
        mCheckBtn.setOnClickListener(this);
        mTextTitle.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mEditTitle.addTextChangedListener(this);
    }

    @Override
    public void onBackPressed() {

        if (mMode == EDIT_MODE_ENABLED){
            onClick(mCheckBtn);
        } else {
            super.onBackPressed();
        }
    }

    private Boolean getIncomingNote(){
        if (getIntent().hasExtra("selected_note")){

            mMode = EDIT_MODE_DISABLED;
            mInitialNote = getIntent().getParcelableExtra("selected_note");

            mFinalNote = new Note();
            mFinalNote.setTitle(mInitialNote.getTitle());
            mFinalNote.setContent(mInitialNote.getContent());
            mFinalNote.setTimestamp(mInitialNote.getTimestamp());
            mFinalNote.setId(mInitialNote.getId());

            mIsNewNote = false;
            return false;
        }

        mMode = EDIT_MODE_ENABLED;
        mIsNewNote = true;
        return true;
    }

    private void saveChanges(){
        if (mIsNewNote){
            saveNewNote();
        } else {
            updateNote();
        }
    }

    private void saveNewNote(){
        mNoteRepository.insertNotesTask(mFinalNote);
    }

    private void updateNote(){
        mNoteRepository.updateNotesTask(mFinalNote);
    }

    private void setEditModeEnabled(){
        mBackContainer.setVisibility(View.GONE);
        mCheckContainer.setVisibility(View.VISIBLE);

        mTextTitle.setVisibility(View.GONE);
        mEditTitle.setVisibility(View.VISIBLE);

        mMode = EDIT_MODE_ENABLED;

        enableContentInteraction();
    }

    private void setEditModeDisabled(){
        mBackContainer.setVisibility(View.VISIBLE);
        mCheckContainer.setVisibility(View.GONE);

        mTextTitle.setVisibility(View.VISIBLE);
        mEditTitle.setVisibility(View.GONE);

        mMode = EDIT_MODE_DISABLED;
        disableContentInteraction();

        String temp = mLinedEditText.getText().toString();
        temp = temp.replace("\n", "");
        temp = temp.replace(" ", "");

        if (temp.length() > 0){
            mFinalNote.setTitle(mEditTitle.getText().toString());
            mFinalNote.setContent(mLinedEditText.getText().toString());
            String timestamp = Utility.getCurrentTimestamp();
            mFinalNote.setTimestamp(timestamp);

            if (!mFinalNote.getContent().equals(mInitialNote.getContent())
            || !mFinalNote.getTitle().equals(mInitialNote.getTitle())){
                saveChanges();
            }
        }
    }

    private void disableContentInteraction(){
        mLinedEditText.setKeyListener(null);
        mLinedEditText.setFocusable(false);
        mLinedEditText.setFocusableInTouchMode(false);
        mLinedEditText.setCursorVisible(false);
        mLinedEditText.clearFocus();
    }

    private void enableContentInteraction(){
        mLinedEditText.setKeyListener(new EditText(this).getKeyListener());
        mLinedEditText.setFocusable(true);
        mLinedEditText.setFocusableInTouchMode(true);
        mLinedEditText.setCursorVisible(true);
        mLinedEditText.requestFocus();
    }

    private void setInitialNoteProperties(){
        mEditTitle.setText(mInitialNote.getTitle());
        mTextTitle.setText(mInitialNote.getTitle());
        mLinedEditText.setText(mInitialNote.getContent());
    }

    private void setNewNoteProperties(){
        mTextTitle.setText("Note title");
        mEditTitle.setText("Note title");

        mInitialNote = new Note();
        mFinalNote = new Note();
        mInitialNote.setTitle("Note title");
        mFinalNote.setTitle("Note title");
    }

    private void hideSoftKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null){
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        setEditModeEnabled();
        Log.d(TAG, "onDoubleTap: double tapped");
        Toast.makeText(this, "Double tapped", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.toolbar_check_button: {
                hideSoftKeyboard();
                setEditModeDisabled();
                break;
            }

            case R.id.note_text_title: {
                setEditModeEnabled();
                mEditTitle.requestFocus();
                mEditTitle.setSelection(mEditTitle.length());
                break;
            }

            case R.id.toolbar_back_button: {
                finish();
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mode", mMode);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMode = savedInstanceState.getInt("mode");
        if (mMode == EDIT_MODE_ENABLED){
            setEditModeEnabled();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mTextTitle.setText(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
