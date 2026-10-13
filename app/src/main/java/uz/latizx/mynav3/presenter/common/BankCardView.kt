package uz.latizx.mynav3.presenter.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.latizx.mynav3.domain.model.Card
import uz.latizx.mynav3.domain.model.CardType
import uz.latizx.mynav3.ui.theme.MyNav3Theme

/**
 * Shared element kaliti: ro'yxatdagi va detal ekranidagi BIR XIL karta bir xil kalitga ega
 * bo'lishi kerak — shunda karta bir ekrandan ikkinchisiga "uchib" o'tadi.
 */
fun bankCardSharedKey(cardId: String): String = "bank-card-$cardId"

/** Plastik karta ko'rinishi. [onClick] null bo'lsa bosilmaydi. */
@Composable
fun BankCardView(
    card: Card,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val gradient = when (card.type) {
        CardType.UZCARD -> listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
        CardType.HUMO -> listOf(Color(0xFF065F46), Color(0xFF10B981))
        CardType.VISA -> listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = card.type.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (card.isBlocked) {
                    Text(
                        text = "BLOKLANGAN",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .background(Color(0x66000000), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            Text(
                text = formatSum(card.balance),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Column {
                Text(
                    text = formatCardNumber(card.number),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = card.holderName,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Preview
@Composable
private fun BankCardViewPreview() {
    MyNav3Theme {
        BankCardView(card = PreviewData.cards.first(), modifier = Modifier.padding(16.dp))
    }
}
