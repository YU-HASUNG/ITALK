package leopardcat.studio.chitchat.components.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import leopardcat.studio.chitchat.R
import leopardcat.studio.chitchat.components.api.client.GPTClient
import leopardcat.studio.chitchat.components.api.client.GistClient
import leopardcat.studio.chitchat.components.api.model.ImageData
import leopardcat.studio.chitchat.components.data.Chat
import leopardcat.studio.chitchat.components.data.ChatState
import leopardcat.studio.chitchat.components.data.HeartState
import leopardcat.studio.chitchat.components.data.LoadingState
import leopardcat.studio.chitchat.components.data.Person
import leopardcat.studio.chitchat.components.data.PersonState
import leopardcat.studio.chitchat.components.data.SubscribeState
import leopardcat.studio.chitchat.components.data.VipState
import leopardcat.studio.chitchat.components.room.chat.ChatList
import leopardcat.studio.chitchat.components.room.chat.ChatListDao
import leopardcat.studio.chitchat.components.util.Preference
import leopardcat.studio.chitchat.components.util.convertToAmPmFormat
import leopardcat.studio.chitchat.components.util.getCurrentDateTime
import leopardcat.studio.chitchat.components.util.isToday
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class MainViewModel(
    private val context: Context,
    private val dao: ChatListDao
) :ViewModel() {
    @SuppressLint("StaticFieldLeak")
    private lateinit var activity: Activity
    private lateinit var prefs: Preference
    private val apiKey=""

    private var mRewardedAd: RewardedAd? = null //보상형 광고상태
    private var mRewardLastClickTime: Long = 0 //보상형 광고 마지막 클릭 시간
    private val REWARD_AD_ID = ""
    private val REWARD_AD_TEST_ID = ""

    private var mInterstitialAd: InterstitialAd? = null //전면형 광고 상태
    private val COVER_AD_ID = ""
    private val COVER_AD_TEST_ID = ""

    val HEART_REWORD = "heart_reword"
    val IMAGE_REWORD = "image_reword"

    fun setActivity(applicationActivity: Activity) {
        activity = applicationActivity
    }

    fun setPreference(applicationContext: Context) {
        prefs = Preference(applicationContext)
    }

    fun getPreference(): Preference {
        return prefs
    }

    /***
     * Image Person Id
     ***/
    var imageId: Int = 0

    /***
     * Delete Chat Id
     ***/
    private var deleteChatId: String = ""

    /***
     * 리워드 광고 관련
     ***/
    fun setAdsInit(context: Context) { // 기본 세팅
        MobileAds.initialize(context)
    }

    fun loadRewardedAd(context: Context) { //광고 불러오기
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            //TODO 광고 ID 설정
            context, REWARD_AD_TEST_ID, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.d("TAG", p0.message)
                    mRewardedAd = null
                }

                override fun onAdLoaded(p0: RewardedAd) {
                    mRewardedAd = p0
                }
            }
        )
    }

    private fun isButtonClickable(): Boolean {
        val currentTime = SystemClock.elapsedRealtime()
        val elapsedTime = currentTime - mRewardLastClickTime

        // 5분(300,000 밀리초) 이상 경과한 경우에만 버튼 클릭 허용
        if (elapsedTime > 60000) {
            mRewardLastClickTime = currentTime
            return true
        }

        return false
    }

    fun showRewardedAd(context: Activity, string: String, id: Int, url: String) { //광고 노출
        if(isButtonClickable()) { //시간 체크 1분
            if (mRewardedAd != null) {
                mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("TAG", "Ad was dismissed")
                        mRewardedAd = null
                        loadRewardedAd(context)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        Log.d("TAG", "Ad failed to show.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("TAG", "Ad showed fullscreen content.")
                        mRewardedAd = null
                    }
                }
                mRewardedAd?.show(context, OnUserEarnedRewardListener() {
                    when (string) {
                        HEART_REWORD -> {
                            if (prefs.getHeart("heart") < 8) {
                                val heart = prefs.getHeart("heart") + 1
                                val heartState = HeartState(heart, false)
                                _heart.postValue(heartState)
                                Toast.makeText(context, context.getString(R.string.view_model_get_heart_toast), Toast.LENGTH_LONG).show()
                            }
                        }

                        IMAGE_REWORD -> {
                            val updatedImageList =
                                imageList.value?.imageList?.get(imageId)?.image?.toMutableList()
                            if (updatedImageList != null && updatedImageList.size > id) {
                                updatedImageList[id] =
                                    updatedImageList[id].copy(id = "any", url = url)
                                val updatedImageDataList =
                                    imageList.value?.imageList?.toMutableList()
                                if (updatedImageDataList != null && updatedImageDataList.size > imageId) {
                                    updatedImageDataList[imageId] =
                                        updatedImageDataList[imageId].copy(image = updatedImageList)
                                    val updatedImageData =
                                        imageList.value?.copy(imageList = updatedImageDataList)
                                    // 이미지 데이터 업데이트
                                    _imageList.postValue(updatedImageData)
                                    Log.d(
                                        "hasung",
                                        "updatedImageList[id] : ${updatedImageList[id]}"
                                    )
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.view_model_get_image_toast),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }

                })
            } else {
                Toast.makeText(context, context.getString(R.string.view_model_get_ads_toast), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.view_model_get_ads_toast), Toast.LENGTH_SHORT).show()
        }
    }

    /***
     * 전면 광고 관련
     ***/
    fun setCoverAds() {
        MobileAds.initialize(context) {
            val adRequest = AdRequest.Builder().build()
            //TODO 광고 ID 설정
            InterstitialAd.load(context,
                COVER_AD_TEST_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialAd = null
                    }
                }
            )
        }
    }

    fun loadCoverAds() {
        mInterstitialAd?.show(activity)
    }

    /***
     * ChatList 상태 관리
     ***/
    private val _chatList = MutableLiveData<ChatState>()
    val chatList: LiveData<ChatState> = _chatList

    /***
     * PersonList 상태 관리
     ***/
    private val _personList = MutableLiveData<PersonState>()
    val personList: LiveData<PersonState> = _personList

    /***
     * Heart 상태 관리
     ***/
    private val _heart = MutableLiveData<HeartState>()
    val heart: LiveData<HeartState> = _heart

    /***
     * VIP 상태 관리
     ***/
    private val _vip = MutableLiveData<VipState>()
    val vip: LiveData<VipState> = _vip

    /***
     * 인앱 구독 상태 관리
     ***/
    private val _subscribe = MutableLiveData<SubscribeState>()
    val subscribe: LiveData<SubscribeState> = _subscribe

    /***
     * Api Loading lottie 상태 관리
     ***/
    private val _apiLoading = MutableLiveData<LoadingState>()
    val apiLoading: LiveData<LoadingState> = _apiLoading

    /***
     * Gist picture Api
     ***/
    private val _imageList = MutableLiveData<ImageData>()
    val imageList: LiveData<ImageData> = _imageList

    /***
     * API 처리
     ***/
    //GPT api
    @SuppressLint("CheckResult")
    fun gptApi(name: String){
        viewModelScope.launch {
            delay(1500)
            GPTClient.getGPTApi().getGPTRepos("application/json", "Bearer $apiKey", setApiInfo(name))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ items ->
                    onEvent(ChatEvent.SetChat(items.choices[0].message.content, name, true))
                    println("AI 응답" + items.choices[0].message.content)
                }, { e ->
                    onEvent(ChatEvent.SetChat(context.getString(R.string.view_model_fail_get_response), name, true))
                    println("AI 응답실패" + e.toString())
                })
        }
    }

    private suspend fun setApiInfo(name: String): RequestBody {

        val chatList = dao.getChatListByName(name)
        val messages = chatList.message
        val isUser = chatList.direction
        val messageCount = messages.size

        val messageList = mutableListOf<JSONObject>()

//        AI 컨셉
        val concept = when(name) {
            context.getString(R.string.name_one) -> context.getString(R.string.ai_one)
            context.getString(R.string.name_two) -> context.getString(R.string.ai_two)
            context.getString(R.string.name_three) -> context.getString(R.string.ai_three)
            context.getString(R.string.name_four) -> context.getString(R.string.ai_four)
            context.getString(R.string.name_five) -> context.getString(R.string.ai_five)
            context.getString(R.string.name_six) -> context.getString(R.string.ai_six)
            else -> context.getString(R.string.ai_one)
        }

        val aiConcept = JSONObject()
        aiConcept.put("role", "system")
        aiConcept.put("content", concept)

        messageList.add(aiConcept) //AI 컨셉 추가

        // 사용자와 AI 대화 메시지 데이터 가공
        //최대 20번까지의 대화내용 전송
        if(messageCount > 21){
            for (i in (messageCount - 20) until messageCount) {
                val userMsg = JSONObject()
                if (isUser[i]) {
                    userMsg.put("role", "assistant")
                } else {
                    userMsg.put("role", "user")
                }
                userMsg.put("content", messages[i])
                messageList.add(userMsg)
            }
        } else {
            for (i in 0 until messageCount) {
                val userMsg = JSONObject()
                if (isUser[i]) {
                    userMsg.put("role", "assistant")
                } else {
                    userMsg.put("role", "user")
                }
                userMsg.put("content", messages[i])
                messageList.add(userMsg)
            }
        }

        // messageList를 JSONArray로 변환
        val jsonArray = JSONArray(messageList)

        //Body 생성
        val requestBody = JSONObject()
        requestBody.put("model", "gpt-3.5-turbo")
        requestBody.put("messages", jsonArray)

        return requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }

    //GIST 이미지 api
    @SuppressLint("CheckResult")
    fun getGISTApi(info: String) {
        GistClient.getGISTApi().getRepos(info)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ items ->
                val imageList = ImageData(items.imageList)
                _imageList.postValue(imageList)
            }, { e ->
                println("gist error : $e")
            })
    }


    /***
     * 이벤트 처리
     ***/
    fun onEvent(event: ChatEvent) {
        when(event) {
            is ChatEvent.SetVip -> { //vip 관리
                val vip = event.vip
                val vipState = VipState(vip)
                _vip.postValue(vipState)
            }
            is ChatEvent.SetPersonImageId -> {
                imageId = event.id
            }
            is ChatEvent.SetHeart -> { //heart 관리
                val heart = event.heart - 1
                val heartState = HeartState(heart, false)
                _heart.postValue(heartState)
            }
            is ChatEvent.SetApiLoading -> { //api 로딩
                val loadingState = LoadingState(event.isLoading)
                _apiLoading.postValue(loadingState)
            }
            is ChatEvent.SetChat -> { //채팅 입력하면 호출받는 곳
                viewModelScope.launch {
                    val chat = dao.getChatListByName(event.name)
                    if(chat != null){
                        val updatedMessageList = chat.message.toMutableList()
                        Log.d("hasung", escapeCommas(event.chat))
                        updatedMessageList.add(escapeCommas(event.chat)) // Add the new message

                        val updatedTimeList = chat.time.toMutableList()
                        updatedTimeList.add(getCurrentDateTime()) // Add the new message

                        val updatedDirectionList = chat.direction.toMutableList()
                        updatedDirectionList.add(event.boolean) // Add the new message

                        val updatedChat = chat.copy(
                            name = event.name,
                            message = updatedMessageList,
                            time = updatedTimeList,
                            direction = updatedDirectionList
                        )
                        Log.d("MainViewModel", updatedChat.toString())
                        dao.upsertChatList(updatedChat)
                    } else {
                        val chatList = ChatList(
                            name = event.name,
                            message = listOf(event.chat),
                            time = listOf(getCurrentDateTime()),
                            direction = listOf(false)
                        )
                        Log.d("MainViewModel", chatList.toString())
                        dao.upsertChatList(chatList)
                    }

                    val chitChat = ChatState(convertChatListToList(dao.getChatListByName(event.name)))
                    _chatList.postValue(chitChat)

                    val loadingState = LoadingState(!event.boolean) //api loading lottie 시작
                    _apiLoading.postValue(loadingState)
                }
            }
            is ChatEvent.SetPerson -> {
                viewModelScope.launch {
                    val chat = dao.getChatListByName(event.name)
                    if(chat != null){
                        val chitChat = ChatState(convertChatListToList(chat))
                        _chatList.postValue(chitChat)
                        Log.d("MainViewModel", chitChat.toString())
                    } else {
                        val chatList = ChatList(
                            name = event.name,
                            message = listOf(context.getString(R.string.view_model_first_chat, event.name)),
                            time = listOf(getCurrentDateTime()),
                            direction = listOf(true)
                        )
                        dao.upsertChatList(chatList)

                        val chitChat = ChatState(convertChatListToList(chatList))
                        _chatList.postValue(chitChat)
                    }
                }
            }
            else -> {
                return
            }
        }
    }

    // ChatList -> Chat
    private fun convertChatListToList(chatList: ChatList): List<Chat> {
        val chatDataList = mutableListOf<Chat>()
        val size = chatList.message.size
        Log.d("hasung", chatList.message.toString())
        Log.d("hasung", chatList.time.toString())
        Log.d("hasung", chatList.direction.toString())

        for (i in 0 until size) {
            val message = unescapeCommas(chatList.message[i])
            val time = chatList.time[i]
            val direction = chatList.direction[i]

            val chat = Chat(message, time, direction)
            chatDataList.add(chat)
        }

        return chatDataList
    }

    // 쉼표를 이스케이프 처리하는 함수
    private fun escapeCommas(input: String): String {
        return input.replace(",", "|||")
    }

    // 이스케이프 처리된 쉼표를 복원하는 함수
    private fun unescapeCommas(input: String): String {
        return input.replace("|||", ",")
    }

    /***
     * 마지막 대화시간 가져오기
     ***/
    fun getPersonList() {
        viewModelScope.launch {
            personListBase.forEach { person ->
                val lastChatTime = dao.getLastChatTimeByName(person.name)
                if (lastChatTime != null) {
                    val personList = lastChatTime.split(",")
                    if (isToday(personList.last())) { //금일
                        personListBase[person.id].lastChat = convertToAmPmFormat(context, personList.last())
                    } else { //금일이 아닌 경우
                        personListBase[person.id].lastChat = personList.last()
                    }
                } else {
                    personListBase[person.id].lastChat = ""
                }
            }

            val personList = PersonState(personListBase)
            _personList.postValue(personList)
        }
    }

    fun getHeartState() { //마지막 채팅 기준으로 하루가 지나면 하트 업데이트 해주기
        viewModelScope.launch {
            var lastChat = ""
            personListBase.forEach { person ->
                val lastChatTime = dao.getLastChatTimeByName(person.name)
                if (lastChatTime != null) {
                    val personList = lastChatTime.split(",")
                    if (personList.isNotEmpty() && personList[personList.lastIndex] > lastChat) {
                        lastChat = personList[personList.lastIndex]
                    }
                }
            }

            if (lastChat.isNotEmpty() && !isToday(lastChat)) {
                val heart = HeartState(8, false)
                _heart.postValue(heart)
            }
        }
    }

    fun setVipTime(): Long {
        val serverTimeMillis = 86400000L // 서버 시간을 밀리초로 나타내는 Long 값으로 설정

        val currentMillis = Instant.now().toEpochMilli()
        val storedTimeMillis = prefs.getVipTime("time")
        val timeDifferenceMillis = currentMillis - storedTimeMillis

        if (storedTimeMillis == 0L) {
            prefs.setVipTime("time", currentMillis)
        } else {
            val remainingTimeMillis = 86400000L - timeDifferenceMillis
            return if (remainingTimeMillis > 0L) {
                remainingTimeMillis
            } else {
                0L
            }
        }

        return serverTimeMillis
    }

    /*
    * 데이터 삭제 관련
     */
    fun setDeleteChatId(id: String) {
        deleteChatId = id
    }
    fun deleteChat() { //데이터 삭제
        viewModelScope.launch {
            dao.deleteChatListByName(deleteChatId)
        }
    }

    /*
    * 인앱 결제 관련
     */
    private val TAG = "BillingLog"
    private lateinit var billingClient: BillingClient

    // 구매 관련 업데이트를 수신하는 리스너
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, Purchase ->
        Log.d(TAG, " purchasesUpdatedListener 호출")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && Purchase != null) { //정상
            for (purchase in Purchase) {
                handlePurchase(purchase)
                Log.d(TAG, "BillingResponseCode.OK")
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){ //이미 구매
            Log.d(TAG, "BillingResponseCode.ITEM_ALREADY_OWNED")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) { //지원 안함
            Log.d(TAG, "BillingResponseCode.FEATURE_NOT_SUPPORTED")
        } else { // 기타 경우
            Toast.makeText(context, "결제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //BillingClient 초기화
    fun initBillingClient(){
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    //결제 프로세스 시작
    fun startBilling(productId: String){
        billingClient.startConnection(object : BillingClientStateListener {

            //연결 실패
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected")
            }

            //연결 성공
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "onBillingSetupFinished")

                    //상품 목록 가져오기
                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                    val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

                    //인앱 상품 세부정보 퀴리 진행
                    //구매 흐름 시작
                    billingClient.queryProductDetailsAsync(params.build()) { _, productDetailsList ->
                        for (productDetails in productDetailsList) {
                            val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
                            val productDetailsParamsList =
                                listOf(
                                    offerToken?. let {
                                        BillingFlowParams. ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .setOfferToken(it)
                                            .build()
                                    }
                                )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()
                            // Launch the billing flow
                            billingClient.launchBillingFlow(activity, billingFlowParams)
                        }
                    }
                }
            }
        })
    }

    //결제 성공 시
    private fun handlePurchase(purchase: Purchase) {
        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED){

            //성공 여부 구글에 인증 해줌
            //난 백엔드가 없으니 검증없이 바로 인증
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener)

            //결제 확인 로직
            if(!purchase.isAcknowledged) { //이미 구독중인지 여부 확인
                if(purchase.products.toString() == "[premium_subscription_monthly]"){
                    Toast.makeText(context, context.getString(R.string.view_model_monthly_subscribe), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "월단위 프리미엄 구독이 완료되었습니다!")
                    prefs.setSubscribeMonthly("monthly", true)
                    prefs.setSubscribeYearly("yearly", false)
                    prefs.setSubscribeYearly24("24", false)
                    val subValue = SubscribeState(
                        month = true,
                        year = false
                    )
                    _subscribe.postValue(subValue)
                    subscribeStart()
                } else if(purchase.products.toString() == "[premium_subscription_yearly]"){
                    Toast.makeText(context, context.getString(R.string.view_model_yearly_subscribe), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "년단위 프리미엄 구독이 완료되었습니다!")
                    prefs.setSubscribeMonthly("monthly", false)
                    prefs.setSubscribeYearly("yearly", true)
                    prefs.setSubscribeYearly24("24", false)
                    val subValue = SubscribeState(
                        month = false,
                        year = true
                    )
                    _subscribe.postValue(subValue)
                    subscribeStart()
                } else if(purchase.products.toString() == "[premium_subscription_yearly_24_event]"){
                    Toast.makeText(context, context.getString(R.string.view_model_yearly_subscribe_event), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "년단위 프리미엄 구독이 완료되었습니다!")
                    prefs.setSubscribeMonthly("monthly", false)
                    prefs.setSubscribeYearly("yearly", false)
                    prefs.setSubscribeYearly24("24", true)
                    val subValue = SubscribeState(
                        month = false,
                        year = true
                    )
                    _subscribe.postValue(subValue)
                    subscribeStart()
                }
            } else{
                Log.d(TAG, "Already_Subscribed")
            }
        }else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "PurchaseState.PENDING")
        } else if (purchase.purchaseState == Purchase. PurchaseState.UNSPECIFIED_STATE) {
            Log.d(TAG, "PurchaseState.UNSPECIFIED_STATE")
        }
    }

    //구독상태 조회
    fun checkBilling() {
        billingClient.startConnection(object : BillingClientStateListener {

            //연결 실패
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected")
            }

            //연결 성공
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                billingClient.queryPurchasesAsync(QueryPurchasesParams
                    .newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build())
                { _, purchaseList ->
                    if (purchaseList.isNotEmpty()) {
                        try {
                            if (purchaseList[0].products.toString() == "[premium_subscription_monthly]") { //월 구독
                                prefs.setSubscribeMonthly("monthly", true)
                                prefs.setSubscribeYearly("yearly", false)
                                prefs.setSubscribeYearly24("24", false)
                                val subValue = SubscribeState(
                                    month = true,
                                    year = false
                                )
                                _subscribe.postValue(subValue)
                                subscribeStart()
                            } else if (purchaseList[0].products.toString() == "[premium_subscription_yearly]") { //년 구독
                                prefs.setSubscribeMonthly("monthly", false)
                                prefs.setSubscribeYearly("yearly", true)
                                prefs.setSubscribeYearly24("24", false)
                                val subValue = SubscribeState(
                                    month = false,
                                    year = true
                                )
                                _subscribe.postValue(subValue)
                                subscribeStart()
                            } else if (purchaseList[0].products.toString() == "[premium_subscription_yearly_24_event]") {
                                prefs.setSubscribeMonthly("monthly", false)
                                prefs.setSubscribeYearly("yearly", false)
                                prefs.setSubscribeYearly24("24", true)
                                val subValue = SubscribeState(
                                    month = false,
                                    year = true
                                )
                                _subscribe.postValue(subValue)
                                subscribeStart()
                            }
                        } catch(e: Exception){
                            Log.d(TAG, "Exception+ $e")
                        }
                    }else {
                        prefs.setSubscribeMonthly("monthly", false)
                        prefs.setSubscribeYearly("yearly", false)
                        prefs.setSubscribeYearly24("24", false)
                        val subValue = SubscribeState(
                            month = false,
                            year = false
                        )
                        _subscribe.postValue(subValue)
                        subscribeFinish()
                        Log.d(TAG, " purchaseList : empty")
                    }
                }
            }
        })
    }

    //결제 완료 상태 확인
    private var acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "BillingResponseCode.OK")
        }
    }

    private fun subscribeStart() {
        //person 잠금 해제
        personListBase.forEach { person ->
            if(person.id >= 3){
                personListBase[person.id].lock = false
            }
        }

        val personList = PersonState(personListBase)
        _personList.postValue(personList)

        //하트 무제한
        val heart = HeartState(8, true)
        _heart.postValue(heart)
    }

    private fun subscribeFinish() {
        personListBase.forEach { person ->
            if(person.id >= 3){
                personListBase[person.id].lock = true
            }
        }

        val personList = PersonState(personListBase)
        _personList.postValue(personList)

        //하트 제한
        val heart = HeartState(prefs.getHeart("heart"), false)
        _heart.postValue(heart)
    }

    private val personListBase = listOf(
        Person(
            0,
            context.getString(R.string.name_one),
            context.getString(R.string.explain_one),
            "@1our",
            context.getString(R.string.follower_one),
            "",
            false,
            R.drawable.one
        ),
        Person(
            1,
            context.getString(R.string.name_two),
            context.getString(R.string.explain_two),
            "@chorong_chorong",
            context.getString(R.string.follower_two),
            "",
            false,
            R.drawable.two
        ),
        Person(
            2,
            context.getString(R.string.name_three),
            context.getString(R.string.explain_three),
            "@Dew-Kang",
            context.getString(R.string.follower_three),
            "",
            false,
            R.drawable.three
        ),
        Person(
            3,
            context.getString(R.string.name_four),
            context.getString(R.string.explain_four),
            "@21",
            context.getString(R.string.follower_four),
            "",
            true,
            R.drawable.four
        ),
        Person(
            4,
            context.getString(R.string.name_five),
            context.getString(R.string.explain_five),
            "@marble",
            context.getString(R.string.follower_five),
            "",
            true,
            R.drawable.five
        ),
        Person(
            5,
            context.getString(R.string.name_six),
            context.getString(R.string.explain_six),
            "@NO-Beauty",
            context.getString(R.string.follower_six),
            "",
            true,
            R.drawable.six
        )
    )
}