package com.prospective.tractorplus.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prospective.tractorplus.viewobject.Image;

import java.util.List;

/**
 * Created by Panacea-Soft on 12/8/17.
 * Contact Email : teamps.is.cool@gmail.com
 */

@Dao
public interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Image image);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Image> imageList);

    @Query("SELECT * FROM Image")
    LiveData<List<Image>> getAll();

    @Query("SELECT * FROM Image")
    LiveData<Image> getUploadImage();

    @Query("DELETE FROM Image WHERE imgId = :imgId")
    void deleteImageById(String imgId);

    @Query("SELECT * FROM Image WHERE imgParentId = :imgParentId AND imgType= :imagetype order by ordering")
    LiveData<List<Image>> getByImageIdAndType(String imgParentId,String imagetype);

    @Query("DELETE FROM Image WHERE imgParentId = :imgParentId AND imgType= :imagetype")
    void deleteByImageIdAndType(String imgParentId,String imagetype);

    @Query("DELETE FROM Image")
    void deleteTable();

}
