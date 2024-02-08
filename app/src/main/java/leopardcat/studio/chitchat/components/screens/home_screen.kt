package leopardcat.studio.chitchat.components.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import leopardcat.studio.chitchat.components.IconComponentDrawable
import leopardcat.studio.chitchat.components.SpacerHeight
import leopardcat.studio.chitchat.components.SpacerWidth
import leopardcat.studio.chitchat.components.data.Person
import leopardcat.studio.chitchat.components.navigation.CHAT_SCREEN
import leopardcat.studio.chitchat.components.navigation.IMAGE_SCREEN
import leopardcat.studio.chitchat.ui.theme.Gray
import leopardcat.studio.chitchat.ui.theme.Line
import leopardcat.studio.chitchat.components.viewmodel.ChatEvent
import leopardcat.studio.chitchat.components.viewmodel.MainViewModel
import leopardcat.studio.chitchat.R
import leopardcat.studio.chitchat.components.data.PersonState
import leopardcat.studio.chitchat.components.data.SubscribeState
import leopardcat.studio.chitchat.components.navigation.HOME_SCREEN
import leopardcat.studio.chitchat.ui.theme.DarkGray
import leopardcat.studio.chitchat.ui.theme.PinkSub
import leopardcat.studio.chitchat.ui.theme.PurpleMain
import leopardcat.studio.chitchat.ui.theme.PurpleSub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: MainViewModel,
    personList: PersonState,
    subscribeState: SubscribeState
) {
    viewModel.getPersonList()// personList 업데이트
    viewModel.checkBilling()//구독 상태 검사
    if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
        viewModel.getHeartState()// heartState 업데이트
    }

    val context = LocalContext.current
    var showAlertDialog by remember { mutableStateOf(false) } //Dialog 상태

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleMain)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
        ) {
            HeaderOrViewStory(navHostController, personList.personList, viewModel)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White, RoundedCornerShape(
                            topStart = 30.dp, topEnd = 30.dp
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.padding(bottom = 15.dp, top = 30.dp)
                ) {
                    items(personList.personList.sortedByDescending { it.lastChat }, key = { it.id }) {
                        val state = rememberDismissState(
                            positionalThreshold = {
                                it * 0.3f
                            },
                            confirmValueChange = { dismissValue ->
                                if(dismissValue == DismissValue.DismissedToStart){
                                    viewModel.setDeleteChatId(it.name)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(200)
                                        showAlertDialog = true
                                    }
                                }
                                false //false는 view가 그대로 남고, true는 없어짐
                            }
                        )
                        SwipeToDismiss(state = state,
                            background = {
                                val color = when (state.dismissDirection) {
                                    DismissDirection.StartToEnd -> Color.Transparent
                                    DismissDirection.EndToStart -> PinkSub
                                    null -> Color.Magenta
                                }
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = color)
                                    .padding(10.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                            },
                            dismissContent = {
                                UserEachRow(person = it) {
                                    if(!it.lock) {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                            "data",
                                            it
                                        )
                                        navHostController.navigate(CHAT_SCREEN)
                                        viewModel.onEvent(ChatEvent.SetPerson(it.name))
                                    } else {
                                        val message = context.getString(R.string.home_screen_lock_toast, it.name)
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            directions = setOf(DismissDirection.EndToStart)
                        )
                    }
                }
            }
        }

    }

    if(showAlertDialog) { //삭제 다이어로그
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = {
                showAlertDialog = false
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = CenterHorizontally, // 가로 가운데 정렬
                    verticalArrangement = Arrangement.Center, // 세로 가운데 정렬
                ) {
                    Text(context.getString(R.string.home_screen_chat_out),
                        textAlign = TextAlign.Center,// 가운데 정렬을 위한 textAlign
                        style = TextStyle(color = DarkGray))
                }
            },
            text = {
                Text(
                    context.getString(R.string.home_screen_chat_out_message),
                    textAlign = TextAlign.Center, // 가운데 정렬을 위한 textAlign
                    style = TextStyle(color = DarkGray),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // 버튼을 가로로 2등분
                ) {
                    Button(
                        onClick = {
                            viewModel.deleteChat()
                            showAlertDialog = false
                            navHostController.popBackStack() //페이지 뒤로가기 후
                            navHostController.navigate(HOME_SCREEN) //페이지 강제 새로고침
                        },
                        modifier = Modifier.weight(1f), // 두 버튼 중 하나의 가중치를 1로 설정
                        colors = ButtonDefaults.buttonColors(PinkSub)
                    ) {
                        Text(context.getString(R.string.check), color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // 버튼 사이에 간격 추가
                    Button(
                        onClick = {
                            showAlertDialog = false
                        },
                        modifier = Modifier.weight(1f), // 두 버튼 중 하나의 가중치를 1로 설정
                        colors = ButtonDefaults.buttonColors(PinkSub)
                    ) {
                        Text(context.getString(R.string.cancel), color = Color.White)
                    }
                }
            }
        )
    }
}

@Composable
fun HeaderOrViewStory(
    navHostController: NavHostController,
    personList: List<Person>,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp)
    ) {
        Header()
        ViewStoryLayout(navHostController, personList, viewModel)
    }
}

@Composable
fun ViewStoryLayout(
    navHostController: NavHostController,
    personList: List<Person>,
    viewModel: MainViewModel
) {
    LazyRow(modifier = Modifier.padding(vertical = 20.dp)) {
        items(personList, key = { it.id }) {
            UserStory(
                person = it,
                onClick = {
                    viewModel.onEvent(ChatEvent.SetPersonImageId(it.id))
                    navHostController.navigate(IMAGE_SCREEN)
                }
            )
        }
    }
}

@Composable
fun UserEachRow(
    person: Person,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .noRippleEffect { onClick() }
            .padding(horizontal = 20.dp, vertical = 5.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Box(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        IconComponentDrawable(icon = person.icon, size = 60.dp)
                        if (person.lock) {
                            // 자물쇠 아이콘을 그립니다.
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Icon",
                                tint = PinkSub, // 아이콘 색상 설정
                                modifier = Modifier.size(24.dp).align(Center) // 아이콘 크기 설정
                            )
                        }
                    }
                    SpacerWidth()
                    Column {
                        Text(
                            text = person.name, style = TextStyle(
                                color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                        )
                        SpacerHeight(5.dp)
                        Text(
                            text = person.explain, style = TextStyle(
                                color = Gray, fontSize = 12.sp
                            )
                        )
                    }
                }
                Text(
                    modifier = Modifier.padding(end = 10.dp),
                    text = person.lastChat, style = TextStyle(
                        color = Gray, fontSize = 12.sp
                    )
                )
            }
            SpacerHeight(15.dp)
            Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Line)
        }
    }

}

@Composable
fun UserStory(
    person: Person, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(end = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, PurpleSub, CircleShape)
                .background(PurpleSub, shape = CircleShape)
                .size(70.dp)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            IconComponentDrawable(icon = person.icon, size = 65.dp)
            if (person.lock) { // 자물쇠 아이콘을 그립니다.
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = PinkSub, // 아이콘 색상 설정
                    modifier = Modifier.size(24.dp).align(Center) // 아이콘 크기 설정
                )
            }
        }
        SpacerHeight(8.dp)
        Text(
            text = person.name, style = TextStyle(
                color = Color.White, fontSize = 13.sp,
            ), modifier = Modifier.align(CenterHorizontally)
        )
    }
}

@Composable
fun Header() {

    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        ) {
            append(stringResource(R.string.friend))
        }
    }

    Text(text = annotatedString)

}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.noRippleEffect(onClick: () -> Unit) = composed {
    clickable(
        interactionSource = MutableInteractionSource(),
        indication = null
    ) {
        onClick()
    }
}