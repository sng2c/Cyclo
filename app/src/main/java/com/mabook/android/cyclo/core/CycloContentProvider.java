package com.mabook.android.cyclo.core;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

public class CycloContentProvider extends ContentProvider {

    private final static int URI_SESSION_ALL = 1;
    private final static int URI_SESSION_ONE = 2;
    private final static int URI_TRACK_ALL = 3;
    private final static int URI_TRACK_ONE = 4;
    private final UriMatcher mUriMatcher;
    private CycloDatabase mDB;

    public CycloContentProvider() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(CycloManager.AUTHORITY, "/session", URI_SESSION_ALL);
        mUriMatcher.addURI(CycloManager.AUTHORITY, "/session/#", URI_SESSION_ONE);
        mUriMatcher.addURI(CycloManager.AUTHORITY, "/session/#/track", URI_TRACK_ALL);
        mUriMatcher.addURI(CycloManager.AUTHORITY, "/session/#/track/#", URI_TRACK_ONE);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");

        // 세션의 삭제만 허용한다.
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case URI_SESSION_ALL:
                return "vnd.android.cursor.dir/session";
            case URI_SESSION_ONE:
                return "vnd.android.cursor.item/session";
            case URI_TRACK_ALL:
                return "vnd.android.cursor.dir/track";
            case URI_TRACK_ONE:
                return "vnd.android.cursor.item/track";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        mDB = new CycloDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        List<String> segs = null;
        switch (mUriMatcher.match(uri)) {
            case URI_SESSION_ALL:
                return mDB.getDatabase().query(CycloDatabase.TABLE_SESSION, projection, selection, selectionArgs, null, null, sortOrder);
            case URI_SESSION_ONE:
                selection = CycloManager.SESSION_FIELD_ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                return mDB.getDatabase().query(CycloDatabase.TABLE_SESSION, projection, selection, selectionArgs, null, null, sortOrder);
            case URI_TRACK_ALL:
                segs = uri.getPathSegments(); // session/#/track
                selection = CycloManager.TRACK_FIELD_SESSION_ID + " = ?";
                selectionArgs = new String[]{segs.get(1)};
                return mDB.getDatabase().query(CycloDatabase.TABLE_TRACK, projection, selection, selectionArgs, null, null, sortOrder);
            case URI_TRACK_ONE:
                segs = uri.getPathSegments(); // session/#/track/#
                selection = CycloManager.TRACK_FIELD_SESSION_ID + " = ? and " + CycloManager.TRACK_FIELD_ID + " = ? ";
                selectionArgs = new String[]{segs.get(1), segs.get(3)};
                return mDB.getDatabase().query(CycloDatabase.TABLE_TRACK, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.

        // 세션이름의 업데이트만 허용한다.

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
