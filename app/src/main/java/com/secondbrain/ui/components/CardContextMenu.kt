package com.secondbrain.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.secondbrain.data.model.Card

@Composable
fun CardContextMenu(
    card: Card,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDuplicate: (Card) -> Unit,
    onPin: (Card) -> Unit,
    onShare: (Card) -> Unit,
    onDelete: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            DropdownMenuItem(
                text = { Text("Duplicate") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = {
                    onDuplicate(card)
                    onDismiss()
                }
            )
            
            DropdownMenuItem(
                text = { Text("Pin") },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                onClick = {
                    onPin(card)
                    onDismiss()
                }
            )
            
            DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = {
                    onShare(card)
                    onDismiss()
                }
            )
            
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    onDelete(card)
                    onDismiss()
                }
            )
        }
    }
}
