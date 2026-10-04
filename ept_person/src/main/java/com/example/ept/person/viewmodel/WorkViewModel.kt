package com.example.ept.person.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.core.model.NavTab
import com.example.core.model.MetroData
import com.example.core.network.RetrofitClient
import com.example.core.network.api.UniversalApi
import com.example.core.network.await
import com.example.core.common.parseWorkListFromCardList
import com.example.core.network.api.defaultCardJSON
import com.example.core.network.api.defaultMaterialJSON
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**   
 * 包名称： com.example.ept.person.pgc.viewmodel
 * 类名称：WorkViewModel
 * 类描述：TODO
 * 创建人：韦西波
 * 创建时间：2026-07-21 21:34
 *
 */
class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private var _liveData = MutableLiveData<WorkState>()
    val liveData: LiveData<WorkState> get() = _liveData
    private lateinit var cardJSON: String
    private lateinit var materialJson: String
    private var lastItemId=""
    private val appService: UniversalApi by lazy {
        RetrofitClient.create()
    }
    private var allWorks:List<MetroData> = emptyList()

    fun initLiveData(workTab: NavTab) {
        viewModelScope.launch {
            try {
                val response = appService.getPage(workTab.pageLabel, workTab.pageType).await()
                val result = parseWorkListFromCardList(response)
                allWorks = result.items
                val firstCard = response.result?.cardList?.firstOrNull { it.type == "call_metro_list" }
                val params = firstCard?.cardData?.body?.apiRequest?.params

                lastItemId = params?.get("last_item_id") as? String ?: ""
                materialJson = params?.get("material") as? String ?: ""
                cardJSON = Gson().toJson(firstCard)

                _liveData.value = WorkState.InitState(result.items.toMutableList(),result.title.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                _liveData.value = WorkState.FailedState(e.message.toString())
            }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            if (materialJson.isNullOrEmpty()){
                materialJson= defaultMaterialJSON
            }
            if (cardJSON.isNullOrEmpty()){
                cardJSON= defaultCardJSON
            }

            val response=appService.getMorePage(
                dataSource= "home_user_work_list",
                materialJSON = materialJson,
                lastItemId = lastItemId,
                pageLabel = "user_center_work",
                cardJSON = cardJSON
            ).await()

            val newItems = response.result?.itemList
            if (lastItemId.isNullOrEmpty()){
                _liveData.value = WorkState.LoadingState(allWorks.toMutableList())
                return@launch
            }
            lastItemId = response.result?.lastItemId?:""
            val workDataList = newItems?.mapNotNull { it.metroData}
            if (!workDataList.isNullOrEmpty()) allWorks=allWorks+workDataList
            _liveData.value = WorkState.LoadingState(allWorks.toMutableList())
        }
    }


}

sealed class WorkState{
    data class InitState(val workList: MutableList<MetroData>,val title: String) : WorkState()
    data class RefreshState(val workList: MutableList<MetroData>) : WorkState()
    data class LoadingState(val newWorkList: MutableList<MetroData>) : WorkState()
    data class FailedState(val msg: String) : WorkState()
}
