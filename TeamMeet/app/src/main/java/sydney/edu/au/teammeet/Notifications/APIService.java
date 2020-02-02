package sydney.edu.au.teammeet.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAbf64nZA:APA91bE1FhIs78JStyJB54re8hOjC8HvOqYCW5dwglsJCS8zXKoZq-vuv82ByKS-K1loA_XzcxYnEEWm9lxWIXv3LkBbaHnxGCHVZ9GajH4zYRCAHizFr3-YxxMd9298_55ucyk-V11e"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
