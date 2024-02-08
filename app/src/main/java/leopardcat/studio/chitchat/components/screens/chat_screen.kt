package leopardcat.studio.chitchat.components.screens


import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import leopardcat.studio.chitchat.components.IconComponentDrawable
import leopardcat.studio.chitchat.components.SpacerWidth
import leopardcat.studio.chitchat.components.data.Chat
import leopardcat.studio.chitchat.components.data.ChatState
import leopardcat.studio.chitchat.components.data.Person
import leopardcat.studio.chitchat.components.viewmodel.ChatEvent
import leopardcat.studio.chitchat.components.viewmodel.MainViewModel
import leopardcat.studio.chitchat.ui.theme.Yellow
import leopardcat.studio.chitchat.R
import leopardcat.studio.chitchat.components.data.HeartState
import leopardcat.studio.chitchat.components.data.LoadingState
import leopardcat.studio.chitchat.components.data.SubscribeState
import leopardcat.studio.chitchat.components.navigation.IMAGE_SCREEN
import leopardcat.studio.chitchat.components.util.convertToAmPmFormat
import leopardcat.studio.chitchat.ui.theme.DarkGray
import leopardcat.studio.chitchat.ui.theme.GreenSub
import leopardcat.studio.chitchat.ui.theme.PinkSub
import leopardcat.studio.chitchat.ui.theme.PurpleMain
import leopardcat.studio.chitchat.ui.theme.PurpleSub
import leopardcat.studio.chitchat.ui.theme.YellowSub

@Composable
fun ChatScreen(
    navHostController: NavHostController,
    chatState: ChatState,
    apiLoading: LoadingState,
    subscribeState: SubscribeState,
    heart: HeartState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    var message by remember { mutableStateOf("") }
    val data = navHostController.previousBackStackEntry?.savedStateHandle?.get<Person>("data") ?: Person()

    val initialScrollIndex = if (chatState.chatList.isNotEmpty()) { chatState.chatList.size - 1 } else { 0 }
    val lazyListState = rememberLazyListState(initialScrollIndex) //채팅 스크롤 상태

    var showAlertDialog by remember { mutableStateOf(false) } //Dialog 상태

    var isWaitResponse by remember { mutableStateOf(false) } //ai 응답 대기 상태
    val loadingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading)) //loading lottie 상태
    val crownComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.crown)) //프리미엄 lottie 상태
    val giftComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gift)) //선물 lottie 상태

    viewModel.getPreference().setHeart("heart", heart.heart) //하트 저장

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleMain)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            UserNameRow(
                person = data,
                subscribeState,
                heart,
                onItemClick = {
                    if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
                        showAlertDialog = true
                    }
                },
                crownComposition,
                viewModel,
                navHostController,
                modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .padding(top = 15.dp)

            ) {
                LazyColumn(
                    modifier = Modifier.padding(
                        start = 15.dp,
                        top = 25.dp,
                        end = 15.dp,
                        bottom = 75.dp
                    ),
                    state = lazyListState,
                ) {
                    items(chatState.chatList) { item ->
                        val index = chatState.chatList.indexOf(item)
                        val lastIndex = chatState.chatList.size-1 == index// 현재 아이템의 인덱스 가져오기
                        ChatRow(context, chat = item, lastIndex, apiLoading, loadingComposition)
                    }
                }
            }
        }

        CustomTextField(
            text = message,
            onValueChange = { message = it },
            onItemClick = {
                if(message != ""){
                    if(heart.heart <= 0){
                        Toast.makeText(context, context.getString(R.string.chat_screen_get_heart), Toast.LENGTH_LONG).show()
                    } else if(apiLoading.loading){
                        Toast.makeText(context, context.getString(R.string.chat_screen_wait), Toast.LENGTH_LONG).show()
                    } else {
                        viewModel.onEvent(ChatEvent.SetApiLoading(true)) // loading바 생성
                        if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
                            viewModel.onEvent(ChatEvent.SetHeart(viewModel.getPreference().getHeart("heart"))) // 하트 개수
                        }
                        viewModel.onEvent(ChatEvent.SetChat(message, data.name, false)) //채팅 저장
                        viewModel.gptApi(data.name) //gpt api 호출
                        isWaitResponse = true
                        message = ""
                    }
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, top = 20.dp, bottom = 10.dp, end = 10.dp)
                .align(BottomCenter)
        )
    }

    // 채팅 목록 / 메시지가 업데이트될 때마다 스크롤 위치를 최신 채팅으로 조정
    LaunchedEffect(chatState.chatList, message) {
        if (chatState.chatList.isNotEmpty()) {
            lazyListState.scrollToItem(chatState.chatList.size - 1)
        }
    }

    if (showAlertDialog) { //Dialog 정의
        AlertDialog(
            containerColor = White,
            onDismissRequest = {
                showAlertDialog = false
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally, // 가로 가운데 정렬
                    verticalArrangement = Arrangement.Center, // 세로 가운데 정렬
                ) {
                    LottieAnimation(
                        composition = giftComposition,
                        modifier = Modifier.size(80.dp), // 원하는 크기로 설정
                        iterations = Int.MAX_VALUE
                    )
                    Text(context.getString(R.string.chat_screen_get_free_heart),
                        textAlign = TextAlign.Center, // 가운데 정렬을 위한 textAlign
                        style = TextStyle(color = DarkGray)
                    )
                }
            },
            text = {
                Text(
                    context.getString(R.string.chat_screen_ads_message),
                    textAlign = TextAlign.Center, // 가운데 정렬을 위한 textAlign
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(color = leopardcat.studio.chitchat.ui.theme.Gray)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if(viewModel.getPreference().getHeart("heart") >= 8){
                            Toast.makeText(context, context.getString(R.string.chat_screen_enough_heart), Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.loadRewardedAd(context) //광고 불러오기
                            viewModel.showRewardedAd(context as Activity, viewModel.HEART_REWORD, 0, "") //광고 보여주기
                        }
                        showAlertDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(PinkSub)

                ) {
                    Text(context.getString(R.string.check), color = White)
                }
            }
        )
    }
}

@Composable
fun ChatRow(
    context: Context,
    chat: Chat,
    lastIndex: Boolean,
    apiLoading: LoadingState,
    composition: LottieComposition?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (chat.direction) Alignment.Start else Alignment.End
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (chat.direction) PurpleMain else PurpleSub,
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Center
        ) {
            Text(
                text = chat.message, style = TextStyle(
                    color = if(chat.direction){
                        Color.White
                    } else {
                        Color.Black
                    },
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 15.dp),
                textAlign = if(chat.direction){
                    TextAlign.Start
                } else {
                    TextAlign.End
                }
            )
        }
        Text(
            text = convertToAmPmFormat(context, chat.time),
            style = TextStyle(
                color = Gray,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(start = 4.dp, top = 1.dp, bottom = 8.dp, end = 4.dp),
        )
    }

    if(lastIndex && apiLoading.loading){ //마지막 채팅 && 로딩 상태
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        PurpleMain,
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Center
            ){
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.size(50.dp), // 원하는 크기로 설정
                    iterations = Int.MAX_VALUE
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomTextField(
    text: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onItemClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current //키보드 상태
    TextField(
        value = text, onValueChange = { onValueChange(it) },
        placeholder = {
            Text(
                text = stringResource(R.string.type_message),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Gray
                ),
                textAlign = TextAlign.Center
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PurpleSub,
            unfocusedContainerColor = PurpleSub,
            disabledContainerColor = PurpleSub,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = DarkGray
        ),
        leadingIcon = { CommonIconButton(R.drawable.baseline_add) },
        trailingIcon = {
            CommonIconButtonDrawable(
                R.drawable.baseline_send,
                onItemClick = {
                    onItemClick()
                    keyboardController?.hide()
                })
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun CommonIconButton(
    @DrawableRes icon: Int,
) {
    Box(
        modifier = Modifier
            .size(33.dp)
            .clip(CircleShape)
            .background(Yellow, CircleShape),
        contentAlignment = Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "",
            tint = White,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
fun CommonIconButtonDrawable(
    @DrawableRes icon: Int,
    onItemClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(33.dp)
            .clip(CircleShape)
            .clickable { onItemClick() }
            .background(PinkSub, CircleShape),
        contentAlignment = Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
fun UserNameRow(
    person: Person,
    subscribeState: SubscribeState,
    heart: HeartState,
    onItemClick: () -> Unit,
    composition: LottieComposition?,
    viewModel: MainViewModel,
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {

            IconComponentDrawable(icon = person.icon, size = 42.dp)
            SpacerWidth()
            Column {
                Text(
                    text = person.name, style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = person.starId, style = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Box( //하트 영역
            modifier = Modifier
                .height(38.dp)
                .background(PinkSub, RoundedCornerShape(10.dp))
                .padding(5.dp)
                .clip(CircleShape)
                .clickable { onItemClick() },
            contentAlignment = Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpacerWidth(5.dp)
                Icon(
                    painter = painterResource(id = R.drawable.outline_heart),
                    contentDescription = "",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                SpacerWidth(6.dp)
                if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
                    Text(text = "${heart.heart} / 8", style = TextStyle(
                        color = White,
                        fontWeight = FontWeight.ExtraBold
                    ))
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.infinity),
                        contentDescription = "",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                SpacerWidth(5.dp)
            }
        }
        SpacerWidth()
        CommonIconTopButton(R.drawable.outline_photo, GreenSub, 38.dp, 27.dp){ //사진 영역
            viewModel.onEvent(ChatEvent.SetPersonImageId(person.id))
            navHostController.navigate(IMAGE_SCREEN)
        }
        SpacerWidth()
        CommonIconTopLottieButton(composition, YellowSub, 45.dp) { //프리미엄 영역
            viewModel.onEvent(ChatEvent.SetVip(true))
        }
    }
}

@Composable
fun CommonIconTopButton(
    @DrawableRes icon: Int,
    color: Color,
    size: Dp,
    dp: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "",
            tint = White,
            modifier = Modifier.size(dp)
        )
    }
}

@Composable
fun CommonIconTopLottieButton(
    composition: LottieComposition?,
    color: Color,
    dp: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Center
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(dp), // 원하는 크기로 설정
            iterations = Int.MAX_VALUE
        )
    }
}

