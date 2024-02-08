package leopardcat.studio.chitchat.components.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import leopardcat.studio.chitchat.R
import leopardcat.studio.chitchat.components.SpacerHeight
import leopardcat.studio.chitchat.components.SpacerWidth
import leopardcat.studio.chitchat.components.data.SubscribeState
import leopardcat.studio.chitchat.components.util.convertMillisToTime
import leopardcat.studio.chitchat.components.viewmodel.MainViewModel
import leopardcat.studio.chitchat.ui.theme.Gold
import leopardcat.studio.chitchat.ui.theme.PurpleSub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipDialog(
    context: Context,
    leftTime: Long,
    subscribeState: SubscribeState,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val crownComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.crown)) //프리미엄 lottie 상태
    val vipComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.vip_button)) //프리미엄 lottie 버튼 상태

    //구독 상태 검사
    viewModel.checkBilling()

    //대표 이미지 지정
    val random = java.util.Random()
    val imageId = when(random.nextInt(4)) {
        0 -> R.drawable.vip_1
        1 -> R.drawable.vip_2
        2 -> R.drawable.vip_3
        3 -> R.drawable.vip_4
        else -> R.drawable.vip_1
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismiss() },
        content = {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxWidth()){
                    Image(
                        painter = painterResource(id = imageId), // 이미지 리소스 ID로 변경
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f / 1.1f)
                    )
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    LottieAnimation(
                        composition = crownComposition,
                        modifier = Modifier
                            .size(55.dp)
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp, top = 8.dp),
                        iterations = Int.MAX_VALUE
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                SetVipText(context)

                Spacer(modifier = Modifier.height(20.dp))

                if(!subscribeState.month && !subscribeState.year){ //구독중이 아닌경우
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp)
                            .background(
                                PurpleSub,
                                RoundedCornerShape(50.dp)
                            )
                            .align(CenterHorizontally)
                            .clickable {
                                viewModel.startBilling("premium_subscription_monthly")
                            },
                    ) {
                        Text(
                            context.getString(R.string.vip_dialog_month),
                            color = Color.Black,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 5.dp, bottom = 5.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TimerText(context, leftTime, vipComposition, viewModel)
                } else { //구독중인 경우
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = context.getString(R.string.vip_dialog_message_one),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(CenterHorizontally),
                                color = Color.White
                            )
                            SpacerHeight(20.dp)
                            Text(
                                text = context.getString(R.string.vip_dialog_message_two),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(CenterHorizontally),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SetVipText(
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = context.getString(R.string.vip_dialog_free_chat),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = context.getString(R.string.vip_dialog_free_ai_girl),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = context.getString(R.string.vip_dialog_free_ads),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = context.getString(R.string.vip_dialog_free_new_ai_girl),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp),
        text = context.getString(R.string.vip_dialog_free_photo),
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TimerText(
    context: Context,
    leftTime: Long,
    vipComposition: LottieComposition?,
    viewModel: MainViewModel
) {

    var vipTime by remember { mutableLongStateOf(leftTime) }

    LaunchedEffect(Unit) {
        while (vipTime > -1) {
            delay(1000) // 1초마다 업데이트
            vipTime -= 1000L
        }
    }

    if(vipTime > 0) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = context.getString(R.string.vip_dialog_discount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(CenterHorizontally),
                    color = Color.White
                )
                Text(
                    text = context.getString(R.string.vip_dialog_discount_text),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(CenterHorizontally),
                    color = Color.Gray
                )
                SpacerHeight(4.dp)
                Row(
                    modifier = Modifier.align(CenterHorizontally)
                ) {
                    Text(
                        text = context.getString(R.string.vip_dialog_left_time),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(CenterVertically),
                        color = Gold
                    )
                    SpacerWidth(5.dp)
                    Text(
                        text = convertMillisToTime(vipTime),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(CenterVertically),
                        color = Gold
                    )
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .clickable {
                if(vipTime > 0){
                    viewModel.startBilling("premium_subscription_yearly_24_event")
                } else {
                    viewModel.startBilling("premium_subscription_yearly")
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            composition = vipComposition,
            modifier = Modifier.fillMaxWidth(),
            iterations = Int.MAX_VALUE
        )
        Column(
            horizontalAlignment = CenterHorizontally
        ) {
            if(vipTime > 0) {
                //html 태그 포메팅
                val formattedText = buildAnnotatedString {
                    append(context.getString(R.string.vip_dialog_year))
                    append(" ")
                    withStyle(style = SpanStyle(textDecoration = LineThrough)) {
                        append(context.getString(R.string.vip_dialog_year_price))
                    }
                    append("  →  ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(context.getString(R.string.vip_dialog_year_discount_price))
                    }
                }
                Text(
                    text = formattedText,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = context.getString(R.string.vip_dialog_year_real_price),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}