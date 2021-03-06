package com.example.bryanyen.godorientationapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by bryan.yen on 2017/6/27.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String TAG = "DataBaseHelper"; // Tag just for the LogCat window
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME = "golddata.db";// Database name
    public static final int DB_VERSION = 1;
    public static int mOldVersion = -1;
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);// 1? Its database Version
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    public void createDataBase() throws IOException {
        //If the database does not exist, copy it from the assets.

        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                //Copy the database from assests
                copyDataBase();
                Log.d(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        //        Log.v("dbFile", dbFile + "   " + dbFile.exists());
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        //        Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createDatabase database created error");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion <= oldVersion) {
            return;
        }
        mOldVersion = oldVersion;

        try {
            copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 從DB取得今日財神方位
     * <p>
     * DB資料來源：http://www.fushantang.com/1013/m1006.html
     */
    public static String getMoneyGodData(Context mContext, String lunarDay) {
        final String TABLE_NAME = "lushData";
        final String DATE_TABLE_NAME = "lushDay";
        final String MONEY_DOD_TABLE_NAME = "moneyGodOrientation";
        try {
            DataBaseHelper mDbHelper = new DataBaseHelper(mContext);
            mDbHelper.createDataBase();
            mDbHelper.openDataBase();
            SQLiteDatabase mDb = mDbHelper.getReadableDatabase();

            String sql = "SELECT " + DATE_TABLE_NAME + "," + MONEY_DOD_TABLE_NAME +
                    " FROM " + TABLE_NAME +
                    " WHERE " + DATE_TABLE_NAME + " IN('" + lunarDay + "')";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur != null) {
                mCur.moveToNext();
            }

            String moneyGodPosition = "";
            if (mCur != null) {
                moneyGodPosition = mCur.getString(mCur.getColumnIndex(MONEY_DOD_TABLE_NAME));
                mCur.close();
            }

            mDbHelper.close();

            return moneyGodPosition;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getTestData >>" + mSQLException.toString());
            throw mSQLException;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString() + "  UnableToCreateDatabase");
        }

        return "";
    }
}
