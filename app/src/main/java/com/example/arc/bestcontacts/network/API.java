package com.example.arc.bestcontacts.network;

import com.example.arc.bestcontacts.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static java.lang.reflect.Modifier.TRANSIENT;

/**
 * Created by arc on 29/12/16.
 */

public interface API {

    String API_BASE_URL = "http://best-mostar.org/bestContacts/";  //PHONE


    @Headers("Content-Type: application/json")
    @POST("login.php")
    Call<User> userLogin(@Body JsonObject jo);

    @Headers("Content-Type: application/json")
    @POST("change_password.php")
    Call<ResponseBody> userChangePass(@Body JsonObject jo);

    @Headers("Content-Type: application/json")
    @POST("contacts.php")
    Call<List<User>> getUserList(@Body JsonObject jo);

    class getServices {


        public static String getApiBaseUrl() {
            return API_BASE_URL;
        }

        private static Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        private static Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson));

//odavde


        //        private static HttpLoggingInterceptor headerLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
        private static HttpLoggingInterceptor bodyLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
//        private static HttpLoggingInterceptor AllLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.);

//

        private static Retrofit medoRetrofit = null;
        public static API AppApiServices = null;
        private static OkHttpClient.Builder httpClient2 = new OkHttpClient.Builder().addInterceptor(bodyLoggingInterceptor);


        public static Retrofit getMedoRetrofit() {
            if (medoRetrofit == null) {
                httpClient2.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        Request.Builder requestBuilder = request.newBuilder()
                                .method(request.method(), request.body());
                        Request request1 = requestBuilder.build();
                        return chain.proceed(request1);

                    }
                });
//                        .addInterceptor(headerLoggingInterceptor);
                OkHttpClient client = httpClient2.build();
                medoRetrofit = builder.client(client).build();

                return medoRetrofit;
            } else {
                return medoRetrofit;
            }
        }

        public static API getAppApiServices() {
            if (AppApiServices == null) {
                AppApiServices = getMedoRetrofit().create(API.class);
                return AppApiServices;
            } else {
                return AppApiServices;
            }

        }

    }

}
