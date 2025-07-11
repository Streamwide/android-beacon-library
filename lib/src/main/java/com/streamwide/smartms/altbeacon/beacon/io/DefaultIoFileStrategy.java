/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 16 May 2024 09:56:21 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 16 May 2024 09:38:17 +0100
 */

package com.streamwide.smartms.altbeacon.beacon.io;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.altbeacon.beacon.logger.Logger;
import com.streamwide.smartms.lib.template.file.IOFileStrategy;
import com.streamwide.smartms.lib.template.security.SecureFile;
import com.streamwide.smartms.lib.template.serialization.ValidatingObjectInputStream;
import com.streamwide.smartms.lib.template.serialization.ValidatorClassNameMatcher;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class DefaultIoFileStrategy implements IOFileStrategy {

    private static final String TAG = "DefaultIoFileStrategy";

    /**
     * The default buffer size ({@value} ) to use for
     * {@link #copyLarge(InputStream, OutputStream)}
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

    private static final int EOF = -1;


    @Nullable
    @Override
    public String getRootDirectoryPath(@NonNull Context context) {
        File rootDirectory = context.getFilesDir();

        if (!rootDirectory.exists()) {
            boolean result = rootDirectory.mkdirs();
            if (!result) {
                Logger.error(TAG, " rootDirectory could not be created");
            }
        }

        return rootDirectory.getPath();
    }

    @Override
    public void write(@NonNull InputStream sourceInputStream, @NonNull String destinationFilePath, boolean append)
            throws IOException {
        if (checkIfNull(sourceInputStream, destinationFilePath)) {
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(destinationFilePath, append)) {
            copy(sourceInputStream, fileOutputStream);
        }
    }

    @Override
    public void write(@NonNull byte[] sourceBytes, @NonNull String destinationFilePath, boolean append)
            throws IOException {
        if (checkIfNull(sourceBytes, destinationFilePath)) {
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(destinationFilePath, append);
             ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sourceBytes)) {
            copy(byteArrayInputStream, fileOutputStream);
        }
    }

    @Override
    public void write(@NonNull String sourceContent, @NonNull String destinationFilePath, boolean append) {
        if (checkIfNull(sourceContent, destinationFilePath)) {
            return;
        }

        writeFile(destinationFilePath, sourceContent, append);
    }

    @Nullable
    @Override
    public String writeString(@NonNull String sourceString) throws IOException, GeneralSecurityException {
        return sourceString;
    }

    @Override
    public void writeObject(@NonNull Context context, @NonNull Object object, @NonNull String destinationFilePath) throws IOException, GeneralSecurityException {
        if (checkIfNull(object, destinationFilePath)) {
            return;
        }

        writeObject(context, destinationFilePath, object);
    }

    @Nullable
    @Override
    public InputStream read(@NonNull String sourceFilePath) throws FileNotFoundException {
        if (checkIfNull(sourceFilePath)) {
            return null;
        }

        return new FileInputStream(sourceFilePath);
    }

    @Nullable
    @Override
    public ByteArrayInputStream readAsByteArray(@NonNull String sourceFilePath) throws IOException {

        InputStream fileInputStream = read(sourceFilePath);
        if (fileInputStream != null) {
            byte[] byteArray = new byte[fileInputStream.available()];
            // Read the content of the FileInputStream into the byte array
            int result = fileInputStream.read(byteArray);
            if (result >= 0) {
                // Create a ByteArrayInputStream from the byte array
                return new ByteArrayInputStream(byteArray);
            }

        }

        return null;
    }

    @Nullable
    @Override
    public String readContent(@NonNull String sourceFilePath) {
        if (checkIfNull(sourceFilePath)) {
            return null;
        }

        return getContentFromFile(sourceFilePath);
    }

    @Nullable
    @Override
    public String readString(@NonNull String sourceString) throws IOException {
        return sourceString;
    }

    @Nullable
    @Override
    public Object readObject(@NonNull Context context, @NonNull String sourceFilePath, @Nullable ValidatorClassNameMatcher validatorClassNameMatcher) throws IOException {

        if (checkIfNull(sourceFilePath)) {
            return null;
        }

        return getObjectFromFile(context, sourceFilePath, validatorClassNameMatcher);
    }

    @Nullable
    @Override
    public MediaMetadataRetriever getMediaMetaDataRetriever(@NonNull String mediaFilePath, long length) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaFilePath);

        return mediaMetadataRetriever;
    }

    @Nullable
    @Override
    public MediaDataSource getMediaDataSource(@NonNull Context context, @NonNull Uri mediaUri, long length) {
        return null;
    }

    private boolean checkIfNull(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }

        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * write passsed content into the file
     *
     * @param filePath File path
     * @param content  The content to be saved into the file
     * @param append   is append, if true, write to the end of file, else clear
     *                 content of file and write into it
     */
    private void writeFile(@NonNull String filePath, @NonNull String content, boolean append) {
        if (isEmpty(content)) {
            return;
        }

        FileWriter fileWriter = null;
        try {
            File file = SecureFile.createSecureFile(filePath);

            if (file == null) {
                throw new IOException("Directory not found");
            }

            /*
             * create parent file if not exist
             */
            final File parentFile = file.getParentFile();
            if (parentFile != null && (!parentFile.mkdirs() && !parentFile.isDirectory())) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            }

            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            Logger.error(TAG, "writeFile : IOException error occured when create file!");
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Logger.error(TAG, "writeFile : IOException error occured when create file!");
                }
            }
        }
    }

    /**
     * write passed object into the file
     *
     * @param filePath File path
     * @param object   The object to be saved into the file
     */
    private void writeObject(@NonNull Context context, @NonNull String filePath, @Nullable Object object) {
        if (object == null) {
            return;
        }
        FileOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            File file = SecureFile.createSecureFile(filePath);

            if (file == null) {
                throw new IOException("Directory not found");
            }

            /*
             * create parent file if not exist
             */
            final File parentFile = file.getParentFile();
            if (parentFile != null && (!parentFile.mkdirs() && !parentFile.isDirectory())) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            }
            String fileName = file.getName();

            outputStream = context.openFileOutput(fileName, MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(object);


        } catch (IOException e) {
            Logger.error("writeFile", "writeFile : IOException error occured when create file!");
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @NonNull
    private String getContentFromFile(@NonNull String path) {
        String text = "";

        SpannableString spannableString = new SpannableString(path);
        String securePath = spannableString.toString();

        try (FileInputStream is = new FileInputStream(securePath)) {

            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesCount = is.read(buffer);
            if (bytesCount > 0) {
                text = new String(buffer);
            }
        } catch (IOException e) {
            Logger.error(TAG, "Error when trying to get content from file", e);
        }
        return text;
    }

    /**
     * Copy bytes from an <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * Large streams (over 2GB) will return a bytes copied value of
     * <code>-1</code> after the copy has completed since the correct number of
     * bytes cannot be returned as an int. For large streams use the
     * <code>copyLarge(InputStream, OutputStream)</code> method.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static int copy(@NonNull InputStream input, @NonNull OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.3
     */
    public static long copyLarge(@NonNull InputStream input, @NonNull OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param buffer the buffer to use for the copy
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.2
     */
    public static long copyLarge(@NonNull InputStream input, @NonNull OutputStream output, @NonNull byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    /**
     * Unconditionally close an <code>OutputStream</code>.
     * <p>
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be
     * ignored. This is typically used in finally blocks.
     * <p>
     * Example code:
     *
     * <pre>
     * byte[] data = &quot;Hello, World&quot;.getBytes();
     *
     * OutputStream out = null;
     * try {
     *     out = new FileOutputStream(&quot;foo.txt&quot;);
     *     out.write(data);
     *     out.close(); // close errors are handled
     * } catch (IOException e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(out);
     * }
     * </pre>
     *
     * @param output the OutputStream to close, may be null or already closed
     */
    public static void closeQuietly(@NonNull OutputStream output) {
        closeQuietly((Closeable) output);
    }

    /**
     * Unconditionally close a <code>Closeable</code>.
     * <p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be
     * ignored. This is typically used in finally blocks.
     * <p>
     * Example code:
     *
     * <pre>
     * Closeable closeable = null;
     * try {
     *     closeable = new FileReader(&quot;foo.txt&quot;);
     *     // process closeable
     *     closeable.close();
     * } catch (Exception e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(closeable);
     * }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @since 2.0
     */
    public static void closeQuietly(@Nullable Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * whether this string is empty(null, "" or only with spaces).
     *
     * @return boolean is empty or not
     */
    public static boolean isEmpty(@Nullable CharSequence value) {
        return (value == null || TextUtils.isEmpty(value.toString().trim()));
    }

    @Nullable
    private Object getObjectFromFile(@NonNull Context context, @NonNull String path, @Nullable ValidatorClassNameMatcher validatorClassNameMatcher) {

        File file = new File(path);
        String fileName = file.getName();
        FileInputStream inputStream;
        ValidatingObjectInputStream validatingObjectInputStream;
        try {
            inputStream = context.openFileInput(fileName);
            validatingObjectInputStream = new ValidatingObjectInputStream(inputStream);

            if (validatorClassNameMatcher != null) {
                validatingObjectInputStream.validator(validatorClassNameMatcher);
            }
            return validatingObjectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Logger.error(TAG, "getObjectFromFile = " + e);
        }
        return null;

    }
}
