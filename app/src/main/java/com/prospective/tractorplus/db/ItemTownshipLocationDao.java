package com.prospective.tractorplus.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.prospective.tractorplus.viewobject.ItemTownshipLocation;

import java.util.List;

@Dao
public interface ItemTownshipLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemTownshipLocation itemLocation);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ItemTownshipLocation itemLocation);

    @Query("DELETE FROM ItemTownshipLocation")
    void deleteAllItemLocation();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemTownshipLocation> itemLocationList);

    @Query("SELECT * FROM ItemTownshipLocation ")
    LiveData<List<ItemTownshipLocation>> getAllItemLocation();

    @Query("SELECT * FROM ItemTownshipLocation WHERE cityId=:cityId")
    LiveData<List<ItemTownshipLocation>> getSubCityList(String cityId);

}
