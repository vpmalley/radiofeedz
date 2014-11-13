package fr.vpm.audiorss.persistence;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by vincent on 06/10/14.
 */
public interface PictureSaver {

  /**
   * Retrieves the picture in the storage if it exists
   *
   * @param pictureFile the file used to save the picture
   * @return the picture if it exists
   */
  Bitmap retrieve(File pictureFile);

  /**
   * Persists a blog picture to be retrieved later
   *
   * @param pictureFile the file to use
   * @param picture picture to save
   * @return whether it has been persisted successfully
   */
  boolean persist(File pictureFile, Bitmap picture);

  /**
   * Deletes a persisted picture
   *
   * @param pictureFile the file to delete
   * @return whether the post was deleted
   */
  boolean delete(File pictureFile);

}
