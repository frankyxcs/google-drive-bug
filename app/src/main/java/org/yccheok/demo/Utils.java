package org.yccheok.demo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yccheok on 16/8/2016.
 */
public class Utils {
    public static class CloudFile {
        public final java.io.File file;
        public final long checksum;
        private CloudFile(java.io.File file, long checksum) {
            this.file = file;
            this.checksum = checksum;
        }

        public static CloudFile newInstance(java.io.File file, long checksum) {
            return new CloudFile(file, checksum);
        }
    }

    private static class GoogleCloudFile {
        private final MetadataBuffer metadataBuffer;
        public final Metadata metadata;
        public final long checksum;
        private GoogleCloudFile(MetadataBuffer metadataBuffer, Metadata metadata, long checksum) {
            this.metadataBuffer = metadataBuffer;
            this.metadata = metadata;
            this.checksum = checksum;
        }

        public static GoogleCloudFile newInstance(MetadataBuffer metadataBuffer, Metadata metadata, long checksum) {
            return new GoogleCloudFile(metadataBuffer, metadata, checksum);
        }
    }

    private static GoogleCloudFile searchFromGoogleDrive(GoogleApiClient googleApiClient) {
        DriveFolder driveFolder = Drive.DriveApi.getAppFolder(googleApiClient);

        // http://stackoverflow.com/questions/34705929/filters-ownedbyme-doesnt-work-in-drive-api-for-android-but-works-correctly-i
        final String titleName = (".TXT");
        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.contains(SearchableField.TITLE, titleName),
                        Filters.eq(SearchableField.TRASHED, false)
                ))
                .build();

        DriveApi.MetadataBufferResult metadataBufferResult = driveFolder.queryChildren(googleApiClient, query).await();

        if (metadataBufferResult == null) {
            return null;
        }

        Status status = metadataBufferResult.getStatus();

        if (!status.isSuccess()) {
            return null;
        }

        MetadataBuffer metadataBuffer = null;
        boolean needToReleaseMetadataBuffer = true;

        try {
            metadataBuffer = metadataBufferResult.getMetadataBuffer();
            if (metadataBuffer != null ) {
                long checksum = 0;
                Metadata metadata = null;

                for (Metadata md : metadataBuffer) {

                    if (md == null || !md.isDataValid()) {
                        continue;
                    }

                    final String title = md.getTitle();

                    // Retrieve checksum, date and version information from filename.
                    final Matcher matcher = googleDocTitlePattern.matcher(title);
                    String _checksum = null;
                    if (matcher.find()){
                        if (matcher.groupCount() == 1) {
                            _checksum = matcher.group(1);
                        }
                    }
                    if (_checksum == null) {
                        continue;
                    }

                    try {
                        checksum = Long.parseLong(_checksum);
                    } catch (NumberFormatException ex) {
                        Log.e(TAG, "", ex);
                        continue;
                    }

                    metadata = md;

                    break;

                }   // for

                if (metadata != null) {
                    // Caller will be responsible to release the resource. If release too early,
                    // metadata will not readable.
                    needToReleaseMetadataBuffer = false;
                    return GoogleCloudFile.newInstance(metadataBuffer, metadata, checksum);
                }
            }   // if
        } finally {
            if (needToReleaseMetadataBuffer) {
                if (metadataBuffer != null) {
                    metadataBuffer.release();
                }
            }
        }

        return null;
    }

    public static boolean saveToGoogleDrive(GoogleApiClient googleApiClient, String content) {
        // Should we new or replace?

        GoogleCloudFile googleCloudFile = searchFromGoogleDrive(googleApiClient);

        try {
            // 123.TXT or 456.TXT
            final String title = content + ".TXT";

            DriveContents driveContents;
            DriveFile driveFile = null;

            if (googleCloudFile == null) {
                DriveApi.DriveContentsResult driveContentsResult = Drive.DriveApi.newDriveContents(googleApiClient).await();

                if (driveContentsResult == null) {
                    return false;
                }

                Status status = driveContentsResult.getStatus();
                if (!status.isSuccess()) {
                    return false;
                }

                driveContents = driveContentsResult.getDriveContents();

            } else {
                driveFile = googleCloudFile.metadata.getDriveId().asDriveFile();
                DriveApi.DriveContentsResult driveContentsResult = driveFile.open(googleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();

                if (driveContentsResult == null) {
                    return false;
                }

                Status status = driveContentsResult.getStatus();
                if (!status.isSuccess()) {
                    return false;
                }

                driveContents = driveContentsResult.getDriveContents();
            }

            OutputStream outputStream = driveContents.getOutputStream();
            InputStream inputStream = null;

            try {
                outputStream.write(content.getBytes(Charset.forName("UTF-8")));
            } catch (IOException e) {
                Log.e(TAG, "", e);
                return false;
            } finally {
                close(outputStream);
                close(inputStream);
            }

            if (googleCloudFile == null) {
                // Create the metadata for the new file including title and MIME
                // type.
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setTitle(title)
                        .setMimeType("text/plain").build();

                DriveFolder driveFolder = Drive.DriveApi.getAppFolder(googleApiClient);

                Log.i("CHEOK", "driveFolder = " + driveFolder);

                DriveFolder.DriveFileResult driveFileResult = driveFolder.createFile(googleApiClient, metadataChangeSet, driveContents).await();

                if (driveFileResult == null) {
                    return false;
                }

                Status status = driveFileResult.getStatus();
                if (!status.isSuccess()) {
                    return false;
                }
            } else {
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setTitle(title).build();

                DriveResource.MetadataResult metadataResult = driveFile.updateMetadata(googleApiClient, metadataChangeSet).await();
                Status status = metadataResult.getStatus();
                if (!status.isSuccess()) {
                    return false;
                }
            }

            Status status;
            try {
                status = driveContents.commit(googleApiClient, null).await();
            } catch (java.lang.IllegalStateException e) {
                // java.lang.IllegalStateException: DriveContents already closed.
                Log.e(TAG, "", e);
                return false;
            }

            if (!status.isSuccess()) {
                return false;
            }

            status = Drive.DriveApi.requestSync(googleApiClient).await();
            if (!status.isSuccess()) {
                // Sync request rate limit exceeded.
                //
                //h.handleStatus(status);
                //return false;
            }

            return true;
        } finally {
            if (googleCloudFile != null) {
                googleCloudFile.metadataBuffer.release();
            }
        }
    }

    public static void close(Closeable closeable) {
        // Instead of returning boolean, we will just simply swallow any
        // exception silently. This is because this method will usually be
        // invoked within finally block. If we are having control statement
        // (return, break, continue) within finally block, a lot of surprise may
        // happen.
        // http://stackoverflow.com/questions/48088/returning-from-a-finally-block-in-java
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException ex) {
                Log.e(TAG, "", ex);
            }
        }
    }

    public static void showLongToast(final String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // UI thread.
            final Toast toast = Toast.makeText(MyApplication.instance(), message, Toast.LENGTH_LONG);
            toast.show();
        } else {
            // non-UI thread.
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    final Toast toast = Toast.makeText(MyApplication.instance(), message, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

    }

    public static CloudFile loadFromGoogleDrive(GoogleApiClient googleApiClient) {
        final java.io.File directory = MyApplication.instance().getExternalCacheDir();
        if (directory == null) {
            Utils.showLongToast("unable_to_access_external_storage");
            return null;
        }

        Status status = Drive.DriveApi.requestSync(googleApiClient).await();
        if (!status.isSuccess()) {
            // Sync request rate limit exceeded.
            //
            //h.handleStatus(status);
            //return null;
        }

        GoogleCloudFile googleCloudFile = searchFromGoogleDrive(googleApiClient);

        if (googleCloudFile == null) {
            return null;
        }

        try {
            DriveFile driveFile = googleCloudFile.metadata.getDriveId().asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult = driveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).await();

            if (driveContentsResult == null) {
                return null;
            }

            status = driveContentsResult.getStatus();
            if (!status.isSuccess()) {
                return null;
            }

            final long checksum = googleCloudFile.checksum;

            final DriveContents driveContents = driveContentsResult.getDriveContents();

            InputStream inputStream = null;
            java.io.File outputFile = null;
            OutputStream outputStream = null;

            try {
                inputStream = driveContents.getInputStream();
                outputFile = java.io.File.createTempFile("TEMP", ".TXT", directory);
                outputFile.deleteOnExit();
                outputStream = new FileOutputStream(outputFile);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException ex) {
                Log.e(TAG, "", ex);
            } finally {
                close(outputStream);
                close(inputStream);
                driveContents.discard(googleApiClient);
            }

            if (outputFile == null) {
                return null;
            }

            return CloudFile.newInstance(outputFile, checksum);
        } finally {
            googleCloudFile.metadataBuffer.release();
        }
    }

    private static final String TAG = "Utils";
    private static final Pattern googleDocTitlePattern = Pattern.compile("\\.TXT", Pattern.CASE_INSENSITIVE);
}
