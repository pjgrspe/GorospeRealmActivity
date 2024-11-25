package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// CustomDialog.kt
@Composable
fun CustomDialog(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    enableConfirm: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                content = content
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = enableConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    "Confirm",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFE57373)
                )
            ) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(24.dp)
    )
}