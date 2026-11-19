package com.yaka.cibuburrentalcar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yaka.cibuburrentalcar.ui.components.CategoryIcon
import com.yaka.cibuburrentalcar.ui.components.getCategoryGradient
import com.yaka.cibuburrentalcar.ui.theme.MediumGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Cari mobil...",
    enabled: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MediumGray,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(query)
                    keyboardController?.hide()
                }
            ),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumGray
                    )
                }
                innerTextField()
            }
        )
        
        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                tint = MediumGray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        onQueryChange("")
                        onSearch("")
                    }
            )
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null
) {

    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (selected) 6.dp else 2.dp,
        animationSpec = tween(300),
        label = "elevation"
    )

    val categoryColors = getCategoryGradient(text)
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            categoryColors.first
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        },
        animationSpec = tween(300),
        label = "textColor"
    )

    Card(
        modifier = modifier
            .scale(animatedScale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon berdasarkan kategori
            CategoryIcon(
                category = text,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )

            // Count badge atau checkmark
            if (count != null && count > 0 && !selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = categoryColors.first.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColors.first,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun FilterRow(
    filters: List<String>,
    selectedFilters: Set<String>,
    onFilterToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    carCounts: Map<String, Int> = emptyMap()
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(filters.size) { index ->
            val filter = filters[index]

            // Animasi masuk sederhana
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300),
                label = "alpha"
            )

            FilterChip(
                text = filter,
                selected = selectedFilters.contains(filter),
                onClick = { onFilterToggle(filter) },
                count = carCounts[filter],
                modifier = Modifier
                    .graphicsLayer(
                        alpha = animatedAlpha
                    )
            )
        }
    }
}
