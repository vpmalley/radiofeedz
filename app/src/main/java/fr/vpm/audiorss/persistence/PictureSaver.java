package fr.vpm.audiorss.persistence;

import android.graphics.Bitmap;

/**
 * Created by vincent on 06/10/14.
 */
public interface PictureSaver {

  /**
   * Determines whether the picture exists in the storage
   *
   * @param pictureFileName the file name used to save the picture
   * @return whether the post already exists
   */
  boolean exists(String pictureFileName);

  /**
   * Retrieves the picture in the storage if it exists
   *
   * @param pictureFileName the file name used to save the picture
   * @return the picture if it exists
   */
  Bitmap retrieve(String pictureFileName);

  /**
   * Persists a blog picture to be retrieved later
   *
   * @param pictureFileName the name of the file to use
   * @param picture picture to save
   * @return whether it has been persisted successfully
   */
  boolean persist(String pictureFileName, Bitmap picture);

  /**
   * Deletes a persisted picture
   *
   * @param pictureFileName the name of the file  to delete
   * @return whether the post was deleted
   */
  boolean delete(String pictureFileName);

}
