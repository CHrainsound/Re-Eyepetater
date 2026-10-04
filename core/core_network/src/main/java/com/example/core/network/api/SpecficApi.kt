package com.example.core.network.api

import com.example.core.model.CallMetroListResponse
import com.example.core.network.NetworkConfig.DeviceProfile
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * description ： api接口
 * email : 3014386984@qq.com
 * date : 2026/7/14 19:57
 */
interface SpecficApi {

    @GET("https://baobab.kaiyanapp.com/api/v3/lightTopics/internal/{topicId}")
    fun getTopicDetailRaw(@Path("topicId") topicId: Int): Call<okhttp3.ResponseBody>

    @FormUrlEncoded
    @POST("v1/card/page/get_page")
    fun getPageRaw(
        @Field("page_type") pageType: String = "card",
        @Field("page_label") pageLabel: String,
        @Field("udid") udid: String = DeviceProfile.UDID,
        @Field("vc") vc: String = DeviceProfile.VC,
        @Field("vn") vn: String = DeviceProfile.VN,
        @Field("deviceModel") deviceModel: String = DeviceProfile.DEVICE_MODEL,
        @Field("first_channel") firstChannel: String = DeviceProfile.FIRST_CHANNEL,
        @Field("size") size: String = DeviceProfile.SCREEN_SIZE,
        @Field("system_version_code") systemVersionCode: String = DeviceProfile.SYSTEM_VERSION_CODE,
        @Field("token") token: String = ""
    ): Call<okhttp3.ResponseBody>

    /** 下滑加载更多 — call_metro_list_v2 */
    @FormUrlEncoded
    @POST("v1/card/metro/call_metro_list_v2")
    fun loadMoreTopics(
        @Field("card_index") cardIndex: String,
        @Field("material") material: String,
        @Field("material_index") materialIndex: String,
        @Field("last_item_id") lastItemId: String,
        @Field("page_params") pageParams: String,
        @Field("page_label") pageLabel: String,
        @Field("card") card: String,
        @Field("data_source") dataSource: String,
        @Field("udid") udid: String = DeviceProfile.UDID,
        @Field("vc") vc: String = DeviceProfile.VC,
        @Field("vn") vn: String = DeviceProfile.VN,
        @Field("deviceModel") deviceModel: String = DeviceProfile.DEVICE_MODEL,
        @Field("first_channel") firstChannel: String = DeviceProfile.FIRST_CHANNEL,
        @Field("size") size: String = DeviceProfile.SCREEN_SIZE,
        @Field("system_version_code") systemVersionCode: String = DeviceProfile.SYSTEM_VERSION_CODE,
        @Field("token") token: String = ""
    ): Call<okhttp3.ResponseBody>

}
