package leopardcat.studio.chitchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import leopardcat.studio.chitchat.components.navigation.MainNavigation
import leopardcat.studio.chitchat.components.room.chat.ChatListDatabase
import leopardcat.studio.chitchat.components.viewmodel.MainViewModel
import leopardcat.studio.chitchat.ui.theme.ChitchatTheme

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            ChatListDatabase::class.java,
            "chat_list.db"
        ).fallbackToDestructiveMigration()//DB Migration 안함. 기존 DB 버림 처리
            .build()
    }

    private val viewModel by viewModels<MainViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(this@MainActivity, db.chatInfoDao) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setActivity(this) //viewModel Context 세팅
        viewModel.setPreference(this) //Preference setting
        viewModel.initBillingClient() //인앱 결제 세팅
        viewModel.setAdsInit(this) //리워드 광고 기본세팅
        viewModel.loadRewardedAd(this) //리워드 광고 불러오기
        viewModel.setCoverAds() //전명 광고 기본세팅
        viewModel.getGISTApi("ag_image") //image api 호출

        setContent {
            ChitchatTheme {
                MainNavigation(viewModel, this)
            }
        }
    }
}