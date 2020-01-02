package com.premtimf.mynotes.persistence;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.premtimf.mynotes.asynctask.DeleteAsyncTask;
import com.premtimf.mynotes.asynctask.InsertAsyncTask;
import com.premtimf.mynotes.asynctask.UpdateAsyncTask;
import com.premtimf.mynotes.model.Note;

import java.util.List;

public class NoteRepository {

    private NoteDatabase mNoteDatabase;

    public NoteRepository (Context context){
        mNoteDatabase = NoteDatabase.getInstance(context);
    }

    public void insertNotesTask(Note note){
        new InsertAsyncTask(mNoteDatabase.getNotesDao()).execute(note);
    }

    public LiveData<List<Note>> retrieveNotesTask(){
        return mNoteDatabase.getNotesDao().getNotes();
    }

    public void updateNotesTask(Note note){
        new UpdateAsyncTask(mNoteDatabase.getNotesDao()).execute(note);
    }

    public void deleteNotesTask(Note note){
        new DeleteAsyncTask(mNoteDatabase.getNotesDao()).execute(note);
    }
}
