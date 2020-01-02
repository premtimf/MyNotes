package com.premtimf.mynotes.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.premtimf.mynotes.model.Note;
import com.premtimf.mynotes.persistence.NoteDao;

public class UpdateAsyncTask extends AsyncTask<Note, Void, Void> {

    private static final String TAG = "UpdateAsyncTask";

    private NoteDao mNoteDao;

    public UpdateAsyncTask(NoteDao noteDao){
        mNoteDao = noteDao;
    }

    @Override
    protected Void doInBackground(Note... notes) {
        Log.d(TAG, "doInBackground: thread: " + Thread.currentThread().getName());
        mNoteDao.updateNotes(notes);
        return null;
    }
}
