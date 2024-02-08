package leopardcat.studio.chitchat.components.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import leopardcat.studio.chitchat.components.ButtonComponent
import leopardcat.studio.chitchat.components.SpacerWidth
import leopardcat.studio.chitchat.components.navigation.HOME_SCREEN
import leopardcat.studio.chitchat.R
import leopardcat.studio.chitchat.components.viewmodel.ChatEvent
import leopardcat.studio.chitchat.components.viewmodel.MainViewModel
import leopardcat.studio.chitchat.ui.theme.Aqua

@Composable
fun StartScreen(
    navHostController: NavHostController,
    viewModel: MainViewModel
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.chitchat)) //start lottie 상태

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.background), contentDescription = "",
                contentScale = ContentScale.FillWidth
            )
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(230.dp), // 원하는 크기로 설정
                iterations = Int.MAX_VALUE
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 220.dp)
                .background(Color.Black)
                .align(Alignment.Center)

        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 40.dp)
            ) {
                Text(
                    text = stringResource(R.string.stay_with_your_friends),
                    style = TextStyle(
                        fontSize = 36.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                CustomCheckBox()
            }
        }
        ButtonComponent(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomCenter)
                .height(60.dp)
        ) {
            viewModel.onEvent(ChatEvent.SetVip(true))
            navHostController.navigate(HOME_SCREEN)
        }
    }
}


@Composable
fun CustomCheckBox() {

    Row(
        modifier = Modifier.padding(vertical = 15.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Aqua, RoundedCornerShape(
                        topStart = 10.dp, topEnd = 10.dp, bottomStart = 80.dp, bottomEnd = 80.dp
                    )
                )
                .size(24.dp), contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "",
                modifier = Modifier.size(15.dp),
                tint = Color.Black
            )
        }
        SpacerWidth(15.dp)
        Text(
            text = stringResource(R.string.secure_private_messaging), style = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }

}