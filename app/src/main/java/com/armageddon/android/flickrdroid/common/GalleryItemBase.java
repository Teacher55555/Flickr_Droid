package com.armageddon.android.flickrdroid.common;

import android.content.Context;

import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.api.RequestResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Sends data from thumbs view (SearchActivity.class) to full-size view (PhotoFullActivity.class)
 * and back.
 * There was a problem to do it simple via Intent (putExtra). If inside response were over 700 GalleryItems
 * i got TransactionTooLargeException. So i did it via Serialization.
 */

public class GalleryItemBase {
   private static RequestResponse<GalleryItem> sResponse;

   public static RequestResponse<GalleryItem> getResponse(Context context) {
      if (sResponse == null) {
         try {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(
                            new File(context.getFilesDir(), "temp_response.dat")));
            sResponse = (RequestResponse<GalleryItem>) ois.readObject();
//            Toast.makeText(context,"LOAD Response SUCCESSFUL ", Toast.LENGTH_LONG).show();
         } catch (IOException | ClassNotFoundException e) {
//            Toast.makeText(context,"LOAD Response FAILED !!!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
         }
      }
      return sResponse;
   }

   public static void setResponse(Context context, RequestResponse<GalleryItem> response) {
      sResponse = response;
      try {
         ObjectOutputStream oos = new ObjectOutputStream(
                 new FileOutputStream(
                         new File(context.getFilesDir(), "temp_response.dat")));
         oos.writeObject(response);
//         Toast.makeText(context,"SAVE Response SUCCESSFUL ", Toast.LENGTH_LONG).show();
      } catch (IOException e) {
//         Toast.makeText(context,"SAVE Response FAILED !!! ", Toast.LENGTH_LONG).show();
         e.printStackTrace();
      }
   }
}
