package com.prospective.tractorplus.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.room.Room;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.api.PSApiService;
import com.prospective.tractorplus.db.AboutUsDao;
import com.prospective.tractorplus.db.BlockUserDao;
import com.prospective.tractorplus.db.BlogDao;
import com.prospective.tractorplus.db.ChatHistoryDao;
import com.prospective.tractorplus.db.CityDao;
import com.prospective.tractorplus.db.CityMapDao;
import com.prospective.tractorplus.db.DeletedObjectDao;
import com.prospective.tractorplus.db.HistoryDao;
import com.prospective.tractorplus.db.ImageDao;
import com.prospective.tractorplus.db.ItemCategoryDao;
import com.prospective.tractorplus.db.ItemCollectionHeaderDao;
import com.prospective.tractorplus.db.ItemConditionDao;
import com.prospective.tractorplus.db.ItemCurrencyDao;
import com.prospective.tractorplus.db.ItemDao;
import com.prospective.tractorplus.db.ItemDealOptionDao;
import com.prospective.tractorplus.db.ItemLocationDao;
import com.prospective.tractorplus.db.ItemMapDao;
import com.prospective.tractorplus.db.ItemPaidHistoryDao;
import com.prospective.tractorplus.db.ItemPriceTypeDao;
import com.prospective.tractorplus.db.ItemSubCategoryDao;
import com.prospective.tractorplus.db.ItemTownshipLocationDao;
import com.prospective.tractorplus.db.ItemTypeDao;
import com.prospective.tractorplus.db.MessageDao;
import com.prospective.tractorplus.db.NotificationDao;
import com.prospective.tractorplus.db.OfferDao;
import com.prospective.tractorplus.db.OfflinePaymentDao;
import com.prospective.tractorplus.db.PSAppInfoDao;
import com.prospective.tractorplus.db.PSAppVersionDao;
import com.prospective.tractorplus.db.PSCoreDb;
import com.prospective.tractorplus.db.PSCountDao;
import com.prospective.tractorplus.db.RatingDao;
import com.prospective.tractorplus.db.ReportedItemDao;
import com.prospective.tractorplus.db.UserDao;
import com.prospective.tractorplus.db.UserMapDao;
import com.prospective.tractorplus.utils.AppLanguage;
import com.prospective.tractorplus.utils.Connectivity;
import com.prospective.tractorplus.utils.LiveDataCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Panacea-Soft on 11/15/17.
 * Contact Email : teamps.is.cool@gmail.com
 */

@Module(includes = ViewModelModule.class)
class AppModule {

    @Singleton
    @Provides
    PSApiService providePSApiService() {

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        return new Retrofit.Builder()
                .baseUrl(Config.APP_API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(PSApiService.class);

    }

    @Singleton
    @Provides
    PSCoreDb provideDb(Application app) {
        return Room.databaseBuilder(app, PSCoreDb.class, "psmulticity.db")
                //.addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    Connectivity provideConnectivity(Application app) {
        return new Connectivity(app);
    }

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
    }

    @Singleton
    @Provides
    UserDao provideUserDao(PSCoreDb db) {
        return db.userDao();
    }

    @Singleton
    @Provides
    UserMapDao provideUserMapDao(PSCoreDb db) {
        return db.userMapDao();
    }

    @Singleton
    @Provides
    AppLanguage provideCurrentLanguage(SharedPreferences sharedPreferences) {
        return new AppLanguage(sharedPreferences);
    }

    @Singleton
    @Provides
    AboutUsDao provideAboutUsDao(PSCoreDb db) {
        return db.aboutUsDao();
    }

    @Singleton
    @Provides
    ImageDao provideImageDao(PSCoreDb db) {
        return db.imageDao();
    }

    @Singleton
    @Provides
    ItemCurrencyDao provideItemCurrencyDao(PSCoreDb db) {
        return db.itemCurrencyDao();
    }

    @Singleton
    @Provides
    ItemTypeDao provideItemTypeDao(PSCoreDb db) {
        return db.itemTypeDao();
    }

    @Singleton
    @Provides
    ItemPriceTypeDao provideItemPriceTypeDao(PSCoreDb db) {
        return db.itemPriceTypeDao();
    }

    @Singleton
    @Provides
    HistoryDao provideHistoryDao(PSCoreDb db) {
        return db.historyDao();
    }

    @Singleton
    @Provides
    RatingDao provideRatingDao(PSCoreDb db) {
        return db.ratingDao();
    }

    @Singleton
    @Provides
    ItemDealOptionDao provideItemDealOptionDao(PSCoreDb db) {
        return db.itemDealOptionDao();
    }

    @Singleton
    @Provides
    ItemConditionDao provideItemConditionDao(PSCoreDb db) {
        return db.itemConditionDao();
    }

    @Singleton
    @Provides
    ItemLocationDao provideItemLocationDao(PSCoreDb db) {
        return db.itemLocationDao();
    }

    @Singleton
    @Provides
    ItemTownshipLocationDao provideItemTownshipLocationDao(PSCoreDb db) {
        return db.itemTownshipLocationDao();
    }

    @Singleton
    @Provides
    NotificationDao provideNotificationDao(PSCoreDb db) {
        return db.notificationDao();
    }

    @Singleton
    @Provides
    BlogDao provideNewsFeedDao(PSCoreDb db) {
        return db.blogDao();
    }

    @Singleton
    @Provides
    PSAppInfoDao providePSAppInfoDao(PSCoreDb db) {
        return db.psAppInfoDao();
    }

    @Singleton
    @Provides
    PSAppVersionDao providePSAppVersionDao(PSCoreDb db) {
        return db.psAppVersionDao();
    }

    @Singleton
    @Provides
    DeletedObjectDao provideDeletedObjectDao(PSCoreDb db) {
        return db.deletedObjectDao();
    }

    @Singleton
    @Provides
    CityDao provideCityDao(PSCoreDb db) {
        return db.cityDao();
    }

    @Singleton
    @Provides
    CityMapDao provideCityMapDao(PSCoreDb db) {
        return db.cityMapDao();
    }

    @Singleton
    @Provides
    ItemDao provideItemDao(PSCoreDb db) {
        return db.itemDao();
    }

    @Singleton
    @Provides
    ItemMapDao provideItemMapDao(PSCoreDb db) {
        return db.itemMapDao();
    }

    @Singleton
    @Provides
    ItemCategoryDao provideCityCategoryDao(PSCoreDb db) {
        return db.itemCategoryDao();
    }

    @Singleton
    @Provides
    ItemCollectionHeaderDao provideItemCollectionHeaderDao(PSCoreDb db) {
        return db.itemCollectionHeaderDao();
    }

    @Singleton
    @Provides
    ItemSubCategoryDao provideItemSubCategoryDao(PSCoreDb db) {
        return db.itemSubCategoryDao();
    }

    @Singleton
    @Provides
    ChatHistoryDao provideChatHistoryDao(PSCoreDb db) {
        return db.chatHistoryDao();
    }

    @Singleton
    @Provides
    OfferDao provideOfferListDao(PSCoreDb db){
        return db.offerDao();
    }

    @Singleton
    @Provides
    ReportedItemDao provideReportedItemDao(PSCoreDb db){
        return db.reportedItemDao();
    }

    @Singleton
    @Provides
    BlockUserDao provideBlockUserDao(PSCoreDb db){
        return db.blockUserDao();
    }

    @Singleton
    @Provides
    OfflinePaymentDao provideOfflinePaymentDao(PSCoreDb db){
        return db.offlinePaymentDao();
    }

    @Singleton
    @Provides
    MessageDao provideMessageDao(PSCoreDb db) {
        return db.messageDao();
    }

    @Singleton
    @Provides
    PSCountDao providePSCountDao(PSCoreDb db) {
        return db.psCountDao();
    }

    @Singleton
    @Provides
    ItemPaidHistoryDao provideItemPaidHistoryDao(PSCoreDb db) {
        return db.itemPaidHistoryDao();
    }
}
