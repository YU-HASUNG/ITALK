package leopardcat.studio.chitchat.components.screens

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.skydoves.landscapist.glide.GlideImage
import leopardcat.studio.chitchat.R
import leopardcat.studio.chitchat.components.IconComponentDrawable
import leopardcat.studio.chitchat.components.SpacerHeight
import leopardcat.studio.chitchat.components.SpacerWidth
import leopardcat.studio.chitchat.components.api.model.ImageData
import leopardcat.studio.chitchat.components.data.PersonState
import leopardcat.studio.chitchat.components.data.SubscribeState
import leopardcat.studio.chitchat.components.viewmodel.ChatEvent
import leopardcat.studio.chitchat.components.viewmodel.MainViewModel
import leopardcat.studio.chitchat.ui.theme.PinkSub
import leopardcat.studio.chitchat.ui.theme.PurpleMain
import leopardcat.studio.chitchat.ui.theme.PurpleSub

@Composable
fun ImageScreen(
    navHostController: NavHostController,
    viewModel: MainViewModel,
    imageData: ImageData,
    personList: PersonState,
    subscribeState: SubscribeState
) {
    val context = LocalContext.current
    val crownComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.crown)) //프리미엄 lottie 상태
    val adsComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ads)) //프리미엄 lottie 상태
    val setImageDialogVisible = remember { mutableStateOf(false) } //이미지 전체화면 상태
    val setImageDialogUrl = remember { mutableStateOf("") } //이미지 url 상태
    val id = viewModel.imageId

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
            HeaderOrViewStory1(personList, id)
            SpacerHeight(20.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .padding(top = 35.dp)
            ) {
                SetImage(imageData, crownComposition, adsComposition, setImageDialogUrl, setImageDialogVisible, subscribeState, viewModel, context)
            }
        }
    }

    if (setImageDialogVisible.value) {
        FullScreenImageDialog(
            imageUrl = setImageDialogUrl.value,
            onDismiss = { setImageDialogVisible.value = false }
        )
    }

    BackHandler(true) {
        if(!subscribeState.month && !subscribeState.year){
            viewModel.loadCoverAds() //전명 광고 노출
//            viewModel.setCoverAds() //전명 광고 기본세팅
        }
        navHostController.navigateUp() //뒤로가기
    }
}

@Composable
fun HeaderOrViewStory1(personList: PersonState, id: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
    ) {
        Row {
            AddStoryLayout1(personList, id)
            SpacerWidth()
            Column {
                Text(
                    text = personList.personList[id].starId, style = TextStyle(
                        color = Color.White, fontSize = 15.sp
                    )
                )
                SpacerHeight(5.dp)
                Text(
                    text = personList.personList[id].follower, style = TextStyle(
                        color = Color.White, fontSize = 14.sp
                    )
                )
                SpacerHeight(5.dp)
                Text(
                    text = personList.personList[id].name, style = TextStyle(
                        color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun AddStoryLayout1(
    personList: PersonState,
    id: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, PurpleSub, CircleShape)
                .background(PurpleSub, shape = CircleShape)
                .size(70.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconComponentDrawable(icon = personList.personList[id].icon, size = 65.dp)
            if (personList.personList[id].lock) { // 자물쇠 아이콘을 그립니다.
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = PinkSub, // 아이콘 색상 설정
                    modifier = Modifier.size(24.dp).align(Alignment.Center) // 아이콘 크기 설정
                )
            }
        }
    }

}

@Composable
fun SetImage(
    imageData: ImageData,
    vipComposition: LottieComposition?,
    adsComposition: LottieComposition?,
    setImageDialogUrl: MutableState<String>,
    setImageDialogVisible: MutableState<Boolean>,
    subscribeState: SubscribeState,
    viewModel: MainViewModel,
    context: Context
) {
    if(imageData.imageList.isNotEmpty()){
        StaggeredGrid(imageData, vipComposition, adsComposition, setImageDialogUrl, setImageDialogVisible, subscribeState, viewModel, context)
    }
}

@Composable
fun StaggeredGrid(
    imageData: ImageData,
    vipComposition: LottieComposition?,
    adsComposition: LottieComposition?,
    setImageDialogUrl: MutableState<String>,
    setImageDialogVisible: MutableState<Boolean>,
    subscribeState: SubscribeState,
    viewModel: MainViewModel,
    context: Context) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = (8.dp)
    ) {
        items(imageData.imageList[viewModel.imageId].image.size) {
            ImageBox(
                imageUrl = imageData.imageList[viewModel.imageId].image[it].url,
                isMosaic = imageData.imageList[viewModel.imageId].image[it].id,
                vipComposition,
                adsComposition,
                subscribeState,
                onItemClick = {
                    when(imageData.imageList[viewModel.imageId].image[it].id) {
                        "ads" -> {
                            if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
                                viewModel.loadRewardedAd(context) //광고 불러오기
                                viewModel.showRewardedAd(context as Activity, viewModel.IMAGE_REWORD, it, imageData.imageList[viewModel.imageId].image[it].url) //이미지 광고 보여주기
                            } else {
                                setImageDialogUrl.value = imageData.imageList[viewModel.imageId].image[it].url
                                setImageDialogVisible.value = true
                            }
                        }
                        "vip" -> {
                            if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
                                viewModel.onEvent(ChatEvent.SetVip(true))
                            } else {
                                setImageDialogUrl.value = imageData.imageList[viewModel.imageId].image[it].url
                                setImageDialogVisible.value = true
                            }
                        }
                        else -> {
                            setImageDialogUrl.value = imageData.imageList[viewModel.imageId].image[it].url
                            setImageDialogVisible.value = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ImageBox(imageUrl: String, isMosaic: String, vipComposition: LottieComposition?, adsComposition: LottieComposition?, subscribeState: SubscribeState ,onItemClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f/1.5f)
        .clip(RoundedCornerShape(10.dp))
        .clickable { onItemClick() }
    ) {
        when(isMosaic) {
            "ads" -> {
                if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
                    Box(
                        modifier = Modifier.blur(20.dp)
                    ) {
                        GlideImage(
                            imageModel = { imageUrl }, // loading a network image using a URL.
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(modifier = Modifier.heightIn(300.dp, 300.dp)) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            },
                        )
                    }
                    LottieAnimation(
                        composition = adsComposition,
                        modifier = Modifier.size(50.dp), // 원하는 크기로 설정
                        iterations = Int.MAX_VALUE
                    )
                } else {
                    GlideImage(
                        imageModel = { imageUrl }, // loading a network image using a URL.
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.heightIn(300.dp, 300.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        },
                    )
                }
            }
            "vip" -> {
                if(!subscribeState.month && !subscribeState.year) { //구독중이 아닌경우
                    Box(modifier = Modifier
                        .blur(20.dp)
                    ) {
                        GlideImage(
                            imageModel = { imageUrl }, // loading a network image using a URL.
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(modifier = Modifier.heightIn(300.dp, 300.dp)) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            },
                        )
                    }
                    LottieAnimation(
                        composition = vipComposition,
                        modifier = Modifier.size(50.dp), // 원하는 크기로 설정
                        iterations = Int.MAX_VALUE
                    )
                } else {
                    GlideImage(
                        imageModel = { imageUrl }, // loading a network image using a URL.
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.heightIn(300.dp, 300.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        },
                    )
                }
            }
            else -> {
                GlideImage(
                    imageModel = { imageUrl }, // loading a network image using an URL.
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.heightIn(300.dp, 300.dp)) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    },
                )
            }
        }
    }
}

// ImageBox 클릭 시 이미지를 표시할 다이얼로그 컴포저
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    val scale = remember { mutableFloatStateOf(1f) }
    val translationX = remember { mutableFloatStateOf(0f) }
    val translationY = remember { mutableFloatStateOf(0f) }

    // 최소 및 최대 이동치 계산
    val screenMetrics = LocalDensity.current.density
    val screenWidth = (LocalConfiguration.current.screenWidthDp * screenMetrics).toInt()
    val screenHeight = (LocalConfiguration.current.screenHeightDp * screenMetrics).toInt()

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismiss() },

        content = {
            Box(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (scale.floatValue * zoom >= 1f) {
                            scale.floatValue *= zoom
                        }
                        if (scale.floatValue * zoom > 1f) {
                            val minTranslationX = (-1f) * (scale.floatValue - 1f) * (screenWidth / 2)
                            val maxTranslationX = (scale.floatValue - 1f) * (screenWidth / 2)
                            val minTranslationY = (-1f) * (scale.floatValue - 1f) * (screenHeight / 2)
                            val maxTranslationY = (scale.floatValue - 1f) * (screenHeight / 2)
                            translationX.floatValue = (translationX.floatValue + pan.x).coerceIn(minTranslationX, maxTranslationX)
                            translationY.floatValue = (translationY.floatValue + pan.y).coerceIn(minTranslationY, maxTranslationY)
                        }
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                // 이미지를 여기서 표시하거나 Zoomable 컴포저 등을 사용하여 확대 가능한 이미지를 표시합니다.
                GlideImage(
                    imageModel = { imageUrl },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale.floatValue,
                            scaleY = scale.floatValue,
                            translationX = translationX.floatValue,
                            translationY = translationY.floatValue
                        )
                )
                IconButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    )
}