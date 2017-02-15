package mvp.jorge.com.rxretrofit20170214;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import mvp.jorge.com.rxretrofit20170214.bean.ROConsult;
import mvp.jorge.com.rxretrofit20170214.entity.HttpResult;
import mvp.jorge.com.rxretrofit20170214.entity.Subject;
import mvp.jorge.com.rxretrofit20170214.http.HttpUtils;
import mvp.jorge.com.rxretrofit20170214.security.ThreeDES;
import mvp.jorge.com.rxretrofit20170214.util.ObjectMaker;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author zj on 2017/2/14.
 */

public class RetrofitHelper {
    public  static  final String UserService_IP = "http://wapi.m.womai.com/";
    public static final String BASE_URL = "https://api.douban.com/v2/movie/";
    private  WoMaiApiService woMaiApiService;
    public  final int DEFAULT_TIMEOUT = 30;
    private OkHttpClient.Builder OkBuilder;
    public RetrofitHelper(){
        initOkHttp();
        getLoginApi();
    }
    public  void  getLoginApi(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(UserService_IP)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        woMaiApiService = retrofit.create(WoMaiApiService.class);
    }
    private void initOkHttp(){
        OkBuilder = new OkHttpClient.Builder();
        Interceptor httpInterceptor =  new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
//                Request request = chain.request();
//                return  null;
                Request request = chain.request();
                HttpUrl httpUrl = request.url();
//                HttpUrl httpUrl = request.url().newBuilder()
//                        .addQueryParameter("token", "tokenValue")
//                        .build();
                Request.Builder builder = request.newBuilder();

                Log.e("initOkHttp","67");
                String originalKeyString = ThreeDES.WOMAI_PUBLIC_KEY;
                String header = ObjectMaker.unConVer(HttpUtils.getHeader());
                Map<String, String> param  = new HashMap<String, String>();;
                try {
                    header = ThreeDES.orginalEncoded(originalKeyString, header);

//                    Map<String, String> headerMap = new HashMap<String, String>();
//                    headerMap.put("headerData", header);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                builder.addHeader("headerData",header);
                Log.e("initOkHttp","80");
                return chain.proceed(request);

            }
        };
        Interceptor cookiesInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                    final StringBuffer cookieBuffer = new StringBuffer();
                    Observable.from(originalResponse.headers("Set-Cookie"))
                            .map(new Func1<String, String>() {
                                @Override
                                public String call(String s) {
                                    String[] cookieArray = s.split(";");
                                    return cookieArray[0];
                                }
                            })
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String cookie) {
                                    if (cookie.startsWith("JSESSIONID=")) {
//                                        HttpUtils.setJessionId(cookie.replace("", "JSESSIONID="));
                                    }
                                }
                            });
//            SharedPreferences sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString("cookie", cookieBuffer.toString());
//            editor.commit();
                }
                Log.e("initOkHttp","111");
                return originalResponse;
            }
        };
        OkBuilder.addInterceptor(httpInterceptor);
        OkBuilder.addInterceptor(cookiesInterceptor);
        OkBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        OkBuilder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    };

    public void getRsa(Subscriber<List<Subject>> subscriber){
        String originalKeyString = ThreeDES.WOMAI_PUBLIC_KEY;
        String header = ObjectMaker.unConVer(HttpUtils.getHeader());
        Map<String, String> param  = new HashMap<String, String>();;
        try {
            header = ThreeDES.orginalEncoded(originalKeyString, header);

//        Map<String, String> headerMap = new HashMap<String, String>();
//        headerMap.put("headerData", header);

        String src = "abc";
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("consult", src);
        Map<String, Object> DATA = HttpUtils.getNoUserRequestMap(data);
        String base64str = ThreeDES.orginalEncoded(originalKeyString, ObjectMaker.unConVer(DATA));

        param.put("data", base64str);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return mGameService.login(params, mRequestHelper.getDeviceId()).subscribeOn(Schedulers.io());
         woMaiApiService.getRsa(param);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                //modify by zqikai 20160317 for 对http请求结果进行统一的预处理 GosnResponseBodyConvert
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ResponseConvertFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        WoMaiApiService   movieService = retrofit.create(WoMaiApiService.class);
        Observable observable = movieService.getRsa(param);

        toSubscribe(observable, subscriber);
    }

    public void getTopMovie(Subscriber<List<Subject>> subscriber, int start, int count){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                //modify by zqikai 20160317 for 对http请求结果进行统一的预处理 GosnResponseBodyConvert
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ResponseConvertFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        WoMaiApiService movieService = retrofit.create(WoMaiApiService.class);
        Observable observable = movieService.getTopMovie(start, count);
//                .map(new HttpResultFunc<List<Subject>>());

        toSubscribe(observable, subscriber);

    }

    private <T> void toSubscribe(Observable<T> o, Subscriber<T> s){
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }

    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     * @param <T>   Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
    private class HttpResultFunc<T> implements Func1<HttpResult<T>, T>{

        @Override
        public T call(HttpResult<T> httpResult) {
            if (httpResult.getCount() == 0) {
                throw new ApiException(100);
            }
            return httpResult.getSubjects();
        }
    }
}
