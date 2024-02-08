package leopardcat.studio.chitchat.components.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import leopardcat.studio.chitchat.components.api.model.ImageData
import leopardcat.studio.chitchat.components.screens.HomeScreen
import leopardcat.studio.chitchat.components.data.ChatState
import leopardcat.studio.chitchat.components.data.HeartState
import leopardcat.studio.chitchat.components.data.LoadingState
import leopardcat.studio.chitchat.components.data.PersonState
import leopardcat.studio.chitchat.components.data.SubscribeState
import leopardcat.studio.chitchat.components.data.VipState
import leopardcat.studio.chitchat.components.screens.ChatScreen
import leopardcat.studio.chitchat.components.screens.ImageScreen
import leopardcat.studio.chitchat.components.screens.StartScreen
import leopardcat.studio.chitchat.components.screens.VipDialog
import leopardcat.studio.chitchat.components.viewmodel.ChatEvent
import leopardcat.studio.chitchat.components.viewmodel.MainViewModel

@Composable
fun MainNavigation(
    viewModel: MainViewModel,
    context: Context
) {
    val navHostController = rememberNavController()
    val chatList by viewModel.chatList.observeAsState(initial = ChatState(emptyList()))
    val apiLoading by viewModel.apiLoading.observeAsState(initial = LoadingState(false))
    val vip by viewModel.vip.observeAsState(initial = VipState(false))
    val personList by viewModel.personList.observeAsState(initial = PersonState(emptyList()))
    val imageData by viewModel.imageList.observeAsState(initial = ImageData(emptyList()))
    val subscribeState by viewModel.subscribe.observeAsState(initial = SubscribeState(viewModel.getPreference().getSubscribeMonthly("monthly"), (viewModel.getPreference().getSubscribeYearly("yearly") || viewModel.getPreference().getSubscribeYearly24("24"))))
    val heart by viewModel.heart.observeAsState(initial = HeartState(viewModel.getPreference().getHeart("heart"), (subscribeState.month || subscribeState.year)))

    NavHost(navController = navHostController, startDestination = START_SCREEN) {
        composable(START_SCREEN) {
            StartScreen(navHostController, viewModel)
        }
        composable(HOME_SCREEN) {
            HomeScreen(navHostController, viewModel, personList, subscribeState)
        }
        composable(CHAT_SCREEN) {
            ChatScreen(navHostController, chatList, apiLoading, subscribeState, heart, viewModel)
        }
        composable(IMAGE_SCREEN) {
            ImageScreen(navHostController, viewModel, imageData, personList, subscribeState)
        }
    }
    if(vip.vip){
        val leftTime = viewModel.setVipTime()
        VipDialog(
            context,
            leftTime,
            subscribeState,
            viewModel,
            onDismiss = {
                viewModel.onEvent(ChatEvent.SetVip(false))
            }
        )
    }
}

const val START_SCREEN = "Start screen"
const val HOME_SCREEN = "Home screen"
const val CHAT_SCREEN = "Char screen"
const val IMAGE_SCREEN = "Image screen"